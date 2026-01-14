package net.phoenix.core.saveddata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;


import java.math.BigInteger;
import java.util.*;

public class TeslaTeamEnergyData extends SavedData {

    private static final String DATA_NAME = "phoenix_tesla_team_energy";

    public static class TeamEnergy {
        public BigInteger stored = BigInteger.ZERO;
        public BigInteger capacity = BigInteger.ZERO;

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Stored", stored.toString());
            tag.putString("Capacity", capacity.toString());
            return tag;
        }

        public static TeamEnergy load(CompoundTag tag) {
            TeamEnergy e = new TeamEnergy();
            e.stored = new BigInteger(tag.getString("Stored"));
            e.capacity = new BigInteger(tag.getString("Capacity"));
            return e;
        }
    }

    private final Map<UUID, TeamEnergy> networks = new HashMap<>();
    private final Set<UUID> onlineNetworks = new HashSet<>();

    public TeamEnergy getOrCreate(UUID team) {
        return networks.computeIfAbsent(team, k -> new TeamEnergy());
    }

    public void setOnline(UUID team, boolean online) {
        if (online) onlineNetworks.add(team);
        else onlineNetworks.remove(team);
        setDirty();
    }

    public boolean isOnline(UUID team) {
        return onlineNetworks.contains(team);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        for (var e : networks.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("Team", e.getKey());
            t.put("Energy", e.getValue().save());
            list.add(t);
        }
        tag.put("Networks", list);
        return tag;
    }

    public static TeslaTeamEnergyData load(CompoundTag tag) {
        TeslaTeamEnergyData data = new TeslaTeamEnergyData();
        ListTag list = tag.getList("Networks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            data.networks.put(
                    t.getUUID("Team"),
                    TeamEnergy.load(t.getCompound("Energy"))
            );
        }
        return data;
    }

    public static TeslaTeamEnergyData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                TeslaTeamEnergyData::load,
                TeslaTeamEnergyData::new,
                DATA_NAME
        );
    }
}


