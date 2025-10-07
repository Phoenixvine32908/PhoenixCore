package net.phoenix.core.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;

import net.phoenix.core.api.capability.SourceRecipeCapability;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class PhoenixRecipeTypes {

    public static GTRecipeType HONEY_CHAMBER_RECIPES;
    public static GTRecipeType PLEASE;

    public static void init() {
        PLEASE = register("please", MULTIBLOCK)
                .setMaxIOSize(4, 1, 8, 4)
                .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.FORGE_HAMMER)
                .setEUIO(IO.IN);
        HONEY_CHAMBER_RECIPES = register("honey_chamber", MULTIBLOCK)
                .setMaxIOSize(4, 4, 4, 4)
                .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.FORGE_HAMMER)
                .setEUIO(IO.IN);
    }
}
