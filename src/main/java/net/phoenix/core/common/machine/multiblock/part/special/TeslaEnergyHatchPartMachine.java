package net.phoenix.core.common.machine.multiblock.part.special;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
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
import net.phoenix.core.phoenixcore;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;
import net.phoenix.core.utils.TeamUtils;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.UUID;

public class TeslaEnergyHatchPartMachine extends EnergyHatchPartMachine implements IDataStickInteractable {

    public static final boolean TESLA_DEBUG = false;

    // ---------------------------------------
    // Managed fields
    // ---------------------------------------
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            TeslaEnergyHatchPartMachine.class, EnergyHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private UUID ownerTeamUUID;

    // Cached reference to tower (not persisted)
    private TeslaTowerMachine boundTower;

    // Tick subscription for wireless hatches outside multiblock
    private TickableSubscription tickSubscription;

    // Inside TeslaEnergyHatchPartMachine.java
    @Getter
    @Persisted // This ensures it saves to the block's NBT
    @DescSynced // This ensures Jade/Client knows the name without opening the UI
    private String customName = "";

    public void setCustomName(String name) {
        this.customName = name;
        self().markDirty();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!getLevel().isClientSide && getLevel() instanceof ServerLevel server) {
            autoLinkTeamIfNeeded();
            if (ownerTeamUUID != null) {
                // Pass the physical IO state on load
                TeslaTeamEnergyData.get(server).setEnergyBuffered(ownerTeamUUID, getPos(),
                        BigInteger.valueOf(energyContainer.getEnergyStored()), getIO() == IO.OUT);
            }
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        // Remove this specific coordinate from the cloud's tracking list
        if (!getLevel().isClientSide && getLevel() instanceof ServerLevel server && ownerTeamUUID != null) {
            TeslaTeamEnergyData.get(server).removeEndpoint(ownerTeamUUID, getPos());
        }

        // Cleanup the tick subscription
        unsubscribeFromTick();
    }

    // ---------------------------------------
    // Constructor & field holder
    // ---------------------------------------
    public TeslaEnergyHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, int amperage, Object... args) {
        super(holder, tier, io, amperage, args);
    }

    @Override
    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] addedToController: {} at {}, isTeslaTower={}",
                controller.getClass().getSimpleName(), getPos(), controller instanceof TeslaTowerMachine);

        // Only disable self-ticking if added to Tesla Tower
        if (controller instanceof TeslaTowerMachine) {
            if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] Unsubscribing from tick (Tesla Tower)");
            unsubscribeFromTick();
        } else {
            if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] Updating tick subscription (Other multiblock)");
            // For other multiblocks
            updateTickSubscription();
        }
    }

    @Override
    public void removedFromController(@NotNull IMultiController controller) {
        super.removedFromController(controller);
        // Outside multiblock, self-tick
        updateTickSubscription();
    }

    /**
     * Updates tick subscription based on wireless status and multiblock membership.
     * Subscribes if wireless AND (not in multiblock OR in non-Tesla multiblock).
     */
    private void updateTickSubscription() {
        boolean shouldTick = false;

        if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] updateTickSubscription called at {}", getPos());
        if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] isWireless={}, controllers={}",
                isWireless(), getControllers().size());

        if (isWireless()) {
            if (getControllers().isEmpty()) {
                // Not in any multiblock - should tick
                shouldTick = true;
                if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] Not in multiblock, should tick");
            } else {
                // In a multiblock - only tick if NOT Tesla Tower
                shouldTick = getControllers().stream()
                        .noneMatch(ctrl -> ctrl instanceof TeslaTowerMachine);
                if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] In multiblock, shouldTick={}", shouldTick);
            }
        }

        if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] shouldTick={}, currentSubscription={}",
                shouldTick, tickSubscription != null);

        if (shouldTick) {
            if (tickSubscription == null) {
                tickSubscription = subscribeServerTick(this::tickWireless);
                if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] Subscribed to tick!");
            }
        } else {
            if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] Unsubscribing from tick");
            unsubscribeFromTick();
        }
    }

    // Unsubscribes from tick to avoid duplicate ticking.

    private void unsubscribeFromTick() {
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
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

    @Getter
    private long lastTransferRate = 0;
    // Inside TeslaEnergyHatchPartMachine
    @Getter
    private long lastTransferAmount = 0; // Field for Jade to read

    public void tickWireless() {
        if (getLevel() == null || getLevel().isClientSide || ownerTeamUUID == null) return;
        if (!isWireless()) return;

        ServerLevel sl = (ServerLevel) getLevel();
        TeslaTeamEnergyData data = TeslaTeamEnergyData.get(sl);
        TeslaTeamEnergyData.TeamEnergy teamData = data.getOrCreate(ownerTeamUUID);
        if (!data.isOnline(ownerTeamUUID)) return;

        // FIX: Use the actual amperage rating of the hatch (e.g., 4, 8, 16)
        long voltage = com.gregtechceu.gtceu.api.GTValues.V[getTier()];
        long transferRate = voltage * getAmperage(); // Dynamic transfer!

        BigInteger moved = BigInteger.ZERO;

        if (getIO() == IO.IN) {
            long space = energyContainer.getEnergyCapacity() - energyContainer.getEnergyStored();
            // Pull from cloud into Hatch
            BigInteger toPull = BigInteger.valueOf(Math.min(transferRate, space));
            moved = teamData.drain(toPull);

            teamData.energyInput.put(getPos(), moved);
            teamData.energyOutput.remove(getPos());

            if (moved.signum() > 0) energyContainer.changeEnergy(moved.longValue());
        } else {
            long stored = energyContainer.getEnergyStored();
            // Push from Hatch into cloud
            BigInteger toPush = BigInteger.valueOf(Math.min(transferRate, stored));
            moved = teamData.fill(toPush);

            teamData.energyOutput.put(getPos(), moved);
            teamData.energyInput.remove(getPos());

            if (moved.signum() > 0) energyContainer.changeEnergy(-moved.longValue());
        }

        // Update the cloud buffer tracking
        data.setEnergyBuffered(ownerTeamUUID, getPos(),
                BigInteger.valueOf(energyContainer.getEnergyStored()), getIO() == IO.OUT);

        teamData.markHatchActive(getPos(), sl.getGameTime());
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
            // Update registry and tick subscription when team changes
            TeslaWirelessRegistry.unregisterHatch(this);
            TeslaWirelessRegistry.registerHatch(this);
            updateTickSubscription();
        }
    }

    // ---------------------------------------
    // Data stick binding
    // ---------------------------------------
    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack binder) {
        if (!binder.is(PhoenixItems.TESLA_BINDER.get())) return InteractionResult.PASS;

        var tag = binder.getTag();
        if (tag != null && tag.hasUUID("TargetTeam")) {
            UUID newTeamUUID = tag.getUUID("TargetTeam");

            if (!getLevel().isClientSide && getLevel() instanceof ServerLevel server) {
                // Only update if the frequency is actually different
                if (!newTeamUUID.equals(ownerTeamUUID)) {

                    // 1. CLEANUP OLD DATA: Remove this hatch from the previous team's stats
                    if (ownerTeamUUID != null) {
                        TeslaTeamEnergyData.get(server).removeEndpoint(ownerTeamUUID, getPos());
                    }

                    // 2. LOGIC UPDATE: Switch the owner UUID
                    this.ownerTeamUUID = newTeamUUID;
                    this.boundTower = null; // We no longer use direct tower references in Cloud Mode
                    self().markDirty();

                    // 3. REGISTRY & TICK: Ensure the wireless system knows the team changed
                    TeslaWirelessRegistry.unregisterHatch(this);
                    TeslaWirelessRegistry.registerHatch(this);
                    updateTickSubscription();

                    // 4. REGISTER NEW DATA: Add this hatch to the new team's stats immediately
                    // Inside the if (!newTeamUUID.equals(ownerTeamUUID)) block
                    TeslaTeamEnergyData.get(server).setEnergyBuffered(
                            ownerTeamUUID,
                            getPos(),
                            java.math.BigInteger.valueOf(energyContainer.getEnergyStored()),
                            getIO() == IO.OUT // Pass the physical state
                    );

                    player.sendSystemMessage(Component
                            .literal("Tesla Hatch: Connected to frequency " + ownerTeamUUID.toString().substring(0, 8) +
                                    "...")
                            .withStyle(ChatFormatting.AQUA));
                } else {
                    player.sendSystemMessage(Component.literal("Tesla Hatch: Already synced to this frequency.")
                            .withStyle(ChatFormatting.GRAY));
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.sidedSuccess(getLevel().isClientSide);
        }
        return InteractionResult.PASS;
    }
}
