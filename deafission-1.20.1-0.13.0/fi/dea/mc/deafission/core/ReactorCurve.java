//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.core;

public class ReactorCurve {
    private ReactorCurveState _state;

    public ReactorCurve(ReactorCurveState state) {
        this._state = state;
    }

    public double getCoolantConversion() {
        double lowerBoundTemp = (double)100.0F;
        return Math.max(this._state.Heat() - lowerBoundTemp, (double)0.0F) / (double)100.0F;
    }

    public double getCooling(double coolantConversion) {
        double coolantCooling = this._state.recipe() != null ? this._state.Heat() * coolantConversion * this._state.recipe().coolantHeat() : (double)0.0F;
        double passiveCooling = this._state.Heat() * 0.01;
        return coolantCooling + passiveCooling;
    }

    public double getMinimumCooling() {
        return (double)25.0F;
    }

    public double getHeating(double throttle) {
        Double fuelHeat = this._state.RecipeFuels().stream().map((f) -> f != null ? f.heat() : (double)0.0F).mapToDouble((d) -> d).sum();
        return fuelHeat * (double)200.0F * throttle;
    }

    public double getFuelWear(double throttle) {
        double mult = (double)1.0F / ((double)1.0F + this._state.components().efficiency());
        return this._state.Heat() / (double)500.0F * mult * throttle;
    }

    public double getMinHeat() {
        return (double)0.0F;
    }

    public double getMaxHeat() {
        return (double)400.0F + this._state.components().heat();
    }

    public double getThrottle(double maintenanceValue) {
        return maintenanceValue / ((double)1.0F + this._state.components().throttle());
    }

    public double getThermalMass() {
        return (double)1000.0F;
    }

    public static double clampHeat(ReactorCurve curve, double t) {
        return Math.min(Math.max(t, curve.getMinHeat()), curve.getMaxHeat());
    }
}
