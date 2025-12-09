package net.phoenix.core.integration.kubejs.builders;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.phoenix.core.api.block.IFissionModeratorType;
import net.phoenix.core.common.block.FissionModeratorBlock;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Accessors(chain = true, fluent = true)
public class FissionModeratorBlockBuilder extends BlockBuilder {

    // Properties exposed to KubeJS
    @Setter
    public transient int EUBoost = 1, fuelDiscount = 1, tier = 1;
    @NotNull
    public transient Supplier<Material> material = () -> GTMaterials.NULL;
    @Setter
    public transient String texture = "phoenixcore:block/missing_moderator_texture";

    public FissionModeratorBlockBuilder(ResourceLocation i) {
        super(i);
        // Default properties for a fission block
        noValidSpawns(true);
        renderType("cutout_mipped");
    }

    // Custom setter for the Material supplier
    public FissionModeratorBlockBuilder moderatorMaterial(@NotNull Supplier<Material> material) {
        this.material = material;
        return this;
    }

    /**
     * Helper class to hold the type data, implementing IFissionModeratorType.
     * This is the equivalent of the enum entry in the Java code.
     */
    private class KjsModeratorType implements IFissionModeratorType, StringRepresentable {

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
        public int getEUBoost() {
            return EUBoost;
        }

        @Override
        public int getFuelDiscount() {
            return fuelDiscount;
        }

        @Override
        public int getTier() {
            return tier;
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
        // 1. Create the KJS-defined Moderator Type instance
        IFissionModeratorType type = new KjsModeratorType();

        // 2. Create the FissionModeratorBlock instance using the block properties and the type
        FissionModeratorBlock result = new FissionModeratorBlock(this.createProperties(), type);

        return result;
    }
}
