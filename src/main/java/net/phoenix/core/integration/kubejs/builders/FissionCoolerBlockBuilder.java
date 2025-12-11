package net.phoenix.core.integration.kubejs.builders;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.phoenix.core.PhoenixAPI;
import net.phoenix.core.api.block.IFissionCoolerType;
import net.phoenix.core.common.block.FissionCoolerBlock;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Accessors(chain = true, fluent = true)
public class FissionCoolerBlockBuilder extends BlockBuilder {

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
        noValidSpawns(true);
        renderType("cutout_mipped");
    }

    public FissionCoolerBlockBuilder coolerMaterial(@NotNull Supplier<Material> material) {
        this.material = material;
        return this;
    }



    private class KjsCoolerType implements IFissionCoolerType, StringRepresentable {

        private final ResourceLocation textureLocation = new ResourceLocation(texture);

        @Override
        public @NotNull String getSerializedName() {
            return id.getPath();
        }

        @Override
        public @NotNull String getName() {
            return id.getPath();
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
        IFissionCoolerType type = new KjsCoolerType();

        FissionCoolerBlock result = new FissionCoolerBlock(this.createProperties(), type);

        PhoenixAPI.FISSION_COOLERS.put(type, () -> result);

        return result;
    }
}
