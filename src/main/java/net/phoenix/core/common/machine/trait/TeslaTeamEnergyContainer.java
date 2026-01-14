package net.phoenix.core.common.machine.trait;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;

import net.minecraft.server.level.ServerLevel;
import net.phoenix.core.common.machine.multiblock.part.special.TeslaEnergyHatchPartMachine;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;

import java.math.BigInteger;
import java.util.UUID;

import javax.annotation.Nullable;

public class TeslaTeamEnergyContainer extends NotifiableEnergyContainer {

    private final TeslaEnergyHatchPartMachine hatch;

    public TeslaTeamEnergyContainer(TeslaEnergyHatchPartMachine hatch, long voltage, int amperage) {
        super(
                hatch,                 // MetaMachine (your hatch implements MetaMachine)
                0L,                    // capacity: actual stored is in TeslaTeamEnergyData
                voltage,               // input voltage
                amperage,              // input amperage
                voltage,               // output voltage
                amperage               // output amperage
        );
        this.hatch = hatch;
    }

    @Nullable
    private TeslaTeamEnergyData.TeamEnergy getTeamNet() {
        if (!(hatch.getLevel() instanceof ServerLevel sl)) return null;
        UUID team = hatch.getOwnerTeamUUID();
        if (team == null) return null;
        return TeslaTeamEnergyData.get(sl).getOrCreate(team);
    }

    @Override
    public long getEnergyStored() {
        var net = getTeamNet();
        return net == null ? 0 :
                net.stored.min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
    }

    @Override
    public long getEnergyCapacity() {
        var net = getTeamNet();
        return net == null ? 0 :
                net.capacity.min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
    }

    /** Adds energy to the Tesla team bank */
    @Override
    public long addEnergy(long amount) {
        var net = getTeamNet();
        if (net == null) return 0;

        BigInteger space = net.capacity.subtract(net.stored);
        long accepted = space.min(BigInteger.valueOf(amount)).longValue();

        if (accepted > 0) {
            net.stored = net.stored.add(BigInteger.valueOf(accepted));
            markDirty();
        }
        return accepted;
    }

    /** Removes energy from the Tesla team bank */
    @Override
    public long removeEnergy(long amount) {
        var net = getTeamNet();
        if (net == null) return 0;

        long drained = net.stored.min(BigInteger.valueOf(amount)).longValue();
        if (drained > 0) {
            net.stored = net.stored.subtract(BigInteger.valueOf(drained));
            markDirty();
        }
        return drained;
    }

    private void markDirty() {
        if (hatch.getLevel() instanceof ServerLevel sl) {
            TeslaTeamEnergyData.get(sl).setDirty();
        }
    }
}
