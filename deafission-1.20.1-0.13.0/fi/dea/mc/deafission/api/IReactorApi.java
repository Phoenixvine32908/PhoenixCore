//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.api;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public interface IReactorApi {
    @Nullable ReactorMetrics getMetrics();

    List<@Nullable ReactorFuel> getFuels();

    byte[] getFuelDurabilityBytes();

    List<String> getMetricsStrings();
}
