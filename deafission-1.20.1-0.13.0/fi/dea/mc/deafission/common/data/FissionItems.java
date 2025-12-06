//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data;

import com.tterrag.registrate.util.entry.ItemEntry;
import fi.dea.mc.deafission.common.data.items.FuelCellItem;

public class FissionItems {
    public static final ItemEntry<FuelCellItem> PlutoniumFuelCell = FuelCellItem.register("fuelcell_plutonium_x1", 1440, 1, (double)2.0F);
    public static final ItemEntry<FuelCellItem> ThoriumFuelCell = FuelCellItem.register("fuelcell_thorium_x1", 240, 1, (double)0.5F);
    public static final ItemEntry<FuelCellItem> UraniumFuelCell = FuelCellItem.register("fuelcell_uranium_x1", 720, 1, (double)1.0F);
    public static final ItemEntry<FuelCellItem> UraniumFuelCell_x4 = FuelCellItem.register("fuelcell_uranium_x4", 720, 4, (double)6.0F);

    public static void init() {
    }
}
