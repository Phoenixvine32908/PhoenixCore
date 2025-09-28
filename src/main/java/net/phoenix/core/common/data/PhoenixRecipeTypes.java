package net.phoenix.core.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import net.phoenix.core.api.capability.SourceRecipeCapability;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class PhoenixRecipeTypes {

    public static GTRecipeType SOURCE_TESTER_RECIPES;
    public static GTRecipeType PLEASE;

    public static void init() {
        PLEASE = register("please", MULTIBLOCK)
                .setMaxIOSize(16, 1, 8, 4)
                .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.FORGE_HAMMER)
                .setEUIO(IO.IN);
        final GTRecipeType SOURCE_TESTER_RECIPES = register("source_tester", MULTIBLOCK)
                .setMaxSize(IO.IN, SourceRecipeCapability.CAP, 1)
                .setMaxSize(IO.OUT, SourceRecipeCapability.CAP, 1)
                .setMaxIOSize(1, 1, 0, 0)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    }
}
