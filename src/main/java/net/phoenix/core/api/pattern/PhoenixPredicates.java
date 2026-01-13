package net.phoenix.core.api.pattern;

import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.phoenix.core.PhoenixAPI;
import net.phoenix.core.api.block.IFissionCoolerType;
import net.phoenix.core.api.block.IFissionModeratorType;
import net.phoenix.core.api.machine.trait.ITeslaBattery;
import net.phoenix.core.common.block.FissionCoolerBlock;
import net.phoenix.core.common.block.FissionModeratorBlock;
import net.phoenix.core.common.block.TeslaBatteryBlock;
import net.phoenix.core.common.machine.multiblock.electric.TeslaTowerMachine;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static net.phoenix.core.common.machine.multiblock.electric.TeslaTowerMachine.TTB_BATTERY_HEADER;

public class PhoenixPredicates {

    public static TraceabilityPredicate fissionCoolers() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();

            for (Map.Entry<IFissionCoolerType, Supplier<FissionCoolerBlock>> entry : PhoenixAPI.FISSION_COOLERS
                    .entrySet()) {

                if (blockState.is(entry.getValue().get())) {

                    var type = entry.getKey();

                    List<IFissionCoolerType> componentList = blockWorldState
                            .getMatchContext()
                            .getOrPut("CoolerTypes", new ArrayList<>());
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
    public static TraceabilityPredicate teslaBatteries() {
        return new TraceabilityPredicate(blockWorldState -> {
            BlockState state = blockWorldState.getBlockState();

            for (Map.Entry<ITeslaBattery, Supplier<TeslaBatteryBlock>> entry : PhoenixAPI.TESLA_BATTERIES.entrySet()) {
                if (state.is(entry.getValue().get())) {
                    ITeslaBattery battery = entry.getKey();

                    if (battery.getTier() != -1 && battery.getCapacity().compareTo(BigInteger.ZERO) > 0) {
                        String key = TTB_BATTERY_HEADER + battery.getBatteryName();

                        // Using getOrCreate ensures we don't overwrite existing counts
                        TeslaTowerMachine.BatteryMatchWrapper wrapper = blockWorldState.getMatchContext()
                                .getOrCreate(key, () -> new TeslaTowerMachine.BatteryMatchWrapper(battery));

                        wrapper.increment();
                    }
                    return true;
                }
            }
            return false;
        }, () -> PhoenixAPI.TESLA_BATTERIES.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                .map(entry -> new BlockInfo(entry.getValue().get().defaultBlockState(), null))
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

                    List<IFissionModeratorType> componentList = blockWorldState
                            .getMatchContext()
                            .getOrPut("ModeratorTypes", new ArrayList<>());
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
