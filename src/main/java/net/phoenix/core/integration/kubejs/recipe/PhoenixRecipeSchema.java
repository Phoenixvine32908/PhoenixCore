package net.phoenix.core.integration.kubejs.recipe;

import com.gregtechceu.gtceu.common.recipe.condition.CleanroomCondition;
import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;

import net.phoenix.core.common.data.recipeConditions.PlasmaTempCondition;
import net.phoenix.core.common.machine.multiblock.BlazingCleanroom;

import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import lombok.experimental.Accessors;

import static com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema.*;

public interface PhoenixRecipeSchema {

    // Define a new RecipeComponent for your BlazingCleanroom

    @SuppressWarnings({ "unused", "UnusedReturnValue" })
    @Accessors(chain = true, fluent = true)
    class PhoenixRecipeJS extends GTRecipeSchema.GTRecipeJS {

        public GTRecipeJS cleanroom(BlazingCleanroom cleanroomType) {
            return addCondition(new CleanroomCondition(cleanroomType));
        }

        public GTRecipeSchema.GTRecipeJS plasmaTemp(int tier) {
            return this.addCondition(new PlasmaTempCondition());
        }
    }

    // Now, create a new schema that is an extension of the GTRecipeSchema
    RecipeSchema SCHEMA = new RecipeSchema(PhoenixRecipeJS.class, PhoenixRecipeJS::new,
            DURATION, DATA, CONDITIONS,
            ALL_INPUTS, ALL_TICK_INPUTS, ALL_OUTPUTS, ALL_TICK_OUTPUTS,
            INPUT_CHANCE_LOGICS, OUTPUT_CHANCE_LOGICS, TICK_INPUT_CHANCE_LOGICS, TICK_OUTPUT_CHANCE_LOGICS, CATEGORY)
            .constructor((recipe, schemaType, keys, from) -> recipe.id(from.getValue(recipe, ID)), ID)
            .constructor(DURATION, CONDITIONS, ALL_INPUTS, ALL_OUTPUTS, ALL_TICK_INPUTS, ALL_TICK_OUTPUTS);
}
