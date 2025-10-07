package net.phoenix.core;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;

import com.mojang.datafixers.util.Pair;
import net.minecraft.data.recipes.FinishedRecipe;
import net.phoenix.core.api.capability.PhoenixRecipeCapabilities;
import net.phoenix.core.common.data.PhoenixMachineRecipes;
import net.phoenix.core.common.data.PhoenixToolRecipes;
import net.phoenix.core.common.data.materials.PhoenixElements;
import net.phoenix.core.common.machine.multiblock.Shield;
import net.phoenix.core.integration.kubejs.recipe.ShieldComponent;


import java.awt.*;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@GTAddon
public class PhoenixGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return phoenixcore.PHOENIX_REGISTRATE;
    }

    @Override
    public void initializeAddon() {}

    @Override
    public String addonModId() {
        return phoenixcore.MOD_ID;
    }

    @Override
    public void registerTagPrefixes() {
        // CustomTagPrefixes.init();
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        PhoenixMachineRecipes.init(provider);
        PhoenixToolRecipes.init(provider);
    }

    @Override
    public void registerElements() {
        IGTAddon.super.registerElements();
        PhoenixElements.init();
    }


    public static final ShieldComponent SHIELD_COMPONENT = new ShieldComponent();
    public static final ContentJS<Shield.ShieldTypes> SHIELD_IN = new ContentJS<>(SHIELD_COMPONENT,
            // SHIELDTYPES is from your capability class: PhoenixRecipeCapabilities
            PhoenixRecipeCapabilities.SHIELDTYPES,
            true // True means "is an input capability"
    );

    @Override
    public void registerRecipeKeys(KJSRecipeKeyEvent event) {
        // ... (existing registrations)

        // Register your shield capability key
        event.registerKey(PhoenixRecipeCapabilities.SHIELDTYPES, Pair.of(SHIELD_IN, null));
    }


    @Override
    public void registerRecipeCapabilities() {
        PhoenixRecipeCapabilities.init();
    }
}
