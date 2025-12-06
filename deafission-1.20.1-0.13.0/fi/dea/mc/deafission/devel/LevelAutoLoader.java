//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.devel;

import fi.dea.mc.deafission.FissionMod;
import fi.dea.mc.deafission.util.JvmProperties;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
    modid = "deafission",
    bus = Bus.FORGE,
    value = {Dist.CLIENT}
)
public class LevelAutoLoader {
    private static boolean hasLoaded = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == Phase.END) {
            if (JvmProperties.AutoLoadLevel != null) {
                Minecraft mc = Minecraft.m_91087_();
                if (!hasLoaded && mc.f_91080_ instanceof TitleScreen) {
                    hasLoaded = true;

                    try {
                        loadWorld(mc, JvmProperties.AutoLoadLevel);
                    } catch (Exception e) {
                        FissionMod.LOG.error("Load world enqueue failed", e);
                    }
                }

            }
        }
    }

    private static void loadWorld(Minecraft mc, String worldName) {
        WorldOpenFlows worldOpenFlows = new WorldOpenFlows(mc, mc.m_91392_());
        mc.execute(() -> {
            try {
                worldOpenFlows.m_233133_((Screen)Objects.requireNonNull(mc.f_91080_), worldName);
                System.out.println("[Fission] Auto-loading world: " + worldName);
            } catch (Exception e) {
                FissionMod.LOG.error("Load world failed", e);
            }

        });
    }
}
