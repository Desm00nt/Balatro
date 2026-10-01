package com.balatro.client;

import com.balatro.core.run.RunState;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Клиентская инициализация. */
public class BalatroClient implements ClientModInitializer {

    /**
     * Открытый на клиенте забег. GUI работает с этой копией,
     * а не ходит на сервер каждый кадр.
     */
    public static final Map<String, RunState> OPEN_RUNS = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        // Сетевые пакеты и рендереры добавляются в следующих версиях.
    }

    /** Открывает игровой стол для локального забега. */
    public static void openTable(RunState run) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        OPEN_RUNS.put(client.player.getGameProfile().getName(), run);
        client.setScreen(new com.balatro.client.screen.BalatroScreen(run));
    }
}
