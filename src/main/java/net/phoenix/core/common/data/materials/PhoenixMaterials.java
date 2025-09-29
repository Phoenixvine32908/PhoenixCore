package net.phoenix.core.common.data.materials;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import net.phoenix.core.api.item.tool.PhoenixToolType;
import net.phoenix.core.common.data.PhoenixMaterialRegistry;
import net.phoenix.core.phoenixcore;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH;

public class PhoenixMaterials {

    public static Material QuantumCoolant;
    public static Material ExtremelyModifiedSpaceGradeSteel;
    public static Material EightyFivePercentPureNevvonianSteel;
    public static Material PHOENIX_ENRICHED_TRITANIUM;
    public static Material PHOENIX_ENRICHED_NAQUADAH;
    public static Material ALUMINFROST;

    public static void register() {
        ALUMINFROST = new Material.Builder(
                phoenixcore.id("aluminfrost"))
                .color(0xadd8e6).secondaryColor(0xc0c0c0).iconSet(MaterialIconSet.DULL)
                .toolStats(ToolProperty.Builder.of(1.8F, 1.7F, 700, 3)
                        .types(
                                GTToolType.SWORD,
                                GTToolType.PICKAXE,
                                GTToolType.SHOVEL)
                        .unbreakable()
                        .enchantment(SILK_TOUCH, 1)
                        .build())
                .buildAndRegister();
        QuantumCoolant = new Material.Builder(
                phoenixcore.id("quantum_coolant"))
                .plasma()
                .buildAndRegister();
        PhoenixMaterialRegistry.register(QuantumCoolant);
        EightyFivePercentPureNevvonianSteel = new Material.Builder(
                phoenixcore.id("eighty_five_percent_pure_nevvonian_steel"))
                .ingot()
                .element(PhoenixElements.APNS)
                .flags(PhoenixMaterialFlags.GENERATE_NANITES)
                .formula("APNS")
                .secondaryColor(593856)
                .toolStats(new ToolProperty(12.0F, 7.0F, 3072, 6,
                        new GTToolType[] { PhoenixToolType.DRILL_LUV, GTToolType.MINING_HAMMER }))
                .iconSet(PhoenixMaterialSet.ALMOST_PURE_NEVONIAN_STEEL)
                .buildAndRegister();
        PHOENIX_ENRICHED_TRITANIUM = new Material.Builder(
                phoenixcore.id("phoenix_enriched_tritanium"))
                .ingot()
                .color(0xFF0000)
                .secondaryColor(0x840707)
                .flags(MaterialFlags.GENERATE_FRAME, PhoenixMaterialFlags.GENERATE_NANITES)
                .formula("PET")
                .iconSet(PhoenixMaterialSet.ALMOST_PURE_NEVONIAN_STEEL)
                .buildAndRegister();
        PHOENIX_ENRICHED_NAQUADAH = new Material.Builder(
                phoenixcore.id("phoenix_enriched_naquadah"))
                .langValue("")
                .ingot()
                .color(0xFFA500)
                .secondaryColor(0x000000)
                .flags(MaterialFlags.GENERATE_FRAME, PhoenixMaterialFlags.GENERATE_NANITES)
                .formula("PENaq")
                .iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();
    }

    public static void modifyMaterials() {
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);

            if (toolProperty != null && toolProperty.hasType(GTToolType.SCREWDRIVER)) {
                toolProperty.addTypes(PhoenixToolType.SCREWDRIVER_MV);
                toolProperty.addTypes(PhoenixToolType.SCREWDRIVER_EV);
            }
        }
        for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
            ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);

            if (toolProperty != null && toolProperty.hasType(GTToolType.DRILL_EV)) {
                toolProperty.addTypes(PhoenixToolType.DRILL_LUV);
            }
        }
    }
}
