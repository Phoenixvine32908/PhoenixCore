package net.phoenix.core.common.data;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.HashMap;
import java.util.Map;

public class PhoenixMaterialRegistry {

    private static final Map<Fluid, Material> FLUID_TO_MATERIAL = new HashMap<>();

    public static void register(Material material) {
        Fluid fluid = material.getFluid();
        if (fluid != null && fluid != Fluids.EMPTY) {
            FLUID_TO_MATERIAL.put(fluid, material);
        }
    }

    public static Material getMaterial(Fluid fluid) {
        return FLUID_TO_MATERIAL.get(fluid);
    }
}
