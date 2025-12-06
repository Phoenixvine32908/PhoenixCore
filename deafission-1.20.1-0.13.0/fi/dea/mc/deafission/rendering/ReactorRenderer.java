//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.rendering;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import fi.dea.mc.deafission.common.data.FissionMachines;
import fi.dea.mc.deafission.common.data.machine.FissionReactorMachine;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor.ABGR32;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ReactorRenderer extends DynamicRender<MultiblockControllerMachine, ReactorRenderer> {
    private static ReactorRenderer s_instance = new ReactorRenderer();
    public static final Codec<ReactorRenderer> CODEC;
    public static final DynamicRenderType<MultiblockControllerMachine, ReactorRenderer> TYPE;
    private static TextureAtlasSprite s_fluidSprite;

    public static ReactorRenderer getInstance() {
        return s_instance;
    }

    private static AABB renderBounds(Direction front, Direction upwards) {
        Direction up = RelativeDirection.UP.getRelative(front, upwards, false);
        Direction back = RelativeDirection.BACK.getRelative(front, upwards, false);
        Direction left = RelativeDirection.LEFT.getRelative(front, upwards, false);
        BlockPos.MutableBlockPos minPos = (new BlockPos.MutableBlockPos()).m_122175_(left, 4).m_122175_(up, 3).m_122175_(back, 1);
        BlockPos.MutableBlockPos maxPos = (new BlockPos.MutableBlockPos()).m_122175_(left, -4).m_122175_(up, 5).m_122175_(back, 7);
        return new AABB(minPos, maxPos);
    }

    private ReactorRenderer() {
        ModelUtils.registerAtlasStitchedEventListener(true, InventoryMenu.f_39692_, (event) -> s_fluidSprite = event.getAtlas().m_118316_(GTCEu.id("block/fluids/fluid.refinery_gas")));
    }

    public DynamicRenderType<MultiblockControllerMachine, ReactorRenderer> getType() {
        return TYPE;
    }

    public AABB getRenderBoundingBox(MultiblockControllerMachine multi) {
        if (multi.isFormed()) {
            AABB bounds = renderBounds(multi.getFrontFacing(), multi.getUpwardsFacing());
            return bounds.m_82338_(multi.getPos());
        } else {
            return super.getRenderBoundingBox(multi);
        }
    }

    public int getViewDistance() {
        return 256;
    }

    public boolean shouldRenderOffScreen(MultiblockControllerMachine machine) {
        return true;
    }

    public void render(MultiblockControllerMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (machine.isFormed()) {
            MachineRenderState renderstate = machine.getRenderState();
            if (renderstate.m_61143_(RecipeLogic.STATUS_PROPERTY) != Status.IDLE) {
                FissionReactorMachine reactor = (FissionReactorMachine)machine;
                Vec3 size = FissionMachines.getReactorCoreSize(machine.getDefinition()).m_82549_(new Vec3((double)0.0F, (double)reactor.getRenderSize(), (double)0.0F));
                Vec3 offset = FissionMachines.getReactorCoreOffset(machine.getDefinition());
                poseStack.m_85836_();
                Vec3 b = size.m_82542_((double)0.5F, (double)0.5F, (double)0.5F);
                Vec3 a = b.m_82542_((double)-1.0F, (double)-1.0F, (double)-1.0F);
                int machineRotation = machine.getFrontFacing().m_122416_();
                int machineRotationUp = machine.getUpwardsFacing().m_122416_();
                Vec3 var10000;
                switch (machineRotation) {
                    case 0 -> var10000 = new Vec3(-offset.f_82481_, offset.f_82480_, -offset.f_82479_);
                    case 1 -> var10000 = new Vec3(offset.f_82479_, offset.f_82480_, offset.f_82481_);
                    case 2 -> var10000 = new Vec3(offset.f_82481_, offset.f_82480_, offset.f_82479_);
                    case 3 -> var10000 = new Vec3(-offset.f_82479_, offset.f_82480_, -offset.f_82481_);
                    default -> var10000 = offset;
                }

                offset = var10000;
                if (machineRotationUp == 0) {
                    offset = new Vec3(offset.f_82479_, -offset.f_82480_, offset.f_82481_);
                    poseStack.m_252880_(0.5F, -0.5F, 0.5F);
                } else {
                    poseStack.m_252880_(0.5F, 0.5F, 0.5F);
                }

                a = a.m_82549_(offset);
                b = b.m_82549_(offset);
                VertexConsumer consumer = bufferSource.m_6299_(Sheets.m_110791_());
                RenderBufferHelper.renderCube(consumer, poseStack.m_85850_(), _intHeatColor((float)reactor.getRenderHeat()), 15728880, s_fluidSprite, (float)a.f_82479_, (float)a.f_82480_, (float)a.f_82481_, (float)b.f_82479_, (float)b.f_82480_, (float)b.f_82481_);
                poseStack.m_85849_();
            }
        }
    }

    private static int _intHeatColor(float t) {
        Vec3 h = _heatColor(t);
        double alpha = Math.min(0.6 + Math.pow((double)t, (double)1.5F) / Math.pow((double)5000.0F, (double)1.5F) * 0.4, (double)1.0F) * (double)255.0F;
        return ABGR32.m_266248_((int)alpha, (int)h.f_82479_, (int)h.f_82480_, (int)h.f_82481_);
    }

    private static Vec3 _heatColor(float t) {
        Vec3 c0 = new Vec3((double)0.0F, (double)0.0F, (double)0.0F);
        Vec3 c1 = new Vec3((double)120.0F, (double)0.0F, (double)0.0F);
        Vec3 c2 = new Vec3((double)255.0F, (double)130.0F, (double)0.0F);
        Vec3 c3 = new Vec3((double)255.0F, (double)255.0F, (double)210.0F);
        Vec3 c4 = new Vec3((double)255.0F, (double)255.0F, (double)255.0F);
        Vec3 c5 = new Vec3((double)200.0F, (double)230.0F, (double)255.0F);
        if (t < 1000.0F) {
            double f = (double)(t / 1000.0F);
            return _mix(c0, c1, f);
        } else if (t < 3000.0F) {
            double f = (double)((t - 1000.0F) / 2000.0F);
            return _mix(c1, c2, f);
        } else if (t < 4500.0F) {
            double f = (double)((t - 3000.0F) / 1500.0F);
            return _mix(c2, c3, f);
        } else if (t < 6500.0F) {
            double f = (double)((t - 4500.0F) / 2000.0F);
            return _mix(c3, c4, f);
        } else {
            double f = (double)((t - 6500.0F) / 4000.0F);
            return _mix(c4, c5, f);
        }
    }

    private static Vec3 srgbToLinear(Vec3 c) {
        return new Vec3(srgbToLinear(c.f_82479_), srgbToLinear(c.f_82480_), srgbToLinear(c.f_82481_));
    }

    private static Vec3 linearToSrgb(Vec3 c) {
        return new Vec3(linearToSrgb(c.f_82479_), linearToSrgb(c.f_82480_), linearToSrgb(c.f_82481_));
    }

    private static double srgbToLinear(double c) {
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static double linearToSrgb(double c) {
        return c <= 0.0031308 ? 12.92 * c : 1.055 * Math.pow(c, 0.4166666666666667) - 0.055;
    }

    private static Vec3 _mix(Vec3 a, Vec3 b, double k) {
        double u = Math.max(Math.min(k, (double)1.0F), (double)0.0F);
        double v = (double)1.0F - u;
        Vec3 a_ = srgbToLinear(a);
        Vec3 b_ = srgbToLinear(b);
        Vec3 c_ = a_.m_82542_(v, v, v).m_82549_(b_.m_82542_(u, u, u));
        return linearToSrgb(c_);
    }

    static {
        CODEC = Codec.unit(s_instance);
        TYPE = new DynamicRenderType(CODEC);
    }
}
