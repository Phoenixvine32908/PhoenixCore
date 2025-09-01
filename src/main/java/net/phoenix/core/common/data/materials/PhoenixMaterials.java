package net.phoenix.core.common.data.materials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.FluidState;

import net.phoenix.core.PhoenixCore;

public class PhoenixMaterials {

    public static Material QuantumCoolant;
    public static Material ExtremelyModifiedSpaceGradeSteel;
    public static Material EightyFivePercentPureNevvonianSteel;
    public static Material PHOENIX_ENRICHED_TRITANIUM;

    public static void register() {
        QuantumCoolant = new Material.Builder(PhoenixCore.id("quantum_coolant"))
                .liquid(new FluidBuilder().state(FluidState.LIQUID).temperature(0))
                .color(0x0040ef).secondaryColor(0x0030cf)
                .buildAndRegister();
        ExtremelyModifiedSpaceGradeSteel = new Material.Builder(PhoenixCore.id("extremely_modified_space_grade_steel"))
                .ingot()
                .color(0xad6161)
                .secondaryColor(0x593856)
                .cableProperties(GTValues.V[GTValues.LuV], 64, 0, true)
                .buildAndRegister();
        EightyFivePercentPureNevvonianSteel = new Material.Builder(
                PhoenixCore.id("eighty_five_percent_pure_nevvonian_steel"))
                .ingot()
                .element(PhoenixElements.APNS)
                .formula("APNS")
                .secondaryColor(593856)
                .iconSet(PhoenixMaterialSet.ALMOST_PURE_NEVONIAN_STEEL)
                .buildAndRegister();
        PHOENIX_ENRICHED_TRITANIUM = new Material.Builder(
                PhoenixCore.id("phoenix_enriched_tritanium"))
                .ingot()
                .color(0xFF0000)
                .secondaryColor(0x840707)
                .formula("PET")
                .iconSet(PhoenixMaterialSet.ALMOST_PURE_NEVONIAN_STEEL)
                .buildAndRegister();
    }
}
