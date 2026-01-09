package net.phoenix.core;

import net.phoenix.core.api.block.IFissionCoolerType;
import net.phoenix.core.api.block.IFissionModeratorType;
import net.phoenix.core.api.machine.trait.ITeslaBattery;
import net.phoenix.core.common.block.FissionCoolerBlock;
import net.phoenix.core.common.block.FissionModeratorBlock;
import net.phoenix.core.common.block.TeslaBatteryBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PhoenixAPI {

    public static PhoenixAPI instance;

    public static final Map<IFissionCoolerType, Supplier<FissionCoolerBlock>> FISSION_COOLERS = new HashMap<>();
    public static final Map<IFissionModeratorType, Supplier<FissionModeratorBlock>> FISSION_MODERATORS = new HashMap<>();
    public static final Map<ITeslaBattery, Supplier<TeslaBatteryBlock>> TESLA_BATTERIES = new HashMap<>();
}
