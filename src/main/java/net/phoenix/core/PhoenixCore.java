package net.phoenix.core;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.sound.SoundEntry;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.phoenix.core.client.PhoenixClient;
import net.phoenix.core.common.block.PhoenixBlocks;
import net.phoenix.core.common.data.PhoenixItems;
import net.phoenix.core.common.data.PhoenixRecipeTypes;
import net.phoenix.core.common.data.materials.PhoenixMaterials;
import net.phoenix.core.common.machine.PhoenixMachines;
import net.phoenix.core.common.machine.PhoenixResearchMachines;
import net.phoenix.core.common.registry.PhoenixRegistration;
import net.phoenix.core.configs.PhoenixConfigs;

import com.tterrag.registrate.util.entry.RegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PhoenixCore.MOD_ID)
public class PhoenixCore {

    public static final String MOD_ID = "phoenixcore";
    public static final Logger LOGGER = LogManager.getLogger();
    public static GTRegistrate EXAMPLE_REGISTRATE = GTRegistrate.create(PhoenixCore.MOD_ID);
    public static RegistryEntry<CreativeModeTab> PHOENIX_CREATIVE_TAB = null;

    public PhoenixCore() {
        // This static init call is a common pattern to ensure your Registrate instance
        // is initialized at the correct time.
        PhoenixCore.init();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        modEventBus.addListener(this::addMaterialRegistries);
        modEventBus.addListener(this::addMaterials);
        modEventBus.addListener(this::modifyMaterials);

        modEventBus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        modEventBus.addGenericListener(SoundEntry.class, this::registerSounds);
        modEventBus.addGenericListener(MachineDefinition.class, this::registerMachines);

        modEventBus.addListener(this::addCreative);
        if (Platform.isClient()) {
            PhoenixClient.init(modEventBus);
        }
        // Most other events are fired on Forge's bus.
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * This method is responsible for registering the Registrate instance,
     * which handles all blocks, items, etc. that were defined.
     */
    public static void init() {
        // Then register everything else.
        PhoenixConfigs.init();
        PhoenixRegistration.REGISTRATE.registerRegistrate();
        PhoenixBlocks.init();
        PhoenixItems.init();
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            // event.accept(ModItems.SAPPHIRE);
            // event.accept(ModItems.RAW_SAPPHIRE);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Hello from common setup! This is *after* registries are done, so we can do this:");
            LOGGER.info("Look, I found a {}!", Items.DIAMOND);
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Hey, we're on Minecraft version {}!", Minecraft.getInstance().getLaunchedVersion());
    }

    /**
     * Create a ResourceLocation in the format "modid:path"
     *
     * @param path
     * @return ResourceLocation with the namespace of your mod
     */
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    /**
     * Create a material manager for your mod using GT's API.
     * You MUST have this if you have custom materials.
     * Remember to register them not to GT's namespace, but your own.
     *
     * @param event
     */
    private void addMaterialRegistries(MaterialRegistryEvent event) {
        GTCEuAPI.materialManager.createRegistry(PhoenixCore.MOD_ID);
    }

    /**
     * You will also need this for registering custom materials
     * Call init() from your Material class(es) here
     *
     * @param event
     */

    private void addMaterials(MaterialEvent event) {
        PhoenixMaterials.register();
    }

    /**
     * (Optional) Used to modify pre-existing materials from GregTech
     *
     * @param event
     */
    private void modifyMaterials(PostMaterialEvent event) {
        // CustomMaterials.modify();
    }

    /**
     * Used to register your own new RecipeTypes.
     * Call init() from your RecipeType class(es) here
     *
     * @param event
     */
    private void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        PhoenixRecipeTypes.init();
    }

    /**
     * Used to register your own new sounds
     * Call init from your Sound class(es) here
     *
     * @param event
     */
    public void registerSounds(GTCEuAPI.RegisterEvent<ResourceLocation, SoundEntry> event) {
        // CustomSounds.init();
    }

    /**
     * Used to register your own new Machines.
     * Call init() from your Machine class(es) here
     *
     * @param event
     */
    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        PhoenixMachines.init();
        PhoenixResearchMachines.init();
    }
}
