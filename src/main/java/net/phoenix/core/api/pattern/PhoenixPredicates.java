package net.phoenix.core.api.pattern;

import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.phoenix.core.PhoenixAPI;
import net.phoenix.core.api.machine.trait.ITeslaBattery;
import net.phoenix.core.common.block.TeslaBatteryBlock;
import net.phoenix.core.common.machine.multiblock.electric.TeslaTowerMachine;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Supplier;

import static net.phoenix.core.common.machine.multiblock.electric.TeslaTowerMachine.TTB_BATTERY_HEADER;

public class PhoenixPredicates {

    public static TraceabilityPredicate teslaBatteries() {
        return new TraceabilityPredicate(blockWorldState -> {
            BlockState state = blockWorldState.getBlockState();

            for (Map.Entry<ITeslaBattery, Supplier<TeslaBatteryBlock>> entry : PhoenixAPI.TESLA_BATTERIES.entrySet()) {
                if (state.is(entry.getValue().get())) {
                    ITeslaBattery battery = entry.getKey();

                    if (battery.getTier() != -1 && battery.getCapacity().compareTo(BigInteger.ZERO) > 0) {
                        String key = TTB_BATTERY_HEADER + battery.getBatteryName();

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
}
