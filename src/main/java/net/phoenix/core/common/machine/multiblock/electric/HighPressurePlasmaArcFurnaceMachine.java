package net.phoenix.core.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.phoenix.core.common.machine.multiblock.Shield;
import net.phoenix.core.common.machine.multiblock.part.fluid.PlasmaHatchPartMachine;
import net.phoenix.core.common.machine.trait.NotifiableShieldContainer; // NEW IMPORT


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * High-Pressure Plasma Arc Furnace with Shield System
 */
@SuppressWarnings("all")
public class HighPressurePlasmaArcFurnaceMachine extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HighPressurePlasmaArcFurnaceMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    // Decay occurs once every this many ticks (e.g., 20 ticks = 1 second)
    private static final int DECAY_TICK_RATE = 20;

    // -------------------- SHIELD FIELDS (NEW) --------------------
    @Persisted @DescSynced
    private Shield.ShieldTypes shieldType = Shield.ShieldTypes.INACTIVE;

    @Persisted @DescSynced
    private int shieldHealth = 0;

    @Persisted @DescSynced
    private int shieldCooldownTimer = 0;

    @Persisted @DescSynced
    private int shieldDecayTimer = DECAY_TICK_RATE; // NEW: Timer for decay

    private final ConditionalSubscriptionHandler shieldHandler;

    @Persisted
    private final NotifiableShieldContainer shieldContainer;

    // -------------------- Plasma Boost Fields (Original) --------------------
    @DescSynced
    public boolean isPlasmaBoosted = false;

    @Nullable
    public PlasmaBoost activeBoost = null;

    private int consumptionTimer = 0;

    // -------------------- Constructor & Lifecycle Hooks --------------------

    public HighPressurePlasmaArcFurnaceMachine(IMachineBlockEntity holder) {
        super(holder);
        // INITIALIZE SHIELD LOGIC
        this.shieldHandler = new ConditionalSubscriptionHandler(this, this::shieldTick, this::isFormed);
        this.shieldContainer = new NotifiableShieldContainer(this);
        this.getTraits().add(this.shieldContainer);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public Shield.ShieldTypes getShieldType() {
        return this.shieldType;
    }

    // Method called by recipes to activate the shield
    public void updateShield(int pKey, boolean setCooldown) {
        this.shieldType = Shield.ShieldTypes.getShieldFromKey(pKey);
        this.shieldHealth = this.shieldType.shieldHealth;
        if (setCooldown) {
            this.shieldCooldownTimer = this.shieldType.shieldCooldownTicks;
        }
        this.markDirty();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        shieldHandler.updateSubscription(); // Start the shield ticker
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        shieldHandler.updateSubscription(); // Stop the shield ticker
    }

    // -------------------- SHIELD DECAY LOGIC --------------------
    public void shieldTick() {
        // 1. Decay logic (only for NORMAL state)
        if (this.shieldType == Shield.ShieldTypes.NORMAL) {

            // Decrement the dedicated decay timer
            this.shieldDecayTimer--;

            if (this.shieldDecayTimer <= 0) {
                // Timer expired: Perform decay and reset timer
                this.shieldHealth -= this.shieldType.decayRate; // Decay only happens here
                this.shieldDecayTimer = DECAY_TICK_RATE; // Reset the timer

                if (this.shieldHealth <= 0) {
                    // Decay complete: switch to DECAYED state and set cooldown
                    this.shieldType = Shield.ShieldTypes.DECAYED;
                    this.shieldHealth = 0;
                    this.shieldCooldownTimer = this.shieldType.shieldCooldownTicks;
                    this.markDirty();
                }
                // Mark dirty when decay happens
                this.markDirty();
            }
        } else {
            // If not in NORMAL state, reset the decay timer
            this.shieldDecayTimer = DECAY_TICK_RATE;
        }

        // 2. Cooldown logic (for activation recipe)
        if (this.shieldCooldownTimer > 0) {
            this.shieldCooldownTimer--;
            this.markDirty(); // Mark dirty for cooldown updates
        }
    }

    // -------------------- RECIPE LOGIC (Shield Integration) --------------------
    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (recipe == null) return false;

        // 1. Check for activation recipe marker
        boolean isActivationRecipe = recipe.data.contains("shield_activation");

        if (this.shieldType != Shield.ShieldTypes.NORMAL) {
            // A. Block normal recipes if shield is not active
            if (!isActivationRecipe) {
                return false;
            }

            // B. Block activation recipe if cooldown is active (DECAYED state cooldown)
            if (this.shieldCooldownTimer > 0) {
                return false;
            }
        } else {
            // C. If shield is active, block the activation recipe
            if (isActivationRecipe) {
                return false;
            }
        }

        return super.beforeWorking(recipe);
    }

    @Override
    public void afterWorking() {
        super.afterWorking();

        // Apply shield state changes after activation recipe finishes
        var activeRecipe = recipeLogic.getLastRecipe();
        if (activeRecipe != null && activeRecipe.data.contains("shield_activation")) {
            // Read "updated_shield_key" (e.g., 1 for NORMAL) from recipe data and set cooldown
            updateShield(activeRecipe.data.getInt("updated_shield_key"), true);
        }
    }


    @Override
    public boolean onWorking() {
        // Existing Plasma boost check & consumption loop
        if (this.consumptionTimer % (activeBoost == null ? 1 : activeBoost.ticksPerConsumption()) == 0) {
            isPlasmaBoosted = false;
            activeBoost = null;

            for (var entry : PLASMA_BOOSTS.entrySet()) {
                var fluid = entry.getKey();
                var boost = entry.getValue();

                if (tryConsumePlasmaFromHatch(fluid, boost.consumeAmount())) {
                    isPlasmaBoosted = true;
                    activeBoost = boost;
                    break;
                }
            }
        }

        boolean value = super.onWorking();

        this.consumptionTimer++;
        if (this.consumptionTimer > 72000) this.consumptionTimer = 0;

        return value;
    }

    @Override
    public boolean isWorkingEnabled() {
        // Plasma boost check (rest of original logic)
        if (this.isPlasmaBoosted && this.activeBoost != null) {
            net.minecraft.world.level.material.Fluid currentFluid = null;
            for (var entry : PLASMA_BOOSTS.entrySet()) {
                if (entry.getValue().equals(this.activeBoost)) {
                    currentFluid = entry.getKey();
                    break;
                }
            }
            if (currentFluid == null || !hasPlasmaInHatch(currentFluid, this.activeBoost.consumeAmount())) {
                return false;
            }
        }
        return super.isWorkingEnabled();
    }

    // -------------------- Plasma Fields & UI --------------------
    // The rest of your helper methods (checkItems, checkFluids, consumeFluids, consumeItems)
    // are kept as they are needed for potential activation recipes.
    // ... (Your helper methods)

    private boolean hasPlasmaInHatch(net.minecraft.world.level.material.Fluid fluid, int requiredAmount) {
        return getParts().stream()
                .filter(PlasmaHatchPartMachine.class::isInstance)
                .map(PlasmaHatchPartMachine.class::cast)
                .flatMap(hatch -> hatch.getRecipeHandlers().stream())
                .flatMap(handler -> handler.getCapability(FluidRecipeCapability.CAP).stream())
                .map(cap -> (NotifiableFluidTank) cap)
                .anyMatch(
                        tank -> !tank.isEmpty() && ((FluidStack) tank.getContents().get(0)).getFluid().isSame(fluid) &&
                                ((FluidStack) tank.getContents().get(0)).getAmount() >= requiredAmount);
    }

    private boolean tryConsumePlasmaFromHatch(net.minecraft.world.level.material.Fluid fluid, int consumeAmount) {
        for (var hatch : getParts().stream()
                .filter(PlasmaHatchPartMachine.class::isInstance)
                .map(PlasmaHatchPartMachine.class::cast)
                .toList()) {

            var tank = (NotifiableFluidTank) hatch.getRecipeHandlers().get(0)
                    .getCapability(FluidRecipeCapability.CAP).get(0);

            if (!tank.isEmpty() && ((FluidStack) tank.getContents().get(0)).getFluid().isSame(fluid) &&
                    ((FluidStack) tank.getContents().get(0)).getAmount() >= consumeAmount) {

                tank.drain(consumeAmount, IFluidHandler.FluidAction.EXECUTE);
                return true;
            }
        }
        return false;
    }

    // Removed @Getter as it conflicts with record accessors
    public record PlasmaBoost(String name, double durationMultiplier, double eutMultiplier, int consumeAmount,
                              int ticksPerConsumption) {}

    private static final Map<net.minecraft.world.level.material.Fluid, PlasmaBoost> PLASMA_BOOSTS = new HashMap<>();

    static {
        PLASMA_BOOSTS.put(GTMaterials.Helium.getFluid(FluidStorageKeys.PLASMA),
                new PlasmaBoost("Helium Plasma", 0.9, 0.8, 1, 40));

        PLASMA_BOOSTS.put(GTMaterials.Iron.getFluid(FluidStorageKeys.PLASMA),
                new PlasmaBoost("Iron Plasma", 0.7, 0.85, 200, 20));

        PLASMA_BOOSTS.put(GTMaterials.Nickel.getFluid(FluidStorageKeys.PLASMA),
                new PlasmaBoost("Nickel Plasma", 0.6, 0.9, 50, 10));
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);

        if (isFormed()) {
            // Display Shield Status (always shows the type: NORMAL, DECAYED, INACTIVE)
            textList.add(Component.translatable("shield.phoenixcore.current_shield",
                    Component.translatable(this.shieldType.langKey)));

            // --- NEW LOGIC FOR HEALTH AND COOLDOWN ---

            // 1. Display Health (ONLY if shield is NORMAL)
            if (this.shieldType == Shield.ShieldTypes.NORMAL) {
                textList.add(Component.translatable("shield.phoenixcore.health", this.shieldHealth));
            }

            // 2. Display Cooldown (ONLY if shield is DECAYED OR INACTIVE with a non-zero timer)
            // Since cooldown only applies after DECAYED, we check for that state AND a timer > 0.
            // The check for this.shieldCooldownTimer > 0 naturally stops the display once the timer is zero.
            if (this.shieldType == Shield.ShieldTypes.DECAYED && this.shieldCooldownTimer > 0) {
                int seconds = this.shieldCooldownTimer / 20;
                textList.add(Component.translatable("shield.phoenixcore.cooldown", seconds));
            }

            // Note: If you want to show the cooldown for INACTIVE too, you'd add:
            // else if (this.shieldType == Shield.ShieldTypes.INACTIVE && this.shieldCooldownTimer > 0) { ... }
            // but typically the cooldown only matters after DECAYED.

            // ------------------------------------------
        }

        // ... (rest of the method, plasma boost info)

        // ... (Plasma boost info, potentially blocked if shield is inactive)
        textList.add(Component.literal("--------------------"));

        // Only display plasma boost status if the shield is active (NORMAL state)
        if (this.shieldType == Shield.ShieldTypes.NORMAL) {
            if (isPlasmaBoosted && activeBoost != null) {
                // ... (rest of plasma boost display)
            } else {
                textList.add(Component.literal("§7No Plasma Catalyst§r"));
            }
        }
    }
    public int getShieldHealth() {
        return this.shieldHealth;
    }

    public int getShieldCooldownTimer() {
        return this.shieldCooldownTimer;
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof HighPressurePlasmaArcFurnaceMachine furnace)) {
            return RecipeModifier.nullWrongType(HighPressurePlasmaArcFurnaceMachine.class, machine);
        }
        if (furnace.isPlasmaBoosted && furnace.activeBoost != null && furnace.shieldType == Shield.ShieldTypes.NORMAL) {
            PlasmaBoost boost = furnace.activeBoost;
            return ModifierFunction.builder()
                    .durationMultiplier(boost.durationMultiplier())
                    .eutMultiplier(boost.eutMultiplier())
                    .build();
        }
        return ModifierFunction.IDENTITY;
    }
}