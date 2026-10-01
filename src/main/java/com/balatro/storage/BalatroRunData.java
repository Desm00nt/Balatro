package com.balatro.storage;

import com.balatro.core.run.RunSnapshot;
import com.balatro.core.run.RunState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Хранит активные забеги всех игроков на диске мира.
 * Ключ — UUID игрока.
 */
public class BalatroRunData extends SavedData {

    private static final String FILE_ID = "balatro_runs";
    private final Map<UUID, CompoundTag> runs = new HashMap<>();

    public BalatroRunData() {
    }

    public static BalatroRunData get(net.minecraft.server.MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(Factory, FILE_ID);
    }

    /** Фабрика загрузки данных: конструктор, десериализатор, тип исправлений. */
    private static final net.minecraft.world.level.saveddata.SavedData.Factory<BalatroRunData>
            Factory = new net.minecraft.world.level.saveddata.SavedData.Factory<>(
            BalatroRunData::new,
            (tag, registries) -> load(tag),
            net.minecraft.util.datafix.DataFixTypes.LEVEL);

    /** Читает данные из NBT. */
    public static BalatroRunData load(CompoundTag tag) {
        BalatroRunData data = new BalatroRunData();
        CompoundTag all = tag.getCompound("runs");
        for (String key : all.getAllKeys()) {
            try {
                data.runs.put(java.util.UUID.fromString(key), all.getCompound(key));
            } catch (IllegalArgumentException ignored) {
                // Повреждённый ключ — пропускаем запись.
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag all = new CompoundTag();
        runs.forEach((uuid, runTag) -> all.put(uuid.toString(), runTag));
        tag.put("runs", all);
        return tag;
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