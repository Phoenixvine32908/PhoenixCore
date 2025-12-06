package net.phoenix.core.common.data.recipe.records;

import net.minecraft.world.item.ItemStack;

public record FullBeeConfig(
                            // Identification
                            String beeId,
                            // Inputs/Outputs
                            String pollinationInputId,
                            ItemStack finalOutputItem,

                            // Recipe Parameters (Now fine-grained)
                            int decantingEut,
                            int decantingDuration,
                            int waxEut,
                            int waxDuration,
                            int fluidEut,
                            int fluidDuration) {}
