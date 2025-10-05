package net.phoenix.core.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;

import com.tterrag.registrate.util.entry.ItemEntry;

import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static net.phoenix.core.common.registry.PhoenixRegistration.REGISTRATE;

@SuppressWarnings("all")
public class PhoenixGTItems {

    public static ItemEntry<ComponentItem> POWER_UNIT_LUV = REGISTRATE.item("luv_power_unit", ComponentItem::create)
            .lang("LuV Power Unit")
            .properties(p -> p.stacksTo(8))
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tools/power_unit_luv")))
            .onRegister(attach(ElectricStats.createElectricItem(30600000L, GTValues.LuV)))
            .register();

    public static void init() {}
}
