package net.phoenix.core.common.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.core.client.renderer.machine.multiblock.PhoenixDynamicRenderHelpers;
import net.phoenix.core.common.data.PhoenixRecipeTypes;
import net.phoenix.core.common.machine.multiblock.electric.HoneyCrystallizationChamberMachine;
import net.phoenix.core.phoenixcore;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.phoenix.core.common.registry.PhoenixRegistration.REGISTRATE;

public class PhoenixBeeMachines {

    public static final MultiblockMachineDefinition HONEY_CRYSTALLIZATION_CHAMBER = REGISTRATE
            .multiblock("honey_crystallization_chamber", HoneyCrystallizationChamberMachine::new)
            .langValue("Honey Crystallization Chamber")
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT, GTRecipeModifiers.PARALLEL_HATCH,
                    HoneyCrystallizationChamberMachine::recipeModifier)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(PhoenixRecipeTypes.HONEY_CHAMBER_RECIPES)
            .appearanceBlock(CASING_STAINLESS_CLEAN)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("BBBBBBBBBBB", "BBBBBBBBBBB", "BBBBBBBBBBB", "BBBBBBBBBBB", "BBBBBBBBBBB", "BBBBBCBBBBB",
                            "BBBBBCBBBBB", "BBBBBBBBBBB", "BBBBBBBBBBB")
                    .aisle("BDDDDDDDDDB", "BBBBBBBBBBB", "BBBBBBBBBBB", "BBBBBBBBBBB", "BBBBBCBBBBB", "BBBBEEEBBBB",
                            "BBBBEEEBBBB", "BBBBBCBBBBB", "BBBBBBBBBBB")
                    .aisle("BDDDDDDDDDB", "BBFBBBBBFBB", "BBFBBBBBFBB", "BBFBBCBBFBB", "BBFGGGGGFBB", "BBGGAAAGGBB",
                            "BBGGAAAGGBB", "BBBGGGGGBBB", "BBBBBCBBBBB")
                    .aisle("BDDDDDDDDDB", "BBBBBBBBBBB", "BBBBBBBBBBB", "BBBCBCBCBBB", "BBGGGGGGGBB", "BBGAAAAAGBB",
                            "BBGAAAAAGBB", "BBGGGGGGGBB", "BBBBBCBBBBB")
                    .aisle("BDDDDDDDDDB", "BBBBBCBBBBB", "BBBBBCBBBBB", "BBBBCCCBBBB", "BBGGAAAGGBB", "BEAAAAAAAEB",
                            "BEAAAAAAAEB", "BBGGGAGGGBB", "BBBBBCBBBBB")
                    .aisle("BDDDDDDDDDB", "BBBBCDCBBBB", "BBBBCDCBBBB", "BBCCCCCCCBB", "BCGAACAAGCB", "CEAAACAAAEC",
                            "CEAAACAAAEC", "BCGGACAGGCB", "BBCCCCCCCBB")
                    .aisle("BDDDDDDDDDB", "BBBBBCBBBBB", "BBBBBCBBBBB", "BBBBCCCBBBB", "BBGGAAAGGBB", "BEAAAAAAAEB",
                            "BEAAAAAAAEB", "BBGGGAGGGBB", "BBBBBCBBBBB")
                    .aisle("BDDDDDDDDDB", "BBBBBBBBBBB", "BBBBBBBBBBB", "BBBCBCBCBBB", "BBGGGGGGGBB", "BBGAAAAAGBB",
                            "BBGAAAAAGBB", "BBGGGGGGGBB", "BBBBBCBBBBB")
                    .aisle("BDDDDDDDDDB", "BBFBBBBBFBB", "BBFBBBBBFBB", "BBFBBCBBFBB", "BBFGGGGGFBB", "BBGGAAAGGBB",
                            "BBGGAAAGGBB", "BBBGGGGGBBB", "BBBBBCBBBBB")
                    .aisle("BDDDDDDDDDB", "BBBBBBBBBBB", "BBBBBBBBBBB", "BBBBBBBBBBB", "BBBBCCCBBBB", "BBBCEHECBBB",
                            "BBBCEEECBBB", "BBBBCCCBBBB", "BBBBBBBBBBB")
                    .aisle("BBBBBBBBBBB", "BBBBBBBBBBB", "BBBBBBBBBBB", "BBBBBBBBBBB", "BBBBBBBBBBB", "BBBBBBBBBBB",
                            "BBBBBBBBBBB", "BBBBBBBBBBB", "BBBBBBBBBBB")
                    .where("A", Predicates.air())
                    .where("B", Predicates.any())
                    .where("C",
                            blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.fromNamespaceAndPath("gtceu",
                                            "steel_frame"))))
                    .where('D', blocks(CASING_BRONZE_BRICKS.get()))
                    .where('E', blocks(CASING_LAMINATED_GLASS.get()))
                    .where('F', blocks(CASING_STEEL_SOLID.get()))
                    .where("G", blocks(CASING_STAINLESS_CLEAN.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2)
                                    .setMinGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                            .or(autoAbilities(true, false, true)))
                    .where('H', controller(blocks(definition.getBlock())))
                    .build())
            .model(
                    createWorkableCasingMachineModel(
                            GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                            GTCEu.id("block/multiblock/implosion_compressor"))
                            .andThen(d -> d
                                    .addDynamicRenderer(
                                            PhoenixDynamicRenderHelpers::getCustomFluidRenderer)))
            .hasBER(true)
            .register();

    public static final MultiblockMachineDefinition COMB_DECANTER = REGISTRATE
            .multiblock("comb_decanter", WorkableElectricMultiblockMachine::new)
            .langValue("Comb Decanter")
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_NON_PERFECT_SUBTICK,
                    GTRecipeModifiers.BATCH_MODE)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(PhoenixRecipeTypes.COMB_DECANTING_RECIPES)
            .appearanceBlock(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("BCDDDCB", "BCDDDCB", "BCDDDCB", "BCDDDCB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBBBBBB")
                    .aisle("CDDDDDC", "CDEAEDC", "CDAAADC", "CDAAADC", "BCFFFCB", "BBGGGBB", "BBFFFBB", "BBDDDBB")
                    .aisle("DDDDDDD", "DECACED", "DACACAD", "DACACAD", "BFFAFFB", "BGGAGGB", "BFFAFFB", "BDDDDDB")
                    .aisle("DDDDDDD", "DAAAAAD", "DAAAAAD", "DAAAAAD", "BFAAAFB", "BGAAAGB", "BFAAAFB", "BDDHDDB")
                    .aisle("DDDDDDD", "DECACED", "DACACAD", "DACACAD", "BFFAFFB", "BGGAGGB", "BFFAFFB", "BDDDDDB")
                    .aisle("CDDDDDC", "CDEAEDC", "CDAAADC", "CDAAADC", "BCFFFCB", "BBGGGBB", "BBFFFBB", "BBDDDBB")
                    .aisle("BCDDDCB", "BCDJDCB", "BCDDDCB", "BCDDDCB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBBBBBB")
                    .where("A", Predicates.air())
                    .where("B", Predicates.any())
                    .where('C',
                            blocks(ChemicalHelper.getBlock(TagPrefix.frameGt,
                                    GTMaterials.TungstenSteel)))
                    .where("D", blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()).setMinGlobalLimited(10)
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                            .or(Predicates.autoAbilities(definition.getRecipeTypes())))
                    .where('E', blocks(Blocks.HONEY_BLOCK))
                    .where('F', blocks(COIL_RTMALLOY.get()))
                    .where("G", blocks(GCYMBlocks.HEAT_VENT.get()))
                    .where("H", abilities(PartAbility.MUFFLER))
                    .where('J', controller(blocks(definition.getBlock())))
                    .build())
            .model(
                    createWorkableCasingMachineModel(
                            GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                            GTCEu.id("block/multiblock/large_miner")))
            .register();

    public static final MultiblockMachineDefinition SWARM_NURTURER = REGISTRATE
            .multiblock("swarm_nurturer", WorkableElectricMultiblockMachine::new)
            .langValue("Swarm Nurturer")
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(PhoenixRecipeTypes.SWARM_NURTURING_RECIPES)
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("BCCCB", "BDDDB", "BDDDB", "BCCCB")
                    .aisle("CBBBC", "DEEED", "DAAAD", "CEEEC")
                    .aisle("CBBBC", "DFFFD", "DAAAD", "CFFFC")
                    .aisle("CBBBC", "DEEED", "DAAAD", "CEEEC")
                    .aisle("CBBBC", "CBGBC", "CBBBC", "CCCCC")
                    .where("A", Predicates.air())
                    .where("B", blocks(CASING_STAINLESS_CLEAN.get()).setMinGlobalLimited(2)
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(Predicates.autoAbilities(definition.getRecipeTypes())))
                    .where('C',
                            blocks(ChemicalHelper.getBlock(TagPrefix.frameGt,
                                    GTMaterials.StainlessSteel)))
                    .where('D', blocks(GCYMBlocks.MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                    .where('E',
                            blocks(ChemicalHelper.getBlock(TagPrefix.frameGt,
                                    GTMaterials.Steel)))
                    .where('F', blocks(CASING_STAINLESS_STEEL_GEARBOX.get()))
                    .where('G', controller(blocks(definition.getBlock())))
                    .build())
            .model(
                    createWorkableCasingMachineModel(
                            GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                            GTCEu.id("block/multiblock/large_miner")))
            .register();
    public static final MultiblockMachineDefinition APIS_PROGENITOR = REGISTRATE
            .multiblock("apis_progenitor", WorkableElectricMultiblockMachine::new)
            .langValue("Apis Progenitor")
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(PhoenixRecipeTypes.APIS_PROGENITOR_RECIPES)
            .appearanceBlock(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("BBBBB", "CDDDC", "CDDDC", "CDDDC", "EFFFE")
                    .aisle("BGGGB", "DHHHD", "DAAAD", "DAAAD", "FBBBF")
                    .aisle("BGGGB", "DHHHD", "DAAAD", "DAAAD", "FBBBF")
                    .aisle("BGGGB", "DHHHD", "DAAAD", "DAAAD", "FBBBF")
                    .aisle("BBIBB", "CDDDC", "CDDDC", "CDDDC", "EFFFE")
                    .where("A", Predicates.air())
                    .where("B", blocks(CASING_TUNGSTENSTEEL_ROBUST.get()).setMinGlobalLimited(2)
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(Predicates.autoAbilities(definition.getRecipeTypes())))
                    .where('C',
                            blocks(ChemicalHelper.getBlock(TagPrefix.frameGt,
                                    GTMaterials.TungstenSteel)))
                    .where('D', blocks(CASING_TEMPERED_GLASS.get()))
                    .where("E", Predicates.any())
                    .where('F',
                            blocks(ChemicalHelper.getBlock(TagPrefix.frameGt,
                                    GTMaterials.TreatedWood)))
                    .where('G', blocks(Blocks.DIRT))
                    .where('H', blocks(Blocks.POPPY))
                    .where('I', controller(blocks(definition.getBlock())))
                    .build())
            .model(
                    createWorkableCasingMachineModel(
                            GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                            phoenixcore.id("block/multiblock/apis_progenitor")))
            .register();
    public static final MultiblockMachineDefinition SIMULATED_COLONY = REGISTRATE
            .multiblock("simulated_colony", WorkableElectricMultiblockMachine::new)
            .langValue("Simulated Colony")
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.BATCH_MODE)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(PhoenixRecipeTypes.SIMULATED_COLONY_RECIPES)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("BCDDDCB", "BCEEECB", "BCEEECB", "BCEEECB", "BBBBBBB")
                    .aisle("CDFFFDC", "CGAHAGC", "CGAIAGC", "CGAAAGC", "BBFFFBB")
                    .aisle("DFFFFFD", "EAAAAAE", "EAAAAAE", "EAAAAAE", "BFFFFFB")
                    .aisle("DFFFFFD", "EHAHAHE", "EIAIAIE", "EAAAAAE", "BFFFFFB")
                    .aisle("DFFFFFD", "EAAAAAE", "EAAAAAE", "EAAAAAE", "BFFFFFB")
                    .aisle("CDFFFDC", "CGAHAGC", "CGAIAGC", "CGAAAGC", "BBFFFBB")
                    .aisle("BCDJDCB", "BCEEECB", "BCEEECB", "BCEEECB", "BBBBBBB")
                    .where("A", Predicates.air())
                    .where('B', Predicates.any())
                    .where('C',
                            blocks(ChemicalHelper.getBlock(TagPrefix.frameGt,
                                    GTMaterials.TreatedWood)))
                    .where("D", blocks(CASING_STEEL_SOLID.get()).setMinGlobalLimited(2)
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                            .or(Predicates.autoAbilities(definition.getRecipeTypes())))
                    .where('E', blocks(CASING_TEMPERED_GLASS.get()))
                    .where('F', blocks(TREATED_WOOD_PLANK.get()))
                    .where('G',
                            blocks(ChemicalHelper.getBlock(TagPrefix.frameGt,
                                    GTMaterials.Steel)))
                    .where('H', blocks(Blocks.DIRT))
                    .where('I', blocks(Blocks.POPPY))
                    .where('J', controller(blocks(definition.getBlock())))
                    .build())
            .model(
                    createWorkableCasingMachineModel(
                            GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                            GTCEu.id("block/multiblock/large_miner")))
            .register();

    public static void init() {}
}
