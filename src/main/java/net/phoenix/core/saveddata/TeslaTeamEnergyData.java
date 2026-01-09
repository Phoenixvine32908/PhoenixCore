package net.phoenix.core.saveddata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.phoenix.core.common.machine.multiblock.electric.TeslaEnergyBank;
import net.phoenix.core.configs.PhoenixConfigs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TeslaTeamEnergyData extends SavedData {

    private static final String DATA_NAME = "phoenix_tesla_team_energy";

    private final Map<UUID, TeslaEnergyBank> energyBanks = new HashMap<>();

    /**
     * Tracks which teams currently have an active, formed, and powered Tesla Tower.
     * This is NOT saved to NBT because it should reset on server restart
     * and be repopulated by the towers as they load.
     */
    private final Set<UUID> activeNetworks = new HashSet<>();

    public TeslaTeamEnergyData() {}

    /* ------------------------------------------------------------ */
    /* NETWORK STATUS */
    /* ------------------------------------------------------------ */

    public void setNetworkOnline(UUID teamId, boolean online) {
        if (online) {
            activeNetworks.add(teamId);
        } else {
            activeNetworks.remove(teamId);
        }
    }

    public boolean isNetworkOnline(UUID teamId) {

        if (PhoenixConfigs.INSTANCE.features.teslaConnectionMode == PhoenixConfigs.TeslaConnectionMode.TEAM_AUTO) {
            return activeNetworks.contains(teamId);
        }

        return activeNetworks.contains(teamId);
    }

    /* ------------------------------------------------------------ */
    /* ENERGY BANK ACCESS */
    /* ------------------------------------------------------------ */

    public TeslaEnergyBank getOrCreateEnergyBank(UUID teamId) {
        return energyBanks.computeIfAbsent(teamId, id -> new TeslaEnergyBank());
    }

    public TeslaEnergyBank getEnergyBank(UUID teamId) {
        return energyBanks.get(teamId);
    }

    /* ------------------------------------------------------------ */
    /* PERSISTENCE */
    /* ------------------------------------------------------------ */

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (var entry : energyBanks.entrySet()) {
            CompoundTag bankTag = new CompoundTag();
            bankTag.putUUID("Team", entry.getKey());
            bankTag.put("Bank", entry.getValue().save());
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
                bank.load(bankTag.getCompound("Bank"));
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
