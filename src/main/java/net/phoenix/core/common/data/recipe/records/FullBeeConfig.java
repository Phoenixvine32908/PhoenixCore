package net.phoenix.core.common.data.recipe.records;

public record FullBeeConfig(
                            String beeId,
                            String pollinationInputId,
                            String finalOutputId,
                            int outputCount,             // 4
                            int decantingEut,            // 5
                            int decantingDuration,       // 6
                            int waxEut,                  // 7
                            int waxDuration,             // 8
                            int fluidEut,                // 9
                            int fluidDuration,           // 10
                            int boostedDecantingEut,     // 11
                            int boostedDecantingDuration,// 12
                            int boostedWaxEut,           // 13
                            int boostedWaxDuration,      // 14
                            int boostedFluidEut,         // 15
                            int boostedFluidDuration     // 16
) {

    public net.minecraft.world.item.ItemStack finalOutputItem() {
        var item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                new net.minecraft.resources.ResourceLocation(finalOutputId));
        return new net.minecraft.world.item.ItemStack(item, outputCount);
    }
}
