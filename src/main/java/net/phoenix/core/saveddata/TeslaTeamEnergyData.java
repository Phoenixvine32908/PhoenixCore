package net.phoenix.core.saveddata;

import com.electronwill.nightconfig.core.utils.ObservedMap;
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


    public java.util.Set<BlockPos> getEndpoints(UUID teamUUID) {
        TeamEnergy team = networks.get(teamUUID);
        if (team == null || team.hatchStored == null) return java.util.Collections.emptySet();
        return team.hatchStored.keySet();
    }
    public static class HatchInfo {
        public final BlockPos pos;
        public BigInteger input = BigInteger.ZERO;
        public BigInteger output = BigInteger.ZERO;
        public BigInteger buffered = BigInteger.ZERO;

        public HatchInfo(BlockPos pos) {
            this.pos = pos;
        }
    }
    public Collection<HatchInfo> getHatches(UUID team) {
        TeamEnergy e = networks.get(team);
        if (e == null) return List.of();

        Map<BlockPos, HatchInfo> map = new HashMap<>();

        e.energyBuffered.forEach((pos, buf) ->
                map.computeIfAbsent(pos, HatchInfo::new).buffered = buf);

        e.energyInput.forEach((pos, in) ->
                map.computeIfAbsent(pos, HatchInfo::new).input = in);

        e.energyOutput.forEach((pos, out) ->
                map.computeIfAbsent(pos, HatchInfo::new).output = out);

        return map.values();
    }


    public static class TeamEnergy {
        public BigInteger stored = BigInteger.ZERO;
        public BigInteger capacity = BigInteger.ZERO;

        public final Map<BlockPos, BigInteger> energyInput = new HashMap<>();
        public final Map<BlockPos, BigInteger> energyOutput = new HashMap<>();
        public final Map<BlockPos, BigInteger> energyBuffered = new HashMap<>();
        private ObservedMap hatchStored;

        // --- NEW: CLOUD POWER LOGIC ---
        public static final int HISTORY = 20;
        public final long[] inHistory = new long[HISTORY];
        public final long[] outHistory = new long[HISTORY];
        public int historyIdx;

        public void pushHistory(long in, long out) {
            inHistory[historyIdx] = in;
            outHistory[historyIdx] = out;
            historyIdx = (historyIdx + 1) % HISTORY;
        }

        /** Adds energy to the cloud, returns how much was actually accepted */
        public BigInteger fill(BigInteger amount) {
            if (amount.signum() <= 0) return BigInteger.ZERO;
            BigInteger space = capacity.subtract(stored).max(BigInteger.ZERO);
            BigInteger toFill = amount.min(space);
            stored = stored.add(toFill);
            return toFill;
        }
        public void setStored(BigInteger amount) {
            // Clamp the value between 0 and the current capacity
            this.stored = amount.max(BigInteger.ZERO).min(this.capacity);
        }
        /**
         * Returns all registered hatch positions for a specific team.
         */


        /** Removes energy from the cloud, returns how much was actually taken */
        public BigInteger drain(BigInteger amount) {
            if (amount.signum() <= 0) return BigInteger.ZERO;
            BigInteger toDrain = stored.min(amount);
            stored = stored.subtract(toDrain);
            return toDrain;
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Stored", stored != null ? stored.toString() : "0");
            tag.putString("Capacity", capacity != null ? capacity.toString() : "0");
            return tag;
        }

        public static TeamEnergy load(CompoundTag tag) {
            TeamEnergy e = new TeamEnergy();
            String storedStr = tag.getString("Stored");
            String capStr = tag.getString("Capacity");

            e.stored = (storedStr.isEmpty()) ? BigInteger.ZERO : new BigInteger(storedStr);
            e.capacity = (capStr.isEmpty()) ? BigInteger.ZERO : new BigInteger(capStr);
            return e;
        }

        public void updateFromTower(BigInteger stored, BigInteger capacity) {
            this.stored = stored;
            this.capacity = capacity;
        }
    }

    private final Map<UUID, TeamEnergy> networks = new HashMap<>();
    private final Set<UUID> onlineNetworks = new HashSet<>();

    // --- NEW: UTILITY FOR CLEAN DEBUG COMMAND ---

    /** Clears the EU/t traffic maps so old hatches don't stay in the debug list */
    public void clearLiveStats(UUID team) {
        TeamEnergy e = networks.get(team);
        if (e != null) {
            e.energyInput.clear();
            e.energyOutput.clear();
            // Note: We keep energyBuffered as that tracks if a hatch physically exists
        }
    }

    public Map<UUID, TeamEnergy> getNetworksView() {
        return Collections.unmodifiableMap(networks);
    }

    public TeamEnergy getTeamEnergy(UUID team) {
        return networks.get(team);
    }

    public Set<UUID> getOnlineNetworks() {
        return Collections.unmodifiableSet(onlineNetworks);
    }

    public int getHatchCount(UUID team) {
        TeamEnergy e = networks.get(team);
        if (e == null) return 0;

        Set<BlockPos> uniquePositions = new HashSet<>();
        uniquePositions.addAll(e.energyInput.keySet());
        uniquePositions.addAll(e.energyOutput.keySet());
        uniquePositions.addAll(e.energyBuffered.keySet());

        return uniquePositions.size();
    }

    public TeamEnergy getOrCreate(UUID team) {
        return networks.computeIfAbsent(team, k -> {
            setDirty();
            return new TeamEnergy();
        });
    }

    // Keep this for legacy support or manual adjustments
    public BigInteger addEnergy(UUID team, BigInteger amount) {
        TeamEnergy e = getOrCreate(team);
        BigInteger before = e.stored;
        e.stored = before.add(amount).max(BigInteger.ZERO).min(e.capacity);
        setDirty();
        return e.stored.subtract(before);
    }

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

    public void removeEndpoint(UUID team, BlockPos pos) {
        TeamEnergy e = networks.get(team);
        if (e != null) {
            e.energyInput.remove(pos);
            e.energyOutput.remove(pos);
            e.energyBuffered.remove(pos);
            setDirty();
        }
    }

    public void setOnline(UUID team, boolean online) {
        if (online) {
            if (onlineNetworks.add(team)) setDirty();
        } else {
            if (onlineNetworks.remove(team)) setDirty();
        }
    }

    public boolean isOnline(UUID team) {
        return onlineNetworks.contains(team);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        for (var entry : networks.entrySet()) {
            CompoundTag teamTag = new CompoundTag();
            teamTag.putUUID("Team", entry.getKey());
            teamTag.put("Energy", entry.getValue().save());
            teamTag.putBoolean("IsOnline", onlineNetworks.contains(entry.getKey()));
            list.add(teamTag);
        }
        tag.put("Networks", list);
        return tag;
    }

    public static TeslaTeamEnergyData load(CompoundTag tag) {
        TeslaTeamEnergyData data = new TeslaTeamEnergyData();
        ListTag list = tag.getList("Networks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag teamTag = list.getCompound(i);
            UUID teamUUID = teamTag.getUUID("Team");
            data.networks.put(teamUUID, TeamEnergy.load(teamTag.getCompound("Energy")));
            if (teamTag.getBoolean("IsOnline")) {
                data.onlineNetworks.add(teamUUID);
            }
        }
        return data;
    }

    public static TeslaTeamEnergyData get(ServerLevel level) {
        // ALWAYS use the Overworld (Level.OVERWORLD) to ensure global dimension-independent data
        ServerLevel overworld = level.getServer().getLevel(ServerLevel.OVERWORLD);
        return (overworld == null ? level : overworld).getDataStorage().computeIfAbsent(
                TeslaTeamEnergyData::load,
                TeslaTeamEnergyData::new,
                DATA_NAME);
    }
}