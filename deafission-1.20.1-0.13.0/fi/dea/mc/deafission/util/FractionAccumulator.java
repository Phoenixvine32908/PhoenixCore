//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.util;

public final class FractionAccumulator {
    private double _fraction;

    public FractionAccumulator(double fraction) {
        this._fraction = fraction;
    }

    public int accumulate(double value) {
        assert value >= (double)0.0F : "NotImplemented: negative accumulation";

        double newTotal = this._fraction + value;
        this._fraction = newTotal % (double)1.0F;
        return (int)newTotal;
    }

    public double getFraction() {
        return this._fraction;
    }
}
