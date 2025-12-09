package net.phoenix.core.integration.kubejs.builders;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.phoenix.core.api.block.IFissionCoolerType;
import net.phoenix.core.common.block.FissionCoolerBlock;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Accessors(chain = true, fluent = true)
public class FissionCoolerBlockBuilder extends BlockBuilder {

    // Properties exposed to KubeJS
    @Setter
    public transient int coolerTemperature = 1000, tier = 1, coolantUsagePerTick = 10;
    @Setter
    @NotNull
    public transient String requiredCoolantMaterialId = "gtceu:distilled_water";
    @NotNull
    public transient Supplier<Material> material = () -> GTMaterials.NULL;
    @Setter
    public transient String texture = "phoenixcore:block/missing_cooler_texture";

    public FissionCoolerBlockBuilder(ResourceLocation i) {
        super(i);
        // Default properties for a fission block
        noValidSpawns(true);
        renderType("cutout_mipped");
    }

    // Custom setter for the Material supplier
    public FissionCoolerBlockBuilder coolerMaterial(@NotNull Supplier<Material> material) {
        this.material = material;
        return this;
    }

    /**
     * Helper class to hold the type data, implementing IFissionCoolerType.
     * This is the equivalent of the enum entry in the Java code.
     */
    private class KjsCoolerType implements IFissionCoolerType, StringRepresentable {

        private final ResourceLocation textureLocation = new ResourceLocation(texture);

        @Override
        public @NotNull String getSerializedName() {
            return id.getPath();
        }

        @Override
        public @NotNull String getName() {
            return "";
        }

        @Override
        public int getCoolerTemperature() {
            return coolerTemperature;
        }

        @Override
        public int getTier() {
            return tier;
        }

        @Override
        public int getCoolantUsagePerTick() {
            return coolantUsagePerTick;
        }

        @Override
        public @NotNull String getRequiredCoolantMaterialId() {
            return requiredCoolantMaterialId;
        }

        @Override
        public @NotNull Material getMaterial() {
            return material.get();
        }

        @Override
        public @NotNull ResourceLocation getTexture() {
            // Use the texture path set in the KJS script
            return textureLocation;
        }
    }

    @Override
    public Block createObject() {
        // 1. Create the KJS-defined Cooler Type instance
        IFissionCoolerType type = new KjsCoolerType();

        // 2. Create the FissionCoolerBlock instance using the block properties and the type
        FissionCoolerBlock result = new FissionCoolerBlock(this.createProperties(), type);

        // You may need to register the custom type name if your registration logic expects it.
        // phoenixcore.FISSION_COOLER_TYPES.put(type.getName(), () -> result);
        // (Assuming you have a way to register new types in your core mod)

        return result;
    }
}
