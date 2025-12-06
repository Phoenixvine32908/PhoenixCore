//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.lowdragmc.lowdraglib.Platform;
import com.mojang.logging.LogUtils;
import com.tterrag.registrate.providers.ProviderType;
import fi.dea.mc.deafission.common.data.FissionFluids;
import fi.dea.mc.deafission.common.data.FissionGtRecipeTypes;
import fi.dea.mc.deafission.common.data.FissionGtRecipes;
import fi.dea.mc.deafission.common.data.FissionItems;
import fi.dea.mc.deafission.common.data.FissionMachines;
import fi.dea.mc.deafission.common.data.FissionTags;
import fi.dea.mc.deafission.common.data.recipe.FissionRecipes;
import fi.dea.mc.deafission.compat.FissionCcReactorPeripheral;
import fi.dea.mc.deafission.compat.FissionGtPlaceholders;
import fi.dea.mc.deafission.rendering.ReactorRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Mod("deafission")
public class FissionMod {
    public static final String MOD_ID = "deafission";
    public static final Logger LOG = LogUtils.getLogger();
    public static final GTRegistrate GR = GTRegistrate.create("deafission");
    public static final boolean HAS_CC = ModList.get().isLoaded("computercraft");
    private static @NotNull MaterialRegistry MATERIAL_REGISTRY = null;

    public FissionMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(Type.COMMON, Config.SPEC);
        bus.addGenericListener(MachineDefinition.class, FissionMod::registerMachines);
        bus.addGenericListener(GTRecipeType.class, FissionMod::registerRecipeTypes);
        bus.addListener(FissionMod::onRegisterMaterialRegistry);
        MinecraftForge.EVENT_BUS.addListener(FissionMod::onServerStarted);
        FissionTags.init();
        FissionItems.init();
        FissionGtPlaceholders.init();
        GR.addDataGenerator(ProviderType.ITEM_TAGS, FissionTags::initItem);
        GR.addDataGenerator(ProviderType.BLOCK_TAGS, FissionTags::initBlock);
        GR.registerRegistrate();
        GR.registerEventListeners(bus);
        FissionRecipes.register();
        if (HAS_CC) {
            FissionCcReactorPeripheral.register();
        }

        if (Platform.isClient()) {
            DynamicRenderManager.register(id("reactor"), ReactorRenderer.TYPE);
        }

    }

    private static void onRegisterMaterialRegistry(MaterialRegistryEvent event) {
        MATERIAL_REGISTRY = GTCEuAPI.materialManager.createRegistry("deafission");
        MATERIAL_REGISTRY.unfreeze();
        FissionFluids.init();
        MATERIAL_REGISTRY.freeze();
    }

    private static void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        FissionMachines.init();
    }

    private static void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        FissionGtRecipeTypes.init();
    }

    private static void onServerStarted(ServerStartedEvent event) {
        FissionGtRecipes.onConfigReloaded();
        FissionGtRecipes.parseAll();
    }

    public static ResourceLocation id(String identifier) {
        return ResourceLocation.fromNamespaceAndPath("deafission", identifier);
    }
}
