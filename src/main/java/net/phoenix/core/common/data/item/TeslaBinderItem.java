package net.phoenix.core.common.data.item;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TeslaBinderItem extends ComponentItem {

    public TeslaBinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (stack.hasTag() && stack.getTag().contains("OwnerName")) {
            String ownerName = stack.getTag().getString("OwnerName");

            int color = getAnimatedColor(0xA330FF, 0xFF66CC, 2000);

            tooltip.add(
                    Component.literal("Bound to: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(ownerName)
                                    .withStyle(Style.EMPTY.withColor(color)))
            );
        } else {
            tooltip.add(
                    Component.translatable("item.phoenix.tesla_binder.desc")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }


    private int getAnimatedColor(int color1, int color2, int duration) {
        float time = (System.currentTimeMillis() % duration) / (float) duration;
        // Smooth sine oscillation
        float phase = (float) Math.sin(time * 2 * Math.PI) * 0.5f + 0.5f;

        int r = (int) (((color1 >> 16) & 0xFF) + (((color2 >> 16) & 0xFF) - ((color1 >> 16) & 0xFF)) * phase);
        int g = (int) (((color1 >> 8) & 0xFF) + (((color2 >> 8) & 0xFF) - ((color1 >> 8) & 0xFF)) * phase);
        int b = (int) ((color1 & 0xFF) + ((color2 & 0xFF) - (color1 & 0xFF)) * phase);

        return (r << 16) | (g << 8) | b;
    }
}
