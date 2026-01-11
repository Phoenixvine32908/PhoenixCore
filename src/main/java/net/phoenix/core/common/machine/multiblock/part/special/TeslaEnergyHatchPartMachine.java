package net.phoenix.core.common.machine.multiblock.part.special;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.phoenix.core.common.data.item.PhoenixItems;
import net.phoenix.core.common.machine.multiblock.electric.TeslaEnergyBank;
import net.phoenix.core.configs.PhoenixConfigs;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;
import net.phoenix.core.utils.TeamUtils;

import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import java.util.UUID;

public class TeslaEnergyHatchPartMachine extends EnergyHatchPartMachine implements IDataStickInteractable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            TeslaEnergyHatchPartMachine.class, EnergyHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private UUID ownerTeamUUID;

    @Nullable
    protected TickableSubscription teslaTransferSub;

    public TeslaEnergyHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, int amperage, Object... args) {
        super(holder, tier, io, amperage, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // ------------------------------------------------------------------
    //  LIFECYCLE & TICKING
    // ------------------------------------------------------------------

    @Override
    public void onLoad() {
        super.onLoad();
        // Subscribe to server tick for wireless transfer logic
        if (!isRemote()) {
            teslaTransferSub = subscribeServerTick(this::processTeslaTransfer);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (teslaTransferSub != null) {
            teslaTransferSub.unsubscribe();
            teslaTransferSub = null;
        }
    }

    private void processTeslaTransfer() {
        // Only transfer if the hatch is part of a formed multiblock
        if (!isFormed()) return;

        TeslaEnergyBank bank = getTeamBank();
        if (bank == null) return;

        long v = GTValues.V[tier];
        long transferLimit = v * amperage;

        if (io == IO.IN) {
            // INPUT: Push from Hatch Buffer -> Tesla Bank
            long stored = energyContainer.getEnergyStored();
            long toDeposit = Math.min(stored, transferLimit);

            if (toDeposit > 0) {
                long accepted = Math.min(toDeposit, bank.getMaxInput());
                if (accepted > 0) {
                    bank.deposit(accepted);
                    energyContainer.changeEnergy(-accepted);
                    TeslaTeamEnergyData.get((ServerLevel)getLevel()).setDirty();
                }
            }
        } else if (io == IO.OUT) {
            // OUTPUT: Pull from Tesla Bank -> Hatch Buffer
            long space = energyContainer.getEnergyCapacity() - energyContainer.getEnergyStored();
            long availableSpace = Math.min(space, transferLimit);

            if (availableSpace > 0) {
                long toPull = Math.min(availableSpace, bank.getMaxOutput());
                long pulled = bank.extract(toPull);
                if (pulled > 0) {
                    energyContainer.changeEnergy(pulled);
                    TeslaTeamEnergyData.get((ServerLevel)getLevel()).setDirty();
                }
            }
        }
    }

    // ------------------------------------------------------------------
    //  TEAM & BANK RESOLUTION
    // ------------------------------------------------------------------

    private TeslaEnergyBank getTeamBank() {
        ensureOwnerTeamUUID();
        if (ownerTeamUUID == null || !(getLevel() instanceof ServerLevel sl)) return null;
        return TeslaTeamEnergyData.get(sl).getEnergyBank(ownerTeamUUID);
    }

    private void ensureOwnerTeamUUID() {
        if (PhoenixConfigs.INSTANCE.features.teslaConnectionMode ==
                PhoenixConfigs.FeatureConfigs.TeslaConnectionMode.DATA_STICK) {
            return;
        }

        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID == null) return;

        ServerPlayer sp = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
        if (sp != null) {
            UUID newTeamUUID = TeamUtils.getTeamIdOrPlayerFallback(sp);
            if (!newTeamUUID.equals(this.ownerTeamUUID)) {
                this.ownerTeamUUID = newTeamUUID;
                self().markDirty();
            }
        }
    }

    // ------------------------------------------------------------------
    //  DATA STICK / BINDER LINKING
    // ------------------------------------------------------------------

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack binder) {
        if (!binder.is(PhoenixItems.TESLA_BINDER.get())) return InteractionResult.PASS;

        if (binder.hasTag() && binder.getTag().hasUUID("TargetTeam")) {
            UUID binderUUID = binder.getTag().getUUID("TargetTeam");

            if (!getLevel().isClientSide) {
                if (!binderUUID.equals(this.ownerTeamUUID)) {
                    this.ownerTeamUUID = binderUUID;
                    self().markDirty();
                    player.sendSystemMessage(Component.literal("Tesla Hatch: Connected to frequency " + ownerTeamUUID).withStyle(ChatFormatting.AQUA));
                } else {
                    player.sendSystemMessage(Component.literal("Tesla Hatch: Already synced.").withStyle(ChatFormatting.GRAY));
                }
            }
            return InteractionResult.sidedSuccess(getLevel().isClientSide);
        }
        return InteractionResult.SUCCESS;
    }

    // ------------------------------------------------------------------
    //  PERSISTENCE
    // ------------------------------------------------------------------

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (ownerTeamUUID != null) tag.putUUID("OwnerTeamUUID", ownerTeamUUID);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (tag.hasUUID("OwnerTeamUUID")) ownerTeamUUID = tag.getUUID("OwnerTeamUUID");
    }
}