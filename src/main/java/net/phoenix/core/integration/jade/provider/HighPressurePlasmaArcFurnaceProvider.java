package net.phoenix.core.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.core.common.machine.multiblock.Shield;
import net.phoenix.core.common.machine.multiblock.electric.HighPressurePlasmaArcFurnaceMachine;
import net.phoenix.core.phoenixcore;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

// Implement both interfaces
public class HighPressurePlasmaArcFurnaceProvider implements IBlockComponentProvider,
                                                  IServerDataProvider<BlockAccessor> {

    // Unique ID for the provider
    public static final ResourceLocation UID = phoenixcore.id("plasma_furnace_info");

    // --- Client Side: Display the data ---
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        // ... (Existing code for machine check)
        CompoundTag data = accessor.getServerData();
        Shield.ShieldTypes shieldType = null;

        // --- 1. Display Shield State & Determine Type ---
        if (data.contains("shieldKey")) {
            // ... (Resolve shieldType and display state)
            int shieldKey = data.getInt("shieldKey");
            shieldType = Shield.ShieldTypes.getShieldFromKey(shieldKey);

            if (shieldType != null) {
                tooltip.add(Component.translatable(
                        "jade.phoenixcore.shield_state",
                        Component.translatable(shieldType.langKey)));
            }
        }

        if (shieldType == null) return;

        // --- 2. FIX: Display Shield Health (ONLY for NORMAL state) ---
        if (shieldType == Shield.ShieldTypes.NORMAL && data.contains("shieldHealth")) {
            int health = data.getInt("shieldHealth");
            tooltip.add(Component.translatable("jade.phoenixcore.shield_health", health));
        }

        // --- 3. FIX: Display Shield Cooldown (ONLY for DECAYED state with timer > 0) ---
        if (shieldType == Shield.ShieldTypes.DECAYED && data.contains("shieldCooldown")) {
            int cooldownTicks = data.getInt("shieldCooldown");
            if (cooldownTicks > 0) {
                int seconds = cooldownTicks / 20; // Convert ticks to seconds
                tooltip.add(Component.translatable("jade.phoenixcore.shield_cooldown", seconds));
            }
        }

        // --- 4. FIX: Display Plasma Boost State (Only if NORMAL) ---
        if (shieldType == Shield.ShieldTypes.NORMAL) {
            if (data.contains("isBoosted") && data.getBoolean("isBoosted")) {
                // ... (rest of boost details display logic as previously defined) ...

                String boostName = data.contains("boostName") ? data.getString("boostName") : "Unknown";
                float durationMultiplier = data.contains("boostDuration") ? data.getFloat("boostDuration") : 0.0f;

                tooltip.add(Component.translatable(
                        "jade.phoenixcore.plasma_boost_active",
                        Component.literal("§b" + boostName)));

                tooltip.add(Component.translatable(
                        "jade.phoenixcore.plasma_boost_duration",
                        (int) (durationMultiplier * 100) + "%").withStyle(style -> style.withColor(0x00FFFF)));

            } else {
                // Display if no boost is active/fueled
                tooltip.add(Component.translatable("jade.phoenixcore.no_plasma_boost")
                        .withStyle(style -> style.withColor(0x777777)));
            }
        }
    }

    // --- Server Side: Collect and sync the data ---
    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof MetaMachineBlockEntity meta_machine_be &&
                meta_machine_be.getMetaMachine() instanceof HighPressurePlasmaArcFurnaceMachine machine &&
                machine.isFormed()) {

            // 1. Shield State, Health, and Cooldown (The data needed for client display)
            compoundTag.putInt("shieldKey", machine.getShieldType().key);

            // --- FIX 1: SYNC HEALTH AND COOLDOWN DATA (Using the getters we confirmed exist) ---
            compoundTag.putInt("shieldHealth", machine.getShieldHealth());
            compoundTag.putInt("shieldCooldown", machine.getShieldCooldownTimer());
            // ---------------------------------------------------------------------------------

            // 2. Plasma Boost Status
            // FIX 2: ONLY true if the machine has plasma AND is ACTIVELY RUNNING A RECIPE.
            boolean isBoostApplied = machine.recipeLogic.isActive() && machine.isPlasmaBoosted;

            compoundTag.putBoolean("isBoosted", isBoostApplied);

            // 3. Plasma Boost Details
            if (isBoostApplied && machine.activeBoost != null) {
                compoundTag.putString("boostName", machine.activeBoost.name());
                compoundTag.putFloat("boostDuration", (float) machine.activeBoost.durationMultiplier());
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
