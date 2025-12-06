package net.phoenix.core.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.phoenix.core.api.gui.PhoenixGuiTextures;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class PhoenixRecipeTypes {

    public static GTRecipeType FISSION_RECIPES;
    // Existing types
    public static GTRecipeType HONEY_CHAMBER_RECIPES;
    public static GTRecipeType PLEASE;

    // ⭐ New Bee Recipe Types ⭐
    public static GTRecipeType SIMULATED_COLONY_RECIPES;
    public static GTRecipeType COMB_DECANTING_RECIPES;
    public static GTRecipeType SWARM_NURTURING_RECIPES;
    public static GTRecipeType APIS_PROGENITOR_RECIPES; // Used for Bee Creation

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
                .setSound(GTSoundEntries.BATH)
                .setEUIO(IO.IN);

        // ----------------------------------------------------------------------
        // SIMULATED COLONY (Makes the comb from raw inputs)
        // ----------------------------------------------------------------------
        SIMULATED_COLONY_RECIPES = register("simulated_colony", MULTIBLOCK)
                .setMaxIOSize(2, 1, 2, 1) // Item, Fluid
                .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.FORGE_HAMMER) // Use a suitable sound
                .setEUIO(IO.IN);

        // ----------------------------------------------------------------------
        // COMB DECANTING (Comb to Raw Wax Dust and Impure Honey)
        // ----------------------------------------------------------------------
        COMB_DECANTING_RECIPES = register("comb_decanting", MULTIBLOCK)
                .setMaxIOSize(1, 2, 1, 2) // Item, Fluid
                .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_MACERATE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.CENTRIFUGE) // Suitable for separation
                .setEUIO(IO.IN);

        // ----------------------------------------------------------------------
        // BREWERY / WAX PROCESSING (Raw Wax Dust to Honeyed Fluid)
        // ----------------------------------------------------------------------

        // ----------------------------------------------------------------------
        // APIS PROGENITOR (Creates new bee species from a base bee + material)
        // ----------------------------------------------------------------------
        APIS_PROGENITOR_RECIPES = register("apis_progenitor", MULTIBLOCK)
                .setMaxIOSize(3, 1, 1, 1) // Item, Fluid
                .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.MIXER) // Suitable for genetic mixing
                .setEUIO(IO.IN);

        SWARM_NURTURING_RECIPES = register("swarm_nurturing", MULTIBLOCK)
                .setMaxIOSize(3, 1, 1, 1) // Item, Fluid
                .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.MIXER) // Suitable for genetic mixing
                .setEUIO(IO.IN);
        FISSION_RECIPES = register("fission_reactor", MULTIBLOCK)
                .setMaxIOSize(2, 2, 2, 2)
                .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
                .setProgressBar(PhoenixGuiTextures.PROGRESS_BAR_FISSION, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setSound(GTSoundEntries.CHEMICAL)
                .setEUIO(IO.OUT)
                .addDataInfo(data -> {
                    int cooling = data.getInt("required_cooling");
                    if (cooling > 0) {
                        return LocalizationUtils.format("emi_info.phoenixcore.required_cooling", cooling);
                    }
                    return "";
                });
    }
}
