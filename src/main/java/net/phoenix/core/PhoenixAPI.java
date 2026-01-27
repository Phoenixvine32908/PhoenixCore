package net.phoenix.core;

import net.phoenix.core.api.machine.trait.ITeslaBattery;
import net.phoenix.core.common.block.TeslaBatteryBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PhoenixAPI {

    public static PhoenixAPI instance;

    public static final Map<ITeslaBattery, Supplier<TeslaBatteryBlock>> TESLA_BATTERIES = new HashMap<>();
}
