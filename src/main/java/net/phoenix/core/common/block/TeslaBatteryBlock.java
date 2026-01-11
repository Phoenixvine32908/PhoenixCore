package net.phoenix.core.common.block;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.phoenix.core.api.machine.trait.ITeslaBattery;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

@Getter
@MethodsReturnNonnullByDefault
public class TeslaBatteryBlock extends Block {

    private final ITeslaBattery batteryData;

    public TeslaBatteryBlock(Properties properties, ITeslaBattery batteryData) {
        super(properties);
        this.batteryData = batteryData;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (batteryData.getTier() == -1) {
            tooltip.add(Component.translatable("block.monilabs.tesla_battery.tooltip_empty"));
        } else {
            tooltip.add(Component.translatable("block.monilabs.tesla_battery.tooltip_filled",
                    FormattingUtil.formatNumbers(batteryData.getCapacity())));
        }
    }

    public enum TeslaBatteryType implements ITeslaBattery {

        UHV(9, 10_000_000_000L),
        UEV(10, 50_000_000_000L),
        UIV(11, 250_000_000_000L),
        UXV(12, 1_000_000_000_000L),
        OPV(13, 10_000_000_000_000L),
        MAX(14, Long.MAX_VALUE);

        private final int tier;
        private final long capacity;

        TeslaBatteryType(int tier, long capacity) {
            this.tier = tier;
            this.capacity = capacity;
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
        public String getBatteryName() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public long getMaxInput() {
            return Long.MAX_VALUE;
        }

        @Override
        public long getMaxOutput() {
            return Long.MAX_VALUE;
        }
    }
}
