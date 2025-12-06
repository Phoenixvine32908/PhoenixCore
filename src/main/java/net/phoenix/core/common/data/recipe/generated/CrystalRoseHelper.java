package net.phoenix.core.common.data.recipe.generated;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.phoenix.core.common.data.materials.PhoenixMaterialFlags;

public class CrystalRoseHelper {

    /** Adds the crystal rose generation flag to a material safely. */
    public static void addCrystalRoseFlag(Material material) {
        if (material == null || material.isNull()) {
            // Optional: log
            // PhoenixCore.LOGGER.warn("[Crystal Rose] Tried to assign flag to null material");
            return;
        }

        material.addFlags(PhoenixMaterialFlags.GENERATE_CRYSTAL_ROSE);
    }

    /** Convenience: add multiple materials in one call */
    public static void addCrystalRoseFlags(Material... materials) {
        if (materials == null) return;
        for (Material m : materials) {
            addCrystalRoseFlag(m);
        }
    }
}
