package net.phoenix.core.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;

public interface ITeslaBattery extends IBatteryData {

    long getMaxInput();

    long getMaxOutput();
}
