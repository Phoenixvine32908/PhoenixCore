package net.phoenix.core.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.*;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.core.common.registry.PhoenixRegistration;

public class CoilMultis {

    // Weird name choice lol
    public static MachineDefinition EMBERWAKE_ALLOY_HEARTH = PhoenixRegistration.REGISTRATE
            .multiblock("emberwake_alloy_hearth", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GCYMRecipeTypes.ALLOY_BLAST_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_NON_PERFECT_SUBTICK,
                    GTRecipeModifiers.BATCH_MODE, (GTRecipeModifiers::ebfOverclock))
            .appearanceBlock(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(multiblockMachineDefinition -> FactoryBlockPattern.start()
                    .aisle("BCCCB", "BCDCB", "BCDCB", "BEEEB", "BFFFB", "BCCCB", "BBBBB", "BBBBB", "BBBBB")
                    .aisle("CCCCC", "CDGDC", "CADAC", "EADAE", "FAFAF", "CCCCC", "BCFCB", "BBFBB", "BBFBB")
                    .aisle("CCCCC", "DGHGD", "DDHDD", "EDHDE", "FFAFF", "CCCCC", "BFCFB", "BFAFB", "BFIFB")
                    .aisle("CCCCC", "CDGDC", "CADAC", "EADAE", "FAFAF", "CCCCC", "BCFCB", "BBFBB", "BBFBB")
                    .aisle("BCCCB", "BCJCB", "BCDCB", "BEEEB", "BFFFB", "BCCCB", "BBBBB", "BBBBB", "BBBBB")
                    .where("A", Predicates.air())
                    .where("B", Predicates.any())
                    .where(
                            "C",
                            Predicates.blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get())
                                    .setMinGlobalLimited(10)
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                    .or(
                                            Predicates.abilities(
                                                    PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                                    .or(Predicates.autoAbilities(multiblockMachineDefinition.getRecipeTypes())))
                    .where("D",
                            Predicates.blocks(
                                    ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:neutronium_frame"))))
                    .where("E", Predicates.blocks(GCYMBlocks.HEAT_VENT.get()))
                    .where("F", Predicates.heatingCoils())
                    .where("G", Predicates.blocks(GTBlocks.FUSION_COIL.get()))
                    .where("H", Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get()))
                    .where("I", Predicates.abilities(PartAbility.MUFFLER).setExactLimit(1))
                    .where("J", Predicates.controller(Predicates.blocks(multiblockMachineDefinition.get())))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/gcym/blast_alloy_smelter"))
            .register();
    public static MachineDefinition ADVANCED_CRACKING_UNIT = PhoenixRegistration.REGISTRATE
            .multiblock("advanced_cracking_unit", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.CRACKING_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_NON_PERFECT_SUBTICK,
                    GTRecipeModifiers.BATCH_MODE, (GTRecipeModifiers::ebfOverclock))
            .appearanceBlock(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
            .pattern(multiblockMachineDefinition -> FactoryBlockPattern.start()
                    .aisle("BBCCCCCBB", "DBDDDDDBD", "DBDDDDDBD", "DBDDDDDBD", "DBDDDDDBD", "DDDDDDDDD", "DDDDDDDDD",
                            "DDDDDDDDD")
                    .aisle("BEEEEEEEB", "BEFFFFFEB", "BEEEEEEEB", "BEEEEEEEB", "BEEEEEEEB", "DBCBBBCBD", "DDCBBBCDD",
                            "DDCBBBCDD")
                    .aisle("CEEEEEEEC", "DFAGAAAFD", "DHAAAAAHD", "DHAGAGAHD", "DHHEEEHHD", "DIHAAAHID", "DDHAAAHDD",
                            "DDBJJJBDD")
                    .aisle("CEEEEEEEC", "DFGKGKGFD", "DHAGAGAHD", "DHGKGKGHD", "DHHEAEHHD", "DIHAAAHID", "DDHAAAHDD",
                            "DDBJLJBDD")
                    .aisle("CEEEEEEEC", "DFAGAGAFD", "DHAAAAAHD", "DHAGAGAHD", "DHHEEEHHD", "DIHAAAHID", "DDHAAAHDD",
                            "DDBJJJBDD")
                    .aisle("BEEEEEEEB", "BEMMMMMEB", "BEMMNMMEB", "BEMMMMMEB", "BEEEEEEEB", "DBCBBBCBD", "DDCBBBCDD",
                            "DDCBBBCDD")
                    .aisle("BBCDDDCBB", "DBCDDDCBD", "DBCDDDCBD", "DBCDDDCBD", "DBCCCCCBD", "DDDDDDDDD", "DDDDDDDDD",
                            "DDDDDDDDD")
                    .where("A", Predicates.air())
                    .where(
                            "B",
                            Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_TURBINE.get())
                                    .setMinGlobalLimited(10)
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                    .or(
                                            Predicates.abilities(
                                                    PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                                    .or(Predicates.autoAbilities(multiblockMachineDefinition.getRecipeTypes())))
                    .where(
                            "C",
                            Predicates.blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.parse("gtceu:void_touched_tungsten_steel_frame"))))
                    .where("D", Predicates.any())
                    .where("E", Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get()))
                    .where("F", Predicates.blocks(GTBlocks.FIREBOX_TUNGSTENSTEEL.get()))
                    .where("G", Predicates.blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where("H", Predicates.heatingCoils())
                    .where("I", Predicates.blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                    .where("J", Predicates.blocks(GTBlocks.CASING_HSSE_STURDY.get()))
                    .where("K", Predicates.blocks(GTBlocks.CASING_EXTREME_ENGINE_INTAKE.get()))
                    .where("L", Predicates.abilities(PartAbility.MUFFLER).setExactLimit(1))
                    .where("M", Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                    .where("N", Predicates.controller(Predicates.blocks(multiblockMachineDefinition.get())))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/mechanic/machine_casing_turbine_tungstensteel"),
                    GTCEu.id("block/multiblock/cracking_unit"))
            .register();

    public static MachineDefinition SUPERHEATED_PYRO_OVEN = PhoenixRegistration.REGISTRATE
            .multiblock("superheated_pyrolyzing_oven", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.PYROLYSE_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_NON_PERFECT_SUBTICK,
                    GTRecipeModifiers.BATCH_MODE, (GTRecipeModifiers::ebfOverclock))
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(multiblockMachineDefinition -> FactoryBlockPattern.start()
                    .aisle("BCBBBBBCB", "BDBBBBBDB", "BDBBBBBDB", "BDBBBBBDB", "BDBBBBBDB", "BDBBBBBDB", "BDBBBBBDB",
                            "BCBBBBBCB")
                    .aisle("CCEEEEECC", "DCFFFFFCD", "DCFFFFFCD", "DCFFFFFCD", "DCFFFFFCD", "DCFFFFFCD", "DCFFFFFCD",
                            "CCCCCCCCC")
                    .aisle("BEGGGGGEB", "BFHIJIHFB", "BFHIJIHFB", "BFHIJIHFB", "BFHIJIHFB", "BFHIJIHFB", "BFHIJIHFB",
                            "BCHCCCHCB")
                    .aisle("BEGGGGGEB", "BFIAAAIFB", "BFIAAAIFB", "BFIAAAIFB", "BFIAAAIFB", "BFIAAAIFB", "BFIAAAIFB",
                            "BCCGGGCCB")
                    .aisle("BEGGGGGEB", "BFJAAAJFB", "BFJAAAJFB", "BFJAAAJFB", "BFJAAAJFB", "BFJAAAJFB", "BFJAAAJFB",
                            "BCCGKGCCB")
                    .aisle("BEGGGGGEB", "BFIAAAIFB", "BFIAAAIFB", "BFIAAAIFB", "BFIAAAIFB", "BFIAAAIFB", "BFIAAAIFB",
                            "BCCGGGCCB")
                    .aisle("BEGGGGGEB", "BFHIJIHFB", "BFHIJIHFB", "BFHIJIHFB", "BFHIJIHFB", "BFHIJIHFB", "BFHIJIHFB",
                            "BCHCCCHCB")
                    .aisle("CCEEEEECC", "DCFFFFFCD", "DCFFFFFCD", "DCFFFFFCD", "DCFFFFFCD", "DCFFFFFCD", "DCFFFFFCD",
                            "CCCCLCCCC")
                    .aisle("BCBBBBBCB", "BDBBBBBDB", "BDBBBBBDB", "BDBBBBBDB", "BDBBBBBDB", "BDBBBBBDB", "BDBBBBBDB",
                            "BCBBBBBCB")
                    .where("A", Predicates.air())
                    .where("B", Predicates.any())
                    .where(
                            "C",
                            Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())
                                    .setMinGlobalLimited(10)
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                    .or(
                                            Predicates.abilities(
                                                    PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                                    .or(Predicates.autoAbilities(multiblockMachineDefinition.getRecipeTypes())))
                    .where(
                            "D",
                            Predicates.blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.parse("gtceu:void_touched_tungsten_steel_frame"))))
                    .where("E", Predicates.blocks(GTBlocks.FIREBOX_STEEL.get()))
                    .where("F", Predicates.blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where("G", Predicates.blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                    .where("H",
                            Predicates.blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.parse("gtceu:resonant_rhodium_alloy_frame"))))
                    .where("I", Predicates.blocks(GCYMBlocks.HEAT_VENT.get()))
                    .where("J", Predicates.heatingCoils())
                    .where("K", Predicates.abilities(PartAbility.MUFFLER).setExactLimit(1))
                    .where("L", Predicates.controller(Predicates.blocks(multiblockMachineDefinition.get())))
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/pyrolyse_oven"))
            .register();

    public static void init() {}
}
