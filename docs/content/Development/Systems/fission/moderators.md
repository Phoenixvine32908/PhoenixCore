---
title: Fission Moderators
---


# How to add new Moderators and explaining how they are defined.

## The Basics
Moderators change or modify the values provided by fuel rods. You do not need to have any of these for a reactor to run, but they are recommended. 

If you are a kubejs dev who merely needs to know how to add new `Moderators`, only the first two sections of this page will be useful.

```js
StartupEvents.registry("block", event => {
    event.create('niobium_modified_silicon_carbide_moderator', 'phoenixcore:fission_moderator')
    .displayName('Nb-SiC Moderator')
    .EUBoost(15)      // Handles how much each moderator boosts eu generation.
    .fuelDiscount(5)  // Improves efficiency of fuels by extending time between each use. 
    .tier(4) // Used for explosion scaling + primary-moderator selection.
    .moderatorMaterial(() => GTMaterials.get('niobium_modified_silicon_carbide')) // Used for some internal names.
    .texture('kubejs:block/fission/niobium_sic_moderator');
    // Moderators don’t require an active texture.
});
```

## Understanding how to actually use the moderators

These blocks are designed to comment the breeder rods as stated above. It is best to design moderators around certain fuel rods, rather than fuel rods around moderators.

This is where non-linear paths really shine. Unlike the other components, moderator values are not tied togethor. You can mix and match any values here. 

For example, you could make a moderator that gives a big eu boost but adds so little fuel discount that it isn't really worth it. 

Or you could give one a big tier, adding a lot more parallel and risk in the explosion, and has good fuel discount but a quite bad eu boost.

## Understanding the classes

Now, back to talking about blanket rod blocks.

They are registered in two parts, an interface named `IFissionBlanketType` and a block class named `FissionBlanketBlock`.

```java
package net.phoenix.core.api.block;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import net.phoenix.core.PhoenixAPI;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;

public interface IFissionModeratorType {

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

    int getEUBoost();

    int getFuelDiscount();

    default double getHeatMultiplier() {
        return getTier() * 0.5;
    }

    /**
     * How many extra parallel channels this moderator enables.
     */
    default int getParallelBonus() {
        return getTier();
    }

    Material getMaterial();

    int getTier();

    ResourceLocation getTexture();

    Lazy<IFissionModeratorType[]> ALL_FISSION_MODERATORS_SORTED = Lazy
            .of(() -> PhoenixAPI.FISSION_MODERATORS.keySet().stream()
                    .sorted(Comparator.comparingInt(IFissionModeratorType::getFuelDiscount))
                    .toArray(IFissionModeratorType[]::new));

    @Nullable
    static IFissionModeratorType getMinRequiredType(int requiredTemperature) {
        return Arrays.stream(ALL_FISSION_MODERATORS_SORTED.get())
                .filter(moderatorType -> moderatorType.getFuelDiscount() >= requiredTemperature)
                .findFirst().orElse(null);
    }
}
```
This is the class we define/change first. Everything goes through this interface for use in `FissionBlanketBlock` and any other classes using the same logic.

- `getName`. This controls the registry name of the `Breeder Block`.
- `getTintColor`. Correctly not working attempt at `auto tinting` blocks, one day it will be real.
- `getTier`. This controls the `Tier` of the `Breeder Block`.
- `getDurationTicks`. This controls the blanket fuel's use in `ticks`.
- `getAmountPerCycle`. This controls the blanket fuel's use per `duration ticks` cycle.
- `getInputKey`. This controls the `blanket fuel` itself.
- `BlanketOutput`. This controls the list of outputs.
- `getOutputs`. Works togethor with `BlanketOutput` as backwards compat.
- `getTexture`. Controls the texture used by the `Breeder Block`.
- `tryResolveMaterial`. Tries to resolve the forge registry fuel as a gtm material.
- `getMaterial`. Controls the gtm material linked to it, used for some internal names.

Then, finally, we have the api call. `ALL_BLANKETS_BY_TIER` is passed to be stored by the PhoenixAPI class. This allows us to pass every class using this interface to the predicate.

```java
package net.phoenix.core.common.block;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.phoenix.core.PhoenixFission;
import net.phoenix.core.api.block.IFissionModeratorType;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@ParametersAreNonnullByDefault
public class FissionModeratorBlock extends ActiveBlock {

    /** Needed for tinting + introspection */
    private final IFissionModeratorType moderatorType;

    public FissionModeratorBlock(Properties properties, IFissionModeratorType moderatorType) {
        super(properties);
        this.moderatorType = moderatorType;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (!GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable("block.phoenix_fission.fission_moderator.shift")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.translatable("block.phoenix_fission.fission_moderator.info_header"));

        tooltip.add(Component.translatable("block.phoenix_fission.fission_moderator.boost",
                moderatorType.getEUBoost()));

        tooltip.add(Component.translatable("block.phoenix_fission.fission_moderator.fuel_discount",
                moderatorType.getFuelDiscount()));
    }

    public enum FissionModeratorTypes implements StringRepresentable, IFissionModeratorType {

        MODERATOR_GRAPHITE(
                "graphite_moderator",
                1, 3, 1,
                PhoenixFission.id("block/fission/graphite_moderator"),
                0xFFB07CFF);

        @Getter
        @NotNull
        private final String name;
        @Getter
        private final int EUBoost;
        @Getter
        private final int fuelDiscount;
        @Getter
        private final int tier;
        @Getter
        @NotNull
        private final ResourceLocation texture;

        /** Per-type tint (ARGB) */
        @Getter
        private final int tintColor;

        FissionModeratorTypes(String name, int EUBoost, int fuelDiscount, int tier,
                              ResourceLocation texture, int tintColor) {
            this.name = name;
            this.EUBoost = EUBoost;
            this.fuelDiscount = fuelDiscount;
            this.tier = tier;
            this.texture = texture;
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
        public com.gregtechceu.gtceu.api.data.chemical.material.Material getMaterial() {
            return com.gregtechceu.gtceu.common.data.GTMaterials.NULL;
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





