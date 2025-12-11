package net.phoenix.core.common.data.recipe.records;

import net.minecraft.world.item.ItemStack;

public record FullBeeConfig(
                            String beeId,
                            String pollinationInputId,
                            ItemStack finalOutputItem,

                            int decantingEut,
                            int decantingDuration,
                            int waxEut,
                            int waxDuration,
                            int fluidEut,
                            int fluidDuration) {}
