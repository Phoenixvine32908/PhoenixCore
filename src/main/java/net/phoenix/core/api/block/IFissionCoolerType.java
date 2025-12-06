package net.phoenix.core.api.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import net.phoenix.core.PhoenixAPI;
import net.phoenix.core.common.data.PhoenixMaterialRegistry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;

public interface IFissionCoolerType {

    @NotNull
    String getName();

    /**
     * This method now represents the effective cooling power provided by the cooler.
     * It is used to satisfy the recipe's 'required_cooling' data.
     */
    int getCoolerTemperature();

    @NotNull
    String getRequiredCoolantMaterialId();

    @NotNull
    default Material getRequiredCoolantMaterial() {
        String id = getRequiredCoolantMaterialId();

        // 1. Use the correct GTCEu API for material lookup by ID string
        Material resolvedMat = GTMaterials.get(id);

        // 2. Fallback to your custom Phoenix registry if the GT lookup returns NULL
        if (resolvedMat == null || resolvedMat == GTMaterials.NULL) {
            // Assuming PhoenixMaterialRegistry.getMaterial(String id) exists
            resolvedMat = PhoenixMaterialRegistry.getMaterial(id);
        }

        // 3. Return Final Result
        return resolvedMat != null ? resolvedMat : GTMaterials.NULL;
    }

    /**
     * The fixed amount of coolant consumed per tick (mB/t).
     */
    int getCoolantUsagePerTick(); // NEW ABSTRACT METHOD

    default int getCoolantPerTick() {
        // Now directly returns the defined usage value
        return getCoolantUsagePerTick();
    }

    // REMOVED: double getCoolantFlowRateMultiplier();

    Material getMaterial();

    int getTier();

    ResourceLocation getTexture();

    // ... (rest of the interface remains the same)
    Lazy<IFissionCoolerType[]> ALL_COOLER_TEMPERATURES_SORTED = Lazy
            .of(() -> PhoenixAPI.FISSION_COOLERS.keySet().stream()
                    .sorted(Comparator.comparingInt(IFissionCoolerType::getCoolerTemperature))
                    .toArray(IFissionCoolerType[]::new));

    @Nullable
    static IFissionCoolerType getMinRequiredType(int requiredTemperature) {
        return Arrays.stream(ALL_COOLER_TEMPERATURES_SORTED.get())
                .filter(cooler -> cooler.getCoolerTemperature() >= requiredTemperature)
                .findFirst()
                .orElse(null);
    }
}
