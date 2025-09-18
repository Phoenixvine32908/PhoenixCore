package net.phoenix.core.common.data.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PhoenixRecipeModifiers {

    public static final RecipeModifier HEAT_DRAWN = PhoenixRecipeModifiers::heatDrawnSpeedBoost;
    private static final int plasmaToConsume = 100;

    private static boolean isFluidHeatedPlasma(FluidStack fluid) {
        return fluid.getFluid().equals(GTMaterials.Argon.getFluid(FluidStorageKeys.PLASMA));
    }

    public static @NotNull ModifierFunction heatDrawnSpeedBoost(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof WorkableTieredMachine workableMachine)) return ModifierFunction.IDENTITY;

        List<Fluid> recipeFluids = new ArrayList();

        for (var content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            if (content.content instanceof FluidIngredient fluidIngredient) {
                for (var stack : fluidIngredient.getStacks()) {
                    recipeFluids.add(stack.getFluid());
                }
            }
        }

        // Get hatches inside machine, tanks inside UI of singleblocks, etc
        var fluidHandlers = workableMachine.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
        for (var fluidHandler : fluidHandlers) {

            if (!(fluidHandler instanceof NotifiableFluidTank fluidTank)) continue;

            for (int i = 0; i < fluidTank.getTanks(); i++) {
                var fluidStack = fluidTank.getFluidInTank(i);
                if (recipeFluids.contains(fluidStack.getFluid())) continue;
                if (isFluidHeatedPlasma(fluidStack)) {
                    int drained = fluidTank.drainInternal(plasmaToConsume, IFluidHandler.FluidAction.SIMULATE)
                            .getAmount();
                    if (drained == plasmaToConsume) {
                        fluidTank.drainInternal(plasmaToConsume, IFluidHandler.FluidAction.EXECUTE);
                        return ModifierFunction.builder()
                                .durationMultiplier(0.8)
                                .eutMultiplier(0.8)
                                .build();
                    }
                }
            }
        }
        return ModifierFunction.IDENTITY;
    }
}
