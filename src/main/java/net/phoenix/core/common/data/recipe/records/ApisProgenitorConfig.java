package net.phoenix.core.common.data.recipe.records;

public record ApisProgenitorConfig(
                                   String id,
                                   String outputBeeType,
                                   String inputBeeType,
                                   String itemInput,
                                   String fluidInput,
                                   int duration,
                                   int EUt) {}
