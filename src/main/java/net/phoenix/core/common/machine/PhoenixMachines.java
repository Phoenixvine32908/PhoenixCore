package net.phoenix.core.common.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.CleaningMaintenanceHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.core.api.machine.PhoenixPartAbility;
import net.phoenix.core.api.pattern.PhoenixPredicates;
import net.phoenix.core.client.renderer.machine.multiblock.PhoenixDynamicRenderHelpers;
import net.phoenix.core.common.block.PhoenixBlocks;
import net.phoenix.core.common.data.PhoenixRecipeTypes;
import net.phoenix.core.common.data.materials.PhoenixMaterials;
import net.phoenix.core.common.machine.multiblock.BlazingCleanroom;
import net.phoenix.core.common.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.core.common.machine.multiblock.electric.research.PhoenixHPCAMachine;
import net.phoenix.core.common.machine.multiblock.part.ShieldRenderProperty;
import net.phoenix.core.common.machine.multiblock.part.fluid.PlasmaHatchPartMachine;
import net.phoenix.core.common.machine.multiblock.part.special.ShieldSensorHatchPartMachine;
import net.phoenix.core.common.machine.multiblock.part.special.SourceHatchPartMachine;
import net.phoenix.core.configs.PhoenixConfigs;
import net.phoenix.core.datagen.models.PhoenixMachineModels;
import net.phoenix.core.phoenixcore;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.capability.recipe.IO.IN;
import static com.gregtechceu.gtceu.api.capability.recipe.IO.OUT;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.ELECTRIC_TIERS;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;
import static net.phoenix.core.api.machine.PhoenixPartAbility.SOURCE_INPUT;
import static net.phoenix.core.api.machine.PhoenixPartAbility.SOURCE_OUTPUT;
import static net.phoenix.core.common.registry.PhoenixRegistration.REGISTRATE;
import static net.phoenix.core.configs.PhoenixConfigs.INSTANCE;

@SuppressWarnings("all")
public class PhoenixMachines {

    public static final String OVERLAY_PLASMA_HATCH_TEX = "overlay_plasma_hatch_input";
    public static final String OVERLAY_PLASMA_HATCH_HALF_PX_TEX = "overlay_plasma_hatch_half_px_out";
    public static MultiblockMachineDefinition DANCE = null;
    public static MachineDefinition BLAZING_CLEANING_MAINTENANCE_HATCH = null;
    public static MachineDefinition HIGH_YEILD_PHOTON_EMISSION_REGULATER = null;

    static {
        REGISTRATE.creativeModeTab(() -> phoenixcore.PHOENIX_CREATIVE_TAB);
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
                            phoenixcore.id("block/machine/part/overlay_maintenance_blazing_cleaning"))
                    // Tier can always be changed later
                    .register();
        }
    }

    public static final MachineDefinition[] SOURCE_IMPORT_HATCH = registerSourceHatch(
            "source_input_hatch", "Source Input Hatch",
            IO.IN, ELECTRIC_TIERS, SOURCE_INPUT);
    public static final MachineDefinition[] SOURCE_EXPORT_HATCH = registerSourceHatch(
            "source_output_hatch", "Source Output Hatch",
            IO.OUT, ELECTRIC_TIERS, SOURCE_OUTPUT);

    private static MachineDefinition[] registerSourceHatch(String name, String displayName, IO io,
                                                           int[] tiers, PartAbility... abilities) {
        return registerTieredMachines(name,
                (holder, tier) -> new SourceHatchPartMachine((MetaMachineBlockEntity) holder, tier, io),
                (tier, builder) -> builder
                        .langValue(GTValues.VNF[tier] + ' ' + displayName)
                        .abilities(abilities)
                        .rotationState(RotationState.ALL)
                        .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                        .overlayTieredHullModel("source_hatch")
                        .tooltipBuilder((item, tooltip) -> {
                            if (io == IO.IN) {
                                tooltip.add(Component.translatable("tooltip.phoenixcore.source_hatch.capacity",
                                        SourceHatchPartMachine.getMaxCapacity(tier)));
                                tooltip.add(Component.translatable("tooltip.phoenixcore.source_hatch.consumption",
                                        SourceHatchPartMachine.getMaxConsumption(tier)));
                            } else
                                tooltip.add(Component.translatable("tooltip.phoenixcore.source_hatch.capacity",
                                        SourceHatchPartMachine.getMaxCapacity(tier)));
                        }).register(),
                tiers);
    }

    public final static MachineDefinition[] PLASMA_INPUT_HATCH = registerPlasmaHatches(
            "plasma_input_hatch", "Plasma Input Hatch", "fluid_hatch.import",
            IN, PlasmaHatchPartMachine.INITIAL_TANK_CAPACITY_1X, 1, new int[] { 7, 8, 9 },
            PhoenixPartAbility.PLASMA_INPUT);

    public static MachineDefinition[] registerPlasmaHatches(String name, String displayName, String tooltip,
                                                            IO io, int initialCapacity, int slots,
                                                            int[] tiers, PartAbility... abilities) {
        return registerPlasmaHatches(GTRegistration.REGISTRATE, name, displayName, tooltip, io, initialCapacity, slots,
                tiers,
                abilities);
    }

    public static MachineDefinition[] registerPlasmaHatches(GTRegistrate registrate, String name, String displayName,
                                                            String tooltip,
                                                            IO io, int initialCapacity, int slots,
                                                            int[] tiers, PartAbility... abilities) {
        final String pipeOverlay;
        if (slots >= 9) {
            pipeOverlay = "overlay_pipe_9x";
        } else if (slots >= 4) {
            pipeOverlay = "overlay_pipe_4x";
        } else {
            pipeOverlay = null;
        }
        final String ioOverlay = io == OUT ? "overlay_pipe_out_emissive" : "overlay_pipe_in_emissive";
        final String emissiveOverlay = slots > 4 ? OVERLAY_PLASMA_HATCH_HALF_PX_TEX : OVERLAY_PLASMA_HATCH_TEX;
        return registerTieredMachines(name,
                (holder, tier) -> new PlasmaHatchPartMachine(holder, tier, io, initialCapacity, slots),
                (tier, builder) -> {
                    builder.langValue(VNF[tier] + ' ' + displayName)
                            .rotationState(RotationState.ALL)
                            .colorOverlayTieredHullModel(ioOverlay, pipeOverlay, emissiveOverlay)
                            .abilities(abilities)
                            .modelProperty(IS_FORMED, false)
                            .tooltips(Component.translatable("gtceu.machine." + tooltip + ".tooltip"))
                            .allowCoverOnFront(true);

                    if (slots == 1) {
                        builder.tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity",
                                FormattingUtil
                                        .formatNumbers(FluidHatchPartMachine.getTankCapacity(initialCapacity, tier))));
                    } else {
                        builder.tooltips(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity_mult",
                                slots, FormattingUtil
                                        .formatNumbers(FluidHatchPartMachine.getTankCapacity(initialCapacity, tier))));
                    }
                    return builder.register();
                },
                tiers);
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

    public static MachineDefinition SHIELD_INTEGRITY_SENSOR_HATCH = REGISTRATE
            .machine("shield_stability_sensor_hatch", ShieldSensorHatchPartMachine::new)
            .langValue("Shield Stability Sensor Hatch")
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("tooltip.phoenixcore.shield_stability_hatch.0"),
                    Component.translatable("tooltip.phoenixcore.shield_stability_hatch.1"))
            .tier(GTValues.HV)
            .modelProperty(ShieldRenderProperty.TYPE, ShieldRenderProperty.NORMAL)
            .modelProperty(IS_FORMED, false)
            .model(PhoenixMachineModels.createOverlayFillLevelCasingMachineModel("stability_hatch",
                    "casings/microverse"))
            .register();

    static {
        if (PhoenixConfigs.INSTANCE.features.creativeEnergyEnabled) {
            DANCE = REGISTRATE
                    .multiblock("phoenix_infuser", FissionWorkableElectricMultiblockMachine::new)
                    .langValue("§cPhoenix Infuser")
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(PhoenixRecipeTypes.PLEASE) // Agora isso não será mais nulo
                    .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH,
                            GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK),
                            FissionWorkableElectricMultiblockMachine::recipeModifier)
                    .pattern(definition -> FactoryBlockPattern.start()
                            .aisle("BBAAAAAAAAAAAAAAAAAAAAAAAAAAAAABB", "BBBAAAAAAAAAAAAAAAAAAAAAAAAAAABBB",
                                    "ABBBAAAAAAAAAAAAAAAAAAAAAAAAABBBA", "AABBBAAAAAAAAAAACAAAAAAAAAAABBBAA",
                                    "AAABBBAAAAAAAAACCCAAAAAAAAABBBAAA", "AAAABBBAAAAAAACCDCCAAAAAAABBBAAAA",
                                    "AAAAABBBAAAAACCEDECCAAAAABBBAAAAA", "AAAAAABBBAAACCFEDEFCCAAABBBAAAAAA",
                                    "AAAAAAABBBACCFGEHEGFCCABBBAAAAAAA", "AAAAAAAABBBCFIIEHEIIFCBBBAAAAAAAA",
                                    "AAAAAAAAABBBFIIEHEIIFBBBAAAAAAAAA", "AAAAAAAACCBBBFGEHEGFBBBCCAAAAAAAA",
                                    "AAAAAAACCFFBBBFEGEFBBBFFCCAAAAAAA", "AAAAAACCFIIFBBBEGEBBBFIIFCCAAAAAA",
                                    "AAAAACCFGIIGFBBBGBBBFGIIGFCCAAAAA", "AAAACCEEEEEEEEBBBBBEEEEEEEECCAAAA",
                                    "AAACCDDDHHHHGGGBBBGGGHHHHDDDCCAAA", "AAAACCEEEEEEEEBBBBBEEEEEEEECCAAAA",
                                    "AAAAACCFGIIGFBBBGBBBFGIIGFCCAAAAA", "AAAAAACCFIIFBBBEGEBBBFIIFCCAAAAAA",
                                    "AAAAAAACCFFBBBFEGEFBBBFFCCAAAAAAA", "AAAAAAAACCBBBFGEHEGFBBBCCAAAAAAAA",
                                    "AAAAAAAAABBBFIIEHEIIFBBBAAAAAAAAA", "AAAAAAAABBBCFIIEHEIIFCBBBAAAAAAAA",
                                    "AAAAAAABBBACCFGEHEGFCCABBBAAAAAAA", "AAAAAABBBAAACCFEDEFCCAAABBBAAAAAA",
                                    "AAAAABBBAAAAACCEDECCAAAAABBBAAAAA", "AAAABBBAAAAAAACCDCCAAAAAAABBBAAAA",
                                    "AAABBBAAAAAAAAACCCAAAAAAAAABBBAAA", "AABBBAAAAAAAAAAACAAAAAAAAAAABBBAA",
                                    "ABBBAAAAAAAAAAAAAAAAAAAAAAAAABBBA", "BBBAAAAAAAAAAAAAAAAAAAAAAAAAAABBB",
                                    "BBAAAAAAAAAAAAAAAAAAAAAAAAAAAAABB")
                            .aisle("BBBAAAAAAAAAAAAAAAAAAAAAAAAAAABBB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAGGGGGGGAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAJJGGGGGJJAAAAAAAAAAAA", "AAAAAAAAAAAAAGGGGGGGAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BBBAAAAAAAAAAAAAAAAAAAAAAAAAAABBB")
                            .aisle("ABBBAAAAAAAAAAAAAAAAAAAAAAAAABBBA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAJJAAAAAAAJJAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "ABBBAAAAAAAAAAAAAAAAAAAAAAAAABBBA")
                            .aisle("AABBBAAAAAAAAAAAAAAAAAAAAAAABBBAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "CAAAAAAAAAJJAAAAAAAAAJJAAAAAAAAAC", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AABBBAAAAAAAAAAAAAAAAAAAAAAABBBAA")
                            .aisle("AAABBBAAAAAAAAAAAAAAAAAAAAABBBAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAJJAAAAAAAAAAAJJAAAAAAAAC", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAABBBAAAAAAAAAAAAAAAAAAAAABBBAAA")
                            .aisle("AAAABBBAAAAAAAAAAAAAAAAAAABBBAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "DAAAAAAAJJAAAAAAAAAAAAAJJAAAAAAAD", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAABBBAAAAAAAAAAAAAAAAAAABBBAAAA")
                            .aisle("AAAAABBBAAAAAAAACAAAAAAAABBBAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE",
                                    "DAAAAAAJJAAAAAAJJJAAAAAAJJAAAAAAD", "EAAAAAAAAAAAAJJJJJJJAAAAAAAAAAAAE",
                                    "CAAAAAAAAAAAAAAJJJAAAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAABBBAAAAAAAACAAAAAAAABBBAAAAA")
                            .aisle("AAAAAABBBAAAAAACCCAAAAAABBBAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAA", "AAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAA",
                                    "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC", "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC",
                                    "FAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAF", "EAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAE",
                                    "DAAAAAJJEAAAJJJJJJJJJAAAEJJAAAAAD", "EAAAAAAEEAAJJJJJJJJJJJAAEEAAAAAAE",
                                    "FAAAAAAEEAAAJJJJJJJJJAAAEEAAAAAAF", "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC",
                                    "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC", "AAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAA",
                                    "AAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAABBBAAAAAACCCAAAAAABBBAAAAAA")
                            .aisle("AAAAAAABBBAAAACCDCCAAAABBBAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAEEAAAAAAAAAAAAAEEAAAAAAAB",
                                    "AAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAA", "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC",
                                    "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC", "FAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAF",
                                    "GAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAG", "EAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAE",
                                    "HAAAAJJEEAJJJJJJJJJJJJJAEEJJAAAAH", "EAAAAAAEEAJJJJJJJJJJJJJAEEAAAAAAE",
                                    "GAAAAAAEEAJJJJJJJJJJJJJAEEAAAAAAG", "FAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAF",
                                    "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC", "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC",
                                    "AAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAA", "BAAAAAAAEEAAAAAAAAAAAAAEEAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAABBBAAAACCGCCAAAABBBAAAAAAA")
                            .aisle("AAAAAAAABBBAACCEDECCAABBBAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAEEAAAAAAAAAAAEEAAAAAAAAB", "BAAAAAAAEEAAAAAAAAAAAAAEEAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI",
                                    "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI", "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE",
                                    "HAAAJJAAAJJJJAAAAAAAJJJJAAAJJAAAH", "EAAAAAAAAJJJJJAAKAAJJJJJAAAAAAAAE",
                                    "IAAAAAAAAJJJJAAAAAAAJJJJAAAAAAAAI", "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAEEAAAAAAAAAAAAAEEAAAAAAAB",
                                    "BAAAAAAAAEEAAAAAAAAAAAEEAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAABBBAACCEGECCAABBBAAAAAAAA")
                            .aisle("AAAAAAAAABBBCCEEHEECCBBBAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAEEAAAAAAAAAEEAAAAAAAAAA",
                                    "AAAAAAAAAEEAAAAAAAAAAAEEAAAAAAAAA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI",
                                    "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI", "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE",
                                    "HAAJJAAAJJJAAAAAKAAAAAJJJAAAJJAAH", "EAAAAAAAJJJJAAAKKKAAAJJJJAAAAAAAE",
                                    "IAAAAAAAJJJAAAAAKAAAAAJJJAAAAAAAI", "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "AAAAAAAAAEEAAAAAAAAAAAEEAAAAAAAAA", "AAAAAAAAAAEEAAAAAAAAAEEAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAABBBCCEEHEECCBBBAAAAAAAAA")
                            .aisle("AAAAAAAAAABBBDDHHHDDBBBAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAEEAAAAAAAEEAAAAAAAAAAA", "AAAAAAAAAAEEAAAAAAAAAEEAAAAAAAAAA",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF",
                                    "GAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAG", "EAAAAAAAAAAAAAALLLAAAAAAAAAAAAAAE",
                                    "HAJJAAAAJJAAAALLLLLAAAAJJAAAAJJAH", "EAAAAAAJJJJAAALLLLLAAAJJJJAAAAAAE",
                                    "GAAAAAAAJJAAAALLLLLAAAAJJAAAAAAAG", "FAAAAAAAAAAAAAALLLAAAAAAAAAAAAAAF",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "AAAAAAAAAAEEAAAAAAAAAEEAAAAAAAAAA",
                                    "AAAAAAAAAAAEEAAAAAAAEEAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAABBBGGHHHGGBBBAAAAAAAAAA")
                            .aisle("AAAAAAAAAACBBBEEHEEBBBCAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAEEAAAAAEEAAAAAAAAAAAA",
                                    "AAAAAAAAAAAEEAAAAAAAEEAAAAAAAAAAA", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAKAAAKAAAAAAAAAAAAAB",
                                    "FAAAAAAAAAAAAKLLLLLKAAAAAAAAAAAAF", "EAAAAAAAAAAAALLAAALLAAAAAAAAAAAAE",
                                    "GJJAAAAJJJAAALAMMMALAAAJJJAAAAJJG", "EAAAAAAJJJAAALAMMMALAAAJJJAAAAAAE",
                                    "FAAAAAAJJJAAALAMMMALAAAJJJAAAAAAF", "BAAAAAAAAAAAALLAAALLAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAKLLLLLKAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "AAAAAAAAAAAEEAAAAAAAEEAAAAAAAAAAA", "AAAAAAAAAAAAEEAAAAAEEAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAACBBBEEHEEBBBCAAAAAAAAAA")
                            .aisle("AAAAAAAAACCDBBBEGEBBBDCCAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAEEAAAEEAAAAAAAAAAAAA", "AAAAAAAAAAAAEEAAAAAEEAAAAAAAAAAAA",
                                    "CAAAAAAAAAAAAKAAAAAKAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI",
                                    "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI", "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF",
                                    "BAAAAAAAAAAAAAAKAKAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAKLLLLLKAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAKLNAAANLKAAAAAAAAAAAB", "EGAAAAAAAAAALKAAAAAKLAAAAAAAAAAGE",
                                    "GJAAAAAJJAAALKAAAAAKLAAAJJAAAAAJG", "EGAAAAJJJJAALKAAKAAKLAAJJJJAAAAGE",
                                    "BAAAAAAJJAAALKAAAAAKLAAAJJAAAAAAB", "BAAAAAAAAAAALKAAAAAKLAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAKLNAAANLKAAAAAAAAAAAB", "FAAAAAAAAAAAAKLLLLLKAAAAAAAAAAAAF",
                                    "IAAAAAAAAAAAAAKAAAKAAAAAAAAAAAAAI", "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAKAAAKAAAAAAAAAAAAAC", "AAAAAAAAAAAAEEAAAAAEEAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAEEAAAEEAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAACCGBBBEGEBBBGCCAAAAAAAAA")
                            .aisle("AAAAAAAACCEDEBBBGBBBEDECCAAAAAAAA", "AAAAAAAAAAAAAACCCCCAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAEEAEEAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAEEAAAEEAAAAAAAAAAAAA", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAKAAAKAAAAAAAAAAAAAC", "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF",
                                    "GAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAG", "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI",
                                    "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI", "GAAAAAAAAAAAAAAKAKAAAAAAAAAAAAAAG",
                                    "FAAAAAAAAAAAAAKLLLKAAAAAAAAAAAAAF", "BAAAAAAAAAAAKLLAAALLKAAAAAAAAAAAB",
                                    "BAAAAAAAAAAALNKAAAKNLAAAAAAAAAAAB", "BGAAAAAAAAAALAAAAAAALAAAAAAAAAAGB",
                                    "GGAAAAAJJAALAAAAAAAAALAAJJAAAAAGG", "BGAAAAJJJAALAAAAKAAAALAAJJJAAAAGB",
                                    "BAAAAAAJJAALAAAAAAAAALAAJJAAAAAAB", "BAAAAAAAAAAALAAAAAAALAAAAAAAAAAAB",
                                    "FAAAAAAAAAAALNKAAAKNLAAAAAAAAAAAF", "GAAAAAAAAAAAALLAAALLAAAAAAAAAAAAG",
                                    "IAAAAAAAAAAAAKKLLLKKAAAAAAAAAAAAI", "IAAAAAAAAAAAAAAKAKAAAAAAAAAAAAAAI",
                                    "GAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAG", "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF",
                                    "CAAAAAAAAAAAAAAKAKAAAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAAKAAAAAAAAAAAAAAAC",
                                    "AAAAAAAAAAAAAEEKKKEEAAAAAAAAAAAAA", "AAAAAAAAAAAAAAEEAEEAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAACCCCCAAAAAAAAAAAAAA",
                                    "AAAAAAAACCEGEBBBGBBBEGECCAAAAAAAA")
                            .aisle("AAAAAAACCEEHEEBBBBBEEHEECCAAAAAAA", "AAAAAAAAAAAAAACGGGCAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAEEEAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAEEKEEAAAAAAAAAAAAAA",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "EAAAAAAAAAAAAAAKKKAAAAAAAAAAAAAAE", "EAAAAAAAAAAAAAAOOOAAAAAAAAAAAAAAE",
                                    "EAAAAAAAAAAAAAAKKKAAAAAAAAAAAAAAE", "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE",
                                    "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE", "EAAAAAAAAAAAAAKGGGKAAAAAAAAAAAAAE",
                                    "EAAAAAAAAAAAAKLLLLLKAAAAAAAAAAAAE", "EAAAAAAAAAAAALAMMMALAAAAAAAAAAAAE",
                                    "BAAAAAAAAAAALAAAAAAALAAAAAAAAAAAB", "BGAAAAAAAAALAAAAPAAAALAAAAAAAAAGB",
                                    "BGAAAAJJJAALMAAPAPAAMLAAJJJAAAAGB", "BGAAAAJJJAKLMAAAKAAAMLKAJJJAAAAGB",
                                    "BAAAAAJJJAALMAAPAPAAMLAAJJJAAAAAB", "EAAAAAAAAAALAAAAPAAAALAAAAAAAAAAE",
                                    "EAAAAAAAAAAALAAAAAAALAAAAAAAAAAAE", "EAAAAAAAAAAAALAMMMALAAAAAAAAAAAAE",
                                    "EAAAAAAAAAAAAALLLLLAAAAAAAAAAAAAE", "EAAAAAAAAAAAAAKGGGKAAAAAAAAAAAAAE",
                                    "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE", "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE",
                                    "EAAAAAAAAAAAAAAAKAAAAAAAAAAAAAAAE", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAKAAAKAAAAAAAAAAAAAC", "AAAAAAAAAAAAAAEEKEEAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAEEEAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAACGGGCAAAAAAAAAAAAAA",
                                    "AAAAAAACCEEHEEBBBBBEEHEECCAAAAAAA")
                            .aisle("AAAAAACCDDHHHGGBBBGGHHHDDCCAAAAAA", "AAAAAAAAAAAAAACGGGCAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAEEEAAAAAAAAAAAAAAA", "CAAAAAAAAAAAAAAKQKAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAAAKAAAAAAAAAAAAAAAC", "DAAAAAAAAAAAAAAAKAAAAAAAAAAAAAAAD",
                                    "DAAAAAAAAAAAAAAKKKAAAAAAAAAAAAAAD", "DAAAAAAAAAAAAAAOOOAAAAAAAAAAAAAAD",
                                    "HAAAAAAAAAAAAAAKKKAAAAAAAAAAAAAAH", "HAAAAAAAAAAAAAAAKAAAAAAAAAAAAAAAH",
                                    "HAAAAAAAAAAAAAAAKAAAAAAAAAAAAAAAH", "HAAAAAAAAAAAAAAGQGAAAAAAAAAAAAAAH",
                                    "GAAAAAAAAAAAAALLLLLAAAAAAAAAAAAAG", "GAAAAAAAAAAAALAMMMALAAAAAAAAAAAAG",
                                    "GAAAAAAAAAAALAAAKAAALAAAAAAAAAAAG", "BGAAAAAGAAALAAAPKPAAALAAAAAAAAAGB",
                                    "BGAAAAJJJAKLMAAAKAAAMLKAJJJAAAAGB", "BGAAAAJJJKKLMKKKRKKKMLKKJJJAAAAGB",
                                    "GAAAAAJJJAKLMAAAKAAAMLKAJJJAAAAAG", "GAAAAAAAAAALAAAPKPAAALAAAAAAAAAAG",
                                    "GAAAAAAAAAAALAAAKAAALAAAAAAAAAAAG", "HAAAAAAAAAAAALAMMMALAAAAAAAAAAAAH",
                                    "HAAAAAAAAAAAAALLLLLAAAAAAAAAAAAAH", "HAAAAAAAAAAAAAAGQGAAAAAAAAAAAAAAH",
                                    "HAAAAAAAAAAAAAAAKAAAAAAAAAAAAAAAH", "DAAAAAAAAAAAAAAAKAAAAAAAAAAAAAAAD",
                                    "DAAAAAAAAAAAAAAKKKAAAAAAAAAAAAAAD", "DAAAAAAAAAAAAAKAKAKAAAAAAAAAAAAAD",
                                    "CAAAAAAAAAAAAAKAKAKAAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAKQKAAAAAAAAAAAAAAC",
                                    "AAAAAAAAAAAAAAAEEEAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAACGGGCAAAAAAAAAAAAAA",
                                    "AAAAAACCGGHHHGGBBBGGHHHGGCCAAAAAA")
                            .aisle("AAAAAAACCEEHEEBBBBBEEHEECCAAAAAAA", "AAAAAAAAAAAAAACGGGCAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAEEEAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAEEKEEAAAAAAAAAAAAAA",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "EAAAAAAAAAAAAAAKKKAAAAAAAAAAAAAAE", "EAAAAAAAAAAAAAAOOOAAAAAAAAAAAAAAE",
                                    "EAAAAAAAAAAAAAAKKKAAAAAAAAAAAAAAE", "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE",
                                    "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE", "EAAAAAAAAAAAAAKGGGKAAAAAAAAAAAAAE",
                                    "EAAAAAAAAAAAAKLLLLLKAAAAAAAAAAAAE", "EAAAAAAAAAAAALAMMMALAAAAAAAAAAAAE",
                                    "BAAAAAAAAAAALAAAAAAALAAAAAAAAAAAB", "BGAAAAAAAAALAAAAPAAAALAAAAAAAAAGB",
                                    "BGAAAAJJJAALMAAPAPAAMLAAJJJAAAAGB", "BGAAAAJJJAKLMAAAKAAAMLKAJJJAAAAGB",
                                    "BAAAAAJJJAALMAAPAPAAMLAAJJJAAAAAB", "EAAAAAAAAAALAAAAPAAAALAAAAAAAAAAE",
                                    "EAAAAAAAAAAALAAAAAAALAAAAAAAAAAAE", "EAAAAAAAAAAAALAMMMALAAAAAAAAAAAAE",
                                    "EAAAAAAAAAAAAALLLLLAAAAAAAAAAAAAE", "EAAAAAAAAAAAAAKGGGKAAAAAAAAAAAAAE",
                                    "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE", "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE",
                                    "EAAAAAAAAAAAAAAAKAAAAAAAAAAAAAAAE", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAKAKAKAAAAAAAAAAAAAC", "AAAAAAAAAAAAAAEEKEEAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAEEEAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAACGGGCAAAAAAAAAAAAAA",
                                    "AAAAAAACCEEHEEBBBBBEEHEECCAAAAAAA")
                            .aisle("AAAAAAAACCEDEBBBGBBBEDECCAAAAAAAA", "AAAAAAAAAAAAAACCCCCAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAEEAEEAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAEEAAAEEAAAAAAAAAAAAA", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAKAAAKAAAAAAAAAAAAAC", "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF",
                                    "GAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAG", "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI",
                                    "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI", "GAAAAAAAAAAAAAAKAKAAAAAAAAAAAAAAG",
                                    "FAAAAAAAAAAAAAKLLLKAAAAAAAAAAAAAF", "BAAAAAAAAAAAKLLAAALLKAAAAAAAAAAAB",
                                    "BAAAAAAAAAAALNKAAAKNLAAAAAAAAAAAB", "BGAAAAAAAAAALAAAAAAALAAAAAAAAAAGB",
                                    "GGAAAAAJJAALAAAAAAAAALAAJJAAAAAGG", "BGAAAAJJJAALAAAAKAAAALAAJJJAAAAGB",
                                    "BAAAAAAJJAALAAAAAAAAALAAJJAAAAAAB", "BAAAAAAAAAAALAAAAAAALAAAAAAAAAAAB",
                                    "FAAAAAAAAAAALNKAAAKNLAAAAAAAAAAAF", "GAAAAAAAAAAAALLAAALLAAAAAAAAAAAAG",
                                    "IAAAAAAAAAAAAKKLLLKKAAAAAAAAAAAAI", "IAAAAAAAAAAAAAAKAKAAAAAAAAAAAAAAI",
                                    "GAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAG", "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF",
                                    "CAAAAAAAAAAAAAAKAKAAAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAAKAAAAAAAAAAAAAAAC",
                                    "AAAAAAAAAAAAAEEKKKEEAAAAAAAAAAAAA", "AAAAAAAAAAAAAAEEAEEAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAACCCCCAAAAAAAAAAAAAA",
                                    "AAAAAAAACCEGEBBBGBBBEGECCAAAAAAAA")
                            .aisle("AAAAAAAAACCDBBBEGEBBBDCCAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAEEAAAEEAAAAAAAAAAAAA", "AAAAAAAAAAAAEEAAAAAEEAAAAAAAAAAAA",
                                    "CAAAAAAAAAAAAKAAAAAKAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI",
                                    "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI", "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF",
                                    "BAAAAAAAAAAAAAAKAKAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAKLLLLLKAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAKLNAAANLKAAAAAAAAAAAB", "EGAAAAAAAAAALKAAAAAKLAAAAAAAAAAGE",
                                    "GJAAAAAJJAAALKAAAAAKLAAAJJAAAAAJG", "EGAAAAJJJJAALKAAKAAKLAAJJJJAAAAGE",
                                    "BAAAAAAJJAAALKAAAAAKLAAAJJAAAAAAB", "BAAAAAAAAAAALKAAAAAKLAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAKLNAAANLKAAAAAAAAAAAB", "FAAAAAAAAAAAAKLLLLLKAAAAAAAAAAAAF",
                                    "IAAAAAAAAAAAAAKAAAKAAAAAAAAAAAAAI", "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAKAAAKAAAAAAAAAAAAAC", "AAAAAAAAAAAAEEAAAAAEEAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAEEAAAEEAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAACCGBBBEGEBBBGCCAAAAAAAAA")
                            .aisle("AAAAAAAAAACBBBEEHEEBBBCAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAEEAAAAAEEAAAAAAAAAAAA",
                                    "AAAAAAAAAAAEEAAAAAAAEEAAAAAAAAAAA", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAKAAAKAAAAAAAAAAAAAB",
                                    "FAAAAAAAAAAAAKLLLLLKAAAAAAAAAAAAF", "EAAAAAAAAAAAALLAAALLAAAAAAAAAAAAE",
                                    "GJJAAAAJJJAAALAMMMALAAAJJJAAAAJJG", "EAAAAAAJJJAAALAMMMALAAAJJJAAAAAAE",
                                    "FAAAAAAJJJAAALAMMMALAAAJJJAAAAAAF", "BAAAAAAAAAAAALLAAALLAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAKLLLLLKAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "AAAAAAAAAAAEEAAAAAAAEEAAAAAAAAAAA", "AAAAAAAAAAAAEEAAAAAEEAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAACBBBEEHEEBBBCAAAAAAAAAA")
                            .aisle("AAAAAAAAAABBBDDHHHDDBBBAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAEEAAAAAAAEEAAAAAAAAAAA", "AAAAAAAAAAEEAAAAAAAAAEEAAAAAAAAAA",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF",
                                    "GAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAG", "EAAAAAAAAAAAAAALLLAAAAAAAAAAAAAAE",
                                    "HAJJAAAAJJAAAALLLLLAAAAJJAAAAJJAH", "EAAAAAAJJJJAAALLLLLAAAJJJJAAAAAAE",
                                    "GAAAAAAAJJAAAALLLLLAAAAJJAAAAAAAG", "FAAAAAAAAAAAAAALLLAAAAAAAAAAAAAAF",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "AAAAAAAAAAEEAAAAAAAAAEEAAAAAAAAAA",
                                    "AAAAAAAAAAAEEAAAAAAAEEAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAABBBGGHHHGGBBBAAAAAAAAAA")
                            .aisle("AAAAAAAAABBBCCEEHEECCBBBAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAEEAAAAAAAAAEEAAAAAAAAAA",
                                    "AAAAAAAAAEEAAAAAAAAAAAEEAAAAAAAAA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI",
                                    "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI", "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE",
                                    "HAAJJAAAJJJAAAAAKAAAAAJJJAAAJJAAH", "EAAAAAAAJJJJAAAKKKAAAJJJJAAAAAAAE",
                                    "IAAAAAAAJJJAAAAAKAAAAAJJJAAAAAAAI", "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "AAAAAAAAAEEAAAAAAAAAAAEEAAAAAAAAA", "AAAAAAAAAAEEAAAAAAAAAEEAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAABBBCCEEHEECCBBBAAAAAAAAA")
                            .aisle("AAAAAAAABBBAACCEDECCAABBBAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAEEAAAAAAAAAAAEEAAAAAAAAB", "BAAAAAAAEEAAAAAAAAAAAAAEEAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI",
                                    "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI", "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE",
                                    "HAAAJJAAAJJJJAAAAAAAJJJJAAAJJAAAH", "EAAAAAAAAJJJJJAAKAAJJJJJAAAAAAAAE",
                                    "IAAAAAAAAJJJJAAAAAAAJJJJAAAAAAAAI", "IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI",
                                    "FAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAF", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAEEAAAAAAAAAAAAAEEAAAAAAAB",
                                    "BAAAAAAAAEEAAAAAAAAAAAEEAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAABBBAACCEGECCAABBBAAAAAAAA")
                            .aisle("AAAAAAABBBAAAACCDCCAAAABBBAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAEEAAAAAAAAAAAAAEEAAAAAAAB",
                                    "AAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAA", "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC",
                                    "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC", "FAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAF",
                                    "GAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAG", "EAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAE",
                                    "HAAAAJJEEAJJJJJJJJJJJJJAEJJJAAAAH", "EAAAAAAEEAJJJJJJJJJJJJJAEEAAAAAAE",
                                    "GAAAAAAEEAJJJJJJJJJJJJJAEEAAAAAAG", "FAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAF",
                                    "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC", "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC",
                                    "AAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAA", "BAAAAAAAEEAAAAAAAAAAAAAEEAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAABBBAAAACCGCCAAAABBBAAAAAAA")
                            .aisle("AAAAAABBBAAAAAACCCAAAAAABBBAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAA", "AAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAA",
                                    "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC", "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC",
                                    "FAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAF", "EAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAE",
                                    "DAAAAAJJEAAAJJJJJJJJJAAAJJJAAAAAD", "EAAAAAAEEAAJJJJJJJJJJJAAEEAAAAAAE",
                                    "FAAAAAAEEAAAJJJJJJJJJAAAEEAAAAAAF", "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC",
                                    "CAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAC", "AAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAA",
                                    "AAAAAAAEEAAAAAAAAAAAAAAAEEAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAABBBAAAAAACCCAAAAAABBBAAAAAA")
                            .aisle("AAAAABBBAAAAAAAACAAAAAAAABBBAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE",
                                    "DAAAAAAJJAAAAAAJJJAAAAAAJJAAAAAAD", "EAAAAAAAAAAAAJJJJJJJAAAAAAAAAAAAE",
                                    "CAAAAAAAAAAAAAAJJJAAAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAABBBAAAAAAAACAAAAAAAABBBAAAAA")
                            .aisle("AAAABBBAAAAAAAAAAAAAAAAAAABBBAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "DAAAAAAAJJAAAAAAAAAAAAAJJAAAAAAAD", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAABBBAAAAAAAAAAAAAAAAAAABBBAAAA")
                            .aisle("AAABBBAAAAAAAAAAAAAAAAAAAAABBBAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "CAAAAAAAAJJAAAAAAAAAAAJJAAAAAAAAC", "CAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAABBBAAAAAAAAAAAAAAAAAAAAABBBAAA")
                            .aisle("AABBBAAAAAAAAAAAAAAAAAAAAAAABBBAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "CAAAAAAAAAJJAAAAAAAAAJJAAAAAAAAAC", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AABBBAAAAAAAAAAAAAAAAAAAAAAABBBAA")
                            .aisle("ABBBAAAAAAAAAAAAAAAAAAAAAAAAABBBA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAJJAAAAAAAJJAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "ABBBAAAAAAAAAAAAAAAAAAAAAAAAABBBA")
                            .aisle("BBBAAAAAAAAAAAAAAAAAAAAAAAAAAABBB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAGGGGGGGAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAJJGGGGGJJAAAAAAAAAAAA", "AAAAAAAAAAAAAGGGGGGGAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                                    "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB", "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB",
                                    "BBBAAAAAAAAAAAAAAAAAAAAAAAAAAABBB")
                            .aisle("BBAAAAAAAAAAAAAAAAAAAAAAAAAAAAABB", "BBBAAAAAAAAAAAAAAAAAAAAAAAAAAABBB",
                                    "ABBBAAAAAAAAAAAAAAAAAAAAAAAAABBBA", "AABBBAAAAAAAAAAACAAAAAAAAAAABBBAA",
                                    "AAABBBAAAAAAAAACCCAAAAAAAAABBBAAA", "AAAABBBAAAAAAACCDCCAAAAAAABBBAAAA",
                                    "AAAAABBBAAAAACCEDECCAAAAABBBAAAAA", "AAAAAABBBAAACCFEDEFCCAAABBBAAAAAA",
                                    "AAAAAAABBBACCFGEHEGFCCABBBAAAAAAA", "AAAAAAAABBBCFIIEHEIIFCBBBAAAAAAAA",
                                    "AAAAAAAAABBBFIIEHEIIFBBBAAAAAAAAA", "AAAAAAAACCBBBFGEHEGFBBBCCAAAAAAAA",
                                    "AAAAAAACCFFBBBFEGEFBBBFFCCAAAAAAA", "AAAAAACCFIIFBBBEGEBBBFIIFCCAAAAAA",
                                    "AAAAACCFGIIGFBBBGBBBFGIIGFCCAAAAA", "AAAACCEEEEEEEEBBBBBEEEEEEEECCAAAA",
                                    "AAACCDDDHHHHGGGBSBGGGHHHHDDDCCAAA", "AAAACCEEEEEEEEBBBBBEEEEEEEECCAAAA",
                                    "AAAAACCFGIIGFBBBGBBBFGIIGFCCAAAAA", "AAAAAACCFIIFBBBEGEBBBFIIFCCAAAAAA",
                                    "AAAAAAACCFFBBBFEGEFBBBFFCCAAAAAAA", "AAAAAAAACCBBBFGEHEGFBBBCCAAAAAAAA",
                                    "AAAAAAAAABBBFIIEHEIIFBBBAAAAAAAAA", "AAAAAAAABBBCFIIEHEIIFCBBBAAAAAAAA",
                                    "AAAAAAABBBACCFGEHEGFCCABBBAAAAAAA", "AAAAAABBBAAACCFEDEFCCAAABBBAAAAAA",
                                    "AAAAABBBAAAAACCEDECCAAAAABBBAAAAA", "AAAABBBAAAAAAACCDCCAAAAAAABBBAAAA",
                                    "AAABBBAAAAAAAAACCCAAAAAAAAABBBAAA", "AABBBAAAAAAAAAAACAAAAAAAAAAABBBAA",
                                    "ABBBAAAAAAAAAAAAAAAAAAAAAAAAABBBA", "BBBAAAAAAAAAAAAAAAAAAAAAAAAAAABBB",
                                    "BBAAAAAAAAAAAAAAAAAAAAAAAAAAAAABB")
                            .where('A', any())
                            .where('B',
                                    blocks(PhoenixBlocks.PHOENIX_ENRICHED_TRITANIUM_CASING.get())
                                            .setMinGlobalLimited(575).setPreviewCount(1200)
                                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                            .or(Predicates.abilities(PhoenixPartAbility.PLASMA_INPUT)
                                                    .setPreviewCount(1))
                                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2)
                                                    .setMinGlobalLimited(1))
                                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1)
                                                    .or(Predicates.abilities(PartAbility.IMPORT_ITEMS)
                                                            .setPreviewCount(1)))
                                            .or(autoAbilities(true, false, true)))
                            .where('C',
                                    blocks(ChemicalHelper.getBlock(TagPrefix.frameGt,
                                            PhoenixMaterials.PHOENIX_ENRICHED_TRITANIUM)))
                            .where('D', blocks(ADVANCED_COMPUTER_CASING.get()))
                            .where('E', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Neutronium)))
                            .where('F', blocks(COMPUTER_CASING.get()))
                            .where('G', blocks(PhoenixBlocks.COIL_TRUE_HEAT_STABLE.get()))
                            .where('H', blocks(PhoenixBlocks.STABLE_LOGIC_CASING.get()))
                            .where('I', blocks(GTResearchMachines.HPCA_BRIDGE_COMPONENT.get()))
                            .where('J', blocks(PhoenixBlocks.RELIABLE_NAQUADAH_ALLOY_MACHINE_CASING.get()))
                            .where('K',
                                    blocks(ChemicalHelper.getBlock(TagPrefix.frameGt,
                                            PhoenixMaterials.PHOENIX_ENRICHED_NAQUADAH)))
                            .where('L', blocks(PhoenixBlocks.SUPER_STABLE_FUSION_CASING.get())
                                    .or(blocks(ForgeRegistries.BLOCKS
                                            .getValue(ResourceLocation.fromNamespaceAndPath("kubejs",
                                                    "phoenix_gaze_panel")))))
                            .where('M', blocks(PhoenixBlocks.BLAZING_CORE_STABILIZER.get()))
                            .where("N",
                                    blocks(ForgeRegistries.BLOCKS.getValue(
                                            ResourceLocation.fromNamespaceAndPath("draconicevolution",
                                                    "awakened_draconium_block"))))
                            .where("O",
                                    blocks(ForgeRegistries.BLOCKS
                                            .getValue(ResourceLocation.fromNamespaceAndPath("expatternprovider",
                                                    "fishbig"))))
                            .where('P', blocks(PhoenixBlocks.GLITCHED_ENTROPY_CASING.get()))
                            .where("Q",
                                    blocks(ForgeRegistries.BLOCKS
                                            .getValue(ResourceLocation.fromNamespaceAndPath("ae2",
                                                    "creative_energy_cell"))))
                            .where('R', blocks(PhoenixBlocks.PHOENIX_HEART_CASING.get()))
                            .where('S', controller(blocks(definition.getBlock())))
                            .build())
                    .model(
                            createWorkableCasingMachineModel(
                                    phoenixcore.id("block/phoenix_enriched_tritanium_casing"),
                                    GTCEu.id("block/multiblock/fusion_reactor"))
                                    .andThen(d -> d
                                            .addDynamicRenderer(
                                                    PhoenixDynamicRenderHelpers::getPlasmaArcFurnaceRenderer)))
                    .hasBER(true)
                    .register();
        }
    }

    static {
        if (PhoenixConfigs.INSTANCE.features.PHPCAEnabled) {
            HIGH_YEILD_PHOTON_EMISSION_REGULATER = REGISTRATE
                    .multiblock("high_yield_photon_emission_regulator", PhoenixHPCAMachine::new)
                    .langValue("§dHigh Yield Photon Emission Regulator (HPCA)")
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
                    .sidedWorkableCasingModel(GTCEu.id("block/casings/hpca/advanced_computer_casing"),
                            GTCEu.id("block/multiblock/hpca"))
                    .register();
        }
    }

    public static final MultiblockMachineDefinition HIGH_PERFORMANCE_BREEDER_REACTOR = REGISTRATE
            .multiblock("high_performance_breeder_reactor", FissionWorkableElectricMultiblockMachine::new)
            .langValue("§bHigh Performance Breeder Reactor")
            .recipeType(PhoenixRecipeTypes.FISSION_RECIPES)
            .generator(true)
            .regressWhenWaiting(false)
            .recipeModifier(FissionWorkableElectricMultiblockMachine::recipeModifier)
            .appearanceBlock(PhoenixBlocks.FISSILE_REACTION_SAFE_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("BBCCCCCBB", "BBDEEEDBB", "BBDEEEDBB", "BBDEEEDBB", "BBDFFFDBB", "BBDFFFDBB", "BBCFFFCBB",
                            "BBCFFFCBB", "BBBBBBBBB")
                    .aisle("BCCCCCCCB", "BGAAAAHGB", "BGAAAAHGB", "BGAAAAHGB", "BGAAAAHGB", "BGAAAAHGB", "BCAAAAHCB",
                            "BCAAAAHCB", "BBCCCCCBB")
                    .aisle("CCCCCCCCC", "DHIAAAIHD", "DHIAAAIHD", "DHIAAAIHD", "DHIAAAIHD", "DHIJJJIHD", "CHIKKKIHC",
                            "CHIAAAIHC", "BCCCCCCCB")
                    .aisle("CCCCCCCCC", "EAALILAAE", "EAALILAAE", "EAALILAAE", "FAALILAAF", "FAJJIJJAF", "FAKKIKKAF",
                            "FAAAIAAAF", "BCCGGGCCB")
                    .aisle("CCCCCCCCC", "EAAIJIAAE", "EAAIJIAAE", "EAAIJIAAE", "FAAIJIAAF", "FAJIJIJAF", "FAKIDIKAF",
                            "FAAIDIAAF", "BCCGJGCCB")
                    .aisle("CCCCCCCCC", "EAALILAAE", "EAALILAAE", "EAALILAAE", "FAALILAAF", "FAJJIJJAF", "FAKKIKKAF",
                            "FAAAIAAAF", "BCCGGGCCB")
                    .aisle("CCCCCCCCC", "DHIAAAIHD", "DHIAAAIHD", "DHIAAAIHD", "DHIAAAIHD", "DHIJJJIHD", "CHIKKKIHC",
                            "CHIAAAIHC", "BCCCCCCCB")
                    .aisle("BCCCCCCCB", "BGHAAAHGB", "BGHAAAHGB", "BGHAAAHGB", "BGHAAAHGB", "BGHAAAHGB", "BCHAAAHCB",
                            "BCHAAAHCB", "BBCCCCCBB")
                    .aisle("BBCCMCCBB", "BBDEEEDBB", "BBDEEEDBB", "BBDEEEDBB", "BBDFFFDBB", "BBDFFFDBB", "BBCFFFCBB",
                            "BBCFFFCBB", "BBBBBBBBB")
                    .where('A', Predicates.air())
                    .where('B', Predicates.any())
                    .where("C", blocks(PhoenixBlocks.FISSILE_REACTION_SAFE_CASING.get()).setMinGlobalLimited(10)
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.SUBSTATION_OUTPUT_ENERGY).setMaxGlobalLimited(2))
                            .or(Predicates.autoAbilities(definition.getRecipeTypes())))
                    .where('D', blocks(PhoenixBlocks.FISSILE_HEAT_SAFE_CASING.get()))
                    .where('E', blocks(Blocks.TINTED_GLASS))
                    .where('F', Predicates.blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                    .where("G", Predicates.blocks(GCYMBlocks.HEAT_VENT.get()))
                    .where("H", Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                    .where('I', PhoenixPredicates.fissionModerators())
                    .where("J", Predicates.blocks(COIL_HSSG.get()))
                    .where("K", PhoenixPredicates.fissionCoolers())
                    .where("L", Predicates.blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                    .where("M", Predicates.controller(Predicates.blocks(definition.get())))
                    .build())
            .model(
                    createWorkableCasingMachineModel(
                            phoenixcore.id("block/fission/fissile_reaction_safe_casing"),
                            GTCEu.id("block/multiblock/fusion_reactor")))
            .register();

    public static void init() {}
}
