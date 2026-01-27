package net.phoenix.core.common.data.materials;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import net.phoenix.core.PhoenixCore;
import net.phoenix.core.api.item.tool.PhoenixToolType;
import net.phoenix.core.common.data.recipe.generated.CrystalRoseHelper;

import static com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier.MID;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class PhoenixMaterials {

    public static Material QuantumCoolant;
    public static Material EightyFivePercentPureNevvonianSteel;
    public static Material PHOENIX_ENRICHED_TRITANIUM;
    public static Material PHOENIX_ENRICHED_NAQUADAH;
    public static Material ALUMINFROST;
    public static Material SOURCE_OF_MAGIC; // Showtime, Fire!
    public static Material SOURCE_IMBUED_TITANIUM;
    public static Material EightyFivePercentPureNevonianSteel;
    public static Material FROST;
    public static Material BORON_CARBIDE;
    public static Material NIOBIUM_MODIFIED_SILICON_CARBIDE;
    public static Material SUGAR_WATER;
    public static Material WAX_MELTING_CATALYST;
    public static Material CRYO_GRAPHITE_BINDING_SOLUTION;

    public static void register() {
        SUGAR_WATER = new Material.Builder(
                PhoenixCore.id("sugar_water"))
                .fluid()
                .color(0xFFFFF0)
                .iconSet(MaterialIconSet.DULL) // Icon set from KubeJS
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .buildAndRegister();
        ALUMINFROST = new Material.Builder(
                PhoenixCore.id("aluminfrost"))
                .color(0xadd8e6).secondaryColor(0xc0c0c0).iconSet(MaterialIconSet.DULL)
                .flags(MaterialFlags.GENERATE_PLATE)
                .buildAndRegister();
        SOURCE_IMBUED_TITANIUM = new Material.Builder(
                PhoenixCore.id("source_imbued_titanium"))
                .ingot()
                .fluid()
                .formula("✨C✨Ti")
                .langValue("§5Source Imbued Titanium")
                .fluidPipeProperties(2800, 200, true, true, false, false)
                .color(0xc600ff).iconSet(MaterialIconSet.METALLIC)
                .flags(MaterialFlags.GENERATE_PLATE, MaterialFlags.GENERATE_RING, MaterialFlags.GENERATE_GEAR,
                        MaterialFlags.PHOSPHORESCENT, MaterialFlags.GENERATE_ROD, MaterialFlags.GENERATE_LONG_ROD,
                        MaterialFlags.GENERATE_BOLT_SCREW, MaterialFlags.GENERATE_FRAME, MaterialFlags.GENERATE_DENSE,
                        MaterialFlags.GENERATE_ROTOR)
                .buildAndRegister();
        SOURCE_OF_MAGIC = new Material.Builder(
                PhoenixCore.id("source_of_magic"))
                .langValue("§5Source Of Magic")
                .fluid()
                .iconSet(MaterialIconSet.BRIGHT)
                .color(0x8F00FF)
                .buildAndRegister();
        EightyFivePercentPureNevonianSteel = new Material.Builder(
                PhoenixCore.id("eightyfivepercentpurenevoniansteel")) // only one n
                .ingot()
                .langValue("85% Pure Nevonian Steel")
                .color(0xFFFFE0).secondaryColor(0xFFD700).iconSet(MaterialIconSet.SHINY)
                .flags(MaterialFlags.GENERATE_PLATE, MaterialFlags.GENERATE_GEAR, MaterialFlags.GENERATE_SMALL_GEAR,
                        MaterialFlags.GENERATE_SPRING, MaterialFlags.PHOSPHORESCENT, MaterialFlags.GENERATE_ROD,
                        MaterialFlags.GENERATE_DENSE, MaterialFlags.GENERATE_BOLT_SCREW, MaterialFlags.GENERATE_FRAME,
                        MaterialFlags.GENERATE_DENSE)
                .blastTemp(3800, MID, GTValues.VA[GTValues.EV], 1200)
                .buildAndRegister();
        EightyFivePercentPureNevvonianSteel = new Material.Builder(
                PhoenixCore.id("eighty_five_percent_pure_nevvonian_steel"))
                .ingot()
                .element(PhoenixElements.APNS)
                .flags(PhoenixMaterialFlags.GENERATE_NANITES)
                .formula("APNS")
                .secondaryColor(593856)
                .toolStats(new ToolProperty(12.0F, 7.0F, 3072, 6,
                        new GTToolType[] { GTToolType.MINING_HAMMER }))
                .iconSet(PhoenixMaterialSet.ALMOST_PURE_NEVONIAN_STEEL)
                .buildAndRegister();
        PHOENIX_ENRICHED_TRITANIUM = new Material.Builder(
                PhoenixCore.id("phoenix_enriched_tritanium"))
                .ingot()
                .color(0xFF0000)
                .secondaryColor(0x840707)
                .flags(MaterialFlags.GENERATE_FRAME, PhoenixMaterialFlags.GENERATE_CRYSTAL_ROSE)
                .formula("PET")
                .iconSet(PhoenixMaterialSet.ALMOST_PURE_NEVONIAN_STEEL)
                .buildAndRegister();
        PHOENIX_ENRICHED_NAQUADAH = new Material.Builder(
                PhoenixCore.id("phoenix_enriched_naquadah"))
                .langValue("")
                .ingot()
                .color(0xFFA500)
                .secondaryColor(0x000000)
                .flags(MaterialFlags.GENERATE_FRAME, PhoenixMaterialFlags.GENERATE_CRYSTAL_ROSE)
                .formula("PENaq")
                .iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();
        FROST = new Material.Builder(
                PhoenixCore.id("frost"))
                .langValue("§bFrost")
                .fluid()
                .color(0xA7D1EB)
                .secondaryColor(0x778899)
                .iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();
        BORON_CARBIDE = new Material.Builder(
                PhoenixCore.id("boron_carbide"))
                .ingot()
                .color(0x353630)
                .iconSet(MaterialIconSet.DULL)
                .blastTemp(3600, BlastProperty.GasTier.LOW, 500, 1500)
                .flags(MaterialFlags.GENERATE_PLATE, MaterialFlags.GENERATE_ROD, MaterialFlags.GENERATE_DENSE)
                .formula("B4C")
                .buildAndRegister();

        NIOBIUM_MODIFIED_SILICON_CARBIDE = new Material.Builder(
                PhoenixCore.id("niobium_modified_silicon_carbide"))
                .ingot()
                .color(0x4A4B6B)
                .secondaryColor(0x101021)
                .iconSet(MaterialIconSet.METALLIC)
                .flags(MaterialFlags.GENERATE_PLATE, MaterialFlags.GENERATE_FRAME, MaterialFlags.GENERATE_FOIL)
                .blastTemp(4500, BlastProperty.GasTier.MID, 2000, 1800)
                .formula("Nb(SiC)x")
                .buildAndRegister();
        WAX_MELTING_CATALYST = new Material.Builder(
                PhoenixCore.id("wax_melting_catalyst"))
                .color(0xADD8E6)
                .fluid()
                .secondaryColor(0x6A5ACD)
                .iconSet(MaterialIconSet.DULL)
                .buildAndRegister();
        CRYO_GRAPHITE_BINDING_SOLUTION = new Material.Builder(
                PhoenixCore.id("cryo_graphite_binding_solution"))
                .fluid()
                .color(0x507080)
                .secondaryColor(0x7090A0)
                .iconSet(MaterialIconSet.DULL)
                .buildAndRegister();
    }

    public static void modifyMaterials() {
        CrystalRoseHelper.addCrystalRoseFlags(
                // --- Basic & Base Metals ---
                Amethyst, Apatite, Bauxite, Cinnabar, Cobalt, Cobaltite, Copper, Diamond,
                Electrotine, Emerald, Galena, Gold, Ilmenite, Invar, Iron, Lapis,
                Lead, Lepidolite, Malachite, Nickel, Opal, Pitchblende, Pyrope, Realgar,
                Ruby, Salt, Sapphire, Scheelite, Silicon, Silver, Steel, Stibnite, Topaz,
                TricalciumPhosphate, Tungstate, Zinc,

                // --- LuV & Specialized Materials ---
                Barite, Bastnasite, Bismuth, Chromite, Graphite, Molybdenum, Oilsands, Platinum,
                Pyrochlore, Pyrolusite, Sphalerite, Sulfur, Tantalite, Tetrahedrite, Thorium,
                Titanium, VanadiumMagnetite,

                // --- Custom & External Materials from your Configs ---
                // PhoenixOres.FLUORITE,
                NetherQuartz,
                RockSalt,
                Sodalite,

                // --- Missing GT Materials from BeeRecipeData ---
                Coal,             // For Coal Bee
                Redstone,         // For Redstone Bee
                Tin,              // For Tin Bee
                Obsidian,         // For Obsidian Bee
                Netherite,        // For Netherite Bee
                CertusQuartz     // For Spacial Bee
        );

        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);

            if (toolProperty != null && toolProperty.hasType(GTToolType.SCREWDRIVER_LV)) {
                toolProperty.addTypes(PhoenixToolType.SCREWDRIVER_MV);
                toolProperty.addTypes(PhoenixToolType.SCREWDRIVER_HV);
                toolProperty.addTypes(PhoenixToolType.SCREWDRIVER_EV);
                toolProperty.addTypes(PhoenixToolType.SCREWDRIVER_IV);
                toolProperty.addTypes(PhoenixToolType.SCREWDRIVER_LuV);
                toolProperty.addTypes(PhoenixToolType.SCREWDRIVER_ZPM);
            }

            if (toolProperty != null && toolProperty.hasType(GTToolType.BUZZSAW)) {
                toolProperty.addTypes(PhoenixToolType.BUZZSAW_MV);
                toolProperty.addTypes(PhoenixToolType.BUZZSAW_HV);
                toolProperty.addTypes(PhoenixToolType.BUZZSAW_EV);
                toolProperty.addTypes(PhoenixToolType.BUZZSAW_IV);
                toolProperty.addTypes(PhoenixToolType.BUZZSAW_LuV);
                toolProperty.addTypes(PhoenixToolType.BUZZSAW_ZPM);
            }

            if (toolProperty != null && toolProperty.hasType(GTToolType.CHAINSAW_LV)) {
                toolProperty.addTypes(PhoenixToolType.CHAINSAW_MV);
                toolProperty.addTypes(PhoenixToolType.CHAINSAW_HV);
                toolProperty.addTypes(PhoenixToolType.CHAINSAW_EV);
                toolProperty.addTypes(PhoenixToolType.CHAINSAW_IV);
                toolProperty.addTypes(PhoenixToolType.CHAINSAW_LuV);
                toolProperty.addTypes(PhoenixToolType.CHAINSAW_ZPM);
            }

            if (toolProperty != null && toolProperty.hasType(GTToolType.WIRE_CUTTER_LV)) {
                toolProperty.addTypes(PhoenixToolType.WIRE_CUTTER_MV);
                toolProperty.addTypes(PhoenixToolType.WIRE_CUTTER_EV);
                toolProperty.addTypes(PhoenixToolType.WIRE_CUTTER_LuV);
                toolProperty.addTypes(PhoenixToolType.WIRE_CUTTER_ZPM);
            }

            if (toolProperty != null && toolProperty.hasType(GTToolType.WRENCH_LV)) {
                toolProperty.addTypes(PhoenixToolType.WRENCH_MV);
                toolProperty.addTypes(PhoenixToolType.WRENCH_EV);
                toolProperty.addTypes(PhoenixToolType.WRENCH_LuV);
                toolProperty.addTypes(PhoenixToolType.WRENCH_ZPM);
            }

            if (toolProperty != null && toolProperty.hasType(GTToolType.DRILL_LV)) {
                toolProperty.addTypes(PhoenixToolType.DRILL_LUV, PhoenixToolType.DRILL_ZPM);
            }

        }
    }
}
