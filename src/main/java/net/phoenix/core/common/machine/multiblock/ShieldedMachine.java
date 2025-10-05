package net.phoenix.core.common.machine.multiblock;

import net.phoenix.core.common.machine.multiblock.Shield.ShieldTypes;

public interface ShieldedMachine {

    /**
     * @return the current shield type (NORMAL, INACTIVE, DECAYED, etc.)
     */
    ShieldTypes getShieldType();

    /**
     * @return the current shield health (0–1000 for NORMAL type, 0 for inactive/decayed)
     */
    int getShieldHealth();

    /**
     * @return true if the shield is considered active
     */
    default boolean isShieldActive() {
        return getShieldType().isActive && getShieldHealth() > 0;
    }
}
