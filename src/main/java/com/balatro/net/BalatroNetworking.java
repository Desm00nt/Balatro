package com.balatro.net;

import com.balatro.BalatroMod;
import com.balatro.core.run.RunState;
import com.balatro.storage.BalatroRunData;
import com.balatro.storage.RunNbt;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Регистрация сетевых каналов и приём действий игрока. */
public final class BalatroNetworking {

    private BalatroNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(BalatroPayloads.StateS2C.ID,
                BalatroPayloads.StateS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(BalatroPayloads.OpenScreenS2C.ID,
                BalatroPayloads.OpenScreenS2C.CODEC);
        PayloadTypeRegistry.playC2S().register(BalatroPayloads.ActionC2S.ID,
                BalatroPayloads.ActionC2S.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(BalatroPayloads.ActionC2S.ID,
                (payload, context) -> context.server().execute(
                        () -> handleAction(payload, context.player())));
    }

    /** Отправляет клиенту полное состояние забега и сохраняет его. */
    public static void sync(ServerPlayer player) {
        BalatroRunData data = BalatroRunData.get(player.server);
        RunState run = data.get(player);
        if (run == null) {
            return;
        }
        data.put(player, run);
        BalatroMod.cacheRun(player, run);
        ServerPlayNetworking.send(player, new BalatroPayloads.StateS2C(
                RunNbt.toNbt(run.toSnapshot())));
    }

    /** Открывает указанный экран на клиенте. */
    public static void openScreen(ServerPlayer player, String screen) {
        ServerPlayNetworking.send(player,
                new BalatroPayloads.OpenScreenS2C(screen));
    }

    private static void handleAction(BalatroPayloads.ActionC2S payload, ServerPlayer player) {
        BalatroRunData data = BalatroRunData.get(player.server);
        RunState run = data.get(player);
        if (run == null) {
            return;
        }
        String result = RunActions.apply(run, payload.action(),
                payload.selection() == null ? java.util.List.of() : payload.selection(),
                player);
        data.put(player, run);
        if (result != null && !result.isEmpty()) {
            player.sendSystemMessage(Component.literal(result));
        }
        sync(player);
    }
}