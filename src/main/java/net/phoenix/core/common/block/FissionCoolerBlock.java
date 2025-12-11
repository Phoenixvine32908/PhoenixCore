package net.phoenix.core.common.block;

// ... (imports remain the same)
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
import net.minecraftforge.fluids.FluidStack;
import net.phoenix.core.api.block.IFissionCoolerType;
import net.phoenix.core.phoenixcore;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FissionCoolerBlock extends ActiveBlock {

    private final IFissionCoolerType coolerType;

    public FissionCoolerBlock(Properties props, IFissionCoolerType type) {
        super(props);
        this.coolerType = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level,
                                List<Component> tooltip, TooltipFlag flag) {
        Material coolant = coolerType.getRequiredCoolantMaterial();
        Component coolantName = getCoolantName(coolant);

        if (!GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable("block.phoenixcore.fission_cooler.shift"));
            return;
        }
        tooltip.add(Component.translatable("phoenix.fission.coolant_required", coolantName));

        tooltip.add(Component.literal("§7Coolant Usage: §f" +
                coolerType.getCoolantPerTick() + " mB/t"));


        tooltip.add(Component.translatable(
                "phoenix.fission.cooling_power",
                coolerType.getCoolerTemperature()));
    }


    private static Component getCoolantName(Material mat) {
        if (mat == null || mat == GTMaterials.NULL || mat.getName().equals("none"))
            return Component.literal("None");

        try {
            FluidStack stack = mat.getFluid(1);
            if (!stack.isEmpty())
                return stack.getDisplayName();
        } catch (Throwable ignored) {
        }

        return Component.translatable(mat.getDefaultTranslation());
    }

    public enum fissionCoolerType implements StringRepresentable, IFissionCoolerType {

        COOLER_BASIC(
                "basic_cooler",
                105, 1, 100,
                "gtceu:distilled_water",
                GTMaterials.Steel,
                phoenixcore.id("block/fission/basic_cooler_block"));

        @Getter
        @NotNull
        private final String name;
        @Getter
        private final int coolerTemperature;
        @Getter
        private final int tier;
        @Getter
        private final int coolantUsagePerTick; // NEW FIELD

        @Getter
        @NotNull
        private final String requiredCoolantMaterialId;
        @Getter
        @NotNull
        private final Material material;
        @Getter
        @NotNull
        private final ResourceLocation texture;

        // NEW: Constructor arguments updated
        fissionCoolerType(String name, int temp, int tier, int usage,
                          String coolantMatId, Material mat, ResourceLocation texture) {
            this.name = name;
            this.coolerTemperature = temp;
            this.tier = tier;
            this.coolantUsagePerTick = usage;
            this.requiredCoolantMaterialId = coolantMatId;
            this.material = mat;
            this.texture = texture;
        }

        @Override
        public int getCoolantUsagePerTick() { // NEW IMPLEMENTATION
            return this.coolantUsagePerTick;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }

        @Override
        public @NotNull String getRequiredCoolantMaterialId() {
            return this.requiredCoolantMaterialId;
        }


    }
}
