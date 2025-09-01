package net.phoenix.core.common.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.CleaningMaintenanceHatchPartMachine;
import com.gregtechceu.gtceu.data.lang.LangHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import net.phoenix.core.PhoenixCore;
import net.phoenix.core.client.renderer.machine.multiblock.CosmicDynamicRenderHelpers;
import net.phoenix.core.common.block.PhoenixBlocks;
import net.phoenix.core.common.data.PhoenixRecipeTypes;
import net.phoenix.core.common.machine.multiblock.BlazingCleanroom;
import net.phoenix.core.common.machine.multiblock.CreativeEnergyMultiMachine;
import net.phoenix.core.common.machine.multiblock.electric.alchemical.AlchemicalImbuer;
import net.phoenix.core.common.machine.multiblock.electric.research.PhoenixHPCAMachine;
import net.phoenix.core.common.machine.multiblock.steam.LargeSteamCompressor;
import net.phoenix.core.configs.PhoenixConfigs;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.phoenix.core.common.registry.PhoenixRegistration.REGISTRATE;
import static net.phoenix.core.configs.PhoenixConfigs.INSTANCE;

@SuppressWarnings("unused")
public class PhoenixMachines {

    public static MultiblockMachineDefinition DANCE = null;
    public static MachineDefinition BLAZING_CLEANING_MAINTENANCE_HATCH = null;
    public static MachineDefinition HIGH_YEILD_PHOTON_EMISSION_REGULATER = null;
    static {
        REGISTRATE.creativeModeTab(() -> PhoenixCore.PHOENIX_CREATIVE_TAB);
    }

    static {
        if (PhoenixConfigs.INSTANCE.features.blazingHatchEnabled) {
            BLAZING_CLEANING_MAINTENANCE_HATCH = REGISTRATE
                    .machine("blazing_cleaning_maintenance_hatch",
                            holder -> new CleaningMaintenanceHatchPartMachine(holder,
                                    BlazingCleanroom.BLAZING_CLEANROOM))
                    .langValue("Blazing Cleaning Maintenance Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.MAINTENANCE)
                    .tooltips(Component.translatable("gtceu.part_sharing.disabled"),
                            Component.translatable("gtceu.machine.maintenance_hatch_cleanroom_auto.tooltip.0"),
                            Component.translatable("gtceu.machine.maintenance_hatch_cleanroom_auto.tooltip.1"))
                    .tooltipBuilder((stack, tooltips) -> tooltips.add(Component.literal("  ").append(Component
                            .translatable(BlazingCleanroom.BLAZING_CLEANROOM.getTranslationKey())
                            .withStyle(ChatFormatting.RED))))
                    .tier(UHV)

                    .overlayTieredHullModel(
                            PhoenixCore.id("block/machine/part/overlay_maintenance_blazing_cleaning"))
                    // Tier can always be changed later
                    .register();
        }
    }

    public static MachineDefinition[] registerTieredMachines(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                             int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    static {
        if (PhoenixConfigs.INSTANCE.features.creativeEnergyEnabled) {
            // 2. Mova toda a lógica de registro para dentro do método init()
            DANCE = REGISTRATE
                    .multiblock("dance", CreativeEnergyMultiMachine::new)
                    .langValue("dance")
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(PhoenixRecipeTypes.PLEASE) // Agora isso não será mais nulo
                    .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                            GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("AAAAAABBBAABBBAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA")
                            .aisle("AAAABBBBBBBBBBBBAAAAA", "AAAAABBBBBBBBBBAAAAAA", "AAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAABBAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA")
                            .aisle("AAAABBBBBBBBBBBBAAAAA", "AAAABBBBBBBBBBBBAAAAA", "AAAAAAABBBBBBBAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAABBBBAAAAAAAAA",
                                    "AAAAAAAABBBBAAAAAAAAA", "AAAAAAAABBBBAAAAAAAAA", "AAAAAAAAABBAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA")
                            .aisle("AAAABBBBBBBBBBBBAAAAA", "AAAABBBAAAAAAABBAAAAA", "AAAAABBBBBBBBBBAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAABBBBBBBBBAAAAAA", "AAAAABBBBBBBBBBAAAAAA", "AAAAABBBBBBBBBBAAAAAA",
                                    "AAAAABBBBBBBBBBAAAAAA")
                            .aisle("AAAABBBBBBBBBBBBAAAAA", "AAAAABAAAAAAAABBAAAAA", "AAAAABBBAABBBBBAAAAAA",
                                    "AAAAAABBBBBBBBAAAAAAA", "AAAAAAABBBBBBAAAAAAAA", "AAAAAABBBBBBBBAAAAAAA",
                                    "AAAAABBAAAAAAABBAAAAA", "AABBBBAAAAAAAAABBBBAA", "ABBBBBAAAAAAAAABBBBBA",
                                    "ABBBBBBBBBBBBBBBAAAAA")
                            .aisle("AAAAABBBBBBBBBBAAAAAA", "AAAAABBAAAAAABBAAAAAA", "AAAAAABBAAAAABAAAAAAA",
                                    "AAAAAAABAAAABBAAAAAAA", "AAAAAAABAAAAABAAAAAAA", "AAAAAABBAAAAABAAAAAAA",
                                    "AAAAABBAAAAAAABBBAAAA", "ABBBBBAAAAAAAAAABBBBA", "ABBAAAAAAAAAAAAABBBBB",
                                    "BBBBBBBBBBBBBBBBBBBBB")
                            .aisle("AAAAABBBBBBBBBBAAAAAA", "AAAAABBAAAAAABBAAAAAA", "AAAAAABBAAAAABAAAAAAA",
                                    "AAAAAAABAAAABBAAAAAAA", "AAAAAAABAAAAABAAAAAAA", "AAAAAABBAAAAABAAAAAAA",
                                    "AAAAABBAAAAAAABBBAAAA", "ABBBBBAAAAAAAAAABBBBA", "ABBAAAAAAAAAAAAABBBBB",
                                    "BBBBBBBBBBBBBBBBBBBBB")
                            .aisle("AAAABBBBBBBBBBBBAAAAA", "AAAAABAAAAAAAABBAAAAA", "AAAAABBBAAAAABBAAAAAA",
                                    "AAAAAABBBBBBBBAAAAAAA", "AAAAAAABBBBBBAAAAAAAA", "AAAAAABBBBBBBBAAAAAAA",
                                    "AAAAABBAAAAAAABBAAAAA", "AABBBBAAAAAAAAABBBBAA", "ABBBBBAAAAAAAAABBBBBA",
                                    "ABBBBBBBBBBBBBBBAAAAA")
                            .aisle("AAAABBBBBBBBBBBBAAAAA", "AAAABBBAAAAAAABBAAAAA", "AAAAAABBBBBBBBBAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAABBAAAAAAAAAA",
                                    "AAAAAABBBBBBBBBAAAAAA", "AAAAABBBBBBBBBBAAAAAA", "AAAAABBBBBBBBBBAAAAAA",
                                    "AAAAABBBBBBBBBBAAAAAA")
                            .aisle("AAAABBBBBBBBBBBBAAAAA", "AAAABBBBBBBBBBBBAAAAA", "AAAAAABBBBBBBBAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAABBBBAAAAAAAAA",
                                    "AAAAAAAABBBBAAAAAAAAA", "AAAAAAAABBBBAAAAAAAAA", "AAAAAAAAABBAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA")
                            .aisle("AAAABBBBBBBBBBBBAAAAA", "AAAAABBBBBBBBBBAAAAAA", "AAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAABBAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA")
                            .aisle("AAAAAABBBAABDBAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAI")
                            .where("A", any())
                            .where("I",
                                    Predicates
                                            .blocks(PartAbility.INPUT_ENERGY.getBlocks(GTValues.ZPM)
                                                    .toArray(Block[]::new))
                                            .setMaxGlobalLimited(2))
                            .where('D', controller(blocks(definition.getBlock())))
                            .where("B", blocks(BRONZE_HULL.get()).setMinGlobalLimited(575)
                                    .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                                    .or(autoAbilities(true, false, true)))
                            .build())
                    .model(
                            createWorkableCasingMachineModel(
                                    PhoenixCore.id("block/akashic_coil_block"),
                                    GTCEu.id("block/multiblock/generator/large_gas_turbine"))
                                    .andThen(d -> d
                                            .addDynamicRenderer(CosmicDynamicRenderHelpers::getArtificialStarRender)))
                    .hasBER(true)
                    .register();
        }
    }
    Component myColoredText = Component.translatable("block.phoenixcore.high_yield_photon_emission_regulator")
            .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF00FF)));

    static {
        if (PhoenixConfigs.INSTANCE.features.PHPCAEnabled) {
            // 2. Mova toda a lógica de registro para dentro do método init()
            HIGH_YEILD_PHOTON_EMISSION_REGULATER = REGISTRATE
                    .multiblock("high_yield_photon_emission_regulator", PhoenixHPCAMachine::new)
                    .langValue("block.phoenixcore.high_yield_photon_emission_regulator")
                    .tooltips(Component.translatable("phoenixcore.tooltip.hyper_machine_purpose",
                            GTMaterials.get(INSTANCE.features.ActiveCoolerCoolantBase).getLocalizedName()
                                    .withStyle(style -> style.withColor(TextColor.fromRgb(GTMaterials
                                            .get(INSTANCE.features.ActiveCoolerCoolantBase).getMaterialARGB()))),
                            GTMaterials.get(INSTANCE.features.ActiveCoolerCoolant1).getLocalizedName()
                                    .withStyle(style -> style.withColor(TextColor.fromRgb(GTMaterials
                                            .get(INSTANCE.features.ActiveCoolerCoolant1).getMaterialARGB()))),
                            GTMaterials.get(INSTANCE.features.ActiveCoolerCoolant2).getLocalizedName()
                                    .withStyle(style -> style.withColor(TextColor.fromRgb(GTMaterials
                                            .get(INSTANCE.features.ActiveCoolerCoolant2).getMaterialARGB())))),
                            Component.translatable("phoenixcore.tooltip.hyper_machine_1"),
                            Component
                                    .translatable("phoenixcore.tooltip.hyper_machine_coolant_base",
                                            GTMaterials.get(INSTANCE.features.ActiveCoolerCoolantBase)
                                                    .getLocalizedName(),
                                            INSTANCE.features.BaseCoolantBoost)
                                    .withStyle(style -> style.withColor(TextColor.fromRgb(GTMaterials
                                            .get(INSTANCE.features.ActiveCoolerCoolantBase).getMaterialARGB()))),
                            Component.translatable("phoenixcore.tooltip.hyper_machine_coolant2",
                                    GTMaterials.get(INSTANCE.features.ActiveCoolerCoolant1).getLocalizedName(),
                                    INSTANCE.features.CoolantBoost1)
                                    .withStyle(style -> style.withColor(TextColor.fromRgb(GTMaterials
                                            .get(INSTANCE.features.ActiveCoolerCoolant1).getMaterialARGB()))),
                            Component
                                    .translatable("phoenixcore.tooltip.hyper_machine_coolant3",
                                            GTMaterials.get(INSTANCE.features.ActiveCoolerCoolant2).getLocalizedName(),
                                            INSTANCE.features.CoolantBoost2)
                                    .withStyle(style -> style.withColor(TextColor.fromRgb(GTMaterials
                                            .get(INSTANCE.features.ActiveCoolerCoolant2).getMaterialARGB()))))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .appearanceBlock(ADVANCED_COMPUTER_CASING)
                    .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                    .tooltips(LangHandler.getMultiLang("gtceu.machine.high_performance_computation_array.tooltip"))
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("BBBBCCCBBBB", "CDDCCCCCDDC", "CCDCCCCCDCC", "CCCCCCCCCCC", "CCCCCCCCCCC",
                                    "CCCCCCCCCCC", "CCCCCCCCCCC", "CCCCCCCCCCC", "CCCCCCCCCCC", "CCCCCCCCCCC")
                            .aisle("BEEBBBBBEEB", "DEEEFFFEEED", "DEEFGGGFEED", "DDDFGGGFDDD", "CBBFGGGFBBC",
                                    "CBBFGGGFBBC", "CCBFGGGFBCC", "CCBEFFFEBCC", "CCBBBBBBBCC", "CCBBBBBBBCC")
                            .aisle("BBHHIIIHHBB", "CDDAAAAADDC", "CIDAAAAADIC", "CIAAAAAAAIC", "CIAAAAAAAIC",
                                    "CEAAAAAAAEC", "CEAAAAAAAEC", "CEAAAAAAAEC", "CEAAAAAAAEC", "CBBIIIIIBBC")
                            .aisle("CBIIIIIIIBC", "CJAAAKAAAJC", "CJAAAKAAAJC", "CJAAAKAAAJC", "CFAAAKAAAFC",
                                    "CLAAAKAAALC", "CLAAAKAAALC", "CLAAAKAAALC", "CFAAAAAAAFC", "CBIIIIIIIBC")
                            .aisle("CBIIIIIIIBC", "CJAAKMKAAJC", "CJAAKMKAAJC", "CJAAKMKAAJC", "CFAAKMKAAFC",
                                    "CLAAKMKAALC", "CLAAKMKAALC", "CLAAKMKAALC", "CFAAAAAAAFC", "CBIIIIIIIBC")
                            .aisle("CBIIIIIIIBC", "CJAAAKAAAJC", "CJAAAKAAAJC", "CJAAAKAAAJC", "CFAAAKAAAFC",
                                    "CLAAAKAAALC", "CLAAAKAAALC", "CLAAAKAAALC", "CFAAAAAAAFC", "CBIIIIIIIBC")
                            .aisle("BBHHIIIHHBB", "CDDAAAAADDC", "CIDAAAAADIC", "CIAAAAAAAIC", "CIAAAAAAAIC",
                                    "CEAAAAAAAEC", "CEAAAAAAAEC", "CEAAAAAAAEC", "CEAAAAAAAEC", "CBBIIIIIBBC")
                            .aisle("BEEBBNBBEEB", "DEEEFFFEEED", "DEEOJJJOEED", "DDDOJJJODDD", "CBBOJJJOBBC",
                                    "CBBOJJJOBBC", "CCBBJJJBBCC", "CCBBJJJBBCC", "CCBBBBBBBCC", "CCBBBBBBBCC")
                            .aisle("BBBBCCCBBBB", "CDDCCCCCDDC", "CCDCCCCCDCC", "CCCCCCCCCCC", "CCCCCCCCCCC",
                                    "CCCCCCCCCCC", "CCCCCCCCCCC", "CCCCCCCCCCC", "CCCCCCCCCCC", "CCCCCCCCCCC")
                            .where("A", air())
                            .where('B', blocks(PhoenixBlocks.SPACE_TIME_COOLED_ETERNITY_CASING.get()))
                            .where('C', any())
                            .where('D', blocks(PhoenixBlocks.AKASHIC_ZERONIUM_CASING.get()))
                            .where('E', blocks(ADVANCED_COMPUTER_CASING.get()).setMinGlobalLimited(20)
                                    .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                            .setMaxGlobalLimited(2, 1))
                                    .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1))
                                    .or(abilities(PartAbility.COMPUTATION_DATA_TRANSMISSION).setExactLimit(1))
                                    .or(autoAbilities(true, false, false)))
                            .where('F', blocks(COMPUTER_HEAT_VENT.get()))
                            .where('G', blocks(PhoenixResearchMachines.ADVANCED_PHOENIX_COMPUTATION_COMPONENT.get())
                                    .or(blocks(PhoenixResearchMachines.ADVANCED_PHOENIX_COMPUTATION_COMPONENT.get()))
                                    .or(blocks(GTResearchMachines.HPCA_ADVANCED_COMPUTATION_COMPONENT.get()))
                                    .or(blocks(GTResearchMachines.HPCA_EMPTY_COMPONENT.get()))
                                    .or(blocks(GTResearchMachines.HPCA_COMPUTATION_COMPONENT.get())))
                            .where('H', blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                            .where('I', blocks(COMPUTER_CASING.get()))
                            .where('J', blocks(FUSION_GLASS.get()))
                            .where('K', blocks(PhoenixBlocks.AKASHIC_COIL_BLOCK.get()))
                            .where('L', blocks(PhoenixResearchMachines.ACTIVE_PHOENIX_COOLER_COMPONENT.get())
                                    .or(blocks(PhoenixResearchMachines.PHOENIX_COOLER_COMPONENT.get()))
                                    .or(blocks(GTResearchMachines.HPCA_EMPTY_COMPONENT.get()))
                                    .or(blocks(GTResearchMachines.HPCA_BRIDGE_COMPONENT.get()))
                                    .or(blocks(GTResearchMachines.HPCA_ACTIVE_COOLER_COMPONENT.get()))
                                    .or(blocks(GTResearchMachines.HPCA_HEAT_SINK_COMPONENT.get())))
                            .where('M', blocks(PhoenixBlocks.PERFECTED_LOGIC.get()))
                            .where('N', controller(blocks(definition.getBlock())))
                            .where('O', blocks(GCYMBlocks.HEAT_VENT.get()))
                            .build())
                    /*
                     * .shapeInfos(definition -> {
                     * List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                     * .ShapeInfoBuilder builder = MultiblockShapeInfo.builder()
                     * .aisle("SA", "CC", "CC", "OC", "AA")
                     * .aisle("VA", "8V", "5V", "2V", "VA")
                     * .aisle("VA", "7V", "4V", "1V", "VA")
                     * .aisle("VA", "6V", "3V", "0V", "VA")
                     * .aisle("AA", "EC", "MC", "HC", "AA")
                     * .where('S', GTResearchMachines.HIGH_PERFORMANCE_COMPUTING_ARRAY, Direction.NORTH)
                     * .where('A', ADVANCED_COMPUTER_CASING)
                     * .where('V', COMPUTER_HEAT_VENT)
                     * .where('C', COMPUTER_CASING)
                     * .where('E', GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV], Direction.SOUTH)
                     * .where('H', GTMachines.FLUID_IMPORT_HATCH[GTValues.LV], Direction.SOUTH)
                     * .where('O', GTResearchMachines.COMPUTATION_HATCH_TRANSMITTER, Direction.NORTH)
                     * .where('M', ConfigHolder.INSTANCE.machines.enableMaintenance ?
                     * GTMachines.MAINTENANCE_HATCH.defaultBlockState().setValue(
                     * GTMachines.MAINTENANCE_HATCH.get().getRotationState().property,
                     * Direction.SOUTH) :
                     * COMPUTER_CASING.getDefaultState());
                     * 
                     * // a few example structures
                     * shapeInfo.add(builder.shallowCopy()
                     * .where('0', GTResearchMachines.HPCA_EMPTY_COMPONENT, Direction.WEST)
                     * .where('1', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .where('2', GTResearchMachines.HPCA_EMPTY_COMPONENT, Direction.WEST)
                     * .where('3', GTResearchMachines.HPCA_EMPTY_COMPONENT, Direction.WEST)
                     * .where('4', GTResearchMachines.HPCA_COMPUTATION_COMPONENT, Direction.WEST)
                     * .where('5', GTResearchMachines.HPCA_EMPTY_COMPONENT, Direction.WEST)
                     * .where('6', GTResearchMachines.HPCA_EMPTY_COMPONENT, Direction.WEST)
                     * .where('7', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .where('8', GTResearchMachines.HPCA_EMPTY_COMPONENT, Direction.WEST)
                     * .build());
                     * 
                     * shapeInfo.add(builder.shallowCopy()
                     * .where('0', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .where('1', GTResearchMachines.HPCA_COMPUTATION_COMPONENT, Direction.WEST)
                     * .where('2', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .where('3', GTResearchMachines.HPCA_ACTIVE_COOLER_COMPONENT, Direction.WEST)
                     * .where('4', GTResearchMachines.HPCA_COMPUTATION_COMPONENT, Direction.WEST)
                     * .where('5', GTResearchMachines.HPCA_BRIDGE_COMPONENT, Direction.WEST)
                     * .where('6', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .where('7', GTResearchMachines.HPCA_COMPUTATION_COMPONENT, Direction.WEST)
                     * .where('8', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .build());
                     * 
                     * shapeInfo.add(builder.shallowCopy()
                     * .where('0', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .where('1', GTResearchMachines.HPCA_COMPUTATION_COMPONENT, Direction.WEST)
                     * .where('2', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .where('3', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .where('4', GTResearchMachines.HPCA_ADVANCED_COMPUTATION_COMPONENT, Direction.WEST)
                     * .where('5', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .where('6', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .where('7', GTResearchMachines.HPCA_BRIDGE_COMPONENT, Direction.WEST)
                     * .where('8', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .build());
                     * 
                     * shapeInfo.add(builder.shallowCopy()
                     * .where('0', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .where('1', GTResearchMachines.HPCA_ADVANCED_COMPUTATION_COMPONENT, Direction.WEST)
                     * .where('2', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .where('3', GTResearchMachines.HPCA_ACTIVE_COOLER_COMPONENT, Direction.WEST)
                     * .where('4', GTResearchMachines.HPCA_BRIDGE_COMPONENT, Direction.WEST)
                     * .where('5', GTResearchMachines.HPCA_ACTIVE_COOLER_COMPONENT, Direction.WEST)
                     * .where('6', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .where('7', GTResearchMachines.HPCA_ADVANCED_COMPUTATION_COMPONENT, Direction.WEST)
                     * .where('8', GTResearchMachines.HPCA_HEAT_SINK_COMPONENT, Direction.WEST)
                     * .build());
                     * 
                     * return shapeInfo;
                     * })
                     */
                    .sidedWorkableCasingModel(GTCEu.id("block/casings/hpca/advanced_computer_casing"),
                            GTCEu.id("block/multiblock/hpca"))
                    .register();
        }
    }

    public static void init() {
        LargeSteamCompressor.init();
        if (ModList.get().isLoaded("ars_nouveau")) {
            AlchemicalImbuer.init();
        }
    }
}
