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
import net.phoenix.core.common.data.materials.PhoenixMaterials;
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
        // This calls the IFissionCoolerType getter, which should now perform a safe, deferred lookup.
        Material coolant = coolerType.getRequiredCoolantMaterial();
        Component coolantName = getCoolantName(coolant);

        if (!GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable("block.phoenixcore.fission_cooler.shift"));
            return;
        }
        tooltip.add(Component.translatable("phoenix.fission.coolant_required", coolantName));

        // Note: getCoolantPerTick() now calls getCoolantUsagePerTick()
        tooltip.add(Component.literal("§7Coolant Usage: §f" +
                coolerType.getCoolantPerTick() + " mB/t"));

        // Tooltip is now directly showing the power value (CoolerTemperature)
        tooltip.add(Component.translatable(
                "phoenix.fission.cooling_power",
                coolerType.getCoolerTemperature()));
    }

    /**
     * Corrected Material to Component name lookup for tooltips.
     * Tries FluidStack display name first, then falls back to Material translation key.
     */
    private static Component getCoolantName(Material mat) {
        // 1. Check for invalid/missing material using GTMaterials.NULL
        if (mat == null || mat == GTMaterials.NULL || mat.getName().equals("none"))
            return Component.literal("None");

        try {
            // 2. Try to get the FluidStack's localized display name
            FluidStack stack = mat.getFluid(1);
            if (!stack.isEmpty())
                return stack.getDisplayName();
        } catch (Throwable ignored) {
            // FluidStack creation/lookup failed, proceed to fallback
        }

        // 3. Fallback: Use the Material's translation key for a localized name
        return Component.translatable(mat.getDefaultTranslation());
    }

    public enum fissionCoolerType implements StringRepresentable, IFissionCoolerType {

        // NEW: coolantUsagePerTick (100) replaces flowRate (1.0)
        COOLER_TRUE_HEAT_STABLE(
                "heat_stable_cooler",
                3000, 20, 100, // <-- Arguments changed: 10000 (Temp), 1 (Tier), 100 (Usage)
                "gtceu:distilled_water",
                PhoenixMaterials.PHOENIX_ENRICHED_TRITANIUM,
                phoenixcore.id("block/fission/true_heat_stable_cooler_block")),

        // NEW: coolantUsagePerTick (200) replaces flowRate (2.0)
        COOLER_HIGH_TEMP(
                "high_temp_cooler",
                5000, 21, 200, // <-- Arguments changed: 5000 (Temp), 3 (Tier), 200 (Usage)
                "phoenixcore:quantum_coolant",
                PhoenixMaterials.PHOENIX_ENRICHED_TRITANIUM,
                phoenixcore.id("block/fission/true_heat_stable_cooler_block"));

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
        fissionCoolerType(String name, int temp, int tier, int usage, // Updated argument list
                          String coolantMatId, Material mat, ResourceLocation texture) {
            this.name = name;
            this.coolerTemperature = temp;
            this.tier = tier;
            this.coolantUsagePerTick = usage; // NEW initialization
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

        // REMOVED: @Override
        // public double getCoolantFlowRateMultiplier() {
        // return this.coolantFlowRateMultiplier;
        // }
    }
}
