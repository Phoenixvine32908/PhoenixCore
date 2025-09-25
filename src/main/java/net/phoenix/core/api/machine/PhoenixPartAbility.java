package net.phoenix.core.api.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

public class PhoenixPartAbility extends PartAbility {

    private PhoenixPartAbility() {
        super("");
    }

    public static final PartAbility PLASMA_INPUT = new PartAbility("input_plasma");
    public static final PartAbility SOURCE_INPUT = new PartAbility("input_source");
    public static final PartAbility SOURCE_OUTPUT = new PartAbility("output_source");
}
