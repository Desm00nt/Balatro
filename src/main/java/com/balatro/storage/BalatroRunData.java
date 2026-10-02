package com.balatro.storage;

import com.balatro.core.run.RunSnapshot;
import com.balatro.core.run.RunState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Хранит активные забеги всех игроков. В Minecraft 26.x данные
 * сохраняются через {@link SavedDataType} и {@link Codec}.
 */
public class BalatroRunData extends SavedData {

    /** Ключ — UUID игрока, значение — NBT снимка забега. */
    private final Map<UUID, CompoundTag> runs = new HashMap<>();

    /** Кодек хранилища: карта UUID -> NBT. */
    private static final Codec<Map<UUID, CompoundTag>> CODEC = Codec.unboundedMap(
            net.minecraft.core.UUIDUtil.CODEC, CompoundTag.CODEC);

    private static final SavedDataType<BalatroRunData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("balatro", "runs"),
            BalatroRunData::new,
            RecordCodecBuilder.create(instance -> instance.group(
                    CODEC.fieldOf("runs").forGetter(BalatroRunData::runs)
            ).apply(instance, BalatroRunData::new)),
            DataFixTypes.LEVEL);

    public BalatroRunData(Map<UUID, CompoundTag> runs) {
        this.runs.putAll(runs);
    }

    public BalatroRunData() {
    }

    public static BalatroRunData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    private Map<UUID, CompoundTag> runs() {
        return runs;
    }

    /** Сохраняет забег игрока. */
    public void put(ServerPlayer player, RunState run) {
        runs.put(player.getUUID(), RunNbt.toNbt(run.toSnapshot()));
        setDirty();
    }

    /** Загружает забег игрока или null. */
    public RunState get(ServerPlayer player) {
        CompoundTag tag = runs.get(player.getUUID());
        if (tag == null) {
            return null;
        }
        RunSnapshot snapshot = RunNbt.fromNbt(tag);
        if (snapshot.startingDeck == null || snapshot.startingDeck.isEmpty()) {
            return null;
        }
        try {
            return RunState.fromSnapshot(snapshot);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean has(ServerPlayer player) {
        return runs.containsKey(player.getUUID());
    }

    public void remove(ServerPlayer player) {
        if (runs.remove(player.getUUID()) != null) {
            setDirty();
        }
    }
}