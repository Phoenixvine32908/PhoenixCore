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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;

public class FissionWailaPlugin$ReactorDataProvider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
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
                    arr.add(new FissionWailaPlugin.ColoredBarElement(new Vec2(10.0F, barLength), new Vec2((float)(10 + pad), 19.0F), new Vec2(10.0F, 16.0F), -1));
                } else {
                    arr.add(new FissionWailaPlugin.ColoredBarElement(new Vec2(10.0F, 16.0F), new Vec2((float)(10 + pad), 19.0F), new Vec2(10.0F, 16.0F), 1157627903));
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
