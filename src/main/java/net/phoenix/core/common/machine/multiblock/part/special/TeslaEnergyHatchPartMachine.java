package net.phoenix.core.common.machine.multiblock.part.special;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.phoenix.core.common.machine.multiblock.electric.TeslaEnergyBank;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;
import net.phoenix.core.utils.TeamUtils;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Tesla Energy Hatch:
 * Acts as a wireless transceiver.
 * Input hatches dump EU into the Team Cloud.
 * Output hatches pull EU from the Team Cloud.
 */
public class TeslaEnergyHatchPartMachine extends EnergyHatchPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            TeslaEnergyHatchPartMachine.class, EnergyHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private UUID ownerTeamUUID;

    public TeslaEnergyHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, int amperage, Object... args) {
        super(holder, tier, io, amperage, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private void ensureOwnerTeamUUID() {
        if (ownerTeamUUID != null || !(getLevel() instanceof ServerLevel serverLevel)) return;
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID == null) return;

        ServerPlayer sp = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
        if (sp != null) {
            ownerTeamUUID = TeamUtils.getTeamIdOrPlayerFallback(sp);
        }
    }

    /**
     * Gets the team energy bank only if the network is currently ONLINE.
     */
    private TeslaEnergyBank getTeamBank() {
        ensureOwnerTeamUUID();
        if (ownerTeamUUID == null || !(getLevel() instanceof ServerLevel serverLevel)) return null;

        TeslaTeamEnergyData data = TeslaTeamEnergyData.get(serverLevel);


        if (!data.isNetworkOnline(ownerTeamUUID)) {
            return null;
        }

        return data.getEnergyBank(ownerTeamUUID);
    }

    private void markTeamDataDirty() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            TeslaTeamEnergyData.get(serverLevel).setDirty();
        }
    }

    @Override
    protected NotifiableEnergyContainer createEnergyContainer(Object... args) {
        long voltage = GTValues.V[tier];

        long capacity = io == IO.IN ? voltage * 16L * amperage : voltage * 64L * amperage;

        return new WirelessEnergyContainer(this, capacity, voltage, amperage);
    }

    public class WirelessEnergyContainer extends NotifiableEnergyContainer {

        public WirelessEnergyContainer(TeslaEnergyHatchPartMachine hatch, long capacity, long voltage, int amperage) {
            super(hatch, capacity,
                    io == IO.IN ? voltage : 0, io == IO.IN ? amperage : 0,
                    io == IO.OUT ? voltage : 0, io == IO.OUT ? amperage : 0);
        }

        @Override
        public void serverTick() {
            super.serverTick();


            TeslaEnergyBank bank = getTeamBank();
            if (bank == null) return;


            long v = GTValues.V[tier];
            long transferLimit = v * amperage;

            if (io == IO.IN) {
                long toDeposit = Math.min(getEnergyStored(), transferLimit);
                if (toDeposit > 0) {
                    long accepted = Math.min(toDeposit, bank.getMaxInput());
                    bank.deposit(accepted);
                    setEnergyStored(getEnergyStored() - accepted);
                    markTeamDataDirty();
                }
            } else if (io == IO.OUT) {
                long space = Math.min(getEnergyCapacity() - getEnergyStored(), transferLimit);
                if (space > 0) {
                    long toPull = Math.min(space, bank.getMaxOutput());
                    long pulled = bank.extract(toPull);
                    if (pulled > 0) {
                        setEnergyStored(getEnergyStored() + pulled);
                        markTeamDataDirty();
                    }
                }
            }
        }
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (ownerTeamUUID != null) tag.putUUID("OwnerTeamUUID", ownerTeamUUID);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (tag.hasUUID("OwnerTeamUUID")) {
            ownerTeamUUID = tag.getUUID("OwnerTeamUUID");
        }
    }
}
