package net.phoenix.core;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;
import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import net.minecraft.data.recipes.FinishedRecipe;
import net.phoenix.core.api.capability.PhoenixRecipeCapabilities;
import net.phoenix.core.common.data.PhoenixMachineRecipes;
import net.phoenix.core.common.data.materials.PhoenixElements;

import java.util.function.Consumer;

@SuppressWarnings("unused")
@GTAddon
public class PhoenixGTAddon implements IGTAddon {

    // Corrected lines: Declare and initialize SOURCE_IN and SOURCE_OUT
    public static final ContentJS<Integer> SOURCE_IN = new ContentJS<>(NumberComponent.ANY_INT,
            PhoenixRecipeCapabilities.SOURCE, false);
    public static final ContentJS<Integer> SOURCE_OUT = new ContentJS<>(NumberComponent.ANY_INT,
            PhoenixRecipeCapabilities.SOURCE, true);

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
    }

    @Override
    public void registerElements() {
        IGTAddon.super.registerElements();
        PhoenixElements.init();
    }

    @Override
    public void registerRecipeKeys(KJSRecipeKeyEvent event) {
        event.registerKey(PhoenixRecipeCapabilities.SOURCE, Pair.of(SOURCE_IN, SOURCE_OUT));
    }
}