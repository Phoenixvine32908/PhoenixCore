package net.phoenix.core.common.machine.multiblock;

import lombok.Getter;

public class Shield {

    public enum ShieldTypes {

        // key, decayRate, isActive, shieldCooldownTicks, shieldTimer, shieldCooldown, shieldHealth, langKey
        NORMAL(1, 10, true, 20 * 60, 1, 1, 1000, "shield.phoenixcore.type.normal"), // Decays over 100s
        INACTIVE(2, 0, false, 0, 0, 0, 0, "shield.phoenixcore.type.inactive"),
        DECAYED(3, 0, false, 20 * 60 * 5, 0, 0, 0, "shield.phoenixcore.type.decayed"); // 5-minute cooldown

        public static final ShieldTypes[] SHIELDS = ShieldTypes.values();

        public final int shieldHealth;
        public final int shieldCooldown;
        public final int shieldTimer;
        public final int decayRate;
        public final int shieldCooldownTicks;
        public final boolean isActive;
        public final String langKey;
        @Getter
        public final int key;

        ShieldTypes(int key, int decayRate, boolean isActive, int shieldCooldownTicks, int shieldTimer, int shieldCooldown, int shieldHealth, String langKey) {
            this.shieldHealth = shieldHealth;
            this.shieldTimer = shieldTimer;
            this.shieldCooldown = shieldCooldown;
            this.shieldCooldownTicks = shieldCooldownTicks;
            this.decayRate = decayRate;
            this.isActive = isActive;
            this.langKey = langKey;
            this.key = key;
        }

        public static ShieldTypes getShieldFromKey(int pKey) {
            int index = pKey - 1;
            if (index >= 0 && index < SHIELDS.length) {
                return SHIELDS[index];
            }
            return INACTIVE;
        }

        @Override
        public String toString() {
            return "Shield{" + name() + "}";
        }
    }
}