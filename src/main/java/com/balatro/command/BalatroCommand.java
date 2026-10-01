package com.balatro.command;

import com.balatro.BalatroMod;
import com.balatro.core.blind.BossRules;
import com.balatro.core.run.RunPhase;
import com.balatro.core.run.RunState;
import com.balatro.core.run.StartingDeck;
import com.balatro.net.BalatroNetworking;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Команда {@code /balatro} для управления забегом. */
public final class BalatroCommand {

    private BalatroCommand() {
    }

    /** Регистрирует команду через Fabric API. */
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
                registerOnServer(dispatcher));
    }

    private static void registerOnServer(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("balatro")
                .requires(src -> src.hasPermission(0))
                .then(Commands.literal("start")
                        .executes(ctx -> start(ctx.getSource(),
                                StartingDeck.RED.displayName()))
                        .then(Commands.argument("deck", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (StartingDeck d : StartingDeck.values()) {
                                        builder.suggest(d.name());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> start(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "deck")))))
                .then(Commands.literal("stop")
                        .executes(ctx -> stop(ctx.getSource())))
                .then(Commands.literal("open")
                        .executes(ctx -> open(ctx.getSource())))
                .then(Commands.literal("shop")
                        .executes(ctx -> openShop(ctx.getSource())))
                .then(Commands.literal("status")
                        .executes(ctx -> status(ctx.getSource())))
                .then(Commands.literal("decks")
                        .executes(ctx -> listDecks(ctx.getSource()))));
    }

    private static int start(CommandSourceStack source, String deckName) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("Команда доступна только игроку"));
            return 0;
        }
        StartingDeck deck;
        try {
            deck = StartingDeck.byName(deckName);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Неизвестная колода: " + deckName));
            return 0;
        }
        BalatroMod.startRun(player, deck);
        RunState run = BalatroMod.runOf(player);
        source.sendSuccess(() -> Component.literal("Забег начат: " + deck.displayName())
                .withStyle(ChatFormatting.GREEN), false);
        source.sendSuccess(() -> Component.literal("Цель: " + run.currentBlind().displayName()
                + " (" + run.currentBlind().scoreTarget() + " очков)"), false);
        source.sendSuccess(() -> Component.literal("Бонусы: " + deck.bonusDescription()),
                false);
        return 1;
    }

    private static int stop(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Команда доступна только игроку"));
            return 0;
        }
        BalatroMod.endRun(player);
        source.sendSuccess(() -> Component.literal("Забег завершён")
                .withStyle(ChatFormatting.YELLOW), false);
        return 1;
    }

    /** Открывает игровой стол. */
    private static int open(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Команда доступна только игроку"));
            return 0;
        }
        RunState run = BalatroMod.runOf(player);
        if (run == null) {
            source.sendFailure(Component.literal("Активного забега нет. "
                    + "Начните командой /balatro start"));
            return 0;
        }
        BalatroNetworking.sync(player);
        BalatroNetworking.openScreen(player,
                com.balatro.net.BalatroPayloads.Screens.TABLE);
        return 1;
    }

    /** Открывает магазин. */
    private static int openShop(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Команда доступна только игроку"));
            return 0;
        }
        RunState run = BalatroMod.runOf(player);
        if (run == null) {
            source.sendFailure(Component.literal("Активного забега нет"));
            return 0;
        }
        BalatroNetworking.sync(player);
        BalatroNetworking.openScreen(player,
                com.balatro.net.BalatroPayloads.Screens.SHOP);
        return 1;
    }

    private static int status(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Команда доступна только игроку"));
            return 0;
        }
        RunState run = BalatroMod.runOf(player);
        if (run == null) {
            source.sendFailure(Component.literal("Активного забега нет"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("=== " + run.startingDeck().displayName()
                + " ==="), false);
        source.sendSuccess(() -> Component.literal("Слепой: " + run.currentBlind().displayName()),
                false);
        source.sendSuccess(() -> Component.literal("Очки: " + run.scoreThisRound() + " / "
                + run.currentBlind().scoreTarget()), false);
        source.sendSuccess(() -> Component.literal("Руки: " + run.handsLeft()
                + " | Сбросы: " + run.discardsLeft()
                + " | Деньги: $" + run.money()), false);
        source.sendSuccess(() -> Component.literal("Джокеры: " + run.jokers().size() + " / "
                + run.jokerSlots()), false);
        source.sendSuccess(() -> Component.literal("Фаза: " + phaseName(run.phase())
                + " | Колода: " + run.deck().totalSize() + " карт"), false);
        if (run.bossBlind() != com.balatro.core.blind.BossBlind.NONE) {
            source.sendSuccess(() -> Component.literal("Босс: " + run.bossBlind().displayName()
                            + " — " + BossRules.description(run.bossBlind())),
                    false);
        }
        if (run.gameOver()) {
            source.sendSuccess(() -> Component.literal("ЗАБЕГ ПРОИГРАН")
                    .withStyle(ChatFormatting.RED), false);
        } else if (run.phase() == RunPhase.GAME_WON) {
            source.sendSuccess(() -> Component.literal("ЗАБЕГ ПРОЙДЕН")
                    .withStyle(ChatFormatting.GREEN), false);
        }
        return 1;
    }

    private static String phaseName(RunPhase phase) {
        return switch (phase) {
            case PLAYING -> "игра";
            case SHOP -> "магазин";
            case BLIND_SELECT -> "выбор слепого";
            case GAME_OVER -> "поражение";
            case GAME_WON -> "победа";
            case DECK_SELECT -> "выбор колоды";
        };
    }

    private static int listDecks(CommandSourceStack source) {
        for (StartingDeck d : StartingDeck.values()) {
            source.sendSuccess(() -> Component.literal(d.name() + " — " + d.bonusDescription()),
                    false);
        }
        return StartingDeck.values().length;
    }
}
