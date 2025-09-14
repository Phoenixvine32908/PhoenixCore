package net.phoenix.core.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.data.GTMaterials;

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
        EightyFivePercentPureNevvonianSteel = new Material.Builder(
                phoenixcore.id("eighty_five_percent_pure_nevvonian_steel"))
                .ingot()
                .element(PhoenixElements.APNS)
                .flags(PhoenixMaterialFlags.GENERATE_NANITES)
                .formula("APNS")
                .secondaryColor(593856)
                .toolStats(ToolProperty.Builder.of(7.0F, 6.0F, 2560, 3)
                        .types(GTToolType.AXE, GTToolType.DRILL_EV)
                        .unbreakable().enchantability(21).enchantment(SILK_TOUCH, 1).build())
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
                .ingot()
                .color(0xFFA500)
                .secondaryColor(0x000000)
                .flags(MaterialFlags.GENERATE_FRAME, PhoenixMaterialFlags.GENERATE_NANITES)
                .formula("PENaq")
                .iconSet(MaterialIconSet.SHINY)
                .buildAndRegister();
    }

    public static void modifyMaterials() {
        if (GTMaterials.Iron.hasProperty(PropertyKey.TOOL)) {
            GTMaterials.Iron.removeProperty(PropertyKey.TOOL);
        }
        GTMaterials.Iron.setProperty(PropertyKey.TOOL,
                (ToolProperty.Builder.of(16, 40, 8192, 6, GTToolType.AXE).magnetic()
                        .unbreakable().build()));
    }
}
