package net.phoenix.core.common.machine.multiblock.part.special;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
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
import net.phoenix.core.phoenixcore;
import net.phoenix.core.utils.TeamUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class TeslaEnergyHatchPartMachine extends EnergyHatchPartMachine implements IDataStickInteractable {

    private static final boolean TESLA_DEBUG = false;

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

    @Override
    public void onLoad() {
        super.onLoad();
        if (isWireless()) {
            TeslaWirelessRegistry.registerHatch(this);
            updateTickSubscription();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        TeslaWirelessRegistry.unregisterHatch(this);
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

    /**
     * Ticks wireless energy transfer.
     * Works for hatches outside the multiblock if they have a bound team.
     */
    public void tickWireless() {
        if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] tickWireless called at {}", getPos());

        if (getLevel() == null) {
            if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] Level is null");
            return;
        }
        if (getLevel().isClientSide) {
            if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] Client side, skipping");
            return;
        }
        if (!isWireless()) {
            if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] Not wireless. ownerTeamUUID={}, mode={}",
                    ownerTeamUUID, PhoenixConfigs.INSTANCE.features.teslaConnectionMode);
            return;
        }
        if (ownerTeamUUID == null) {
            // if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] ownerTeamUUID is null");
            return;
        }

        if (TESLA_DEBUG)
            phoenixcore.LOGGER.info("[TESLA DEBUG] Passed initial checks. ownerTeamUUID={}", ownerTeamUUID);

        // Try to (re)bind to the team's tower if cache reference was lost (e.g. world/chunk reload).
        if (boundTower == null && ownerTeamUUID != null) {
            boundTower = TeslaTowerMachine.getTowerByTeam(ownerTeamUUID);
            // if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] Looked up tower by team, found: {}", boundTower
            // != null);
            if (boundTower != null) bindToTower(boundTower);
        }

        // Still unbound (tower not formed/registered yet) → try again next tick
        if (boundTower == null) {
            // if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] No tower bound yet; skipping this tick");
            return;
        }

        if (boundTower.getEnergyBank() == null) {
            // if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] Energy bank is null");
            return;
        }

        TeslaTowerMachine.TeslaEnergyBank bank = boundTower.getEnergyBank();

        long stored = energyContainer.getEnergyStored();
        long capacity = energyContainer.getEnergyCapacity();

        // Use tier-based voltage (GTValues.VEX[tier])
        long voltage = com.gregtechceu.gtceu.api.GTValues.V[getTier()];
        long amperage = 2;
        long transferRate = voltage * amperage;

        // if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] IO={}, stored={}, capacity={}, transferRate={},
        // bankStored={}",
        // getIO(), stored, capacity, transferRate, bank.getStored());

        if (getIO() == IO.IN) {
            // Output hatch: pull energy from tower → fills hatch → supplies to multiblock
            long space = capacity - stored;
            long toPull = Math.min(transferRate, space);
            long pulled = bank.drain(toPull);
            if (pulled > 0) {
                energyContainer.changeEnergy(pulled);
            }
            // if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] OUTPUT: Tried to pull {}, actually pulled {}, new
            // stored={}",
            // toPull, pulled, energyContainer.getEnergyStored());

        } else if (getIO() == IO.OUT) {
            // Input hatch: push energy to tower (from generator)
            // Use setEnergyStored() instead of changeEnergy()
            long toPush = Math.min(transferRate, stored);
            long accepted = bank.fill(toPush);
            if (accepted > 0) {
                energyContainer.changeEnergy(-accepted);
            }
            // if (TESLA_DEBUG) phoenixcore.LOGGER.info("[TESLA DEBUG] INPUT: Tried to push {}, actually pushed {}",
            // toPush, accepted);
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

        if (binder.hasTag()) {
            assert binder.getTag() != null;
            if (binder.getTag().hasUUID("TargetTeam")) {
                UUID binderUUID = binder.getTag().getUUID("TargetTeam");

                if (!Objects.requireNonNull(getLevel()).isClientSide) {
                    if (!binderUUID.equals(ownerTeamUUID)) {
                        ownerTeamUUID = binderUUID;
                        boundTower = TeslaTowerMachine.getTowerByTeam(ownerTeamUUID);
                        self().markDirty();

                        // Update registry and tick subscription
                        TeslaWirelessRegistry.unregisterHatch(this);
                        TeslaWirelessRegistry.registerHatch(this);
                        updateTickSubscription();

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
        }

        return InteractionResult.SUCCESS;
    }
}
