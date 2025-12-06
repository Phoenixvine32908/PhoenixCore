//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.recipe;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import fi.dea.mc.deafission.common.data.recipe.ComponentRecipe.Cache;
import fi.dea.mc.deafission.common.data.recipe.ComponentRecipe.Serializer;
import fi.dea.mc.deafission.common.data.recipe.ComponentRecipe.Type;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.registries.ForgeRegistries;

public class FissionRecipes {
    public static void register() {
        GTRegistries.register(BuiltInRegistries.f_256769_, Type.ID, Serializer.INSTANCE);
        ForgeRegistries.RECIPE_TYPES.register(Type.ID, Type.INSTANCE);
    }

    public static void onConfigReloaded() {
        Cache.clear();
    }
}
