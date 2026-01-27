package net.phoenix.core.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.core.saveddata.FissionExplosionWorldData;

import java.util.*;

@Mod.EventBusSubscriber(modid = "PhoenixCore", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FissionExplosionManager {

    private static final List<FissionExplosionTask> ACTIVE = new ArrayList<>();

    private static FissionExplosionWorldData data(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                FissionExplosionWorldData::load,
                FissionExplosionWorldData::new,
                FissionExplosionWorldData.NAME);
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel lvl)) return;

        ACTIVE.removeIf(t -> t.getLevel() == lvl);

        FissionExplosionWorldData d = data(lvl);
        if (d.isEmpty()) return;

        for (FissionExplosionWorldData.Entry e : d.all()) {
            ACTIVE.add(new FissionExplosionTask(lvl, e.id, e.center, e.radius, e.processed));
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel lvl)) return;

        FissionExplosionWorldData d = data(lvl);

        Iterator<FissionExplosionTask> it = ACTIVE.iterator();
        while (it.hasNext()) {
            FissionExplosionTask t = it.next();
            if (t.getLevel() != lvl) continue;

            if (!t.isDone()) {
                d.put(new FissionExplosionWorldData.Entry(
                        t.getId(), t.getCenter(), t.getRadiusBlocks(), t.exportProcessedChunks()));
            }

            it.remove();
        }
    }

    public static void start(ServerLevel level, BlockPos center, int radiusBlocks) {
        if (!level.getServer().isSameThread()) {
            level.getServer().execute(() -> start(level, center, radiusBlocks));
            return;
        }

        FissionExplosionTask task = new FissionExplosionTask(level, center, radiusBlocks);
        ACTIVE.add(task);

        data(level).put(new FissionExplosionWorldData.Entry(
                task.getId(), task.getCenter(), task.getRadiusBlocks(), task.exportProcessedChunks()));

        task.tickImmediate();
    }

    // Ensures tasks are added to the work queue when chunks load
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel lvl)) return;

        ChunkPos cp = event.getChunk().getPos();

        for (FissionExplosionTask task : ACTIVE) {
            if (task.getLevel() == lvl) {
                task.onChunkLoaded(cp);
            }
        }
    }

    // Ticks all active tasks every server tick (The actual scheduler)
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (ACTIVE.isEmpty()) return;

        Iterator<FissionExplosionTask> it = ACTIVE.iterator();
        while (it.hasNext()) {
            FissionExplosionTask task = it.next();

            if (task.isDone()) {
                data(task.getLevel()).remove(task.getId());
                it.remove();
                continue;
            }

            // Executes work based on a fixed time budget
            task.tick();

            // Persists progress when the task reports it's dirty
            if (task.consumePersistDirty()) {
                data(task.getLevel()).put(new FissionExplosionWorldData.Entry(
                        task.getId(), task.getCenter(), task.getRadiusBlocks(), task.exportProcessedChunks()));
            }
        }
    }
}
