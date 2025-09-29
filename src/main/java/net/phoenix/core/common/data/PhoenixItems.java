package net.phoenix.core.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import net.minecraft.world.item.Item;
import net.phoenix.core.common.registry.PhoenixRegistration;

import com.tterrag.registrate.util.entry.ItemEntry;

import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static net.phoenix.core.common.registry.PhoenixRegistration.REGISTRATE;
import static net.phoenix.core.phoenixcore.PHOENIX_CREATIVE_TAB;

public class PhoenixItems {

    static {
        REGISTRATE.creativeModeTab(() -> PHOENIX_CREATIVE_TAB);
    }
    // Modelo explícito para quantum_anomaly
    public static ItemEntry<Item> basic_fuel_rod = REGISTRATE
            .item("basic_fuel_rod", Item::new)
            .model((ctx, prov) -> {
                prov.withExistingParent(ctx.getName(), prov.mcLoc("item/generated"))
                        .texture("layer0", prov.modLoc("item/" + ctx.getName()));
            })
            .register();


    public static void init() {}
}
