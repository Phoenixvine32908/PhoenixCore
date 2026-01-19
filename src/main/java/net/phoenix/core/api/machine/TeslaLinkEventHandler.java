package net.phoenix.core.api.machine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;

@Mod.EventBusSubscriber(modid = "phoenixcore", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TeslaLinkEventHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos brokenPos = event.getPos();
        // Check if the block being broken is a MetaMachine
        MetaMachine machine = MetaMachine.getMachine(level, brokenPos);

        if (machine instanceof TieredEnergyMachine) {
            TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);
            // We need to find which team this machine belongs to.
            // Since we don't want to iterate every team every time a block breaks,
            // your SavedData should ideally have a reverse-lookup Map<BlockPos, UUID>
            // or we just call a global cleanup method.
            data.removeMachineFromAllTeams(brokenPos);
        }
    }
}
