package net.phoenix.core.api.pattern;

import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.network.chat.Component;
import net.phoenix.core.PhoenixAPI;
import net.phoenix.core.api.block.IFissionCoolerType;
import net.phoenix.core.api.block.IFissionModeratorType;
import net.phoenix.core.common.block.FissionCoolerBlock;
import net.phoenix.core.common.block.FissionModeratorBlock;

import java.util.Comparator;
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
                    Object existing = blockWorldState.getMatchContext().getOrPut("CoolerType", type);

                    if (!existing.equals(type)) {
                        blockWorldState.setError(new PatternStringError(
                                "phoenix.multiblock.pattern.error.single_cooler_type"));
                        return false;
                    }

                    return true;
                }
            }

            return false;
        },
                () -> PhoenixAPI.FISSION_COOLERS.entrySet().stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                        .map(e -> BlockInfo.fromBlockState(e.getValue().get().defaultBlockState()))
                        .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("phoenix.multiblock.pattern.error.single_cooler_type"));
    }

    public static TraceabilityPredicate fissionModerators() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();

            for (Map.Entry<IFissionModeratorType, Supplier<FissionModeratorBlock>> entry : PhoenixAPI.FISSION_MODERATORS
                    .entrySet()) {

                if (blockState.is(entry.getValue().get())) {

                    var type = entry.getKey();
                    Object existing = blockWorldState.getMatchContext().getOrPut("ModeratorType", type);

                    if (!existing.equals(type)) {
                        blockWorldState.setError(new PatternStringError(
                                "phoenix.multiblock.pattern.error.single_moderator_type"));
                        return false;
                    }

                    return true;
                }
            }

            return false;
        },
                () -> PhoenixAPI.FISSION_MODERATORS.entrySet().stream()
                        .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                        .map(e -> BlockInfo.fromBlockState(e.getValue().get().defaultBlockState()))
                        .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("phoenix.multiblock.pattern.error.single_moderator_type"));
    }
}
