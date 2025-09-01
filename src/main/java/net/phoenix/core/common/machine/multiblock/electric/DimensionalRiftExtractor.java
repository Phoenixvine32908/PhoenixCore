package net.phoenix.core.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.core.common.data.PhoenixRecipeTypes;
import net.phoenix.core.common.registry.PhoenixRegistration;

public class DimensionalRiftExtractor {
    public static MachineDefinition DIMENSIONAL_ANCHOR = PhoenixRegistration.REGISTRATE.multiblock("dimensional_anchor", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(PhoenixRecipeTypes.DIMENSIONAL_ANCHORING)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_TITANIUM_STABLE)
            .pattern(multiblockMachineDefinition -> FactoryBlockPattern.start()
                    .aisle("BCDCB", "BBEBB", "BBEBB", "BBEBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB")
                    .aisle("CCCCC", "BEFEB", "BEGEB", "BBEBB", "BBEBB", "BBEBB", "BBEBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB")
                    .aisle("DCCCD", "EFFFE", "EGHGE", "EEGEE", "BEGEB", "BEGEB", "BEGEB", "BBEBB", "BBEBB", "BBEBB", "BBEBB", "BBEBB")
                    .aisle("CCCCC", "BEFEB", "BEGEB", "BBEBB", "BBEBB", "BBEBB", "BBEBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB")
                    .aisle("BCICB", "BBEBB", "BBEBB", "BBEBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB")
                    .where("B", Predicates.any())
                    .where(
                            "C",
                            Predicates.blocks(GTBlocks.CASING_TITANIUM_STABLE.get())
                                    .setMinGlobalLimited(5)
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                    .or(Predicates.autoAbilities(multiblockMachineDefinition.getRecipeTypes()))
                            )
                    .where("D", Predicates.blocks(GTBlocks.FIREBOX_TITANIUM.get()))
                    .where("E", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:source_imbued_titanium_frame"))))
                    .where("F", Predicates.blocks(GTBlocks.COIL_NICHROME.get()))
                    .where("G", Predicates.blocks(GTBlocks.CASING_TITANIUM_GEARBOX.get()))
                    .where("H", Predicates.blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                    .where("I", Predicates.controller(Predicates.blocks(multiblockMachineDefinition.get())))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_stable_titanium"), GTCEu.id("block/multiblock/large_miner"))
            .register();

    public static MachineDefinition AETHERIAL_FABRICATOR = PhoenixRegistration.REGISTRATE.multiblock("aetherial_fabricator", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(PhoenixRecipeTypes.AETHERIAL_FABRICATION)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .pattern(multiblockMachineDefinition -> FactoryBlockPattern.start()
                    .aisle("BCCCB", "BDDDB", "BCCCB", "BBCBB", "BBCBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB")
                    .aisle("CEEEC", "DAAAD", "CAAAC", "BEAEB", "BEAEB", "BEEEB", "BBEBB", "BBEBB", "BBBBB", "BBBBB", "BBBBB")
                    .aisle("CEEEC", "DAAAD", "CAAAC", "CAAAC", "CAAAC", "BEAEB", "BEAEB", "BEAEB", "BBCBB", "BBDBB", "BBCBB")
                    .aisle("CEEEC", "DAAAD", "CAAAC", "BEAEB", "BEAEB", "BEEEB", "BBEBB", "BBEBB", "BBBBB", "BBBBB", "BBBBB")
                    .aisle("BCCCB", "BCFCB", "BCCCB", "BBCBB", "BBCBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB", "BBBBB")
                    .where("A", Predicates.air())
                    .where("B", Predicates.any())
                    .where(
                            "C",
                            Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
                                    .setMinGlobalLimited(5)
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                    .or(Predicates.autoAbilities(multiblockMachineDefinition.getRecipeTypes()))
                            )
                    .where("D", Predicates.blocks(GTBlocks.COIL_KANTHAL.get()))
                    .where(
                            "E",
                            Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:frost_reinforced_stained_steel_frame")))
                            )
                    .where("F", Predicates.controller(Predicates.blocks(multiblockMachineDefinition.get())))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"), GTCEu.id("block/multiblock/large_miner"))
            .register();

    public static void init() {}
}
