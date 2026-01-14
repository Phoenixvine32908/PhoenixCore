package net.phoenix.core.saveddata;

import net.minecraft.core.BlockPos;
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

        // diagnostics / stats (keep as Long for simplicity)
        public final Map<BlockPos, BigInteger> energyInput = new HashMap<>();
        public final Map<BlockPos, BigInteger> energyOutput = new HashMap<>();
        public final Map<BlockPos, BigInteger> energyBuffered = new HashMap<>();

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

    // Change the method to accept 'amount'
    public BigInteger addEnergy(UUID team, BigInteger amount) {
        TeamEnergy e = getOrCreate(team);

        BigInteger before = e.stored;
        // Calculate new total, clamping between 0 and Capacity
        BigInteger after = before.add(amount).max(BigInteger.ZERO).min(e.capacity);

        e.stored = after;
        setDirty();

        return after.subtract(before); // returns the actual change
    }

    // --- UPDATED: BigInteger versions ---
    public void setEnergyInput(UUID team, BlockPos pos, BigInteger euPerTick) {
        getOrCreate(team).energyInput.put(pos, euPerTick);
        setDirty();
    }

    public void setEnergyOutput(UUID team, BlockPos pos, BigInteger euPerTick) {
        getOrCreate(team).energyOutput.put(pos, euPerTick);
        setDirty();
    }

    public void setEnergyBuffered(UUID team, BlockPos pos, BigInteger stored) {
        getOrCreate(team).energyBuffered.put(pos, stored);
        setDirty();
    }
    // ------------------------------------

    public void removeEndpoint(UUID team, BlockPos pos) {
        TeamEnergy e = getOrCreate(team);
        e.energyInput.remove(pos);
        e.energyOutput.remove(pos);
        e.energyBuffered.remove(pos);
        setDirty();
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
                    TeamEnergy.load(t.getCompound("Energy")));
        }
        return data;
    }

    public static TeslaTeamEnergyData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                TeslaTeamEnergyData::load,
                TeslaTeamEnergyData::new,
                DATA_NAME);
    }
}
