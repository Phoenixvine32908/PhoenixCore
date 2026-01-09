package net.phoenix.core.common.block;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.phoenix.core.api.machine.trait.ITeslaBattery;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

@MethodsReturnNonnullByDefault
public class TeslaBatteryBlock extends Block {

    private final ITeslaBattery batteryData;

    public TeslaBatteryBlock(Properties properties, ITeslaBattery batteryData) {
        super(properties);
        this.batteryData = batteryData;
    }

    public ITeslaBattery getBatteryData() {
        return batteryData;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (batteryData.getTier() == -1) {
            tooltip.add(Component.translatable("block.monilabs.tesla_battery.tooltip_empty"));
        } else {
            tooltip.add(Component.translatable("block.monilabs.tesla_battery.tooltip_filled",
                    FormattingUtil.formatNumbers(batteryData.getCapacity())));
        }
    }


    public enum TeslaBatteryType implements ITeslaBattery {

        EMPTY_TIER_I(-1, 0, 0, 0),
        TIER_I(1, 50_000_000L, 2048L, 2048L),
        TIER_II(2, 200_000_000L, 8192L, 8192L),
        TIER_III(3, 1_000_000_000L, 32768L, 32768L),
        ULTIMATE(4, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);

        private final int tier;
        private final long capacity;
        private final long maxInput;
        private final long maxOutput;

        TeslaBatteryType(int tier, long capacity, long maxInput, long maxOutput) {
            this.tier = tier;
            this.capacity = capacity;
            this.maxInput = maxInput;
            this.maxOutput = maxOutput;
        }

        @Override
        public int getTier() {
            return tier;
        }

        @Override
        public long getCapacity() {
            return capacity;
        }

        @Override
        public long getMaxInput() {
            return maxInput;
        }

        @Override
        public long getMaxOutput() {
            return maxOutput;
        }

        @Override
        public String getBatteryName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
