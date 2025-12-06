//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.core;

import java.util.Arrays;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public record ReactorCoolantRecipe(ResourceLocation id, @Unmodifiable Item[] validFuels, FluidStack inputCoolant, double coolantHeat, @Unmodifiable FluidStack[] output, int divisor) {
    public boolean isValidFuel(@Nullable ItemStack is) {
        return is != null && Arrays.stream(this.validFuels()).anyMatch((f) -> !is.m_41619_() && is.m_150930_(f));
    }
}
