package net.phoenix.core.common.machine.multiblock.part.special;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.phoenix.core.common.data.item.PhoenixItems;
import net.phoenix.core.common.machine.multiblock.electric.TeslaTowerMachine;
import net.phoenix.core.common.machine.multiblock.electric.TeslaWirelessRegistry;
import net.phoenix.core.configs.PhoenixConfigs;
import net.phoenix.core.utils.TeamUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TeslaEnergyHatchPartMachine extends EnergyHatchPartMachine implements IDataStickInteractable {

    // ---------------------------------------
    // Managed fields
    // ---------------------------------------
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            TeslaEnergyHatchPartMachine.class, EnergyHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private UUID ownerTeamUUID;

    // Cached reference to tower (not persisted)
    private TeslaTowerMachine boundTower;

    @Override
    public void onLoad() {
        super.onLoad();
        if (isWireless()) {
            TeslaWirelessRegistry.registerHatch(this);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        TeslaWirelessRegistry.unregisterHatch(this);
    }

    // ---------------------------------------
    // Constructor & field holder
    // ---------------------------------------
    public TeslaEnergyHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, int amperage, Object... args) {
        super(holder, tier, io, amperage, args);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // ---------------------------------------
    // Energy container accessor
    // ---------------------------------------
    public IEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public IO getIO() {
        return io;
    }

    public @Nullable TeslaTowerMachine getBoundTower() {
        return boundTower;
    }

    public void bindToTower(TeslaTowerMachine tower) {
        this.boundTower = tower;
        self().markDirty();
    }

    // ---------------------------------------
    // Wireless logic
    // ---------------------------------------
    public boolean isWireless() {
        if (ownerTeamUUID == null) return false;

        PhoenixConfigs.FeatureConfigs.TeslaConnectionMode mode = PhoenixConfigs.INSTANCE.features.teslaConnectionMode;

        return mode == PhoenixConfigs.FeatureConfigs.TeslaConnectionMode.TEAM_AUTO ||
                mode == PhoenixConfigs.FeatureConfigs.TeslaConnectionMode.DATA_STICK;
    }

    /**
     * Ticks wireless energy transfer.
     * Works for hatches outside the multiblock if they have a bound team.
     */
    public void tickWireless() {
        if (getLevel().isClientSide || !isWireless() || ownerTeamUUID == null) return;

        // TEAM_AUTO: auto-bind tower if not bound
        if (PhoenixConfigs.INSTANCE.features.teslaConnectionMode ==
                PhoenixConfigs.FeatureConfigs.TeslaConnectionMode.TEAM_AUTO && boundTower == null) {
            boundTower = TeslaTowerMachine.getTowerByTeam(ownerTeamUUID);
            if (boundTower != null) bindToTower(boundTower);
        }

        // Skip if still unbound (DATA_STICK or auto)
        if (boundTower == null || boundTower.getEnergyBank() == null) return;

        TeslaTowerMachine.TeslaEnergyBank bank = boundTower.getEnergyBank();

        long stored = energyContainer.getEnergyStored();
        long capacity = energyContainer.getEnergyCapacity();
        long transferRate = energyContainer.getInputVoltage() * energyContainer.getInputAmperage();

        if (getIO() == IO.OUT) {
            // IN hatch: pull energy from tower → fills hatch
            long space = capacity - stored;
            long toPull = Math.min(transferRate, space);
            long pulled = bank.drain(toPull);
            energyContainer.changeEnergy(pulled);
        } else if (getIO() == IO.IN) {
            long toPush = Math.min(transferRate, stored);
            long accepted = bank.fill(toPush);
            energyContainer.changeEnergy(-accepted);
        }
    }

    // ---------------------------------------
    // Team logic
    // ---------------------------------------
    public @Nullable UUID getOwnerTeamUUID() {
        autoLinkTeamIfNeeded();
        return ownerTeamUUID;
    }

    private void autoLinkTeamIfNeeded() {
        if (!(getLevel() instanceof ServerLevel sl)) return;

        // DATA_STICK mode: manual bind only
        if (PhoenixConfigs.INSTANCE.features.teslaConnectionMode ==
                PhoenixConfigs.FeatureConfigs.TeslaConnectionMode.DATA_STICK)
            return;

        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID == null) return;

        ServerPlayer sp = sl.getServer().getPlayerList().getPlayer(ownerUUID);
        if (sp == null) return;

        UUID team = TeamUtils.getTeamIdOrPlayerFallback(sp);
        if (!team.equals(ownerTeamUUID)) {
            ownerTeamUUID = team;
            self().markDirty();
        }
    }

    // ---------------------------------------
    // Data stick binding
    // ---------------------------------------
    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack binder) {
        if (!binder.is(PhoenixItems.TESLA_BINDER.get())) return InteractionResult.PASS;

        if (binder.hasTag() && binder.getTag().hasUUID("TargetTeam")) {
            UUID binderUUID = binder.getTag().getUUID("TargetTeam");

            if (!getLevel().isClientSide) {
                if (!binderUUID.equals(ownerTeamUUID)) {
                    ownerTeamUUID = binderUUID;
                    boundTower = TeslaTowerMachine.getTowerByTeam(ownerTeamUUID);
                    self().markDirty();
                    player.sendSystemMessage(
                            Component.literal("Tesla Hatch: Connected to frequency " + ownerTeamUUID)
                                    .withStyle(ChatFormatting.AQUA));
                } else {
                    player.sendSystemMessage(
                            Component.literal("Tesla Hatch: Already synced.")
                                    .withStyle(ChatFormatting.GRAY));
                }
            }
            return InteractionResult.sidedSuccess(getLevel().isClientSide);
        }

        return InteractionResult.SUCCESS;
    }
}
