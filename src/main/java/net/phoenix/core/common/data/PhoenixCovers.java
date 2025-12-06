package net.phoenix.core.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer;
import com.gregtechceu.gtceu.common.data.GTCovers;

import net.phoenix.core.common.cover.PhoenixCoverSolarPanel;
import net.phoenix.core.phoenixcore;

import java.util.Arrays;
import java.util.Locale;

public class PhoenixCovers {

    // Define the tiers you want to register (UHV (8) and above)
    // NOTE: If you are replacing GT's default UHV, use GTValues.UHV (8) as the start.
    public static final int START_TIER = GTValues.UHV;
    public static final int END_TIER = GTValues.MAX; // Or whatever your highest tier is

    public static final int[] PHOENIX_SOLAR_PANEL_TIERS = GTValues.tiersBetween(START_TIER, END_TIER);

    // This array will hold your custom solar panels
    public final static CoverDefinition[] SOLAR_PANEL_TIERED;

    static {
        GTRegistries.COVERS.unfreeze();
        SOLAR_PANEL_TIERED = registerPhoenixTiered(
                "solar_panel",
                PhoenixCoverSolarPanel::new, // Use your custom cover logic
                PHOENIX_SOLAR_PANEL_TIERS);
        GTRegistries.COVERS.freeze();
    }

    public static void init() {
        // Simple call to ensure static initializer runs and registers covers
    }

    /**
     * Helper to register a set of tiered covers using your mod's namespace.
     * This is required because you cannot use GTCovers' private registerTiered method.
     */
    private static CoverDefinition[] registerPhoenixTiered(
                                                           String id,
                                                           CoverDefinition.TieredCoverBehaviourProvider behaviorCreator,
                                                           int... tiers) {
        return Arrays.stream(tiers).mapToObj(tier -> {
            var name = id + "." + GTValues.VN[tier].toLowerCase(Locale.ROOT);
            var resourceLocation = phoenixcore.id(name);

            var definition = new CoverDefinition(
                    resourceLocation,
                    (def, coverable, side) -> behaviorCreator.create(def, coverable, side, tier),
                    () -> () -> new SimpleCoverRenderer(GTCEu.id("block/cover/" + id)));

            GTRegistries.COVERS.register(definition.getId(), definition);
            return definition;
        }).toArray(CoverDefinition[]::new);
    }
}
