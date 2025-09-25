package net.phoenix.core.integration.jade;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.phoenix.core.integration.jade.provider.SourceHatchProvider;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class PhoenixJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        // Register the new Source Hatch provider for the server-side data
        registration.registerBlockDataProvider(new SourceHatchProvider(), BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Register the new Source Hatch provider for the client-side tooltip
        registration.registerBlockComponent(new SourceHatchProvider(), Block.class);
    }
}
