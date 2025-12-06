//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import fi.dea.mc.deafission.FissionMod;
import java.util.Objects;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.item.crafting.RecipeType;

public class FissionGtRecipeTypes {
    public static final String EBF_TEMP = "ebf_temp";
    public static final String HEAT_PER_TICK = "heat_per_tick";
    public static final String COOLANT_HEAT_PER_TICK = "coolant_heat_per_tick";
    public static final GTRecipeType ReactorCoolantRecipe;
    public static final GTRecipeType ReactorFuelRecipe;
    public static final GTRecipeType ReactorProcessingRecipe;

    public static void init() {
    }

    static {
        ReactorCoolantRecipe = GTRecipeTypes.register(FissionMod.id("fission_reactor_coolant").toString(), "multiblock", new RecipeType[0]).setEUIO(IO.NONE).setMaxIOSize(1, 0, 1, 4).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT).setSound(GTSoundEntries.FURNACE).addDataInfo((data) -> {
            double value = ((NumericTag)Objects.requireNonNull(data.m_128423_("coolant_heat_per_tick"))).m_7061_();
            return LocalizationUtils.format("deafission.recipe.coolant_heat_per_tick", new Object[]{value});
        });
        ReactorFuelRecipe = GTRecipeTypes.register(FissionMod.id("fission_reactor_fuel").toString(), "dummy", new RecipeType[0]).setEUIO(IO.NONE).setMaxIOSize(1, 1, 4, 4).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT).setSound(GTSoundEntries.FURNACE);
        ReactorProcessingRecipe = GTRecipeTypes.register(FissionMod.id("fission_reactor_processing").toString(), "dummy", new RecipeType[0]).setEUIO(IO.NONE).setMaxIOSize(1, 1, 0, 1).setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT).addDataInfo((data) -> {
            int value = data.m_128451_("ebf_temp");
            return LocalizationUtils.format("gtceu.recipe.temperature", new Object[]{value});
        }).addDataInfo((data) -> {
            int value = data.m_128451_("heat_per_tick");
            return LocalizationUtils.format("deafission.recipe.heat_per_tick", new Object[]{value});
        }).setSound(GTSoundEntries.FURNACE);
    }
}
