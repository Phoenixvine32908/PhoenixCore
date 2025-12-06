package net.phoenix.core.integration.jade;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.phoenix.core.integration.jade.provider.FissionMachineProvider;
import net.phoenix.core.integration.jade.provider.HighPressurePlasmaArcFurnaceProvider;
import net.phoenix.core.integration.jade.provider.SourceHatchProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class PhoenixJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {

        // Server (NBT)
        registration.registerBlockDataProvider(new SourceHatchProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new HighPressurePlasmaArcFurnaceProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new FissionMachineProvider(), BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {

        // Client (tooltip)
        registration.registerBlockComponent(new SourceHatchProvider(), Block.class);
        registration.registerBlockComponent(new HighPressurePlasmaArcFurnaceProvider(), Block.class);
        registration.registerBlockComponent(new FissionMachineProvider(), Block.class);
    }
}
