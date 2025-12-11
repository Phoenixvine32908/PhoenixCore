package net.phoenix.core.common.data.recipe.generated;

import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicate;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicates;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.core.common.data.bees.BeeRecipeData;
import net.phoenix.core.common.data.recipe.records.ApisProgenitorConfig;

import java.util.function.Consumer;

import static net.phoenix.core.common.data.PhoenixRecipeTypes.APIS_PROGENITOR_RECIPES;
import static net.phoenix.core.common.data.bees.BeeRecipeData.MOD_ID;

public class PhoenixBeeRecipeGenerator {

    private static final ItemStack EMPTY_BEE_CAGE = new ItemStack(
            ForgeRegistries.ITEMS.getValue(new ResourceLocation("productivebees:bee_cage")));

    private static FluidStack getFluidStack(String fluidString) {
        String[] parts = fluidString.split(" ");
        ResourceLocation id = new ResourceLocation(parts[0]);
        long amount = Long.parseLong(parts[1]);

        net.minecraft.world.level.material.Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);

        if (fluid instanceof ForgeFlowingFluid flowingFluid) {
            return new FluidStack(flowingFluid.getSource(), (int) amount);
        }

        if (fluid != null) {
            return new FluidStack(fluid, (int) amount);
        }

        throw new IllegalArgumentException("Fluid not found for ID: " + id);
    }

    private static ItemStack createBeeCage(String typeId) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("entity", "productivebees:configurable_bee");
        nbt.putString("type", "productivebees:" + typeId);

        String displayName = BeeRecipeData.BEE_DISPLAY_NAMES.getOrDefault(typeId, typeId + " Bee");
        nbt.putString("name", displayName);

        ItemStack cage = EMPTY_BEE_CAGE.copy();
        cage.setTag(nbt);
        return cage;
    }

    public static void loadBeeRecipes(Consumer<FinishedRecipe> provider) {
        generateApisProgenitorRecipes(provider);
    }

    private static void generateApisProgenitorRecipes(Consumer<FinishedRecipe> provider) {
        for (ApisProgenitorConfig config : BeeRecipeData.UNIQUE_APIS_PROGENITOR_CONFIGS) {

            NBTPredicate inputPredicate = NBTPredicates.eqString("type", "productivebees:" + config.inputBeeType());
            ItemStack outputBeeCage = createBeeCage(config.outputBeeType());

            GTRecipeBuilder builder = APIS_PROGENITOR_RECIPES.recipeBuilder(MOD_ID + "/apis_progenitor/" + config.id())
                    .EUt(config.EUt())
                    .duration(config.duration())

                    .inputItemNbtPredicate(EMPTY_BEE_CAGE, inputPredicate)

                    .inputItems(config.itemInput())

                    .outputItems(outputBeeCage);

            if (config.fluidInput() != null) {
                builder.inputFluids(getFluidStack(config.fluidInput()));
            }

            builder.save(provider);
        }
    }
}
