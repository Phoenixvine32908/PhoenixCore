//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission;

import fi.dea.mc.deafission.common.data.FissionGtRecipes;
import fi.dea.mc.deafission.common.data.recipe.FissionRecipes;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@EventBusSubscriber(
    modid = "deafission",
    bus = Bus.FORGE
)
public class ConfigReloader {
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent e) {
        e.addListener(new Reloader());
    }

    private static class Reloader implements PreparableReloadListener {
        private void apply(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
            Config.reload();
            Config.configureReactor();
            FissionGtRecipes.onConfigReloaded();
            FissionRecipes.onConfigReloaded();
        }

        public CompletableFuture<Void> m_5540_(PreparableReloadListener.PreparationBarrier pPreparationBarrier, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
            CompletableFuture var10000 = CompletableFuture.runAsync(() -> {
            }, pBackgroundExecutor);
            Objects.requireNonNull(pPreparationBarrier);
            return var10000.thenCompose(pPreparationBarrier::m_6769_).thenRunAsync(() -> this.apply(pResourceManager, pReloadProfiler), pGameExecutor);
        }
    }
}
