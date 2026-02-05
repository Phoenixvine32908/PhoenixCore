---
title: Fission Coolers
---


# How to add new Coolers and explaining how they are done

## The Basics

Coolers provide cooling power for the use of running reactors. Each cooler uses a different coolant and provides a different cooling power.

If you are a kubejs dev who merely needs to know how to add new `Coolers`, only the first two sections of this page will be useful. 

```js
StartupEvents.registry("block", event => {
    event.create('aether_flow_cooler', 'phoenixcore:fission_cooler')
        .displayName('Aether-Flow Cooler') 
        .coolerTemperature(1025)
        // Cooling power given per cooler (HU/t). If total cooling doesn’t meet reactor needs,
        // core heat rises and meltdown logic will eventually trigger.
        .coolantUsagePerTick(10) // The amount of coolant this cooler uses per tick. Additive logic is a config value.
        .tier(3) // Used for explosion scaling + progression/tier logic.
        .requiredCoolantMaterialId('phoenixcore:frost') // The "cold" coolant from running reactors, only the primary cooler defines this.
        .outputCoolantFluidId('phoenixcore:warm_frost') // The "hot" coolant from running reactors, only the primary cooler defines this.
        .coolerMaterial(() => GTMaterials.get('tungsten_steel'))
        .texture('kubejs:block/fission/aether_flow_cooler'); // Also needs a texture the same name with _active appended to the end.
});
```

## Understanding how to actually use the coolers

The output coolant will be given if the multiblock reactor has a output hatch. 
The input coolant is still necessary for the `Cooler` to provide cooling power. 

Any amount of `Coolers` will provide cooling power in an additive form.
This also includes different tiers of `Coolers`. They look for coolant inside input hatches and me input hatches, without said coolant they will not provide any cooling power.

## Understanding the classes

Now, back to talking about blanket rod blocks.

They are registered in two parts, an interface named `IFissionBlanketType` and a block class named `FissionBlanketBlock`.

```java
package net.phoenix.core.api.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import net.phoenix.core.PhoenixAPI;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;

public interface IFissionCoolerType {

    @NotNull
    String getName();

    int getTier();

    int getCoolerTemperature();

    @NotNull
    String getRequiredCoolantMaterialId();

    @NotNull
    default String getOutputCoolantFluidId() {
        return getRequiredCoolantMaterialId();
    }

    @NotNull
    default String getInputCoolantFluidId() {
        return getRequiredCoolantMaterialId();
    }
    
    int getCoolantUsagePerTick();

    default int getCoolantPerTick() {
        return getCoolantUsagePerTick();
    }

    default int getTintColor() {
        return 0xFFFFFFFF;
    }

    Material getMaterial();

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

```
This is the class we define/change first. Everything goes through this interface for use in `FissionBlanketBlock` and any other classes using the same logic.

- `getName`. This controls the registry name of the `Cooler Block`.
- `getTier`. This controls the `Tier` of the `Cooler Block`.
- `getCoolerTemperature`.
- `getDurationTicks`. This controls the blanket fuel's use in `ticks`.
- `getAmountPerCycle`. This controls the blanket fuel's use per `duration ticks` cycle.
- `getInputKey`. This controls the `blanket fuel` itself.
- `BlanketOutput`. This controls the list of outputs.
- `getOutputs`. Works togethor with `BlanketOutput` as backwards compat.
- `getTexture`. Controls the texture used by the `Breeder Block`.
- `tryResolveMaterial`. Tries to resolve the forge registry fuel as a gtm material.
- `getMaterial`. Controls the material linked to it, used for some internal names.

Then, finally, we have the api call. `ALL_COOLERS_BY_TIER` is passed to be stored by the PhoenixAPI class. This allows us to pass every class using this interface to the predicate.

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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.core.PhoenixFission;
import net.phoenix.core.api.block.IFissionCoolerType;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@ParametersAreNonnullByDefault
public class FissionCoolerBlock extends ActiveBlock {

    /** Needed for tinting + introspection */
    private final IFissionCoolerType coolerType;

    public FissionCoolerBlock(Properties props, IFissionCoolerType type) {
        super(props);
        this.coolerType = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (!GTUtil.isShiftDown()) {
            tooltip.add(Component.literal("Hold §fShift§7 for details")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        String inId = coolerType.getInputCoolantFluidId();
        Component inName = getFluidDisplayName(inId);
        tooltip.add(Component.translatable("phoenix.fission.coolant_required", inName));

        String outId = coolerType.getOutputCoolantFluidId();
        if (!outId.equalsIgnoreCase(inId)) {
            Component outName = getFluidDisplayName(outId);
            tooltip.add(Component.translatable("phoenix.fission.coolant_output", outName));
        }

        tooltip.add(Component.literal("§7Coolant Usage: §f" +
                coolerType.getCoolantUsagePerTick() + " mB/t"));

        tooltip.add(Component.translatable("phoenix.fission.cooling_power",
                coolerType.getCoolerTemperature()));
    }

    /** Registry-only fluid display name (no Material/GTMaterials). */
    public static Component getFluidDisplayName(@NotNull String fluidId) {
        if (fluidId.isEmpty() || "none".equalsIgnoreCase(fluidId)) {
            return Component.literal("None").withStyle(ChatFormatting.GRAY);
        }

        ResourceLocation rl = ResourceLocation.tryParse(fluidId);
        if (rl == null) return Component.literal(fluidId).withStyle(ChatFormatting.YELLOW);

        Fluid f = ForgeRegistries.FLUIDS.getValue(rl);
        if (f != null && f != Fluids.EMPTY) {
            return Component.translatable(f.getFluidType().getDescriptionId());
        }

        return Component.literal(fluidId).withStyle(ChatFormatting.YELLOW);
    }

    public enum FissionCoolerTypes implements StringRepresentable, IFissionCoolerType {

        COOLER_BASIC(
                "basic_cooler",
                50500, 1, 10,
                "gtceu:distilled_water",
                "gtceu:steam",
                PhoenixFission.id("block/fission/basic_cooler_block"),
                0xFF7DE7FF);

        @Getter
        @NotNull
        private final String name;
        @Getter
        private final int coolerTemperature;
        @Getter
        private final int tier;
        @Getter
        private final int coolantUsagePerTick;

        /** INPUT fluid registry id */
        @Getter
        @NotNull
        private final String requiredCoolantMaterialId;

        /** OUTPUT fluid registry id (hot return) */
        @Getter
        @NotNull
        private final String outputCoolantFluidId;

        @Getter
        @NotNull
        private final ResourceLocation texture;

        /** Per-type tint (ARGB) */
        @Getter
        private final int tintColor;

        FissionCoolerTypes(String name, int temp, int tier, int usage,
                           String inputCoolantFluidId, String outputCoolantFluidId,
                           ResourceLocation texture, int tintColor) {
            this.name = name;
            this.coolerTemperature = temp;
            this.tier = tier;
            this.coolantUsagePerTick = usage;
            this.requiredCoolantMaterialId = inputCoolantFluidId;
            this.outputCoolantFluidId = outputCoolantFluidId;
            this.texture = texture;
            this.tintColor = tintColor;
        }

        @Override
        public int getCoolantUsagePerTick() {
            return this.coolantUsagePerTick;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }

        /**
         * Legacy name: now treated as INPUT coolant fluid id.
         */
        @Override
        public @NotNull String getRequiredCoolantMaterialId() {
            return this.requiredCoolantMaterialId;
        }

        @Override
        public @NotNull String getOutputCoolantFluidId() {
            return this.outputCoolantFluidId;
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
For completeness, we will assume you already know how to make active blocks in the gtm api. So we will focus on what makes this actually unique.

Tooltips

- `Shift` to show the full info, helps to keep inventory cleaner.
- The `fuel` used and the list of output `fuels`.
- How often/how much of the input `fuel` is used.

Values

- `String name`. A `String` designed to hold the registration name of the `Breeder Block`, still needs to follow a-z, 0-9.
- `int tier`. An `int` designed to hold the `tier` of the `Breeder Block`, handles `primary blanket` logic in reactor.
- `int duration`. An `int` designed to hold the full duration in `ticks` of `fuel` use.
- `int amount`. An `int` designed to hold the amount of `fuel` used per cycle.
- `String in`. A `String` designed to handle one fully realized `Forge ID`.
- `List BlanketOutputs`. A `List` of all the `blanket outputs` and their `weight/instability` fields.
- `int tintColor`. Currently, doesn't do anything, just put white.







