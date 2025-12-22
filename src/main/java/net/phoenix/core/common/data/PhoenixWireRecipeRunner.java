package net.phoenix.core.common.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.data.recipes.FinishedRecipe;
import net.phoenix.core.common.data.recipe.generated.PhoenixWireRecipeHandler;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class PhoenixWireRecipeRunner {

    public static void init(@NotNull Consumer<FinishedRecipe> provider) {
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            PhoenixWireRecipeHandler.run(provider, material);
        }
    }
}
