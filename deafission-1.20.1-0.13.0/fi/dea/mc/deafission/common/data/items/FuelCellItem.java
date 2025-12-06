//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.items;

import com.tterrag.registrate.util.entry.ItemEntry;
import fi.dea.mc.deafission.FissionMod;
import fi.dea.mc.deafission.api.ReactorFuel;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FuelCellItem extends Item {
    public final int Rods;
    public final double Heat;

    public FuelCellItem(Item.Properties properties, int rods, double heat) {
        super(properties);
        this.Rods = rods;
        this.Heat = heat;
    }

    public FuelCellItem(int durability, int rods, double heat) {
        this(makeProps(durability), rods, heat);
    }

    private static Item.Properties makeProps(int durability) {
        return (new Item.Properties()).m_41497_(Rarity.UNCOMMON).m_41503_(durability).setNoRepair();
    }

    public void m_7373_(ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        FuelCellItem item = (FuelCellItem)stack.m_41720_();
        ReactorFuel stats = item.getFuelStats(stack);
        if (Screen.m_96639_()) {
            tooltip.add(Component.m_237110_("tooltip.deafission.fuelcell.rodcount", new Object[]{stats.rods()}).m_130940_(ChatFormatting.GOLD));
        }

        tooltip.add(Component.m_237110_("tooltip.deafission.fuelcell.heat", new Object[]{Math.round(stats.heat() * (double)100.0F)}).m_130940_(ChatFormatting.GOLD));
        if (stack.m_41773_() == 0) {
            int max = stack.m_41776_();
            tooltip.add(Component.m_237110_("item.durability", new Object[]{max, max}));
        }

    }

    public ReactorFuel getFuelStats(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(this);
        return new ReactorFuel(id, stack.m_41773_(), stack.m_41776_(), this.Rods, this.Heat);
    }

    public static ItemEntry<FuelCellItem> register(String id, int durability, int rods, double heat) {
        ItemEntry<FuelCellItem> entry = FissionMod.GR.item(id, (p) -> new FuelCellItem(durability, rods, heat)).register();
        return entry;
    }
}
