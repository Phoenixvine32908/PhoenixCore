package net.phoenix.core.datagen.models;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.phoenix.core.common.machine.multiblock.part.ShieldRenderProperty;
import net.phoenix.core.phoenixcore;

public class PhoenixMachineModels {

    public static MachineBuilder.ModelInitializer createOverlayFillLevelCasingMachineModel(String overlayName,
                                                                                           String casingTexturePath) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> {
                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(GTCEu.id("block/overlay/2_layer/front_emissive")));
                casingTextures(model, casingTexturePath);

                var prop = state.getValue(ShieldRenderProperty.TYPE);
                var key = prop.getSerializedName();
                model.texture("overlay", phoenixcore.id("block/overlay/machine/" + overlayName + "_base"));
                model.texture("overlay_emissive",
                        phoenixcore.id("block/overlay/machine/" + overlayName + "_overlay_" + key));
                return model;
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createOverlayCasingMachineModel(String overlayName,
                                                                                  String casingTexturePath) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> {
                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(GTCEu.id("block/overlay/2_layer/front_emissive")));
                casingTextures(model, casingTexturePath);

                model.texture("overlay", phoenixcore.id("block/overlay/machine/" + overlayName + "_base"));
                model.texture("overlay_emissive",
                        phoenixcore.id("block/overlay/machine/" + overlayName + "_emissive"));
                return model;
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static void casingTextures(BlockModelBuilder model, String casingTexturePath) {
        ResourceLocation casingTexture = phoenixcore.id("block/" + casingTexturePath);
        model.texture("bottom", casingTexture);
        model.texture("top", casingTexture);
        model.texture("side", casingTexture);
    }
}
