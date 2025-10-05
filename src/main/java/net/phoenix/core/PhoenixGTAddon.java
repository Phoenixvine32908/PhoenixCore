package net.phoenix.core;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;

import net.minecraft.data.recipes.FinishedRecipe;
import net.phoenix.core.api.capability.PhoenixRecipeCapabilities;
import net.phoenix.core.common.data.PhoenixMachineRecipes;
import net.phoenix.core.common.data.PhoenixToolRecipes;
import net.phoenix.core.common.data.materials.PhoenixElements;
import net.phoenix.core.common.machine.multiblock.Shield;
import net.phoenix.core.integration.kubejs.recipe.ShieldComponent;

import com.mojang.datafixers.util.Pair;

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





    @Override
    public void registerRecipeCapabilities() {
        PhoenixRecipeCapabilities.init();
    }
}
