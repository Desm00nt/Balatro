package com.balatro.net;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Пакеты сетевого взаимодействия с игрой Balatro. */
public final class BalatroPayloads {

    /** Полное состояние забега: сервер отправляет клиенту. */
    public record StateS2C(CompoundTag tag) implements CustomPacketPayload {

        public static final Type<StateS2C> ID = ResourceIds.type("state");
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

        public static final Type<OpenScreenS2C> ID = ResourceIds.type("open");
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

        public static final Type<ActionC2S> ID = ResourceIds.type("action");
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

    /**
     * Идентификаторы сетевых каналов.
     *
     * <p>В 26.2 конструктору {@link CustomPacketPayload.Type} нужно передавать
     * готовый {@link Identifier}: {@code createType(String)} принимает только путь
     * и всегда подставляет пространство имён {@code minecraft}, из-за чего строка
     * вида {@code "balatro:state} вызывает IdentifierException.
     */
    public static final class ResourceIds {

        private ResourceIds() {
        }

        /** Создаёт тип пакета с пространством имён {@code balatro}. */
        public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String path) {
            return new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath("balatro", path));
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