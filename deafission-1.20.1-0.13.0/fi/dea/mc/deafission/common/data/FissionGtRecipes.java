//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;
import fi.dea.mc.deafission.Config;
import fi.dea.mc.deafission.FissionMod;
import fi.dea.mc.deafission.common.data.items.FuelCellItem;
import fi.dea.mc.deafission.core.ReactorCoolantRecipe;
import fi.dea.mc.deafission.core.ReactorFuelRecipe;
import fi.dea.mc.deafission.core.ReactorProcessingRecipe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class FissionGtRecipes {
    @Nullable
    private static List<ReactorCoolantRecipe> _coolantRecipes;
    @Nullable
    private static Map<Item, ReactorFuelRecipe> _fuelRecipes;
    @Nullable
    private static Map<Item, ReactorProcessingRecipe> _processingRecipes;

    public static void init(Consumer<FinishedRecipe> provider) {
        if (Config.addDefaultRecipes) {
            FissionGtRecipeTypes.ReactorCoolantRecipe.recipeBuilder(FissionMod.id("uranium/water_to_steam")).notConsumable(Ingredient.m_43929_(new ItemLike[]{FissionItems.UraniumFuelCell, FissionItems.UraniumFuelCell_x4})).duration(1000).inputFluids(_fluid("minecraft", "water", 1000)).outputFluids(_fluid("gtceu", "steam", 100000)).addData("coolant_heat_per_tick", DoubleTag.m_128500_((double)1.0F)).save(provider);
            FissionGtRecipeTypes.ReactorCoolantRecipe.recipeBuilder(FissionMod.id("thorium/nak_to_plasma")).notConsumable(FissionItems.ThoriumFuelCell).perTick(true).inputFluids(_fluid("gtceu", "sodium_potassium", 1)).outputFluids(_fluid("gtceu", "helium_plasma", 1)).duration(1).addData("coolant_heat_per_tick", DoubleTag.m_128500_((double)100.0F)).save(provider);
            FissionGtRecipeTypes.ReactorFuelRecipe.recipeBuilder(FissionMod.id("uranium")).inputItems(FissionItems.UraniumFuelCell).outputFluids(_fluid("gtceu", "glue", 1000)).duration(fuelLifeExpectancy((FuelCellItem)FissionItems.UraniumFuelCell.get())).save(provider);
            FissionGtRecipeTypes.ReactorFuelRecipe.recipeBuilder(FissionMod.id("thorium")).inputItems(FissionItems.ThoriumFuelCell).outputFluids(_fluid("gtceu", "glue", 1000)).outputItems(_item("gtceu", "raw_thorium", 11)).duration(fuelLifeExpectancy((FuelCellItem)FissionItems.ThoriumFuelCell.get())).save(provider);
            FissionGtRecipeTypes.ReactorProcessingRecipe.recipeBuilder(FissionMod.id("charcoal")).inputItems(Items.f_41837_).outputItems(new ItemStack(Items.f_42414_, 1)).duration(100).blastFurnaceTemp(500).addData("heat_per_tick", 100).save(provider);
            FissionGtRecipeTypes.ReactorProcessingRecipe.recipeBuilder(FissionMod.id("cooling/blue_ice")).inputItems(Items.f_42363_).duration(1).blastFurnaceTemp(600).addData("heat_per_tick", 10000).save(provider);
        }
    }

    public static void onConfigReloaded() {
        _coolantRecipes = null;
        _fuelRecipes = null;
        _processingRecipes = null;
    }

    public static void parseAll() {
        get_coolantRecipes();
        get_fuelRecipes();
        get_processingRecipes();
    }

    public static List<ReactorCoolantRecipe> get_coolantRecipes() {
        if (_coolantRecipes == null) {
            _coolantRecipes = parseCoolantRecipes();
        }

        return _coolantRecipes;
    }

    public static Map<Item, ReactorFuelRecipe> get_fuelRecipes() {
        if (_fuelRecipes == null) {
            _fuelRecipes = parseFuelRecipes();
        }

        return _fuelRecipes;
    }

    public static Map<Item, ReactorProcessingRecipe> get_processingRecipes() {
        if (_processingRecipes == null) {
            _processingRecipes = parseProcessingRecipes();
        }

        return _processingRecipes;
    }

    private static List<ReactorCoolantRecipe> parseCoolantRecipes() {
        GTRecipeType recipeType = FissionGtRecipeTypes.ReactorCoolantRecipe;
        Map<GTRecipeCategory, Set<GTRecipe>> map = recipeType.getCategoryMap();
        ArrayList<ReactorCoolantRecipe> output = new ArrayList();

        for(Map.Entry<GTRecipeCategory, Set<GTRecipe>> i : map.entrySet()) {
            for(GTRecipe r : (Set)i.getValue()) {
                output.add(_parseCoolantRecipe(r));
            }
        }

        return output;
    }

    private static Map<Item, ReactorFuelRecipe> parseFuelRecipes() {
        GTRecipeType recipeType = FissionGtRecipeTypes.ReactorFuelRecipe;
        Map<GTRecipeCategory, Set<GTRecipe>> map = recipeType.getCategoryMap();
        HashMap<Item, ReactorFuelRecipe> output = new HashMap();

        for(Map.Entry<GTRecipeCategory, Set<GTRecipe>> i : map.entrySet()) {
            for(GTRecipe r : (Set)i.getValue()) {
                ReactorFuelRecipe recipe = _parseFuelRecipe(r);

                for(Item input : recipe.input()) {
                    boolean hadPrev = output.put(input, recipe) != null;

                    assert !hadPrev : "Reactor fuel participates in multiple recipes";
                }
            }
        }

        return output;
    }

    private static Map<Item, ReactorProcessingRecipe> parseProcessingRecipes() {
        GTRecipeType recipeType = FissionGtRecipeTypes.ReactorProcessingRecipe;
        Map<GTRecipeCategory, Set<GTRecipe>> map = recipeType.getCategoryMap();
        HashMap<Item, ReactorProcessingRecipe> output = new HashMap();

        for(Map.Entry<GTRecipeCategory, Set<GTRecipe>> i : map.entrySet()) {
            for(GTRecipe r : (Set)i.getValue()) {
                ReactorProcessingRecipe recipe = _parseProcessingRecipe(r);

                for(Item input : recipe.input()) {
                    boolean hadPrev = output.put(input, recipe) != null;

                    assert !hadPrev : "Reactor processing input participates in multiple recipes";
                }
            }
        }

        return output;
    }

    private static int fuelLifeExpectancy(FuelCellItem fuel) {
        double FUEL_DAMAGE_AT_ONE_ROD_HEAT = 0.4;
        return (int)((double)fuel.m_7968_().m_41776_() / fuel.Heat / 0.4);
    }

    private static ReactorCoolantRecipe _parseCoolantRecipe(GTRecipe recipe) {
        Stream<FluidIngredient> recipeFluidsIn = recipe.getInputContents(GTRecipeCapabilities.FLUID).stream().map((r) -> (FluidIngredient)FluidRecipeCapability.CAP.of(r.content));
        Stream<FluidIngredient> recipeFluidsOut = recipe.getOutputContents(GTRecipeCapabilities.FLUID).stream().map((r) -> (FluidIngredient)FluidRecipeCapability.CAP.of(r.content));
        Stream<Ingredient> recipeItemsIn = recipe.getInputContents(GTRecipeCapabilities.ITEM).stream().map((r) -> (Ingredient)ItemRecipeCapability.CAP.of(r.content));
        Stream<Ingredient> recipeItemsOut = recipe.getOutputContents(GTRecipeCapabilities.ITEM).stream().map((r) -> (Ingredient)ItemRecipeCapability.CAP.of(r.content));
        Stream<FluidIngredient> tickFluidsIn = recipe.getTickInputContents(GTRecipeCapabilities.FLUID).stream().map((r) -> (FluidIngredient)FluidRecipeCapability.CAP.of(r.content));
        Stream<FluidIngredient> tickFluidsOut = recipe.getTickOutputContents(GTRecipeCapabilities.FLUID).stream().map((r) -> (FluidIngredient)FluidRecipeCapability.CAP.of(r.content));
        Stream<Ingredient> tickItemsIn = recipe.getTickInputContents(GTRecipeCapabilities.ITEM).stream().map((r) -> (Ingredient)ItemRecipeCapability.CAP.of(r.content));
        Stream<Ingredient> tickItemsOut = recipe.getTickOutputContents(GTRecipeCapabilities.ITEM).stream().map((r) -> (Ingredient)ItemRecipeCapability.CAP.of(r.content));
        List<FluidIngredient> fluidsIn = Stream.concat(recipeFluidsIn, tickFluidsIn).toList();
        List<FluidIngredient> fluidsOut = Stream.concat(recipeFluidsOut, tickFluidsOut).toList();
        List<Ingredient> itemsIn = Stream.concat(recipeItemsIn, tickItemsIn).toList();
        List<Ingredient> itemsOut = Stream.concat(recipeItemsOut, tickItemsOut).toList();

        assert itemsIn.size() == 1 : "NotSupported: Need exactly 1 input item";

        assert fluidsIn.size() == 1 : "NotSupported: Need exactly 1 input fluid";

        assert itemsOut.isEmpty() : "NotSupported: Output items in reactor recipe";

        assert recipe.data.m_128425_("coolant_heat_per_tick", 99) : "NotSupported: COOLANT_HEAT_PER_TICK missing";

        double dataCoolantHeat = recipe.data.m_128459_("coolant_heat_per_tick");

        assert dataCoolantHeat > (double)0.0F : "NotSupported: COOLANT_HEAT_PER_TICK must be >0";

        assert !recipe.hasTick() || recipe.duration == 1 : "NotImplemented: Duration scaling for recipes with per-tick contents. Use duration=1.";

        Ingredient fuelItem = (Ingredient)itemsIn.get(0);

        assert Arrays.stream(fuelItem.m_43908_()).allMatch((is) -> is.m_41720_() instanceof FuelCellItem) : "Reactor coolant recipe input must be a reactor fuel";

        assert fluidsIn.stream().allMatch((fs) -> fs.getStacks().length == 1) : "NotImplemented: Non-singular reactor recipe coolant fluid";

        assert fluidsOut.stream().allMatch((fs) -> fs.getStacks().length == 1) : "Reactor recipe output fluids must refer to singlular ingredients";

        FluidStack coolantFluid = ((FluidIngredient)fluidsIn.get(0)).getStacks()[0];
        FluidStack[] outputFluids = (FluidStack[])fluidsOut.stream().flatMap((fs) -> Arrays.stream(fs.getStacks())).toArray((x$0) -> new FluidStack[x$0]);
        return new ReactorCoolantRecipe(recipe.id, (Item[])Arrays.stream(fuelItem.m_43908_()).map((is) -> is.m_41720_()).toArray((x$0) -> new Item[x$0]), coolantFluid, dataCoolantHeat, outputFluids, recipe.duration);
    }

    private static ReactorFuelRecipe _parseFuelRecipe(GTRecipe recipe) {
        List<FluidIngredient> fluidsIn = recipe.getInputContents(GTRecipeCapabilities.FLUID).stream().map((r) -> (FluidIngredient)FluidRecipeCapability.CAP.of(r.content)).toList();
        List<FluidIngredient> fluidsOut = recipe.getOutputContents(GTRecipeCapabilities.FLUID).stream().map((r) -> (FluidIngredient)FluidRecipeCapability.CAP.of(r.content)).toList();
        List<Ingredient> itemsIn = recipe.getInputContents(GTRecipeCapabilities.ITEM).stream().map((r) -> (Ingredient)ItemRecipeCapability.CAP.of(r.content)).toList();
        List<Ingredient> itemsOut = recipe.getOutputContents(GTRecipeCapabilities.ITEM).stream().map((r) -> (Ingredient)ItemRecipeCapability.CAP.of(r.content)).toList();

        assert !recipe.hasTick() : "Reactor fuel recipes can't be per-tick";

        assert itemsIn.size() == 1 : "NotSupported: Need exactly 1 input item";

        assert fluidsIn.size() == 0 : "NotSupported: Need exactly 0 input fluids";

        Ingredient fuelItem = (Ingredient)itemsIn.get(0);

        assert Arrays.stream(fuelItem.m_43908_()).allMatch((is) -> is.m_41720_() instanceof FuelCellItem) : "Reactor fuel recipe input must be a reactor fuel";

        assert itemsOut.stream().allMatch((fs) -> fs.m_43908_().length == 1) : "Reactor fuel recipe output items must refer to singlular ingredients";

        assert fluidsOut.stream().allMatch((fs) -> fs.getStacks().length == 1) : "Reactor fuel recipe output fluids must refer to singlular ingredients";

        return new ReactorFuelRecipe((Item[])Arrays.stream(fuelItem.m_43908_()).map((is) -> is.m_41720_()).toArray((x$0) -> new Item[x$0]), (ItemStack[])itemsOut.stream().map((o) -> o.m_43908_()[0]).toArray((x$0) -> new ItemStack[x$0]), (FluidStack[])fluidsOut.stream().map((o) -> o.getStacks()[0]).toArray((x$0) -> new FluidStack[x$0]));
    }

    private static ReactorProcessingRecipe _parseProcessingRecipe(GTRecipe recipe) {
        List<FluidIngredient> fluidsIn = recipe.getInputContents(GTRecipeCapabilities.FLUID).stream().map((r) -> (FluidIngredient)FluidRecipeCapability.CAP.of(r.content)).toList();
        List<FluidIngredient> fluidsOut = recipe.getOutputContents(GTRecipeCapabilities.FLUID).stream().map((r) -> (FluidIngredient)FluidRecipeCapability.CAP.of(r.content)).toList();
        List<Ingredient> itemsIn = recipe.getInputContents(GTRecipeCapabilities.ITEM).stream().map((r) -> (Ingredient)ItemRecipeCapability.CAP.of(r.content)).toList();
        List<Ingredient> itemsOut = recipe.getOutputContents(GTRecipeCapabilities.ITEM).stream().map((r) -> (Ingredient)ItemRecipeCapability.CAP.of(r.content)).toList();

        assert !recipe.hasTick() : "Reactor processing recipes can't be per-tick";

        assert itemsIn.size() == 1 : "NotSupported: Need exactly 1 input item";

        assert itemsOut.size() <= 1 : "NotSupported: Need at most 1 output item";

        assert fluidsIn.size() == 0 : "NotSupported: Need exactly 0 input fluids";

        assert fluidsOut.size() <= 1 : "NotSupported: Need at most 1 output fluids";

        assert recipe.data.m_128425_("ebf_temp", 99) : "NotSupported: Temperature data missing (any numeric type)";

        assert recipe.data.m_128425_("heat_per_tick", 99) : "NotSupported: Heat data missing (any numeric type)";

        int dataTemp = recipe.data.m_128451_("ebf_temp");
        int dataHeat = recipe.data.m_128451_("heat_per_tick");

        assert dataTemp >= 0 : "NotSupported: Negative heat level";

        assert dataHeat >= 0 : "NotSupported: Processing recipes that generate heat (yet)";

        assert itemsOut.stream().allMatch((fs) -> fs.m_43908_().length == 1) : "Reactor processing recipe output items must refer to singlular ingredients";

        assert fluidsOut.stream().allMatch((fs) -> fs.getStacks().length == 1) : "Reactor processing recipe output fluids must refer to singlular ingredients";

        return new ReactorProcessingRecipe((Item[])Arrays.stream(((Ingredient)itemsIn.get(0)).m_43908_()).map((is) -> is.m_41720_()).toArray((x$0) -> new Item[x$0]), itemsOut.size() != 0 ? ((Ingredient)itemsOut.get(0)).m_43908_()[0] : null, fluidsOut.size() != 0 ? ((FluidIngredient)fluidsOut.get(0)).getStacks()[0] : null, dataTemp, recipe.duration, (double)dataHeat);
    }

    private static FluidStack _fluid(String ns, String id, int amount) {
        Fluid fluid = (Fluid)ForgeRegistries.FLUIDS.getValue(ResourceLocation.fromNamespaceAndPath(ns, id));
        return new FluidStack((Fluid)Objects.requireNonNull(fluid), amount);
    }

    private static ItemStack _item(String ns, String id, int amount) {
        Item fluid = (Item)ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(ns, id));
        return new ItemStack((ItemLike)Objects.requireNonNull(fluid), amount);
    }
}
