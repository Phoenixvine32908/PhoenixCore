package net.phoenix.core.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;

import net.phoenix.core.PhoenixCore;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;

public class PhoenixBeeMaterials {

    public static Material SOURCE_OF_MAGIC, FRUCTOSE, GLUCOSE, SUCROSE, SUGAR_WATER;
    public static Material PEANUT_BUTTER, PEANUT, CREAM, SKIM_MILK, MOLASSES;
    public static Material AMMONIUM_BISULFATE, AMMONIUM_BISULFATE_SOLUTION, AMMONIUM_PERSULFATE, PROTEIN_SOLUTION,
            AMINO_ACIDS;
    public static Material HONEY_CATALYST, HONEY_COMB_BASE_MIXTURE, POLLEN_CONCENTRATE_FLUID, WAX_MELTING_CATALYST;
    public static Material AURUM_WOOD, INVERT_SUGAR_SOLUTION;

    public static void register() {
        SOURCE_OF_MAGIC = new Material.Builder(PhoenixCore.id("source_of_magic")).fluid().color(0x8F00FF)
                .iconSet(MaterialIconSet.BRIGHT).buildAndRegister();
        FRUCTOSE = new Material.Builder(PhoenixCore.id("fructose")).fluid().color(0xF0F0F0)
                .iconSet(MaterialIconSet.DULL).buildAndRegister();
        GLUCOSE = new Material.Builder(PhoenixCore.id("glucose")).fluid().color(0xFFFAF0).iconSet(MaterialIconSet.DULL)
                .buildAndRegister();
        SUCROSE = new Material.Builder(PhoenixCore.id("sucrose")).fluid().color(0xF8F8F8).iconSet(MaterialIconSet.DULL)
                .buildAndRegister();
        SUGAR_WATER = new Material.Builder(PhoenixCore.id("sugar_water")).fluid().color(0xFFFFF0)
                .flags(DISABLE_DECOMPOSITION).iconSet(MaterialIconSet.DULL).buildAndRegister();
        INVERT_SUGAR_SOLUTION = new Material.Builder(PhoenixCore.id("invert_sugar_solution")).fluid().color(0xFFFCDE)
                .iconSet(MaterialIconSet.DULL).buildAndRegister();
        MOLASSES = new Material.Builder(PhoenixCore.id("molasses")).fluid().color(0x82574C)
                .iconSet(MaterialIconSet.DULL).buildAndRegister();

        PEANUT_BUTTER = new Material.Builder(PhoenixCore.id("peanut_butter")).fluid().color(0xD8BC9D)
                .iconSet(MaterialIconSet.DULL).buildAndRegister();
        PEANUT = new Material.Builder(PhoenixCore.id("peanut")).dust().color(0xE0C8A0).iconSet(MaterialIconSet.DULL)
                .buildAndRegister();
        CREAM = new Material.Builder(PhoenixCore.id("cream")).fluid().color(0xFFFBE6).iconSet(MaterialIconSet.DULL)
                .buildAndRegister();
        SKIM_MILK = new Material.Builder(PhoenixCore.id("skim_milk")).fluid().color(0xF8F8FF)
                .iconSet(MaterialIconSet.DULL).buildAndRegister();

        AMMONIUM_BISULFATE = new Material.Builder(PhoenixCore.id("ammonium_bisulfate")).dust().color(0xF0F0F0)
                .iconSet(MaterialIconSet.DULL).buildAndRegister();
        AMMONIUM_BISULFATE_SOLUTION = new Material.Builder(PhoenixCore.id("ammonium_bisulfate_solution")).fluid()
                .color(0xF0F0F0).iconSet(MaterialIconSet.DULL).buildAndRegister();
        AMMONIUM_PERSULFATE = new Material.Builder(PhoenixCore.id("ammonium_persulfate")).fluid().color(0xF0FFFF)
                .iconSet(MaterialIconSet.DULL).buildAndRegister();
        PROTEIN_SOLUTION = new Material.Builder(PhoenixCore.id("protein_solution")).fluid().color(0xFFE0C0)
                .iconSet(MaterialIconSet.DULL).buildAndRegister();
        AMINO_ACIDS = new Material.Builder(PhoenixCore.id("amino_acids")).fluid().color(0xFFFFFF)
                .iconSet(MaterialIconSet.DULL).buildAndRegister();

        HONEY_CATALYST = new Material.Builder(PhoenixCore.id("honey_catalyst")).fluid().color(0xFFF9E3)
                .iconSet(MaterialIconSet.DULL).buildAndRegister();
        HONEY_COMB_BASE_MIXTURE = new Material.Builder(PhoenixCore.id("honey_comb_base_mixture")).fluid()
                .color(0xFFF0F5).iconSet(MaterialIconSet.DULL).buildAndRegister();
        POLLEN_CONCENTRATE_FLUID = new Material.Builder(PhoenixCore.id("pollen_concentrate_fluid")).fluid()
                .color(0xFFC200).iconSet(MaterialIconSet.DULL).buildAndRegister();
        WAX_MELTING_CATALYST = new Material.Builder(PhoenixCore.id("wax_melting_catalyst")).fluid().color(0xADD8E6)
                .secondaryColor(0x6A5ACD).iconSet(MaterialIconSet.DULL).buildAndRegister();

        AURUM_WOOD = new Material.Builder(PhoenixCore.id("aurum_wood")).dust().color(0x291306).secondaryColor(0xfccd31)
                .iconSet(MaterialIconSet.DULL).buildAndRegister();
    }
}
