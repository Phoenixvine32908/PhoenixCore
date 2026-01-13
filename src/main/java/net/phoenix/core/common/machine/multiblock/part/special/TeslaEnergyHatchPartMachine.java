package net.phoenix.core.common.machine.multiblock.part.special;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
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
import net.phoenix.core.configs.PhoenixConfigs;
import net.phoenix.core.utils.TeamUtils;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import javax.annotation.Nullable;

public class TeslaEnergyHatchPartMachine extends EnergyHatchPartMachine
                                         implements IDataStickInteractable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            TeslaEnergyHatchPartMachine.class,
            EnergyHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private UUID ownerTeamUUID;

    public TeslaEnergyHatchPartMachine(IMachineBlockEntity holder,
                                       int tier,
                                       IO io,
                                       int amperage,
                                       Object... args) {
        super(holder, tier, io, amperage, args);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }



    public @Nullable UUID getOwnerTeamUUID() {
        autoLinkTeamIfNeeded();
        return ownerTeamUUID;
    }

    private void autoLinkTeamIfNeeded() {
        if (!(getLevel() instanceof ServerLevel sl)) return;

        if (PhoenixConfigs.INSTANCE.features.teslaConnectionMode ==
                PhoenixConfigs.FeatureConfigs.TeslaConnectionMode.DATA_STICK) {
            return;
        }

        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID == null) return;

        ServerPlayer sp = sl.getServer()
                .getPlayerList()
                .getPlayer(ownerUUID);

        if (sp != null) {
            UUID team = TeamUtils.getTeamIdOrPlayerFallback(sp);
            if (!team.equals(ownerTeamUUID)) {
                ownerTeamUUID = team;
                self().markDirty();
            }
        }
    }



    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack binder) {
        if (!binder.is(PhoenixItems.TESLA_BINDER.get())) return InteractionResult.PASS;
        if (!binder.hasTag() || !binder.getTag().hasUUID("TargetTeam")) return InteractionResult.FAIL;

        UUID targetTeam = binder.getTag().getUUID("TargetTeam");

        if (!getLevel().isClientSide) {
            if (!targetTeam.equals(ownerTeamUUID)) {
                ownerTeamUUID = targetTeam;
                self().markDirty();
                player.sendSystemMessage(
                        Component.literal("Tesla Hatch linked.")
                                .withStyle(ChatFormatting.GREEN));
            } else {
                player.sendSystemMessage(
                        Component.literal("Already linked.")
                                .withStyle(ChatFormatting.GRAY));
            }
        }
        return InteractionResult.sidedSuccess(getLevel().isClientSide);
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack binder) {
        if (!binder.is(PhoenixItems.TESLA_BINDER.get())) return InteractionResult.PASS;

        if (!getLevel().isClientSide && ownerTeamUUID != null) {
            binder.getOrCreateTag().putUUID("TargetTeam", ownerTeamUUID);
            player.sendSystemMessage(
                    Component.literal("Tesla Frequency Copied.")
                            .withStyle(ChatFormatting.GREEN));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(getLevel().isClientSide);
    }



    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (ownerTeamUUID != null) tag.putUUID("OwnerTeamUUID", ownerTeamUUID);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (tag.hasUUID("OwnerTeamUUID"))
            ownerTeamUUID = tag.getUUID("OwnerTeamUUID");
    }
}
