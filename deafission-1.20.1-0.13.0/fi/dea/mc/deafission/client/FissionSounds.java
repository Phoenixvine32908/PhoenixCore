//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.client;

import com.gregtechceu.gtceu.api.sound.SoundEntry;
import fi.dea.mc.deafission.FissionMod;

public class FissionSounds {
    public static final SoundEntry FISSION_LOOP;

    public static void init() {
    }

    static {
        FISSION_LOOP = FissionMod.GR.sound(FissionMod.id("fission_loop")).build();
    }
}
