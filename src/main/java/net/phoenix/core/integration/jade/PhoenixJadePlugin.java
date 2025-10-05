// In your PhoenixJadePlugin.java
package net.phoenix.core.integration.jade;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
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
        // Source Hatch (Assuming it targets generic BlockEntity)
        registration.registerBlockDataProvider(new SourceHatchProvider(), BlockEntity.class);

        // Plasma Arc Furnace: Server-side data sync
        // Targets MetaMachineBlockEntity for precision, using the general method.
        registration.registerBlockDataProvider(new HighPressurePlasmaArcFurnaceProvider(),
                MetaMachineBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Register the Source Hatch provider for the client-side tooltip
        registration.registerBlockComponent(new SourceHatchProvider(), net.minecraft.world.level.block.Block.class);

        // FIX: Register the Plasma Furnace provider for the client-side component
        // by targeting the generic Block.class to satisfy the method signature.
        registration.registerBlockComponent(new HighPressurePlasmaArcFurnaceProvider(),
                net.minecraft.world.level.block.Block.class);
    }
}
