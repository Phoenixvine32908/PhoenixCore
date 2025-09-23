package net.phoenix.core.api.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

public class PhoenixPartAbility extends PartAbility {

    private PhoenixPartAbility() {
        super("");
    }

    public static final PartAbility PLASMA_INPUT = new PartAbility("input_plasmas");
}
