package net.phoenix.core.api;

import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.phoenix.core.phoenixcore;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class PhoenixSounds {

    public static final SoundEntry MICROVERSE = REGISTRATE.sound(phoenixcore.id("microverse")).build();

    public static void init() {}
}
