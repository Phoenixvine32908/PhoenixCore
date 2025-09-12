package net.phoenix.core.common.data.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import org.jetbrains.annotations.NotNull;

public class PhoenixRecipeModifiers {

    public static final RecipeModifier HEAT_DRAWN = PhoenixRecipeModifiers::heatDrawnSpeedBoost;

    public static @NotNull ModifierFunction heatDrawnSpeedBoost(MetaMachine machine, GTRecipe recipe) {
        for (var content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
            if (content.content instanceof FluidIngredient fluidIngredient) {
                for (var fluidStack : fluidIngredient.getStacks()) {
                    if (fluidStack.getFluid().getFluidType().getTemperature() > 6000 &&
                            fluidIngredient.equals(FluidStorageKeys.PLASMA)) {
                        return ModifierFunction.builder()
                                .durationMultiplier(0.8)
                                .eutMultiplier(0.8)
                                .build();
                    }
                }
            }

        }
        return ModifierFunction.builder()
                .durationMultiplier(1.0)
                .eutMultiplier(1.0)
                .build();
    }
}
