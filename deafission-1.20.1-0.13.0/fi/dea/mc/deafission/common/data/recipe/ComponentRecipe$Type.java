//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.recipe;

import fi.dea.mc.deafission.FissionMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

public class ComponentRecipe$Type implements RecipeType<ComponentRecipe> {
    public static final ResourceLocation ID = FissionMod.id("fission_component");
    public static final ComponentRecipe$Type INSTANCE = new ComponentRecipe$Type();

    private ComponentRecipe$Type() {
    }

    public String toString() {
        return ID.toString();
    }
}
