package net.phoenix.core.api.capability;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;

public class SourceProviderCap implements ISourceProviderCapability {

    private final ISourceTile source;

    public SourceProviderCap(ISourceTile source) {
        this.source = source;
    }

    @Override
    public ISourceTile getSource() {
        return source;
    }
}
