//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.compat;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import fi.dea.mc.deafission.api.IReactorApi;
import fi.dea.mc.deafission.api.ReactorMetrics;
import fi.dea.mc.deafission.common.data.machine.FissionReactorMachine;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElement.Align;

@WailaPlugin
public class FissionWailaPlugin implements IWailaPlugin {
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new ReactorDataProvider(), MetaMachineBlockEntity.class);
    }

    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new ReactorDataProvider(), Block.class);
    }

    public static class ReactorDataProvider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            int barWidth = 10;
            int barHeight = 16;
            int barPadding = 4;
            CompoundTag data = accessor.getServerData();
            if (data.m_128441_("ReactorHeat")) {
                int heat = data.m_128451_("ReactorHeat");
                int heatMax = data.m_128451_("ReactorHeatMax");
                byte[] fuels = data.m_128463_("ReactorFuel");
                tooltip.add(Component.m_237113_("Heat: " + heat + " / " + heatMax));
                ArrayList<IElement> arr = new ArrayList(fuels.length);

                for(int i = 0; i < fuels.length; ++i) {
                    int byteValue = fuels[i] & 255;
                    int pad = i < fuels.length - 1 ? 4 : 0;
                    if (byteValue != 0) {
                        float barLength = (float)Math.floor((double)((float)(16 * (byteValue - 1) / 254) + 0.5F));
                        arr.add(new ColoredBarElement(new Vec2(10.0F, barLength), new Vec2((float)(10 + pad), 19.0F), new Vec2(10.0F, 16.0F), -1));
                    } else {
                        arr.add(new ColoredBarElement(new Vec2(10.0F, 16.0F), new Vec2((float)(10 + pad), 19.0F), new Vec2(10.0F, 16.0F), 1157627903));
                    }
                }

                tooltip.add(arr);
                String debug = data.m_128461_("ReactorDebug");

                for(String line : debug.split("\n")) {
                    tooltip.add(Component.m_237113_(line));
                }

            }
        }

        public ResourceLocation getUid() {
            return ResourceLocation.fromNamespaceAndPath("deafission", "reactor");
        }

        public void appendServerData(CompoundTag tag, BlockAccessor blockAccessor) {
            BlockEntity var5 = blockAccessor.getBlockEntity();
            if (var5 instanceof MetaMachineBlockEntity be) {
                MetaMachine var7 = be.getMetaMachine();
                if (var7 instanceof FissionReactorMachine reactor) {
                    IReactorApi r = reactor.getApi();
                    ReactorMetrics m = r.getMetrics();
                    if (m != null) {
                        tag.m_128405_("ReactorHeat", (int)Math.round(m.HeatPost()));
                        tag.m_128405_("ReactorHeatMax", (int)Math.round(m.MaxHeat()));
                        tag.m_128382_("ReactorFuel", r.getFuelDurabilityBytes());
                        tag.m_128359_("ReactorDebug", String.join("\n", r.getMetricsStrings()));
                    }
                }
            }

        }
    }

    public static class ColoredBarElement implements IElement {
        private Vec2 size;
        private Vec2 layoutSize;
        private Vec2 fullSize;
        private Vec2 pos;
        private int color;
        private ResourceLocation tag;

        public ColoredBarElement(Vec2 size, Vec2 layoutSize, Vec2 fullSize, int color) {
            this.size = Vec2.f_82462_;
            this.layoutSize = Vec2.f_82462_;
            this.fullSize = Vec2.f_82462_;
            this.pos = Vec2.f_82462_;
            this.size = size;
            this.layoutSize = layoutSize;
            this.fullSize = fullSize;
            this.color = color;
        }

        public IElement size(@Nullable Vec2 vec2) {
            this.layoutSize = vec2;
            return this;
        }

        public Vec2 getSize() {
            return this.layoutSize;
        }

        public Vec2 getCachedSize() {
            return this.layoutSize;
        }

        public void render(GuiGraphics guiGraphics, float x, float y, float maxx, float maxy) {
            Vec2 pos2 = this.size.m_165910_(new Vec2(x, y));
            Vec2 pos3 = this.fullSize.m_165910_(new Vec2(x, y));
            float dy = this.fullSize.f_82471_ - this.size.f_82471_;
            guiGraphics.m_280509_((int)x, (int)y, (int)pos3.f_82470_, (int)pos3.f_82471_, 1145324714);
            guiGraphics.m_280509_((int)x, (int)(y + dy), (int)pos2.f_82470_, (int)(pos2.f_82471_ + dy), this.color);
        }

        public IElement align(IElement.Align align) {
            return this;
        }

        public IElement.Align getAlignment() {
            return Align.LEFT;
        }

        public IElement translate(Vec2 vec2) {
            this.pos = this.pos.m_165910_(vec2);
            return this;
        }

        public Vec2 getTranslation() {
            return this.pos;
        }

        public IElement tag(ResourceLocation resourceLocation) {
            this.tag = resourceLocation;
            return this;
        }

        public ResourceLocation getTag() {
            return this.tag;
        }

        public @Nullable String getCachedMessage() {
            return "";
        }

        public IElement clearCachedMessage() {
            return this;
        }

        public IElement message(@Nullable String s) {
            return this;
        }
    }
}
