package net.phoenix.core.datagen.models;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;

import com.gregtechceu.gtceu.common.block.BatteryBlock;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.phoenix.core.api.block.IFissionCoolerType;
import net.phoenix.core.api.block.IFissionModeratorType;
import net.phoenix.core.api.machine.trait.ITeslaBattery;
import net.phoenix.core.common.block.TeslaBatteryBlock;
import net.phoenix.core.common.machine.multiblock.part.ShieldRenderProperty;
import net.phoenix.core.phoenixcore;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

public class PhoenixMachineModels {

    // ============================================================
    // BASE MODELS (EXISTING)
    // ============================================================

    public static MachineBuilder.ModelInitializer createOverlayFillLevelCasingMachineModel(
                                                                                           String overlayName,
                                                                                           String casingTexturePath) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> {
                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(
                                GTCEu.id("block/overlay/2_layer/front_emissive")));
                casingTextures(model, casingTexturePath);

                var prop = state.getValue(ShieldRenderProperty.TYPE);
                var key = prop.getSerializedName();

                model.texture("overlay",
                        phoenixcore.id("block/overlay/machine/" + overlayName + "_base"));

                model.texture("overlay_emissive",
                        phoenixcore.id("block/overlay/machine/" + overlayName + "_overlay_" + key));

                return model;
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }



    public static NonNullBiConsumer<DataGenContext<Block, TeslaBatteryBlock>, RegistrateBlockstateProvider> createTeslaBlockModel(ITeslaBattery batteryData) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(ctx.getName(),
                    phoenixcore.id("block/casings/batteries/" + batteryData.getBatteryName() + "/side"),
                    phoenixcore.id("block/casings/batteries/" + batteryData.getBatteryName() + "/top"),
                    phoenixcore.id("block/casings/batteries/" + batteryData.getBatteryName() + "/top")));
        };
    }

    public static MachineBuilder.ModelInitializer createOverlayCasingMachineModel(
                                                                                  String overlayName,
                                                                                  String casingTexturePath) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> {
                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(
                                GTCEu.id("block/overlay/2_layer/front_emissive")));
                casingTextures(model, casingTexturePath);

                model.texture("overlay",
                        phoenixcore.id("block/overlay/machine/" + overlayName + "_base"));

                model.texture("overlay_emissive",
                        phoenixcore.id("block/overlay/machine/" + overlayName + "_emissive"));

                return model;
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static void casingTextures(BlockModelBuilder model, String casingTexturePath) {
        ResourceLocation casing = phoenixcore.id("block/" + casingTexturePath);
        model.texture("bottom", casing);
        model.texture("top", casing);
        model.texture("side", casing);
    }

    public static <
            T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> createActiveCoolerModel(IFissionCoolerType type) {
        return (ctx, prov) -> {
            String name = ctx.getName();
            Block block = ctx.getEntry();

            var inactive = prov.models().cubeAll(name, type.getTexture());

            var active = prov.models()
                    .withExistingParent(name + "_active", GTCEu.id("block/cube_2_layer/all"))
                    .texture("bot_all", type.getTexture())
                    .texture("top_all", type.getTexture().withSuffix("_active"));

            prov.getVariantBuilder(block)
                    .partialState().with(GTBlockStateProperties.ACTIVE, false)
                    .modelForState().modelFile(inactive).addModel()

                    .partialState().with(GTBlockStateProperties.ACTIVE, true)
                    .modelForState().modelFile(active).addModel();
        };
    }

    public static <
            T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> createFissionModeratorModel(IFissionModeratorType type) {
        return (ctx, prov) -> {
            String name = ctx.getName();
            Block block = ctx.getEntry();

            var model = prov.models().cubeAll(name, type.getTexture());

            prov.simpleBlock(block, model);
        };
    }
}
