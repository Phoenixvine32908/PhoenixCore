package net.phoenix.core.common.block;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.phoenix.core.api.block.IFissionModeratorType;
import net.phoenix.core.phoenixcore;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FissionModeratorBlock extends ActiveBlock {

    private final IFissionModeratorType moderatorType;

    public FissionModeratorBlock(Properties properties, IFissionModeratorType moderatorType) {
        super(properties);
        this.moderatorType = moderatorType;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        if (!GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable("block.phoenixcore.fission_moderator.shift"));
            return;
        }

        tooltip.add(Component.translatable("block.phoenixcore.fission_moderator.info_header"));

        tooltip.add(Component.translatable("block.phoenixcore.fission_moderator.boost",
                this.moderatorType.getEUBoost()));

        tooltip.add(Component.translatable("block.phoenixcore.fission_moderator.fuel_discount",
                this.moderatorType.getFuelDiscount()));
    }

    public enum fissionModeratorType implements StringRepresentable, IFissionModeratorType {

        MODERATOR_GRAPHITE("graphite_moderator", 1, 3, 1, GTMaterials.Graphite,
                phoenixcore.id("block/fission/graphite_moderator"));

        @NotNull
        @Getter
        private final String name;
        @Getter
        private final int EUBoost;
        @Getter
        private final int fuelDiscount;
        @Getter
        private final int tier;
        @NotNull
        @Getter
        private final Material material;
        @NotNull
        @Getter
        private final ResourceLocation texture;

        fissionModeratorType(String name, int EUBoost, int fuelDiscount, int tier, Material material,
                             ResourceLocation texture) {
            this.name = name;
            this.EUBoost = EUBoost;
            this.fuelDiscount = fuelDiscount;
            this.tier = tier;
            this.material = material;
            this.texture = texture;
        }

        @NotNull
        @Override
        public String toString() {
            return getName();
        }

        @Override
        @NotNull
        public String getSerializedName() {
            return name;
        }
    }
}
