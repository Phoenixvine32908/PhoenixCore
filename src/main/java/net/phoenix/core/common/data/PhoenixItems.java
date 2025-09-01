package net.phoenix.core.common.data;

import net.minecraft.world.item.Item;
import net.phoenix.core.common.registry.PhoenixRegistration;

import com.tterrag.registrate.util.entry.ItemEntry;

import static net.phoenix.core.PhoenixCore.PHOENIX_CREATIVE_TAB;

public class PhoenixItems {

    static {
        PhoenixRegistration.REGISTRATE.creativeModeTab(() -> PHOENIX_CREATIVE_TAB);
    }
    // Modelo explícito para quantum_anomaly
    public static ItemEntry<Item> basic_fuel_rod = PhoenixRegistration.REGISTRATE
            .item("basic_fuel_rod", Item::new)
            .model((ctx, prov) -> {
                prov.withExistingParent(ctx.getName(), prov.mcLoc("item/generated"))
                        .texture("layer0", prov.modLoc("item/" + ctx.getName()));
            })
            .register();

    public static void init() {}
}
