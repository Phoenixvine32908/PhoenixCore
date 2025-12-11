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

    int getCoolerTemperature();

    @NotNull
    String getRequiredCoolantMaterialId();

    @NotNull
    default Material getRequiredCoolantMaterial() {
        String id = getRequiredCoolantMaterialId();

        Material resolvedMat = GTMaterials.get(id);

        if (resolvedMat == null || resolvedMat == GTMaterials.NULL) {
            resolvedMat = PhoenixMaterialRegistry.getMaterial(id);
        }

        return resolvedMat != null ? resolvedMat : GTMaterials.NULL;
    }

    int getCoolantUsagePerTick(); // NEW ABSTRACT METHOD

    default int getCoolantPerTick() {
        return getCoolantUsagePerTick();
    }


    Material getMaterial();

    int getTier();

    ResourceLocation getTexture();

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
