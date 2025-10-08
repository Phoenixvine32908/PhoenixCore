package net.phoenix.core.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IFluidRenderMulti;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.block.FluidBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.RenderTypeHelper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Custom renderer that draws the current recipe output fluid on chosen faces.
 */
@SuppressWarnings("all")
public class CustomFluidRender extends DynamicRender<IFluidRenderMulti, CustomFluidRender> {

    public static final CustomFluidRender INSTANCE = new CustomFluidRender();
    public static final Codec<CustomFluidRender> CODEC = Codec.unit(CustomFluidRender::new);
    public static final DynamicRenderType<IFluidRenderMulti, CustomFluidRender> TYPE = new DynamicRenderType<>(CODEC);

    private final FluidBlockRenderer fluidRenderer;
    private final List<RelativeDirection> drawFaces;

    private @Nullable Fluid cachedFluid = null;

    public CustomFluidRender() {
        this.fluidRenderer = FluidBlockRenderer.Builder.create()
                .setFaceOffset(-0.125f)
                .setForcedLight(LightTexture.FULL_BRIGHT)
                .getRenderer();

        // You can change which sides the fluid renders on here
        this.drawFaces = List.of(
                RelativeDirection.DOWN,  // bottom
                RelativeDirection.UP     // top
        // add more: RelativeDirection.FRONT, LEFT, RIGHT, BACK
        );
    }

    @Override
    public @NotNull DynamicRenderType<IFluidRenderMulti, CustomFluidRender> getType() {
        return TYPE;
    }

    @Override
    public void render(@NotNull IFluidRenderMulti machine, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        // 1️⃣ Respect config — don’t render if disabled
        if (!ConfigHolder.INSTANCE.client.renderer.renderFluids) return;

        // 2️⃣ Only render if formed and has fluid offsets
        if (!machine.isFormed() || machine.getFluidOffsets() == null) return;

        // 3️⃣ Fetch the recipe safely
        var recipeLogic = machine.getRecipeLogic();
        if (recipeLogic == null) return;

        var lastRecipe = recipeLogic.getLastRecipe();
        if (lastRecipe == null) return;

        // 4️⃣ Cache/update fluid
        if (machine.isActive()) {
            // only update occasionally to prevent flicker
            if (cachedFluid == null || machine.self().getOffsetTimer() % 20 == 0) {
                cachedFluid = RenderUtil.getRecipeFluidToRender(lastRecipe);
            }
        } else {
            cachedFluid = null;
        }

        if (cachedFluid == null) return;

        // 5️⃣ Setup render layer and consumer
        var fluidRenderType = ItemBlockRenderTypes.getRenderLayer(cachedFluid.defaultFluidState());
        var consumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(fluidRenderType, false));

        // 6️⃣ Draw on each configured face
        for (RelativeDirection face : drawFaces) {
            poseStack.pushPose();

            var dir = face.getRelative(machine.self().getFrontFacing(), machine.self().getUpwardsFacing(),
                    machine.self().isFlipped());

            // Flip so it renders inward (GTCEu convention)
            if (dir.getAxis() != Direction.Axis.Y) {
                dir = dir.getOpposite();
            }

            // ✅ Draw the actual fluid plane
            fluidRenderer.drawPlane(
                    dir,
                    machine.getFluidOffsets(),
                    poseStack.last().pose(),
                    consumer,
                    cachedFluid,
                    RenderUtil.FluidTextureType.STILL,
                    packedOverlay,
                    machine.self().getPos());

            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull IFluidRenderMulti machine) {
        return true;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(IFluidRenderMulti machine) {
        return new AABB(machine.self().getPos()).inflate(32);
    }
}
