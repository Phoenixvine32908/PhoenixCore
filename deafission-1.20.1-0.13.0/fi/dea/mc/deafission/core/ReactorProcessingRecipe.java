//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.core;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public record ReactorProcessingRecipe(Item[] input, @Nullable ItemStack output, @Nullable FluidStack fluidOutput, int minHeat, int ticks, double heatPerTick) {
}
