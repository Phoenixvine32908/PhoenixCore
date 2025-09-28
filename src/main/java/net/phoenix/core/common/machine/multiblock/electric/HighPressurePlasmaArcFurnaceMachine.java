package net.phoenix.core.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler; // <-- New Import
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Getter;
import lombok.Setter; // <-- New Import for Setters
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.phoenix.core.common.machine.multiblock.part.fluid.PlasmaHatchPartMachine;

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

    // -------------------- Shield Fields --------------------

    // Handler to run shieldTick() every tick the machine is formed (like MicroverseProjectorMachine)
    private final ConditionalSubscriptionHandler shieldTickHandler;

    @DescSynced
    @Getter
    @Setter // Added Setter for cleaner state management
    private boolean shieldActive = false;

    @DescSynced
    @Getter
    @Setter // Added Setter for cleaner state management
    private int shieldHealth = 0;

    @DescSynced
    @Getter
    @Setter // Added Setter for cleaner state management
    private int shieldCooldown = 0;

    @Persisted // Timer for shieldTick() update frequency
    private int shieldTimer = 0;

    private static final int SHIELD_MAX_HEALTH = 200;
    private static final int SHIELD_DECAY = 1; // per working tick (handled in onWorking)
    private static final int SHIELD_COOLDOWN_TICKS = 2400; // 2 minutes @ 20 TPS

    // Shield Activation Inputs
    private static final ItemStack ACTIVATE_ITEM_1 = new ItemStack(Items.GOLD_INGOT, 1);
    private static final ItemStack ACTIVATE_ITEM_2 = new ItemStack(Items.IRON_INGOT, 1);
    private static final FluidStack ACTIVATE_FLUID_1 = new FluidStack(GTMaterials.SodiumPotassium.getFluid(), 1000);
    private static final FluidStack ACTIVATE_FLUID_2 = new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000);

    // -------------------- Plasma Boost Fields (Original) --------------------
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

    @DescSynced
    private boolean isPlasmaBoosted = false;

    @Nullable
    private PlasmaBoost activeBoost = null;

    private int consumptionTimer = 0;

    // -------------------- Constructor & Lifecycle Hooks --------------------

    public HighPressurePlasmaArcFurnaceMachine(IMachineBlockEntity holder) {
        super(holder);
        // Initialize the dedicated shield ticker
        this.shieldTickHandler = new ConditionalSubscriptionHandler(this, this::shieldTick, this::isFormed);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // Start the shield ticker when structure is formed
        shieldTickHandler.updateSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        // Stop the shield ticker when structure breaks
        shieldTickHandler.updateSubscription();
    }


    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        // A. SECONDARY HALT/INPUT GUARD:
        // If shield is NOT active, but the activation materials are present, block the recipe.
        // This is CRITICAL to prevent standard recipes from consuming shield inputs.
        if (!isShieldActive() && checkShieldMaterialsArePresent()) {
            return false;
        }

        // B. PRIMARY HALT: If the shield is down/broken, block the recipe.
        if (!isShieldActive()) {
            return false;
        }

        return super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        // Shield Decay: Apply decay during the working tick
        if (isShieldActive()) {
            shieldHealth = Math.max(0, shieldHealth - SHIELD_DECAY);
            if (shieldHealth == 0) {
                setShieldActive(false);
                setShieldCooldown(SHIELD_COOLDOWN_TICKS);
                // Fail the recipe when the shield breaks mid-process
                recipeLogic.resetRecipeLogic();
                return false;
            }
        }

        // Plasma boost check & consumption loop
        // This logic is separate from the shield and remains in onWorking
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
        // If the shield isn't active (or cooling down), we cannot perform any work.
        if (!isShieldActive() || getShieldCooldown() > 0) {
            return false;
        }

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

    // -------------------- Dedicated Shield Tick Logic --------------------

    /**
     * Dedicated tick method called by shieldTickHandler every game tick when formed.
     */
    public void shieldTick() {
        // 1. Handle Cooldown
        if (shieldCooldown > 0) {
            shieldCooldown = Math.max(0, shieldCooldown - 1);
            return;
        }

        if (isShieldActive()) {
            return;
        }


        if (shieldTimer++ % 20 != 0) {
            return;
        }

        tryActivateShield();
    }

    /**
     * Attempts to consume materials from input hatches and sets the shield to active.
     */
    public boolean tryActivateShield() {
        // 1. Check if all resources are present (simulate consumption)
        boolean hasItems = checkItems(ACTIVATE_ITEM_1, ACTIVATE_ITEM_2);
        boolean hasFluids = checkFluids(ACTIVATE_FLUID_1, ACTIVATE_FLUID_2);

        if (hasItems && hasFluids) {
            // 2. If present, actually consume them.
            boolean itemsConsumed = consumeItems(ACTIVATE_ITEM_1, ACTIVATE_ITEM_2);
            boolean fluidsConsumed = consumeFluids(ACTIVATE_FLUID_1, ACTIVATE_FLUID_2);

            if (itemsConsumed && fluidsConsumed) {
                setShieldActive(true);
                setShieldHealth(SHIELD_MAX_HEALTH);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if activation materials are sitting in the hatches (used to guard beforeWorking).
     */
    private boolean checkShieldMaterialsArePresent() {
        return checkItems(ACTIVATE_ITEM_1, ACTIVATE_ITEM_2) &&
                checkFluids(ACTIVATE_FLUID_1, ACTIVATE_FLUID_2);
    }

    // -------------------- Resource Helper Methods --------------------

    // Helper to check for items without consuming
    private boolean checkItems(ItemStack... requiredItems) {
        var itemHandlers = getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        for (ItemStack required : requiredItems) {
            boolean found = false;
            for (var handler : itemHandlers) {
                if (!(handler instanceof NotifiableItemStackHandler itemHandler)) continue;
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack stack = itemHandler.getStackInSlot(i);
                    if (ItemStack.isSameItemSameTags(stack, required) && stack.getCount() >= required.getCount()) {
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
            if (!found) return false;
        }
        return true;
    }

    // Helper to check for fluids without consuming
    private boolean checkFluids(FluidStack... requiredFluids) {
        var fluidHandlers = getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
        for (FluidStack required : requiredFluids) {
            boolean found = false;
            for (var handler : fluidHandlers) {
                if (!(handler instanceof NotifiableFluidTank fluidTank)) continue;

                // Use simulated drain to check availability
                FluidStack simulated = fluidTank.drainInternal(required.getAmount(), FluidAction.SIMULATE);
                if (simulated.getFluid().isSame(required.getFluid()) && simulated.getAmount() == required.getAmount()) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private boolean consumeFluids(FluidStack... requiredFluids) {
        var fluidHandlers = getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);

        for (FluidStack required : requiredFluids) {
            boolean found = false;
            for (var handler : fluidHandlers) {
                if (!(handler instanceof NotifiableFluidTank fluidTank)) continue;

                FluidStack simulated = fluidTank.drainInternal(required, FluidAction.SIMULATE);
                if (simulated.getAmount() == required.getAmount()) {
                    // Actually consume it
                    fluidTank.drainInternal(required, FluidAction.EXECUTE);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private boolean consumeItems(ItemStack... requiredItems) {
        var itemHandlers = getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);

        for (ItemStack required : requiredItems) {
            boolean found = false;
            for (var handler : itemHandlers) {
                if (!(handler instanceof NotifiableItemStackHandler itemHandler)) continue;

                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack stack = itemHandler.getStackInSlot(i);
                    if (ItemStack.isSameItemSameTags(stack, required) && stack.getCount() >= required.getCount()) {
                        // actually drain the items
                        itemHandler.extractItemInternal(i, required.getCount(), false);
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
            if (!found) return false;
        }
        return true;
    }

    // -------------------- Plasma Fields & UI  --------------------
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

    private record PlasmaBoost(String name, double durationMultiplier, double eutMultiplier, int consumeAmount,
                               int ticksPerConsumption) {
    }

    private static final Map<net.minecraft.world.level.material.Fluid, PlasmaBoost> PLASMA_BOOSTS = new HashMap<>();

    static {
        PLASMA_BOOSTS.put(GTMaterials.Helium.getFluid(FluidStorageKeys.PLASMA),
                new PlasmaBoost("Helium Plasma", 0.9, 0.8, 1, 40));

        PLASMA_BOOSTS.put(GTMaterials.Iron.getFluid(FluidStorageKeys.PLASMA),
                new PlasmaBoost("Iron Plasma", 0.7, 0.85, 200, 20));

        PLASMA_BOOSTS.put(GTMaterials.Nickel.getFluid(FluidStorageKeys.PLASMA),
                new PlasmaBoost("Nickel Plasma", 0.6, 0.9, 50, 10));
    }


// In HighPressurePlasmaArcFurnaceMachine.java

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);

        if (isFormed()) {
            // --- Shield info ---
            if (isShieldActive()) {
                textList.add(Component.literal("§bShield Field Active§r"));
                textList.add(Component.literal(" - Health: " + getShieldHealth()));
            } else {
                textList.add(Component.literal("§7Shield Offline§r"));
                if (getShieldCooldown() > 0) {
                    textList.add(Component.literal(" - Cooldown: " + (getShieldCooldown() / 20) + "s"));
                } else {
                    // Check if materials are present but shield is off
                    if (checkShieldMaterialsArePresent()) {
                        textList.add(Component.literal(" - Ready for Activation"));
                    } else {
                        textList.add(Component.literal(" - Requires startup recipe"));
                    }
                }
            }

            // -----------------------------------------------------
            // --- MODIFIED: Plasma boost info ONLY if shield is active ---
            // -----------------------------------------------------
            if (isShieldActive()) { // <-- NEW CHECK
                if (isPlasmaBoosted && activeBoost != null) {
                    textList.add(Component.literal("§b" + activeBoost.name + " Boost Active!§r"));
                    textList.add(Component.literal(" - " + (int) (activeBoost.durationMultiplier * 100) + "% duration"));
                    textList.add(Component.literal(" - " + (int) (activeBoost.eutMultiplier * 100) + "% EUt"));
                    textList.add(Component.literal(
                            " - " + activeBoost.consumeAmount + " mB every " + activeBoost.ticksPerConsumption + " ticks"));
                } else {
                    textList.add(Component.literal("§7No Plasma Catalyst§r"));
                }
            }
        }
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof HighPressurePlasmaArcFurnaceMachine furnace)) {
            return RecipeModifier.nullWrongType(HighPressurePlasmaArcFurnaceMachine.class, machine);
        }
        if (furnace.isPlasmaBoosted && furnace.activeBoost != null) {
            PlasmaBoost boost = furnace.activeBoost;
            return ModifierFunction.builder()
                    .durationMultiplier(boost.durationMultiplier)
                    .eutMultiplier(boost.eutMultiplier)
                    .build();
        }
        return ModifierFunction.IDENTITY;
    }
}