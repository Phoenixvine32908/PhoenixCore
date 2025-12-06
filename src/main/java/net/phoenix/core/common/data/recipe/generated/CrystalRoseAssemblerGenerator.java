package net.phoenix.core.common.data.recipe.generated;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.phoenix.core.common.data.bees.BeeRecipeData;
import net.phoenix.core.common.data.materials.PhoenixMaterialFlags;

import java.util.function.Consumer;

public class CrystalRoseAssemblerGenerator {

    /** Generate all crystal rose assembler recipes */
    public static void generateCrystalRoseRecipes(Consumer<FinishedRecipe> provider) {
        BeeRecipeData.ALL_BEE_CONFIGS.forEach((id, config) -> {

            ItemStack output = config.finalOutputItem();
            if (output == null || output.isEmpty()) return;

            // Try to link Item → GTCEu Material
            Material material = tryGetMaterialFromOutput(output);
            if (material == null || material.isNull()) return;

            // Build correct 4x input stack with prefix fallback
            ItemStack inputStack = getPreferredInputForMaterial(material);
            if (inputStack.isEmpty()) return;
            inputStack.setCount(4);

            // Crystal rose item lookup
            // Crystal rose item lookup
            ItemStack roseStack = ChemicalHelper.get(PhoenixMaterialFlags.crystal_rose, material, 1);
            if (roseStack.isEmpty()) return; // material does not support crystal rose

            // Sodium Potassium fluid (native GTCEu)
            FluidStack sodiumPotassium = GTMaterials.SodiumPotassium.getFluid(144);

            GTRecipeBuilder builder = GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(
                    "phoenixcore:crystal_rose_" + material.getName())
                    .EUt(GTValues.V[GTValues.IV])     // EV voltage
                    .duration(200)
                    .inputItems(inputStack)
                    .inputFluids(sodiumPotassium)
                    .outputItems(roseStack);

            builder.save(provider);
        });
    }

    /** Add Phoenix Flag to all materials producing a crystal rose */
    public static void linkCrystalRoseFlags() {
        BeeRecipeData.ALL_BEE_CONFIGS.forEach((id, config) -> {

            ItemStack output = config.finalOutputItem();
            if (output == null || output.isEmpty()) return;

            Material material = tryGetMaterialFromOutput(output);
            if (material == null || material.isNull()) return;

            material.addFlags(PhoenixMaterialFlags.GENERATE_CRYSTAL_ROSE);
        });
    }

    /** Convert finalOutputItem → GT Material via ChemicalHelper */
    private static Material tryGetMaterialFromOutput(ItemStack stack) {
        MaterialEntry entry = ChemicalHelper.getMaterialEntry(stack.getItem());
        if (entry == null || entry.isEmpty()) return null;

        Material mat = entry.material();
        if (mat == null || mat.isNull()) return null;

        return mat;
    }

    /**
     * Preferred fallback chain for assembler:
     * 1. rawOre
     * 2. dust
     * 3. gem
     */
    private static ItemStack getPreferredInputForMaterial(Material material) {
        // 1 — raw ore (best)
        if (!TagPrefix.rawOre.isIgnored(material)) {
            ItemStack raw = ChemicalHelper.get(TagPrefix.rawOre, material, 1);
            if (!raw.isEmpty()) return raw;
        }

        // 2 — dust (universal fallback)
        if (!TagPrefix.dust.isIgnored(material)) {
            ItemStack dust = ChemicalHelper.get(TagPrefix.dust, material, 1);
            if (!dust.isEmpty()) return dust;
        }

        // 3 — gems (ruby, sapphire, diamond…)
        if (!TagPrefix.gem.isIgnored(material)) {
            ItemStack gem = ChemicalHelper.get(TagPrefix.gem, material, 1);
            if (!gem.isEmpty()) return gem;
        }

        return ItemStack.EMPTY;
    }
}
