package net.phoenix.core.saveddata;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;

public class TeslaTeamEnergyData extends SavedData {

    private static final String DATA_NAME = "phoenix_tesla_team_energy";
    private final Map<UUID, TeamEnergy> networks = new HashMap<>();
    private final Set<UUID> onlineNetworks = new HashSet<>();

    // --- DATA TRANSFER OBJECTS ---

    public static class HatchInfo {

        public final BlockPos pos;
        public BigInteger input = BigInteger.ZERO;
        public BigInteger output = BigInteger.ZERO;
        public BigInteger buffered = BigInteger.ZERO;
        public boolean isPhysicalOutput = false;
        public boolean isSoulLinked = false;

        public HatchInfo(BlockPos pos) {
            this.pos = pos;
        }
    }

    // --- INTERNAL TEAM CLASS ---

    public static class TeamEnergy {

        public BigInteger stored = BigInteger.ZERO;
        public BigInteger capacity = BigInteger.ZERO;

        public final Map<BlockPos, Long> lastSeen = new HashMap<>();
        public final Map<BlockPos, BigInteger> energyInput = new HashMap<>();
        public final Map<BlockPos, BigInteger> energyOutput = new HashMap<>();
        public final Map<BlockPos, BigInteger> energyBuffered = new HashMap<>();
        public final Map<BlockPos, Boolean> hatchIsOutput = new HashMap<>();

        // Stores independent machines (Single Blocks)
        public final Set<BlockPos> soulLinkedMachines = new HashSet<>();

        public void markHatchActive(BlockPos pos, long gameTime) {
            lastSeen.put(pos, gameTime);
        }

        public int getLiveHatchCount(long currentGameTime) {
            lastSeen.entrySet().removeIf(entry -> (currentGameTime - entry.getValue()) > 40);
            return lastSeen.size();
        }

        public BigInteger fill(BigInteger amount) {
            if (amount.signum() <= 0) return BigInteger.ZERO;
            BigInteger space = capacity.subtract(stored).max(BigInteger.ZERO);
            BigInteger toFill = amount.min(space);
            stored = stored.add(toFill);
            return toFill;
        }

        public BigInteger drain(BigInteger amount) {
            if (amount.signum() <= 0) return BigInteger.ZERO;
            BigInteger toDrain = stored.min(amount);
            stored = stored.subtract(toDrain);
            return toDrain;
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Stored", stored.toString());
            tag.putString("Capacity", capacity.toString());

            // Physical Hatches
            ListTag hatchIOList = new ListTag();
            hatchIsOutput.forEach((pos, isOut) -> {
                CompoundTag entry = new CompoundTag();
                entry.putLong("p", pos.asLong());
                entry.putBoolean("o", isOut);
                hatchIOList.add(entry);
            });
            tag.put("HatchIO", hatchIOList);

            // Buffer Data
            ListTag bufferList = new ListTag();
            energyBuffered.forEach((pos, buf) -> {
                CompoundTag entry = new CompoundTag();
                entry.putLong("p", pos.asLong());
                entry.putString("b", buf.toString());
                bufferList.add(entry);
            });
            tag.put("Buffers", bufferList);

            // Soul Links (Single Blocks)
            ListTag soulList = new ListTag();
            soulLinkedMachines.forEach(pos -> soulList.add(LongTag.valueOf(pos.asLong())));
            tag.put("SoulLinks", soulList);

            return tag;
        }

        public static TeamEnergy load(CompoundTag tag) {
            TeamEnergy e = new TeamEnergy();
            e.stored = new BigInteger(tag.getString("Stored").isEmpty() ? "0" : tag.getString("Stored"));
            e.capacity = new BigInteger(tag.getString("Capacity").isEmpty() ? "0" : tag.getString("Capacity"));

            if (tag.contains("HatchIO")) {
                ListTag list = tag.getList("HatchIO", Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag entry = list.getCompound(i);
                    e.hatchIsOutput.put(BlockPos.of(entry.getLong("p")), entry.getBoolean("o"));
                }
            }

            if (tag.contains("Buffers")) {
                ListTag list = tag.getList("Buffers", Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag entry = list.getCompound(i);
                    e.energyBuffered.put(BlockPos.of(entry.getLong("p")), new BigInteger(entry.getString("b")));
                }
            }

            if (tag.contains("SoulLinks")) {
                ListTag list = tag.getList("SoulLinks", Tag.TAG_LONG);
                for (int i = 0; i < list.size(); i++) {
                    e.soulLinkedMachines.add(BlockPos.of(((LongTag) list.get(i)).getAsLong()));
                }
            }
            return e;
        }
    }

    // --- WRAPPER & HELPER METHODS ---

    public Map<UUID, TeamEnergy> getNetworksView() {
        return Collections.unmodifiableMap(networks);
    }

    public TeamEnergy getOrCreate(UUID team) {
        return networks.computeIfAbsent(team, k -> {
            setDirty();
            return new TeamEnergy();
        });
    }

    public void setEnergyBuffered(UUID team, BlockPos pos, BigInteger stored, boolean isOutput) {
        TeamEnergy e = getOrCreate(team);
        e.energyBuffered.put(pos, stored);
        e.hatchIsOutput.put(pos, isOutput);
        setDirty();
    }

    public boolean toggleSoulLink(UUID team, BlockPos pos) {
        TeamEnergy e = getOrCreate(team);
        boolean removed = e.soulLinkedMachines.remove(pos);
        if (!removed) {
            e.soulLinkedMachines.add(pos.immutable());
        }
        setDirty();
        return !removed;
    }

    public Collection<HatchInfo> getHatches(UUID team) {
        TeamEnergy e = networks.get(team);
        if (e == null) return List.of();

        Map<BlockPos, HatchInfo> map = new HashMap<>();

        // 1. Process Physical Hatches
        e.energyBuffered.forEach((pos, buf) -> {
            HatchInfo info = map.computeIfAbsent(pos, HatchInfo::new);
            info.buffered = buf;
            info.isPhysicalOutput = e.hatchIsOutput.getOrDefault(pos, false);
        });

        // 2. Process Soul Linked Machines (Single Blocks)
        e.soulLinkedMachines.forEach(pos -> {
            HatchInfo info = map.computeIfAbsent(pos, HatchInfo::new);
            info.isSoulLinked = true;
        });

        // 3. Sync live I/O activity
        e.energyInput.forEach((pos, in) -> {
            if (map.containsKey(pos)) map.get(pos).input = in;
        });
        e.energyOutput.forEach((pos, out) -> {
            if (map.containsKey(pos)) map.get(pos).output = out;
        });

        return map.values();
    }

    public void removeEndpoint(UUID team, BlockPos pos) {
        TeamEnergy e = networks.get(team);
        if (e != null) {
            e.energyInput.remove(pos);
            e.energyOutput.remove(pos);
            e.energyBuffered.remove(pos);
            e.hatchIsOutput.remove(pos);
            e.soulLinkedMachines.remove(pos);
            setDirty();
        }
    }

    public void setOnline(UUID team, boolean online) {
        if (online ? onlineNetworks.add(team) : onlineNetworks.remove(team)) {
            setDirty();
        }
    }

    public boolean isOnline(UUID team) {
        return onlineNetworks.contains(team);
    }

    // --- SAVED DATA OVERRIDES ---

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        for (var entry : networks.entrySet()) {
            CompoundTag teamTag = new CompoundTag();
            teamTag.putUUID("Team", entry.getKey());
            teamTag.put("Energy", entry.getValue().save());
            teamTag.putBoolean("Online", onlineNetworks.contains(entry.getKey()));
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
            if (teamTag.getBoolean("Online")) {
                data.onlineNetworks.add(teamUUID);
            }
        }
        return data;
    }

    public static TeslaTeamEnergyData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(ServerLevel.OVERWORLD);
        return (overworld == null ? level : overworld).getDataStorage().computeIfAbsent(
                TeslaTeamEnergyData::load,
                TeslaTeamEnergyData::new,
                DATA_NAME);
    }
}
