package net.phoenix.core.api.machine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;

@Mod.EventBusSubscriber(modid = "PhoenixCore", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TeslaLinkEventHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos brokenPos = event.getPos();
        MetaMachine machine = MetaMachine.getMachine(level, brokenPos);

        // If it's a TieredMachine, it could be a Soul Consumer or a Wireless Charger
        if (machine instanceof TieredEnergyMachine) {
            TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);

            // This should remove it from soulLinkedMachines AND activeChargers
            data.removeMachineFromAllTeams(brokenPos);
        }
    }
}
