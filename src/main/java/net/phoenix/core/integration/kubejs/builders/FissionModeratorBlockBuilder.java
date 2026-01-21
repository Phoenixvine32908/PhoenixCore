package net.phoenix.core.integration.kubejs.builders;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.phoenix.core.PhoenixAPI;
import net.phoenix.core.api.block.IFissionModeratorType;
import net.phoenix.core.common.block.FissionModeratorBlock;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Accessors(chain = true, fluent = true)
public class FissionModeratorBlockBuilder extends BlockBuilder {

    @Setter
    public transient int EUBoost = 1, fuelDiscount = 1, tier = 1;
    @NotNull
    public transient Supplier<Material> material = () -> GTMaterials.NULL;
    @Setter
    public transient String texture = "PhoenixCore:block/missing_moderator_texture";

    public FissionModeratorBlockBuilder(ResourceLocation i) {
        super(i);
        noValidSpawns(true);
        renderType("cutout_mipped");
    }

    public FissionModeratorBlockBuilder moderatorMaterial(@NotNull Supplier<Material> material) {
        this.material = material;
        return this;
    }

    private class KjsModeratorType implements IFissionModeratorType, StringRepresentable {

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
            return textureLocation;
        }
    }

    @Override
    public Block createObject() {
        IFissionModeratorType type = new KjsModeratorType();

        FissionModeratorBlock result = new FissionModeratorBlock(this.createProperties(), type);

        PhoenixAPI.FISSION_MODERATORS.put(type, () -> result);

        return result;
    }
}
