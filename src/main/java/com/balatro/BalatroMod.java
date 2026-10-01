package com.balatro;

import com.balatro.core.run.RunState;
import com.balatro.core.run.StartingDeck;
import com.balatro.net.BalatroNetworking;
import com.balatro.storage.BalatroRunData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Точка входа мода. Игровая логика живёт в {@code com.balatro.core},
 * здесь только связывание с сервером Minecraft.
 */
public class BalatroMod implements ModInitializer {

    public static final String MOD_ID = "balatro";

    /** Кэш активных забегов, чтобы не читать с диска каждый ход. */
    private static final Map<UUID, RunState> RUNS = new HashMap<>();

    @Override
    public void onInitialize() {
        BalatroNetworking.register();
        com.balatro.command.BalatroCommand.register();

        // При входе на сервер подгружаем сохранённый забег.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                server.execute(() -> {
                    ServerPlayer player = handler.getPlayer();
                    RunState loaded = BalatroRunData.get(server).get(player);
                    if (loaded != null) {
                        RUNS.put(player.getUUID(), loaded);
                    }
                }));

        // При выходе сохраняем прогресс и чистим кэш.
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                server.execute(() -> {
                    ServerPlayer player = handler.getPlayer();
                    RunState run = RUNS.get(player.getUUID());
                    if (run != null) {
                        BalatroRunData.get(server).put(player, run);
                    }
                    RUNS.remove(player.getUUID());
                }));

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> RUNS.clear());
    }

    public static RunState runOf(ServerPlayer player) {
        RunState cached = RUNS.get(player.getUUID());
        if (cached != null) {
            return cached;
        }
        RunState loaded = BalatroRunData.get(player.server).get(player);
        if (loaded != null) {
            RUNS.put(player.getUUID(), loaded);
        }
        return loaded;
    }

    public static void cacheRun(ServerPlayer player, RunState run) {
        RUNS.put(player.getUUID(), run);
    }

    public static void startRun(ServerPlayer player, StartingDeck deck) {
        RunState run = new RunState(deck, System.nanoTime());
        RUNS.put(player.getUUID(), run);
        BalatroRunData.get(player.server).put(player, run);
    }

    public static void endRun(ServerPlayer player) {
        RUNS.remove(player.getUUID());
        BalatroRunData.get(player.server).remove(player);
    }

    public static boolean hasRun(ServerPlayer player) {
        RunState run = runOf(player);
        return run != null && !run.gameOver();
    }
}
