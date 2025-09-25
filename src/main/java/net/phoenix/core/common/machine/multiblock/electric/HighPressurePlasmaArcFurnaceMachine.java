package net.phoenix.core.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.phoenix.core.common.machine.multiblock.part.fluid.PlasmaHatchPartMachine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * High-Pressure Plasma Arc Furnace:
 * - Runs normally without plasma.
 * - If supported plasma is present in plasma input hatch, consumes it as a catalyst
 * and boosts recipe duration/EUt accordingly.
 */
public class HighPressurePlasmaArcFurnaceMachine extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HighPressurePlasmaArcFurnaceMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    private boolean hasPlasmaInHatch(net.minecraft.world.level.material.Fluid fluid, int requiredAmount) {
        return getParts().stream()
                .filter(PlasmaHatchPartMachine.class::isInstance)
                .map(PlasmaHatchPartMachine.class::cast)
                .flatMap(hatch -> hatch.getRecipeHandlers().stream())
                .flatMap(handler -> handler.getCapability(FluidRecipeCapability.CAP).stream())
                .map(cap -> (NotifiableFluidTank) cap) // <-- cast here
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

    /**
     * PlasmaBoost config:
     * - name for display
     * - duration multiplier
     * - EUt multiplier
     * - consumeAmount = how much plasma to consume per interval (mB)
     * - ticksPerConsumption = how many ticks between each consumption
     */
    private record PlasmaBoost(String name, double durationMultiplier, double eutMultiplier, int consumeAmount,
                               int ticksPerConsumption) {}

    // --- Plasma registry with per-plasma configs ---
    private static final Map<net.minecraft.world.level.material.Fluid, PlasmaBoost> PLASMA_BOOSTS = new HashMap<>();
    static {

        PLASMA_BOOSTS.put(GTMaterials.Helium.getFluid(FluidStorageKeys.PLASMA),
                new PlasmaBoost("Helium Plasma", 0.9, 0.8, 1, 40));

        PLASMA_BOOSTS.put(GTMaterials.Iron.getFluid(FluidStorageKeys.PLASMA),
                new PlasmaBoost("Iron Plasma", 0.7, 0.85, 200, 20));

        PLASMA_BOOSTS.put(GTMaterials.Nickel.getFluid(FluidStorageKeys.PLASMA),
                new PlasmaBoost("Nickel Plasma", 0.6, 0.9, 50, 10));
    }

    @DescSynced
    private boolean isPlasmaBoosted = false;

    @Nullable
    private PlasmaBoost activeBoost = null;

    private int consumptionTimer = 0;

    public HighPressurePlasmaArcFurnaceMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    // ------------------------------------------//
    // Recipe Logic //
    // ------------------------------------------//

    private GTRecipe getPlasmaRecipe(PlasmaBoost boost, net.minecraft.world.level.material.Fluid fluid) {
        return GTRecipeBuilder.ofRaw().inputFluids(new FluidStack(fluid, boost.consumeAmount())).buildRawRecipe();
    }

    @Override
    public boolean onWorking() {
        // If already boosted, making sure hatch still contains valid plasma
        if (this.isPlasmaBoosted && this.activeBoost != null) {
            net.minecraft.world.level.material.Fluid currentFluid = null;
            for (var entry : PLASMA_BOOSTS.entrySet()) {
                if (entry.getValue().equals(this.activeBoost)) {
                    currentFluid = entry.getKey();
                    break;
                }
            }

            if (currentFluid == null || !hasPlasmaInHatch(currentFluid, this.activeBoost.consumeAmount())) {
                return false; // stop if boost plasma missing
            }
        }

        // Every "ticksPerConsumption", try to consume plasma from plasma hatches
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

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
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

    @NotNull
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
