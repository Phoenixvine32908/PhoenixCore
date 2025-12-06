//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.core;

import java.util.function.Function;

public record ReactorConfiguration(Function<ReactorCurveState, ReactorCurve> CurveFactory, Boolean RunWithoutCoolant) {
}
