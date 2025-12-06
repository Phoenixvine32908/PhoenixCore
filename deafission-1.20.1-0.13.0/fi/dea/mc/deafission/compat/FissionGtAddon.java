//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.compat;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import fi.dea.mc.deafission.FissionMod;
import fi.dea.mc.deafission.client.FissionSounds;
import fi.dea.mc.deafission.common.data.FissionGtRecipes;
import java.util.function.Consumer;
import net.minecraft.data.recipes.FinishedRecipe;

@GTAddon
public class FissionGtAddon implements IGTAddon {
    public GTRegistrate getRegistrate() {
        return FissionMod.GR;
    }

    public void initializeAddon() {
    }

    public String addonModId() {
        return "deafission";
    }

    public void addRecipes(Consumer<FinishedRecipe> provider) {
        FissionGtRecipes.init(provider);
    }

    public void registerSounds() {
        FissionSounds.init();
    }
}
