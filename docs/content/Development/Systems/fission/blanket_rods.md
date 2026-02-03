---
title: Fission Blanket Rods
---


# How to add new Blanket Rods and explaining how they are done

## Blanket rods provide the core values for the `BreederReactorMachine` class to perform breeder operations. 

They are registered in two parts, an interface named `IFissionBlanketType` and a block class named `FissionBlanketBlock`

```java
package net.phoenix.core.api.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import net.phoenix.core.PhoenixAPI;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

import static net.phoenix.core.common.data.PhoenixMaterialRegistry.getMaterial;

public interface IFissionBlanketType {

    @NotNull
    String getName();

    default int getTintColor() {
        Material m = getMaterial();
        if (m != null && m != GTMaterials.NULL) {
            try {
                return 0xFF000000 | m.getMaterialRGB();
            } catch (Throwable ignored) {}
        }
        return 0xFFFFFFFF;
    }

    int getTier();

    int getDurationTicks();

    int getAmountPerCycle();
    
    @NotNull
    String getInputKey();

    public record BlanketOutput(String key, int weight, int instability) {}

    List<BlanketOutput> getOutputs();
    default String getOutputKey() {
        var outs = getOutputs();
        return outs.isEmpty() ? "" : outs.get(0).key();
    }

    @NotNull
    ResourceLocation getTexture();

    @Nullable
    default Material tryResolveMaterial() {
        Material mat = getMaterial();
        if (mat == null || mat == GTMaterials.NULL) return null;
        return mat;
    }

    Material getMaterial();

    Lazy<IFissionBlanketType[]> ALL_BLANKETS_BY_TIER = Lazy.of(() -> PhoenixAPI.FISSION_BLANKETS.keySet().stream()
            .sorted(Comparator.comparingInt(IFissionBlanketType::getTier))
            .toArray(IFissionBlanketType[]::new));
}
```
This is the class we define/change first. Everything goes through this interface for use in `FissionBlanketBlock` and any other classes using the same logic.
- `getName`
- `getTintColor`
- `getTier`
- `getDurationTicks`
- `getAmountPerCycle`
- `getInputKey`
- `BlanketOutput`
- `getOutputs` and `getOutputKey`
- `getTexture`
- `tryResolveMaterial` 
- `getMaterial`
- Then, finally, we have the api call. ALL_BLANKETS_BY_TIER is passed to be stored by the PhoenixAPI class. This allows us to pass every class using this interface to the predicate.

```java

package net.phoenix.core.common.block;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.phoenix.core.api.block.IFissionBlanketType;
import net.phoenix.core.api.block.IFissionBlanketType.BlanketOutput;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@ParametersAreNonnullByDefault
public class FissionBlanketBlock extends ActiveBlock {

    /** Needed for tinting + introspection */
    private final IFissionBlanketType blanketType;

    public FissionBlanketBlock(Properties properties, IFissionBlanketType blanketType) {
        super(properties);
        this.blanketType = blanketType;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (!GTUtil.isShiftDown()) {
            tooltip.add(Component.literal("Hold §fShift§7 for details")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        Component inputName = FissionFuelRodBlock.getRegistryDisplayName(blanketType.getInputKey());
        tooltip.add(Component.translatable("phoenix.fission.blanket_input", inputName)
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        tooltip.add(Component.translatable("phoenix.fission.blanket_outputs")
                .withStyle(ChatFormatting.GOLD));

        List<BlanketOutput> outs = blanketType.getOutputs();
        if (outs == null || outs.isEmpty()) {
            tooltip.add(Component.literal("• (none)")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            int shown = 0;
            for (BlanketOutput o : outs) {
                if (o == null) continue;
                if (shown++ >= 5) break;

                Component outName = FissionFuelRodBlock.getRegistryDisplayName(o.key());
                tooltip.add(Component.literal("• ")
                        .append(outName)
                        .append(Component.literal("  w=" + o.weight() + "  inst=" + o.instability())
                                .withStyle(ChatFormatting.DARK_GRAY))
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        double seconds = blanketType.getDurationTicks() / 20.0;
        tooltip.add(Component.translatable(
                        "phoenix.fission.blanket_cycle",
                        Component.literal(String.valueOf(blanketType.getAmountPerCycle()))
                                .withStyle(ChatFormatting.WHITE),
                        Component.literal(String.format("%.2f", seconds))
                                .withStyle(ChatFormatting.GOLD))
                .withStyle(ChatFormatting.GRAY));
    }

    public enum BreederBlanketTypes implements StringRepresentable, IFissionBlanketType {

        PLUTONIUM_BREEDER("plutonium_breeder",
                2, 2400, 1,
                "gtceu:uranium_238_nugget",
                List.of(
                        new BlanketOutput("gtceu:plutonium_nugget", 70, 1),
                        new BlanketOutput("gtceu:plutonium_241_nugget", 20, 3),
                        new BlanketOutput("gtceu:plutonium_238_nugget", 10, 4)
                ),
                0xFFB07CFF),

        THORIUM_BREEDER("thorium_breeder",
                1, 240, 1,
                "gtceu:uranium_235_nugget",
                List.of(
                        new BlanketOutput("gtceu:plutonium_nugget", 85, 1),
                        new BlanketOutput("gtceu:plutonium_241_nugget", 15, 3)
                ),
                0xFFFFD27D);

        @Getter
        @NotNull
        private final String name;
        @Getter
        private final int tier;
        @Getter
        private final int durationTicks;
        @Getter
        private final int amountPerCycle;
        @Getter
        @NotNull
        private final String inputKey;

        @Getter
        @NotNull
        private final List<BlanketOutput> outputs;

        @Getter
        @NotNull
        private final ResourceLocation texture;
        
        @Getter
        private final int tintColor;

        BreederBlanketTypes(String name, int tier, int duration, int amount,
                            String in, List<BlanketOutput> outs, int tintColor) {
            this.name = name;
            this.tier = tier;
            this.durationTicks = duration;
            this.amountPerCycle = amount;
            this.inputKey = in;
            this.outputs = outs;
            this.texture = new ResourceLocation("phoenix_fission", "block/blanket/" + name);
            this.tintColor = tintColor;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }

        @Override
        public int getTintColor() {
            return tintColor;
        }

        @Override
        public Material getMaterial() {
            return GTMaterials.NULL;
        }
    }
}
```

