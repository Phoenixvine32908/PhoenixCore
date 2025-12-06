package net.phoenix.core.common.cover; // Your mod's package

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

public class PhoenixCoverSolarPanel extends CoverBehavior {

    protected final long EUt;
    protected TickableSubscription subscription;

    // Default constructor (e.g., used for a basic, non-tiered cover)
    public PhoenixCoverSolarPanel(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        // Default to ULV voltage (8 EUt) instead of 1 EUt for better utility
        this.EUt = GTValues.V[GTValues.ULV];
    }

    // Tiered constructor (used by the GTCovers.registerTiered mechanism)
    public PhoenixCoverSolarPanel(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide,
                                  int tier) {
        super(definition, coverHolder, attachedSide);
        this.EUt = GTValues.V[tier];
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscription = coverHolder.subscribeServerTick(subscription, this::update);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @Override
    public boolean canAttach() {
        // Solar panels must be on top and require an energy container
        return super.canAttach() && attachedSide == Direction.UP && getEnergyContainer() != null;
    }

    protected void update() {
        Level level = coverHolder.getLevel();
        BlockPos blockPos = coverHolder.getPos();
        // Check if the panel can see the sun
        if (GTUtil.canSeeSunClearly(level, blockPos)) {
            IEnergyContainer energyContainer = getEnergyContainer();
            if (energyContainer != null) {
                // Accepts EUt energy every tick
                energyContainer.acceptEnergyFromNetwork(null, EUt, 1);
            }
        }
    }

    @Nullable
    protected IEnergyContainer getEnergyContainer() {
        return GTCapabilityHelper.getEnergyContainer(coverHolder.getLevel(), coverHolder.getPos(), attachedSide);
    }
}
