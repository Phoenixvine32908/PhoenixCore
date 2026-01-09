package net.phoenix.core.api.pattern;

import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.network.chat.Component;
import net.phoenix.core.PhoenixAPI;
import net.phoenix.core.api.block.IFissionCoolerType;
import net.phoenix.core.api.block.IFissionModeratorType;
import net.phoenix.core.api.machine.trait.ITeslaBattery;
import net.phoenix.core.common.block.FissionCoolerBlock;
import net.phoenix.core.common.block.FissionModeratorBlock;
import net.phoenix.core.common.block.TeslaBatteryBlock;
import net.phoenix.core.common.machine.multiblock.electric.TeslaTowerMachine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PhoenixPredicates {

    public static TraceabilityPredicate fissionCoolers() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();

            for (Map.Entry<IFissionCoolerType, Supplier<FissionCoolerBlock>> entry : PhoenixAPI.FISSION_COOLERS
                    .entrySet()) {

                if (blockState.is(entry.getValue().get())) {

                    var type = entry.getKey();

                    @SuppressWarnings("unchecked")
                    List<IFissionCoolerType> componentList = (List<IFissionCoolerType>) blockWorldState
                            .getMatchContext()
                            .getOrPut("CoolerTypes", new ArrayList<IFissionCoolerType>());
                    componentList.add(type);

                    return true;
                }
            }

            return false;
        },
                () -> PhoenixAPI.FISSION_COOLERS.entrySet().stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                        .map(e -> BlockInfo.fromBlockState(e.getValue().get().defaultBlockState()))
                        .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("phoenix.multiblock.pattern.info.multiple_coolers"));
    }

    public static final String TTB_BATTERY_HEADER = "TeslaTowerBattery_";

    public static TraceabilityPredicate teslaBatteries() {
        return new TraceabilityPredicate(blockWorldState -> {
            var state = blockWorldState.getBlockState();

            for (Map.Entry<ITeslaBattery, java.util.function.Supplier<net.phoenix.core.common.block.TeslaBatteryBlock>> entry : PhoenixAPI.TESLA_BATTERIES
                    .entrySet()) {

                if (state.is(entry.getValue().get())) {
                    ITeslaBattery battery = entry.getKey();


                    if (battery.getTier() != -1 && battery.getCapacity() > 0) {
                        String key = TTB_BATTERY_HEADER + battery.getBatteryName();


                        TeslaTowerMachine.BatteryMatchWrapper wrapper = blockWorldState.getMatchContext().get(key);
                        if (wrapper == null) wrapper = new TeslaTowerMachine.BatteryMatchWrapper(battery);

                        blockWorldState.getMatchContext().set(key, wrapper.increment());
                    }
                    return true;
                }
            }
            return false;
        }, () -> PhoenixAPI.TESLA_BATTERIES.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                .map(e -> new BlockInfo(e.getValue().get().defaultBlockState(), null))
                .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.batteries"));
    }

    public static TraceabilityPredicate fissionModerators() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();

            for (Map.Entry<IFissionModeratorType, Supplier<FissionModeratorBlock>> entry : PhoenixAPI.FISSION_MODERATORS
                    .entrySet()) {

                if (blockState.is(entry.getValue().get())) {

                    var type = entry.getKey();

                    @SuppressWarnings("unchecked")
                    List<IFissionModeratorType> componentList = (List<IFissionModeratorType>) blockWorldState
                            .getMatchContext()
                            .getOrPut("ModeratorTypes", new ArrayList<IFissionModeratorType>());
                    componentList.add(type);

                    return true;
                }
            }

            return false;
        },
                () -> PhoenixAPI.FISSION_MODERATORS.entrySet().stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                        .map(e -> BlockInfo.fromBlockState(e.getValue().get().defaultBlockState()))
                        .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("phoenix.multiblock.pattern.info.multiple_moderators"));
    }
}
