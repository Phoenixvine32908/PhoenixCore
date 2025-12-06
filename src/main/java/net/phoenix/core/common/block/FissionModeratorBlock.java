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

    // 🌟 FIX: Changed from a static field (DEFAULT_MODERATOR_TYPE)
    // to a non-static, final instance field. This ensures each block
    // instance holds its own correct data.
    private final IFissionModeratorType moderatorType;

    // The static field can be removed, or renamed to something
    // more descriptive if it is absolutely needed elsewhere.
    // For now, we will assume it's not needed and remove the problematic line.

    public FissionModeratorBlock(Properties properties, IFissionModeratorType moderatorType) {
        super(properties);
        // 🌟 FIX: Assign the passed-in type to the instance field.
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

        // 🌟 FIX: Use the instance field (this.moderatorType) to retrieve the correct stats.
        tooltip.add(Component.translatable("block.phoenixcore.fission_moderator.boost",
                this.moderatorType.getEUBoost()));

        tooltip.add(Component.translatable("block.phoenixcore.fission_moderator.fuel_discount",
                this.moderatorType.getFuelDiscount()));
    }

    // The enum definition remains the same as it correctly defines the data for each type.
    public enum fissionModeratorType implements StringRepresentable, IFissionModeratorType {

        MODERATOR_GRAPHITE("graphite_moderator", 5, 5, 5, GTMaterials.Graphite,
                phoenixcore.id("block/fission/graphite_moderator")),
        MODERATOR_BERYLLIUM("beryllium_moderator", 17, 50, 3, GTMaterials.Beryllium,
                phoenixcore.id("block/fission/beryllium_moderator"));

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
