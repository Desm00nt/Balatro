package com.balatro.net;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

/** Пакеты сетевого взаимодействия с игрой Balatro. */
public final class BalatroPayloads {

    /** Полное состояние забега: сервер отправляет клиенту. */
    public record StateS2C(CompoundTag tag) implements CustomPacketPayload {

        public static final Type<StateS2C> ID =
                CustomPacketPayload.createType(ResourceIds.STATE);
        public static final StreamCodec<RegistryFriendlyByteBuf, StateS2C> CODEC =
                StreamCodec.of((buf, value) -> buf.writeNbt(value.tag()),
                        buf -> new StateS2C(buf.readNbt()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    /** Открытие экрана игры. */
    public record OpenScreenS2C(String screen) implements CustomPacketPayload {

        public static final Type<OpenScreenS2C> ID =
                CustomPacketPayload.createType(ResourceIds.OPEN);
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreenS2C> CODEC =
                StreamCodec.of((buf, value) -> buf.writeUtf(value.screen()),
                        buf -> new OpenScreenS2C(buf.readUtf()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    /** Действие игрока: клиент отправляет серверу. */
    public record ActionC2S(String action, List<Integer> selection)
            implements CustomPacketPayload {

        public static final Type<ActionC2S> ID =
                CustomPacketPayload.createType(ResourceIds.ACTION);
        public static final StreamCodec<RegistryFriendlyByteBuf, ActionC2S> CODEC =
                StreamCodec.of((buf, value) -> {
                    buf.writeUtf(value.action());
                    List<Integer> selection = value.selection();
                    buf.writeVarInt(selection.size());
                    for (int i : selection) {
                        buf.writeVarInt(i);
                    }
                }, buf -> {
                    String action = buf.readUtf();
                    int size = buf.readVarInt();
                    List<Integer> selection = new ArrayList<>(Math.max(0, size));
                    for (int i = 0; i < size; i++) {
                        selection.add(buf.readVarInt());
                    }
                    return new ActionC2S(action, selection);
                });

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    /** Идентификаторы сетевых каналов. */
    public static final class ResourceIds {
        public static final String STATE = "balatro:state";
        public static final String ACTION = "balatro:action";
        public static final String OPEN = "balatro:open";

        private ResourceIds() {
        }
    }

    /** Все действия, которые клиент может отправить серверу. */
    public static final class Actions {
        public static final String PLAY_HAND = "play_hand";
        public static final String DISCARD = "discard";
        public static final String USE_CONSUMABLE = "use_consumable";
        public static final String BUY_JOKER = "buy_joker";
        public static final String SELL_JOKER = "sell_joker";
        public static final String BUY_VOUCHER = "buy_voucher";
        public static final String BUY_PACK = "buy_pack";
        public static final String REROLL = "reroll";
        public static final String NEXT_ROUND = "next_round";
        public static final String SKIP_BLIND = "skip_blind";
        public static final String OPEN_SHOP = "open_shop";

        private Actions() {
        }
    }

    /** Имена экранов. */
    public static final class Screens {
        public static final String TABLE = "table";
        public static final String SHOP = "shop";

        private Screens() {
        }
    }

    private BalatroPayloads() {
    }
}