package net.phoenix.core.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;

import com.tterrag.registrate.util.entry.ItemEntry;

import static net.phoenix.core.common.registry.PhoenixRegistration.REGISTRATE;
import static net.phoenix.core.phoenixcore.PHOENIX_CREATIVE_TAB;

public class PhoenixItems {

    static {
        REGISTRATE.creativeModeTab(() -> PHOENIX_CREATIVE_TAB);
    }
    public static ItemEntry<ComponentItem> POWER_UNIT_LUV = REGISTRATE.item("luv_power_unit", ComponentItem::create)
            .lang("LuV Power Unit")
            .properties(p -> p.stacksTo(8))
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tools/power_unit_luv")))
            .onRegister((c) -> c.attachComponents(ElectricStats.createElectricItem(102400000L, GTValues.LuV)))
            .register();
    public static ItemEntry<ComponentItem> POWER_UNIT_ZPM = REGISTRATE.item("zpm_power_unit", ComponentItem::create)
            .lang("ZPM Power Unit")
            .properties(p -> p.stacksTo(8))
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tools/power_unit_zpm")))
            .onRegister((c) -> c.attachComponents(ElectricStats.createElectricItem(409600000L, GTValues.ZPM)))
            .register();
    private static final int UHV_RELATIVE_INDEX = GTValues.UHV - PhoenixCovers.START_TIER;

    /*
     * public static ItemEntry<ComponentItem> ENERGY_LAPOTRONIC_ORB = REGISTRATE
     * .item("lapotronic_energy_orb", ComponentItem::create)
     * .lang("Lapotronic Energy Orb")
     * .model(overrideModel(GTCEu.id("battery"), 8))
     * .onRegister(modelPredicate(GTCEu.id("battery"), ElectricStats::getStoredPredicate))
     * .onRegister(attach(ElectricStats.createRechargeableBattery(250_000_000L, GTValues.IV)))
     * .tag(CustomTags.IV_BATTERIES).register();
     */

    public static void init() {}
}
