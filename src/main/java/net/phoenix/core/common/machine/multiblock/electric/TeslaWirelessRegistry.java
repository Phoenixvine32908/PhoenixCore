package net.phoenix.core.common.machine.multiblock.electric;

import net.phoenix.core.common.machine.multiblock.part.special.TeslaEnergyHatchPartMachine;

import org.jetbrains.annotations.Nullable;

import java.util.*;

import static software.bernie.geckolib.util.ClientUtils.getLevel;

public final class TeslaWirelessRegistry {

    // Map team UUID → set of wireless hatches for that team
    private static final Map<UUID, Set<TeslaEnergyHatchPartMachine>> TEAM_HATCHES = new HashMap<>();

    // Map team UUID → tower
    private static final Map<UUID, TeslaTowerMachine> TEAM_TOWERS = new HashMap<>();

    // --------------------------
    // Wireless hatch management
    // --------------------------

    public static void registerHatch(TeslaEnergyHatchPartMachine hatch) {
        if (hatch.getOwnerTeamUUID() == null) return;
        TEAM_HATCHES.computeIfAbsent(hatch.getOwnerTeamUUID(), k -> new HashSet<>()).add(hatch);
    }


    public static void unregisterHatch(TeslaEnergyHatchPartMachine hatch) {
        if (hatch.getOwnerTeamUUID() == null) return;
        Set<TeslaEnergyHatchPartMachine> hatches = TEAM_HATCHES.get(hatch.getOwnerTeamUUID());
        if (hatches != null) {
            hatches.remove(hatch);
            if (hatches.isEmpty()) TEAM_HATCHES.remove(hatch.getOwnerTeamUUID());
        }
    }

    @Nullable
    public static Set<TeslaEnergyHatchPartMachine> getHatches(UUID team) {
        return TEAM_HATCHES.get(team);
    }

    // --------------------------
    // Tower management
    // --------------------------

    public static void registerTower(TeslaTowerMachine tower) {
        if (tower.getOwnerUUID() != null) {
            TEAM_TOWERS.put(tower.getOwnerUUID(), tower);
        }
    }

    public static void unregisterTower(TeslaTowerMachine tower) {
        if (tower.getOwnerUUID() != null) {
            TEAM_TOWERS.remove(tower.getOwnerUUID());
        }
    }

    @Nullable
    public static TeslaTowerMachine getTowerByTeam(UUID team) {
        return TEAM_TOWERS.get(team);
    }

    @Nullable
    public static TeslaTowerMachine.TeslaEnergyBank getBank(UUID team) {
        TeslaTowerMachine tower = getTowerByTeam(team);
        if (tower == null) return null;
        return tower.getEnergyBank();
    }

    // --------------------------
    // Tick all wireless hatches for a team
    // --------------------------

    public static void tickTeamHatches(UUID team) {
        TeslaTowerMachine.TeslaEnergyBank bank = getBank(team);
        if (bank == null) return;

        Set<TeslaEnergyHatchPartMachine> hatches = getHatches(team);
        if (hatches == null) return;

        for (TeslaEnergyHatchPartMachine hatch : hatches) {
            // Only tick if wireless
            if (!hatch.isWireless()) continue;

            // Make sure IN pushes to tower, OUT pulls from tower
            hatch.tickWireless();
        }
    }
}