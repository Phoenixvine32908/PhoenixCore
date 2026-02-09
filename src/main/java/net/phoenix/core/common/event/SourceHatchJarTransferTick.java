package net.phoenix.core.common.event;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.core.PhoenixCore;
import net.phoenix.core.common.machine.multiblock.part.special.SourceHatchPartMachine;
import net.phoenix.core.configs.PhoenixConfigs;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import com.hollingsworth.arsnouveau.common.block.tile.SourceJarTile;

@Mod.EventBusSubscriber(modid = PhoenixCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SourceHatchJarTransferTick {

    public SourceHatchJarTransferTick() {}

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;

        // once per second
        if (level.getGameTime() % 20L != 0L) return;

        int radius = PhoenixConfigs.INSTANCE.sourceHatch.sourceJarCheckRadius;
        if (radius <= 0) return;

        for (BlockPos hatchPos : SourceHatchTracker.get(level.dimension())) {
            if (!level.isLoaded(hatchPos)) continue;

            BlockEntity be = level.getBlockEntity(hatchPos);
            if (!(be instanceof MetaMachineBlockEntity metaBE)) continue;

            var machine = metaBE.getMetaMachine();
            if (!(machine instanceof SourceHatchPartMachine hatch)) continue;

            ISourceTile tank = hatch.getSource();
            if (tank == null) continue;

            IO io = hatch.getIo();

            int minX = hatchPos.getX() - radius, maxX = hatchPos.getX() + radius;
            int minY = hatchPos.getY() - radius, maxY = hatchPos.getY() + radius;
            int minZ = hatchPos.getZ() - radius, maxZ = hatchPos.getZ() + radius;

            // Cap per tick to hatch transfer rate (per second here)
            int rate = tank.getTransferRate();
            if (rate <= 0) continue;

            if (io == IO.IN) {
                if (!tank.canAcceptSource()) continue;

                int space = tank.getMaxSource() - tank.getSource();
                int remaining = Math.min(space, rate);
                if (remaining <= 0) continue;

                outer:
                for (int y = minY; y <= maxY; y++)
                    for (int x = minX; x <= maxX; x++)
                        for (int z = minZ; z <= maxZ; z++) {

                            if (remaining <= 0) break outer;

                            BlockPos jarPos = new BlockPos(x, y, z);
                            BlockEntity jarBe = level.getBlockEntity(jarPos);
                            if (!(jarBe instanceof SourceJarTile jar)) continue;

                            int available = jar.getSource();
                            if (available <= 0) continue;

                            int toMove = Math.min(remaining, available);

                            jar.removeSource(toMove);
                            jar.setChanged();
                            jar.updateBlock();

                            tank.addSource(toMove);

                            ParticleUtil.spawnFollowProjectile(level, jarPos, hatchPos, jar.getColor());

                            remaining -= toMove;
                        }

            } else if (io == IO.OUT) {
                int available = tank.getSource();
                int remaining = Math.min(available, rate);
                if (remaining <= 0) continue;

                outer:
                for (int y = minY; y <= maxY; y++)
                    for (int x = minX; x <= maxX; x++)
                        for (int z = minZ; z <= maxZ; z++) {

                            if (remaining <= 0) break outer;

                            BlockPos jarPos = new BlockPos(x, y, z);
                            BlockEntity jarBe = level.getBlockEntity(jarPos);
                            if (!(jarBe instanceof SourceJarTile jar)) continue;

                            int jarSpace = jar.getMaxSource() - jar.getSource();
                            if (jarSpace <= 0) continue;

                            int toMove = Math.min(remaining, jarSpace);

                            tank.removeSource(toMove);

                            jar.addSource(toMove);
                            jar.setChanged();
                            jar.updateBlock();

                            ParticleUtil.spawnFollowProjectile(level, hatchPos, jarPos, jar.getColor());

                            remaining -= toMove;
                        }
            }
        }
    }
}
