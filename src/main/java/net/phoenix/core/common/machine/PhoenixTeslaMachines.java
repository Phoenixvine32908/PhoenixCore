package net.phoenix.core.common.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.phoenix.core.api.pattern.PhoenixPredicates;
import net.phoenix.core.common.block.PhoenixBlocks;
import net.phoenix.core.common.machine.multiblock.electric.TeslaTowerMachine;
import net.phoenix.core.common.machine.multiblock.part.special.TeslaEnergyHatchPartMachine;
import net.phoenix.core.common.machine.singleblock.TeslaWirelessChargerMachine;
import net.phoenix.core.common.registry.PhoenixRegistration;
import net.phoenix.core.datagen.models.PhoenixMachineModels;
import net.phoenix.core.phoenixcore;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.VCF;
import static com.gregtechceu.gtceu.api.GTValues.VOLTAGE_NAMES;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;
import static net.phoenix.core.common.machine.PhoenixMachines.registerTieredMachines;
import static net.phoenix.core.common.registry.PhoenixRegistration.REGISTRATE;

public class PhoenixTeslaMachines {

    public static final MultiblockMachineDefinition TESLA_TOWER = REGISTRATE
            .multiblock("tesla_tower", TeslaTowerMachine::new)
            .langValue("Tesla Tower")
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(PhoenixBlocks.INSANELY_SUPERCHARGED_TESLA_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("BCDDDCB", "BCDDDCB", "BCDDDCB", "BCDDDCB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBBBBBB")
                    .aisle("CDDDDDC", "CDAAADC", "CDAAADC", "CDAAADC", "BCFFFCB", "BBGGGBB", "BBFFFBB", "BBDDDBB")
                    .aisle("DDDDDDD", "DACACAD", "DACACAD", "DACACAD", "BFFAFFB", "BGGAGGB", "BFFAFFB", "BDDDDDB")
                    .aisle("DDDDDDD", "DAAAAAD", "DAAAAAD", "DAAAAAD", "BFAAAFB", "BGAAAGB", "BFAAAFB", "BDDHDDB")
                    .aisle("DDDDDDD", "DECACAD", "DACACAD", "DACACAD", "BFFAFFB", "BGGAGGB", "BFFAFFB", "BDDDDDB")
                    .aisle("CDDDDDC", "CDAAADC", "CDAAADC", "CDAAADC", "BCFFFCB", "BBGGGBB", "BBFFFBB", "BBDDDBB")
                    .aisle("BCDDDCB", "BCDJDCB", "BCDDDCB", "BCDDDCB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBBBBBB")

                    .where('J', controller(blocks(definition.get())))
                    .where('D', blocks(PhoenixBlocks.INSANELY_SUPERCHARGED_TESLA_CASING.get())
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(10)
                                    .setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(10)
                                    .setPreviewCount(1)))
                    .where('E', PhoenixPredicates.teslaBatteries().setExactLimit(1))
                    .where('A', Predicates.air())
                    .where('B', Predicates.any())
                    .where('C', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where('F', blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                    .where('G', blocks(GTBlocks.CASING_TITANIUM_STABLE.get()))
                    .where('H', Predicates.air())
                    .build())
            .workableCasingModel(
                    phoenixcore.id("block/casings/multiblock/tesla_casing"),
                    phoenixcore.id("block/multiblock/tesla_tower"))
            .register();

    public static final MachineDefinition[] TESLA_ENERGY_INPUT_HATCH = registerTieredMachines(
            "tesla_energy_input_hatch",
            (holder, tier) -> new TeslaEnergyHatchPartMachine(holder, tier, IO.OUT, 2),
            (tier, builder) -> builder
                    .langValue(GTValues.VNF[tier] + " Tesla Energy Uplink Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.OUTPUT_ENERGY)
                    .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                    .tooltips(
                            Component.translatable("gtceu.universal.tooltip.voltage_in",
                                    FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_in", 2),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil.formatNumbers(GTValues.V[tier] * 16L * 2)),
                            Component.translatable("tooltip.phoenixcore.tesla_hatch.input"))
                    .overlayTieredHullModel("tesla_hatches/tesla_input")
                    .register(),
            GTValues.ALL_TIERS);

    public static final MachineDefinition[] TESLA_ENERGY_OUTPUT_HATCH = registerTieredMachines(
            "tesla_energy_output_hatch",
            (holder, tier) -> new TeslaEnergyHatchPartMachine(holder, tier, IO.IN, 2),
            (tier, builder) -> builder
                    .langValue(GTValues.VNF[tier] + " Tesla Energy Downlink Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.INPUT_ENERGY) // Acts as INPUT to multiblock (receives from tower, gives to
                    // multiblock)
                    .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                    .tooltips(
                            Component.translatable("gtceu.universal.tooltip.voltage_in",
                                    FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]),
                            Component.translatable("gtceu.universal.tooltip.amperage_out", 2),
                            Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                    FormattingUtil.formatNumbers(GTValues.V[tier] * 64L * 2)),
                            Component.translatable("tooltip.phoenixcore.tesla_hatch.output"))
                    .overlayTieredHullModel("tesla_hatches/tesla_output")
                    .register(),
            GTValues.ALL_TIERS);

    public static MachineDefinition[] registerWirelessCharger(
            GTRegistrate registrate,
            String name,
            BiFunction<IMachineBlockEntity, Integer, TeslaWirelessChargerMachine> factory
    ) {
        return registerChargerTieredMachines(
                registrate,
                name,
                (holder, tier) -> factory.apply(holder, tier),
                (tier, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .modelProperty(GTMachineModelProperties.CHARGER_STATE, ChargerMachine.State.IDLE)
                        .model(PhoenixMachineModels.createWirelessChargerModel())
                        .langValue("%s Tesla Wireless Charger".formatted(
                                VCF[tier] + GTValues.VOLTAGE_NAMES[tier] + ChatFormatting.RESET))
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.voltage_in",
                                        FormattingUtil.formatNumbers(GTValues.V[tier]),
                                        GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.amperage_in", 4),
                                Component.literal("Wireless Range: ").withStyle(ChatFormatting.GRAY)
                                        .append(Component.literal(
                                                        tier >= GTValues.LuV ? "Global (Cross-Dimensional)" :
                                                                (8 * (tier + 1)) + "m")
                                                .withStyle(ChatFormatting.AQUA)),
                                Component.literal("Charges armor and tools from the Team Energy Cloud")
                                        .withStyle(ChatFormatting.GREEN))
                        .register(),
                GTValues.ALL_TIERS
        );
    }
    public static MachineDefinition[] registerChargerTieredMachines(
            GTRegistrate registrate,
            String name,
            BiFunction<IMachineBlockEntity, Integer, MetaMachine> machineFactory,
            BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> definitionBuilder,
            int... tiers
    ) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];

        for (int tier : tiers) {
            var builder = registrate
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> machineFactory.apply(holder, tier))
                    .tier(tier);

            definitions[tier] = definitionBuilder.apply(tier, builder);
        }

        return definitions;
    }
    public static final MachineDefinition[] TESLA_WIRELESS_CHARGER =
            registerWirelessCharger(
                    PhoenixRegistration.REGISTRATE,
                    "tesla_wireless_charger",
                    TeslaWirelessChargerMachine::new
            );


    /*
     * private static MachineDefinition[] registerTeslaEnergyHatch(String name,
     * String displayName,
     * IO io,
     * int[] tiers,
     * int amperage,
     * PartAbility... abilities) {
     * return registerTieredMachines(
     * name,
     * (holder, tier) -> new TeslaEnergyHatchPartMachine(holder, tier, io, amperage),
     * (tier, builder) -> builder
     * .langValue(GTValues.VNF[tier] + " " + displayName)
     * .rotationState(RotationState.ALL)
     * .abilities(abilities)
     * .modelProperty(GTMachineModelProperties.IS_FORMED, false)
     * .overlayTieredHullModel(io == IO.IN
     * ? "tesla_hatches/tesla_input"
     * : "tesla_hatches/tesla_output")
     * .tooltips(
     * Component.translatable(io == IO.IN ? "gtceu.universal.tooltip.voltage_in" :
     * "gtceu.universal.tooltip.voltage_out",
     * FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]),
     * Component.translatable(io == IO.IN ? "gtceu.universal.tooltip.amperage_in" :
     * "gtceu.universal.tooltip.amperage_out", amperage),
     * Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
     * FormattingUtil.formatNumbers(io == IO.IN ? GTValues.V[tier] * 16L * amperage : GTValues.V[tier] * 64L *
     * amperage)),
     * Component.translatable(io == IO.IN ? "tooltip.phoenixcore.tesla_hatch.input" :
     * "tooltip.phoenixcore.tesla_hatch.output"))
     * .register(),
     * tiers);
     * }
     */
    public static void init() {}
}
