package net.phoenix.core.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class PhoenixRecipeTypes {

    // 1. Declare os campos para ambos os tipos de receita.
    public static GTRecipeType PLEASE;// <-- NOVO CAMPO ADICIONADO

    public static void init() {
        PLEASE = register("please", MULTIBLOCK)
                .setMaxIOSize(16, 1, 8, 4)
                .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.FORGE_HAMMER)
                .setEUIO(IO.IN);
    }
}
