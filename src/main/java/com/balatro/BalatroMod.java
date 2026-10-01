package com.balatro;

import com.balatro.core.run.RunState;
import com.balatro.core.run.StartingDeck;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Точка входа мода. Хранит активный забег каждого игрока
 * и регистрирует команду {@code /balatro}.
 */
public class BalatroMod implements ModInitializer {

    public static final String MOD_ID = "balatro";

    /** Активные забеги по UUID игрока. */
    private static final Map<UUID, RunState> RUNS = new HashMap<>();
    private static BalatroMod instance;

    @Override
    public void onInitialize() {
        instance = this;
        com.balatro.command.BalatroCommand.register();

        // Забег живёт только пока игрок в игре.
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> RUNS.remove(handler.getPlayer().getUUID()));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> RUNS.clear());
    }

    public static BalatroMod getInstance() {
        return instance;
    }

    public static RunState runOf(ServerPlayer player) {
        return RUNS.get(player.getUUID());
    }

    public static void startRun(ServerPlayer player, StartingDeck deck) {
        long seed = System.nanoTime();
        RUNS.put(player.getUUID(), new RunState(deck, seed));
    }

    public static void endRun(ServerPlayer player) {
        RUNS.remove(player.getUUID());
    }

    public static boolean hasRun(ServerPlayer player) {
        RunState run = RUNS.get(player.getUUID());
        return run != null && !run.gameOver();
    }
}
