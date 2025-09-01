package net.phoenix.core.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.core.common.registry.PhoenixRegistration;

import com.apoptosis.supersteamsystems.machines.SteamParallelMultiblockMultiRecipeTypeMachine;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;

public class LargeSteamCompressor {

    public static final MachineDefinition LARGE_STEAM_COMPRESSOR = PhoenixRegistration.REGISTRATE
            .multiblock("large_steam_compressor", SteamParallelMultiblockMultiRecipeTypeMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GTRecipeTypes.COMPRESSOR_RECIPES, GTRecipeTypes.SIFTER_RECIPES)
            .recipeModifier(SteamParallelMultiblockMultiRecipeTypeMachine::recipeModifier, true)
            .pattern(multiblockMachineDefinition -> FactoryBlockPattern.start()
                    .aisle("BCCCB", "BBCBB", "BBCBB", "BBBBB", "BBBBB")
                    .aisle("CDDDC", "BDBDB", "BDEDB", "BBDBB", "BBBBB")
                    .aisle("CDDDC", "CBBBC", "CEFEC", "BDDDB", "BBGBB")
                    .aisle("CDDDC", "BDBDB", "BDEDB", "BBDBB", "BBBBB")
                    .aisle("BCCCB", "BBHBB", "BBCBB", "BBBBB", "BBBBB")
                    .where("B", Predicates.any())
                    .where("C",
                            blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.parse("gtceu:steam_machine_casing")))
                                    .setMinGlobalLimited(6)
                                    .or(Predicates.abilities(PartAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.STEAM).setPreviewCount(1)))
                    .where("D",
                            blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.parse("gtceu:industrial_steam_casing"))))
                    .where("E", Predicates.blocks(CASING_BRONZE_BRICKS.get()))
                    .where("F", Predicates.blocks(FIREBOX_BRONZE.get()))
                    .where("G",
                            Predicates.blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.parse("gtceu:bronze_machine_casing"))))
                    .where("H", Predicates.controller(blocks(multiblockMachineDefinition.getBlock())))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/steam/bronze/bottom"),
                    GTCEu.id("block/machines/compressor"))
            .register();

    public static void init() {}
}
