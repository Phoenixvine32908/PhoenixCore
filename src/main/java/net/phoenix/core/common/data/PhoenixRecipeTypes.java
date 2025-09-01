package net.phoenix.core.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import net.minecraft.client.gui.Gui;

import java.awt.*;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class PhoenixRecipeTypes {

    // 1. Declare os campos para ambos os tipos de receita.
    public static GTRecipeType PLEASE;// <-- NOVO CAMPO ADICIONADO
    public static GTRecipeType SOURCE_IMBUMENT;
    public static GTRecipeType SOURCE_EXTRACTION;

    public static GTRecipeType COMB_DECANTING;
    public static GTRecipeType SWARM_NURTURING;
    public static GTRecipeType APIS_PROGENITOR;
    public static GTRecipeType SIMULATED_COLONY;

    public static GTRecipeType DIMENSIONAL_ANCHORING;
    public static GTRecipeType AETHERIAL_FABRICATION;

    public static void init() {
        // 2. Inicialize o INFINITY_FORGE como antes.
        PLEASE = register("please", MULTIBLOCK)
                .setMaxIOSize(16, 1, 8, 4)
                .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.FORGE_HAMMER)
                .setEUIO(IO.IN);

        // Source Imbument
        SOURCE_IMBUMENT = register("source_imbument", MULTIBLOCK)
                .setMaxIOSize(3, 1, 1, 1)
                .setEUIO(IO.IN)
                .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.CHEMICAL);

        // Source Extraction
        SOURCE_EXTRACTION = register("source_extraction", MULTIBLOCK)
                .setMaxIOSize(3, 1, 1, 1)
                .setEUIO(IO.IN)
                .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.CHEMICAL);

        // Comb Decanting
        COMB_DECANTING = register("comb_decanting", MULTIBLOCK)
                .setMaxIOSize(1, 2, 0, 2)
                .setEUIO(IO.IN)
                .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.ARC);
        // Swarm Nurturing
        SWARM_NURTURING = register("swarm_nurturing", MULTIBLOCK)
                .setMaxIOSize(2, 1, 0, 0)
                .setEUIO(IO.IN)
                .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.ARC);
        // Apis Progenitor
        APIS_PROGENITOR = register("apis_progenitor", MULTIBLOCK)
                .setMaxIOSize(2, 1, 0, 0)
                .setEUIO(IO.IN)
                .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.ARC);
        // Simulated Colony
        SIMULATED_COLONY = register("simulated_colony", MULTIBLOCK)
                .setMaxIOSize(3, 1, 1, 0)
                .setEUIO(IO.IN)
                .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.ARC);

        //Dimensional Anchoring
        DIMENSIONAL_ANCHORING = register("dimensional_anchoring", MULTIBLOCK)
                .setMaxIOSize(3, 30, 1, 0)
                .setEUIO(IO.IN)
                .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.ARC);

        //Aetherial Fabrication
        AETHERIAL_FABRICATION = register("aetherial_fabrication", MULTIBLOCK)
                .setMaxIOSize(3, 25, 1, 0)
                .setEUIO(IO.IN)
                .setSlotOverlay(false, false, GuiTextures.SOLIDIFIER_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.ARC);
    }
}
