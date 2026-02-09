package net.phoenix.core.common.data.recipe.generated;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import net.phoenix.core.common.data.materials.PhoenixMaterialFlags;

public class BeePrefixHelper {

    public static void addTierOneBeeFlag(Material material) {
        if (material == null || material.isNull()) {
            return;
        }

        material.addFlags(PhoenixMaterialFlags.GENERATE_CRYSTAL_ROSE);
    }

    public static void addTierOneBeeFlag(Material... materials) {
        if (materials == null) return;
        for (Material m : materials) {
            addTierOneBeeFlag(m);
        }
    }
}
