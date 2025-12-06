//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission;

import fi.dea.mc.deafission.core.ReactorConfiguration;
import fi.dea.mc.deafission.core.ReactorCurve;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@EventBusSubscriber(
    modid = "deafission",
    bus = Bus.MOD
)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.BooleanValue ADD_DEFAULT_RECIPES;
    static final ForgeConfigSpec SPEC;
    public static boolean addDefaultRecipes;
    public static ReactorConfiguration reactorConfiguration;

    @SubscribeEvent
    public static void onLoad(ModConfigEvent event) {
        reload();
    }

    public static void reload() {
        addDefaultRecipes = (Boolean)ADD_DEFAULT_RECIPES.get();
    }

    static void configureReactor() {
        reactorConfiguration = new ReactorConfiguration((s) -> new ReactorCurve(s), true);
    }

    static {
        ADD_DEFAULT_RECIPES = BUILDER.comment("Whether to add the default recipes. false is recommended for modpacks.").define("addDefaultRecipes", true);
        SPEC = BUILDER.build();
    }
}
