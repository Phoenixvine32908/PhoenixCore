package net.phoenix.core.client;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.core.PhoenixCore;
import net.phoenix.core.client.renderer.machine.ArtificialStarRender;
import net.phoenix.core.client.renderer.machine.EyeOfHarmonyRender;

@Mod.EventBusSubscriber(modid = PhoenixCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PhoenixClient {

    private PhoenixClient() {}

    public static void init(IEventBus modBus) {
        modBus.register(PhoenixClient.class);
        DynamicRenderManager.register(PhoenixCore.id("eye_of_harmony"), EyeOfHarmonyRender.TYPE);
        DynamicRenderManager.register(PhoenixCore.id("artificial_star"), ArtificialStarRender.TYPE);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(EyeOfHarmonyRender.SPACE_SHELL_MODEL_RL);
        event.register(EyeOfHarmonyRender.STAR_MODEL_RL);
        EyeOfHarmonyRender.ORBIT_OBJECTS_RL.forEach(event::register);
        event.register(ArtificialStarRender.ARTIFICIAL_STAR_MODEL_RL);
    }
}
