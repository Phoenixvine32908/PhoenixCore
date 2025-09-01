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
import net.phoenix.core.PhoenixCore;
import net.phoenix.core.common.data.PhoenixRecipeTypes;
import net.phoenix.core.common.registry.PhoenixRegistration;

public class BeeMultiblocks {

    public static final MachineDefinition COMB_DECANTER_MULTIBLOCK = PhoenixRegistration.REGISTRATE
            .multiblock("comb_decanter", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(PhoenixRecipeTypes.COMB_DECANTING)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_NON_PERFECT_SUBTICK,
                    GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(multiblockMachineDefinition -> FactoryBlockPattern.start()
                    .aisle("BCDDDCB", "BCDDDCB", "BCDDDCB", "BCDDDCB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBBBBBB")
                    .aisle("CDDDDDC", "CDEAEDC", "CDAAADC", "CDAAADC", "BCFFFCB", "BBGGGBB", "BBFFFBB", "BBDDDBB")
                    .aisle("DDDDDDD", "DECACED", "DACACAD", "DACACAD", "BFFAFFB", "BGGAGGB", "BFFAFFB", "BDDDDDB")
                    .aisle("DDDDDDD", "DAAAAAD", "DAAAAAD", "DAAAAAD", "BFAAAFB", "BGAAAGB", "BFAAAFB", "BDDHDDB")
                    .aisle("DDDDDDD", "DECACED", "DACACAD", "DACACAD", "BFFAFFB", "BGGAGGB", "BFFAFFB", "BDDDDDB")
                    .aisle("CDDDDDC", "CDEAEDC", "CDAAADC", "CDAAADC", "BCFFFCB", "BBGGGBB", "BBFFFBB", "BBDDDBB")
                    .aisle("BCDDDCB", "BCDJDCB", "BCDDDCB", "BCDDDCB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBBBBBB")
                    .where("A", Predicates.air())
                    .where("B", Predicates.any())
                    .where("C",
                            Predicates.blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.parse("gtceu:tungsten_steel_frame"))))
                    .where(
                            "D",
                            Predicates.blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get())
                                    .setMinGlobalLimited(10)
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                    .or(
                                            Predicates.abilities(
                                                    PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                                    .or(Predicates.autoAbilities(multiblockMachineDefinition.getRecipeTypes())))
                    .where("E",
                            Predicates.blocks(
                                    ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:honey_block"))))
                    .where("F", Predicates.blocks(GTBlocks.COIL_RTMALLOY.get()))
                    .where("G", Predicates.blocks(GCYMBlocks.HEAT_VENT.get()))
                    .where("H", Predicates.abilities(PartAbility.MUFFLER).setExactLimit(1))
                    .where("J", Predicates.controller(Predicates.blocks(multiblockMachineDefinition.get())))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/large_miner"))
            .register();
    public static MachineDefinition SWARM_NURTURER_MULTIBLOCK = PhoenixRegistration.REGISTRATE
            .multiblock("swarm_nurturer", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(PhoenixRecipeTypes.SWARM_NURTURING)
            .recipeModifiers(
                    GTRecipeModifiers.OC_NON_PERFECT,
                    GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .pattern(multiblockMachineDefinition -> FactoryBlockPattern.start()
                    .aisle("BCCCB", "BDDDB", "BDDDB", "BCCCB")
                    .aisle("CBBBC", "DEEED", "DAAAD", "CEEEC")
                    .aisle("CBBBC", "DFFFD", "DAAAD", "CFFFC")
                    .aisle("CBBBC", "DEEED", "DAAAD", "CEEEC")
                    .aisle("CBBBC", "CBGBC", "CBBBC", "CCCCC")
                    .where("A", Predicates.air())
                    .where(
                            "B",
                            Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
                                    .setMinGlobalLimited(2)
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                    .or(Predicates.autoAbilities(multiblockMachineDefinition.getRecipeTypes())))
                    .where("C",
                            Predicates.blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.parse("gtceu:stainless_steel_frame"))))
                    .where("D", Predicates.blocks(GCYMBlocks.MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                    .where("E",
                            Predicates.blocks(
                                    ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:steel_frame"))))
                    .where("F", Predicates.blocks(GTBlocks.CASING_STAINLESS_STEEL_GEARBOX.get()))
                    .where("G", Predicates.controller(Predicates.blocks(multiblockMachineDefinition.get())))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/large_miner"))
            .register();
    public static MachineDefinition APIS_PROGENITOR_MULTIBLOCK = PhoenixRegistration.REGISTRATE
            .multiblock("apis_progenitor", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(PhoenixRecipeTypes.APIS_PROGENITOR)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
            .pattern(multiblockMachineDefinition -> FactoryBlockPattern.start()
                    .aisle("BBBBB", "CDDDC", "CDDDC", "CDDDC", "EFFFE")
                    .aisle("BGGGB", "DHHHD", "DAAAD", "DAAAD", "FBBBF")
                    .aisle("BGGGB", "DHHHD", "DAAAD", "DAAAD", "FBBBF")
                    .aisle("BGGGB", "DHHHD", "DAAAD", "DAAAD", "FBBBF")
                    .aisle("BBIBB", "CDDDC", "CDDDC", "CDDDC", "EFFFE")
                    .where("A", Predicates.air())
                    .where(
                            "B",
                            Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())
                                    .setMinGlobalLimited(2)
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                    .or(Predicates.autoAbilities(multiblockMachineDefinition.getRecipeTypes())))
                    .where("C",
                            Predicates.blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.parse("gtceu:tungsten_steel_frame"))))
                    .where("D", Predicates.blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where("E", Predicates.any())
                    .where("F",
                            Predicates.blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.parse("gtceu:treated_wood_frame"))))
                    .where("G",
                            Predicates
                                    .blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:dirt"))))
                    .where("H",
                            Predicates
                                    .blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:poppy"))))
                    .where("I", Predicates.controller(Predicates.blocks(multiblockMachineDefinition.get())))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                    PhoenixCore.id("block/multiblock/apis_progenitor"))
            .register();

    public static MachineDefinition SIMULATED_COLONY_MULTIBLOCK = PhoenixRegistration.REGISTRATE
            .multiblock("simulated_colony", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(PhoenixRecipeTypes.SIMULATED_COLONY)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(multiblockMachineDefinition -> FactoryBlockPattern.start()
                    .aisle("BCDDDCB", "BCEEECB", "BCEEECB", "BCEEECB", "BBBBBBB")
                    .aisle("CDFFFDC", "CGAHAGC", "CGAIAGC", "CGAAAGC", "BBFFFBB")
                    .aisle("DFFFFFD", "EAAAAAE", "EAAAAAE", "EAAAAAE", "BFFFFFB")
                    .aisle("DFFFFFD", "EHAHAHE", "EIAIAIE", "EAAAAAE", "BFFFFFB")
                    .aisle("DFFFFFD", "EAAAAAE", "EAAAAAE", "EAAAAAE", "BFFFFFB")
                    .aisle("CDFFFDC", "CGAHAGC", "CGAIAGC", "CGAAAGC", "BBFFFBB")
                    .aisle("BCDJDCB", "BCEEECB", "BCEEECB", "BCEEECB", "BBBBBBB")
                    .where("A", Predicates.air())
                    .where("B", Predicates.any())
                    .where("C",
                            Predicates.blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.parse("gtceu:treated_wood_frame"))))
                    .where("D", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where(
                            "D",
                            Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                                    .setMinGlobalLimited(2)
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                    .or(
                                            Predicates.abilities(
                                                    PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                                    .or(Predicates.autoAbilities(multiblockMachineDefinition.getRecipeTypes())))
                    .where("E", Predicates.blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where("F", Predicates.blocks(GTBlocks.TREATED_WOOD_PLANK.get()))
                    .where("G",
                            Predicates.blocks(
                                    ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:steel_frame"))))
                    .where("H",
                            Predicates
                                    .blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:dirt"))))
                    .where("I",
                            Predicates
                                    .blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:poppy"))))
                    .where("J", Predicates.controller(Predicates.blocks(multiblockMachineDefinition.get())))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/large_miner"))
            .register();

    public static void init() {}
}
