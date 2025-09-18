package net.phoenix.core.common.data.recipe.builder;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.core.common.data.recipeConditions.PlasmaTempCondition;

import lombok.experimental.Accessors;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A recipe builder for Phoenix-specific recipes that supports plasma-based conditions.
 */
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Accessors(chain = true, fluent = true)
public class PhoenixRecipeBuilder extends GTRecipeBuilder {

    public PhoenixRecipeBuilder(ResourceLocation id, GTRecipeType recipeType) {
        super(id, recipeType);
    }

    /**
     * Adds a PlasmaTempCondition based on the given plasma material.
     * * @param plasmaFluidId The resource location of the plasma fluid.
     * 
     * @return This builder instance for chaining.
     */
    public PhoenixRecipeBuilder plasmaTemp(ResourceLocation plasmaFluidId) {
        return (PhoenixRecipeBuilder) this.addCondition(
                PlasmaTempCondition.of(plasmaFluidId.toString()));
    }
}
