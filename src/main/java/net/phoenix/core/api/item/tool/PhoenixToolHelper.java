package net.phoenix.core.api.item.tool;

import net.minecraft.world.item.ItemStack;
import net.phoenix.core.common.data.PhoenixItems;

import java.util.function.Supplier;

public class PhoenixToolHelper {
    // Suppliers for broken tool stacks
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_LUV = () -> PhoenixItems.POWER_UNIT_LUV.get()
            .getDefaultInstance();


}