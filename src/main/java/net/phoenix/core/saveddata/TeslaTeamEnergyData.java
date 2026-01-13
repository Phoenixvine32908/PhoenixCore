package net.phoenix.core.saveddata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.phoenix.core.common.machine.multiblock.electric.TeslaEnergyBank;

import java.util.*;

/**
 * Tracks all TeslaTeam energy banks across the server.
 * Each team has a single TeslaEnergyBank instance.
 */
public class TeslaTeamEnergyData extends SavedData {

    private static final String DATA_NAME = "phoenix_tesla_team_energy";

    private final Map<UUID, TeslaEnergyBank> energyBanks = new HashMap<>();
    private final Set<UUID> activeNetworks = new HashSet<>();

    public TeslaTeamEnergyData() {}

    public void setNetworkOnline(UUID teamId, boolean online) {
        if (online) activeNetworks.add(teamId);
        else activeNetworks.remove(teamId);
    }

    public boolean isNetworkOnline(UUID teamId) {
        return activeNetworks.contains(teamId);
    }

    public TeslaEnergyBank getOrCreateEnergyBank(UUID teamId) {
        if (!energyBanks.containsKey(teamId)) {
            energyBanks.put(teamId, new TeslaEnergyBank());
            setDirty();
        }
        return energyBanks.get(teamId);
    }

    public TeslaEnergyBank getEnergyBank(UUID teamId) {
        return energyBanks.get(teamId);
    }


    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, TeslaEnergyBank> entry : energyBanks.entrySet()) {
            CompoundTag bankTag = new CompoundTag();
            bankTag.putUUID("Team", entry.getKey());
            bankTag.put("Bank", entry.getValue().writeToNBT());
            list.add(bankTag);
        }
        tag.put("Banks", list);
        return tag;
    }

    public static TeslaTeamEnergyData load(CompoundTag tag) {
        TeslaTeamEnergyData data = new TeslaTeamEnergyData();

        if (tag.contains("Banks", ListTag.TAG_LIST)) {
            ListTag list = tag.getList("Banks", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag bankTag = list.getCompound(i);
                UUID team = bankTag.getUUID("Team");
                TeslaEnergyBank bank = new TeslaEnergyBank();
                bank.readFromNBT(bankTag.getCompound("Bank"));
                data.energyBanks.put(team, bank);
            }
        }

        return data;
    }

    public static TeslaTeamEnergyData get(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("TeslaTeamEnergyData accessed on client!");
        }
        return serverLevel.getDataStorage().computeIfAbsent(
                TeslaTeamEnergyData::load,
                TeslaTeamEnergyData::new,
                DATA_NAME);
    }
}
