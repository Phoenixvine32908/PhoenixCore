package net.phoenix.core.integration.kubejs;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.phoenix.core.client.renderer.machine.multiblock.PhoenixDynamicRenderHelpers;
import net.phoenix.core.common.block.PhoenixBlocks;
import net.phoenix.core.common.data.PhoenixItems;
import net.phoenix.core.common.data.PhoenixRecipeTypes;
import net.phoenix.core.common.data.materials.PhoenixElements;
import net.phoenix.core.common.data.materials.PhoenixMaterials;
import net.phoenix.core.common.machine.PhoenixMachines;
import net.phoenix.core.common.machine.multiblock.*;
import net.phoenix.core.configs.PhoenixConfigs;
import net.phoenix.core.integration.kubejs.builders.FissionCoolerBlockBuilder;
import net.phoenix.core.integration.kubejs.builders.FissionModeratorBlockBuilder;
import net.phoenix.core.integration.kubejs.recipe.PhoenixRecipeSchema;
import net.phoenix.core.phoenixcore;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;

public class PhoenixKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void initStartup() {
        super.initStartup();
    }

    @Override
    public void init() {
        super.init();
        RegistryInfo.BLOCK.addType(
                // The ID used in KubeJS scripts (e.g., 'phoenixcore:fission_cooler')
                phoenixcore.MOD_ID + ":fission_cooler",
                // The Builder class
                FissionCoolerBlockBuilder.class,
                // The constructor reference
                FissionCoolerBlockBuilder::new);

        // 2. Register the Fission Moderator Builder
        RegistryInfo.BLOCK.addType(
                // The ID used in KubeJS scripts (e.g., 'phoenixcore:fission_moderator')
                phoenixcore.MOD_ID + ":fission_moderator",
                // The Builder class
                FissionModeratorBlockBuilder.class,
                // The constructor reference
                FissionModeratorBlockBuilder::new);
    }

    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        super.registerClasses(type, filter);
        // allow user to access all gtceu classes by importing them.
        filter.allow("net.phoenix.core");
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        for (var entry : GTRegistries.RECIPE_TYPES.entries()) {
            event.register(entry.getKey(), PhoenixRecipeSchema.SCHEMA);
        }
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        super.registerBindings(event);
        event.add("ShieldType", Shield.ShieldTypes.class);
        event.add("PhoenixMaterials", PhoenixMaterials.class);
        event.add("PhoenixConfigs", PhoenixConfigs.class);
        event.add("BlazingCleanroom", BlazingCleanroom.class);
        event.add("PhoenixElements", PhoenixElements.class);
        event.add("PhoenixBlocks", PhoenixBlocks.class);
        event.add("PhoenixMachines", PhoenixMachines.class);
        event.add("PhoenixResearchMachines", PhoenixMachines.class);
        event.add("CreativeEnergyMultiMachine", CreativeEnergyMultiMachine.class);
        event.add("PhoenixItems", PhoenixItems.class);
        event.add("PhoenixRecipeTypes", PhoenixRecipeTypes.class);
        event.add("FissionWorkableElectricMultiblockMachine", FissionWorkableElectricMultiblockMachine.class);
        event.add("FissionSteamMultiblockMachine", FissionSteamMultiblockMachine.class);

        event.add("PhoenixDynamicRenderHelpers", PhoenixDynamicRenderHelpers.class);
        event.add("PhoenixCore", phoenixcore.class);
    }
}
