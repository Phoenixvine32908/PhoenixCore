package net.phoenix.core.common.data.recipe.generated;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicate;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicates;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.core.common.data.bees.BeeRecipeData;
import net.phoenix.core.common.data.recipe.records.ApisProgenitorConfig;
import net.phoenix.core.common.data.recipe.records.FullBeeConfig;

import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static net.phoenix.core.common.data.PhoenixRecipeTypes.*;
import static net.phoenix.core.common.data.bees.BeeRecipeData.MOD_ID;
import static net.phoenix.core.common.data.materials.PhoenixMaterialFlags.crystal_rose;

public class PhoenixBeeRecipeGenerator {

    private static final ItemStack EMPTY_BEE_CAGE = new ItemStack(
            ForgeRegistries.ITEMS.getValue(new ResourceLocation("productivebees:bee_cage")));

    private static final ItemStack WAX_OUTPUT_ITEM = new ItemStack(
            ForgeRegistries.ITEMS.getValue(new ResourceLocation("productivebees:wax")));

    private static final ItemStack BASE_CONFIGURABLE_COMB = new ItemStack(
            ForgeRegistries.ITEMS.getValue(new ResourceLocation("productivebees:configurable_honeycomb")));

    /**
     * Creates a Configurable Honeycomb ItemStack with the correct EntityTag NBT.
     * This ensures JEI/EMI displays the correct name (e.g., "Steel Honeycomb").
     */
    private static ItemStack createConfigurableComb(String beeId) {
        ItemStack comb = BASE_CONFIGURABLE_COMB.copy();
        CompoundTag root = new CompoundTag();
        CompoundTag entityTag = new CompoundTag();
        entityTag.putString("type", "productivebees:" + beeId);
        root.put("EntityTag", entityTag);
        comb.setTag(root);
        return comb;
    }

    private static FluidStack getFluidStack(String fluidString) {
        String[] parts = fluidString.split(" ");
        ResourceLocation id = new ResourceLocation(parts[0]);
        int amount = Integer.parseInt(parts[1]);
        return new FluidStack(ForgeRegistries.FLUIDS.getValue(id), amount);
    }

    private static ItemStack createBeeCage(String typeId) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("entity", "productivebees:configurable_bee");
        nbt.putString("type", "productivebees:" + typeId);
        nbt.putString("name", typeId.substring(0, 1).toUpperCase() + typeId.substring(1) + " Bee");

        ItemStack cage = EMPTY_BEE_CAGE.copy();
        cage.setTag(nbt);
        return cage;
    }

    public static void loadBeeRecipes(Consumer<FinishedRecipe> provider) {
        generateApisProgenitorRecipes(provider);
        generateSimulatedColonyRecipes(provider);
        loadBeeCombProductionRecipes(provider);
        generateLumberBeeRecipes(provider);
        generateSwarmNurturingRecipes(provider);
    }

    public static void generateApisProgenitorRecipes(Consumer<FinishedRecipe> provider) {
        for (ApisProgenitorConfig config : BeeRecipeData.UNIQUE_APIS_PROGENITOR_CONFIGS) {
            ItemStack inputBeeCage = createBeeCage(config.inputBeeType());
            NBTPredicate inputPredicate = NBTPredicates.eqString("type", "productivebees:" + config.inputBeeType());
            ItemStack outputBeeCage = createBeeCage(config.outputBeeType());

            GTRecipeBuilder builder = APIS_PROGENITOR_RECIPES.recipeBuilder(MOD_ID + "/apis_progenitor/" + config.id())
                    .EUt(config.EUt())
                    .duration(config.duration())
                    .inputItemNbtPredicate(inputBeeCage, inputPredicate)
                    .inputItems(config.itemInput())
                    .outputItems(outputBeeCage);

            if (config.fluidInput() != null) builder.inputFluids(getFluidStack(config.fluidInput()));
            builder.save(provider);
        }
    }

    public static void generateSimulatedColonyRecipes(Consumer<FinishedRecipe> provider) {
        final ItemStack honeyCombBaseInput = new ItemStack(
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("kubejs", "honey_comb_base")), 1);

        for (FullBeeConfig config : BeeRecipeData.ALL_BEE_CONFIGS.values()) {
            String beeId = config.beeId();
            Material beeMaterial = GTMaterials.get(beeId);

            ItemStack combOutputStack = createConfigurableComb(beeId);
            ItemStack inputBeeCage = createBeeCage(beeId);
            ItemStack resourceBlock = new ItemStack(
                    ForgeRegistries.ITEMS.getValue(new ResourceLocation(config.pollinationInputId())));
            FluidStack sugarWater = BeeRecipeData.SUGAR_WATER_MATERIAL.getFluid(BeeRecipeData.SUGAR_WATER_AMOUNT);

            SIMULATED_COLONY_RECIPES.recipeBuilder(MOD_ID + "/simulated_colony/" + beeId)
                    .EUt(config.decantingEut()).duration(config.decantingDuration())
                    .notConsumable(inputBeeCage)
                    .inputItems(honeyCombBaseInput).inputItems(resourceBlock)
                    .inputFluids(sugarWater)
                    .outputItems(combOutputStack, 1)
                    .save(provider);

            if (beeMaterial != null) {
                ItemStack crystalRoseStack = ChemicalHelper.get(crystal_rose, beeMaterial);

                if (!crystalRoseStack.isEmpty()) {
                    SIMULATED_COLONY_RECIPES.recipeBuilder(MOD_ID + "/simulated_colony_boosted/" + beeId)
                            .EUt(config.boostedDecantingEut()).duration(config.boostedDecantingDuration())
                            .notConsumable(inputBeeCage)
                            .inputItems(honeyCombBaseInput).inputItems(resourceBlock)
                            .inputFluids(sugarWater)
                            .inputItems(crystalRoseStack.copyWithCount(1))
                            .outputItems(combOutputStack, 2)
                            .save(provider);
                }
            }
        }
    }

    static final ItemStack HONEY_COMB_BASE = new ItemStack(
            ForgeRegistries.ITEMS.getValue(new ResourceLocation("kubejs", "honey_comb_base")), 1);

    public static void loadBeeCombProductionRecipes(Consumer<FinishedRecipe> provider) {
        for (FullBeeConfig config : BeeRecipeData.ALL_BEE_CONFIGS.values()) {
            String beeId = config.beeId();

            if (beeId.equals("rancher") || beeId.equals("steamy")) continue;

            ItemStack visualComb = createConfigurableComb(beeId);
            NBTPredicate combPredicate = NBTPredicates.eqString("EntityTag.type", "productivebees:" + beeId);

            String waxDustId = "gtceu:raw_" + beeId + "_wax_dust";
            String honeyedFluidId = "gtceu:honeyed_" + beeId;

            COMB_DECANTING_RECIPES.recipeBuilder(MOD_ID + "/decanting/" + beeId)
                    .EUt(config.decantingEut()).duration(config.decantingDuration())
                    .inputItemNbtPredicate(visualComb, combPredicate)
                    .outputItems(new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(waxDustId))))
                    .outputItems(new ItemStack(
                            ForgeRegistries.ITEMS.getValue(new ResourceLocation("kubejs:honey_comb_base"))))
                    .save(provider);

            BREWING_RECIPES.recipeBuilder(MOD_ID + "/wax_melting/" + beeId)
                    .EUt(config.waxEut()).duration(400)
                    .inputItems(new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(waxDustId))))
                    .inputFluids(GTMaterials.get("melting_catalyst").getFluid(100))
                    .outputFluids(getFluidStack(honeyedFluidId + " 1000"))
                    .save(provider);

            var purifier = CENTRIFUGE_RECIPES.recipeBuilder(MOD_ID + "/honeyed_purifying/" + beeId)
                    .EUt(config.decantingEut()).duration(400)
                    .inputFluids(getFluidStack(honeyedFluidId + " 1000"))
                    .outputFluids(getFluidStack("gtceu:impure_honey 500"));

            if (beeId.equals("water")) {
                purifier.outputItems(
                        new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:salmon"))));
            } else if (beeId.equals("wannabee")) {
                purifier.outputItems(new ItemStack(
                        ForgeRegistries.ITEMS.getValue(new ResourceLocation(config.pollinationInputId())), 2));
            } else {
                purifier.outputItems(config.finalOutputItem());
            }

            purifier.save(provider);

            MIXER_RECIPES.recipeBuilder(MOD_ID + "/honey_production/" + beeId)
                    .EUt(config.fluidEut()).duration(config.fluidDuration())
                    .inputItemNbtPredicate(visualComb, combPredicate)
                    .inputFluids(BeeRecipeData.SUGAR_WATER_MATERIAL.getFluid(BeeRecipeData.SUGAR_WATER_AMOUNT))
                    .outputFluids(
                            getFluidStack(BeeRecipeData.HONEY_FLUID + " " + BeeRecipeData.FINAL_HONEY_OUTPUT_AMOUNT))
                    .save(provider);
        }
    }

    public static void generateSwarmNurturingRecipes(Consumer<FinishedRecipe> provider) {
        record MobType(String entityId, String name, String drop, int amount) {}
        List<MobType> mobs = List.of(
                new MobType("minecraft:creeper", "Creeper", "minecraft:gunpowder", 128),
                new MobType("minecraft:skeleton", "Skeleton", "minecraft:bone", 128),
                new MobType("minecraft:zombie", "Zombie", "minecraft:rotten_flesh", 128),
                new MobType("minecraft:spider", "Spider", "minecraft:string", 128),
                new MobType("minecraft:slime", "Slime", "minecraft:slime_ball", 32),
                new MobType("minecraft:magma_cube", "Magma Cube", "minecraft:magma_cream", 32),
                new MobType("minecraft:blaze", "Blaze", "minecraft:blaze_rod", 32),
                new MobType("minecraft:ghast", "Ghast", "minecraft:ghast_tear", 16),
                new MobType("minecraft:phantom", "Phantom", "minecraft:phantom_membrane", 32),
                new MobType("minecraft:enderman", "Enderman", "minecraft:ender_pearl", 64),
                new MobType("minecraft:guardian", "Guardian", "minecraft:prismarine_crystals", 32),
                new MobType("minecraft:witch", "Witch", "minecraft:fermented_spider_eye", 16),
                new MobType("minecraft:wither_skeleton", "Wither Skeleton", "minecraft:wither_skeleton_skull", 4),
                new MobType("minecraft:evoker", "Evoker", "minecraft:emerald", 64),
                new MobType("minecraft:squid", "Squid", "minecraft:ink_sac", 64),
                new MobType("minecraft:cow", "Cow", "minecraft:leather", 128),
                new MobType("minecraft:sheep", "Sheep", "minecraft:white_wool", 128),
                new MobType("minecraft:chicken", "Chicken", "minecraft:feather", 32),
                new MobType("minecraft:pig", "Pig", "minecraft:porkchop", 16));

        ItemStack amberBeeCage = createBeeCage("amber");
        NBTPredicate amberPredicate = NBTPredicates.eqString("type", "productivebees:amber");

        for (MobType mob : mobs) {
            ItemStack amberOutput = new ItemStack(
                    ForgeRegistries.ITEMS.getValue(new ResourceLocation("productivebees:amber")));
            CompoundTag nbt = new CompoundTag();
            CompoundTag blockEntityTag = new CompoundTag();
            CompoundTag entityData = new CompoundTag();
            entityData.putString("entityType", mob.entityId());
            entityData.putString("name", mob.name());
            blockEntityTag.put("EntityData", entityData);
            nbt.put("BlockEntityTag", blockEntityTag);
            amberOutput.setTag(nbt);

            SWARM_NURTURING_RECIPES.recipeBuilder(MOD_ID + "/swarm_nurturing/" + mob.entityId().replace(":", "_"))
                    .EUt(GTValues.VA[GTValues.IV]).duration(1200)
                    .notConsumable(amberBeeCage)
                    .inputItemNbtPredicate(amberBeeCage, amberPredicate)
                    .inputItems(new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(mob.drop())),
                            mob.amount()))
                    .outputItems(amberOutput).save(provider);
        }
    }

    public static void generateLumberBeeRecipes(Consumer<FinishedRecipe> provider) {
        ItemStack lumberBeeCage = createBeeCage("lumber");
        NBTPredicate lumberPredicate = NBTPredicates.eqString("entity", "productivebees:lumber_bee");
        for (String logId : BeeRecipeData.LUMBER_LOG_TYPES) {
            ItemStack logStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(logId)));
            SIMULATED_COLONY_RECIPES.recipeBuilder(MOD_ID + "/simulated_colony/lumber_" + logId.replace(":", "_"))
                    .EUt(GTValues.VA[GTValues.IV]).duration(1200)
                    .inputItemNbtPredicate(lumberBeeCage, lumberPredicate)
                    .notConsumable(logStack)
                    .inputFluids(BeeRecipeData.SUGAR_WATER_MATERIAL.getFluid(BeeRecipeData.SUGAR_WATER_AMOUNT))
                    .outputItems(logStack.copyWithCount(64)).save(provider);
        }
    }
}
