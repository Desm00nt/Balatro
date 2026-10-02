package com.balatro.client;

import com.balatro.core.run.RunSnapshot;
import com.balatro.net.BalatroPayloads;
import com.balatro.storage.RunNbt;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

/**
 * Клиентская часть мода: принимает состояние забега с сервера
 * и открывает нужные экраны. Логика игры здесь не выполняется.
 */
public class BalatroClient implements ClientModInitializer {

    /** Последнее состояние, полученное от сервера. */
    private static RunSnapshot state;
    private static String screenToOpen;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(BalatroPayloads.StateS2C.ID,
                (payload, context) -> context.client().execute(() -> {
                    state = RunNbt.fromNbt(payload.tag());
                }));

        ClientPlayNetworking.registerGlobalReceiver(BalatroPayloads.OpenScreenS2C.ID,
                (payload, context) -> context.client().execute(() -> {
                    screenToOpen = payload.screen();
                    openPendingScreen();
                }));

        // Сбрасываем состояние при выходе с сервера.
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            state = null;
            screenToOpen = null;
        });
    }

    /** Состояние забега на клиенте (может быть null). */
    public static RunSnapshot state() {
        return state;
    }

    /** Открывает экран, если его запросил сервер. */
    private static void openPendingScreen() {
        if (screenToOpen == null) {
            return;
        }
        String screen = screenToOpen;
        screenToOpen = null;
        if (state == null) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (screen.equals(BalatroPayloads.Screens.SHOP)) {
            client.setScreenAndShow(new com.balatro.client.screen.ShopScreen(state));
        } else {
            client.setScreenAndShow(new com.balatro.client.screen.BalatroScreen(state));
        }
    }

    /** Отправляет действие серверу с выбранными индексами карт. */
    public static void sendAction(String action, int... selection) {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (int i : selection) {
            list.add(i);
        }
        ClientPlayNetworking.send(new BalatroPayloads.ActionC2S(action, list));
    }

    /** Компактный NBT последнего состояния (для отладки). */
    public static CompoundTag debugTag() {
        return state == null ? new CompoundTag() : RunNbt.toNbt(state);
    }
}
