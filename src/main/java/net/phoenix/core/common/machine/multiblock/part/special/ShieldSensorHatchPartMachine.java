package net.phoenix.core.common.machine.multiblock.part.special;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import net.minecraft.core.Direction;
import net.phoenix.core.common.machine.multiblock.Shield.ShieldTypes;
import net.phoenix.core.common.machine.multiblock.ShieldedMachine;
import net.phoenix.core.common.machine.multiblock.part.SensorHatchPartMachine;
import net.phoenix.core.common.machine.multiblock.part.ShieldRenderProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ShieldSensorHatchPartMachine extends SensorHatchPartMachine {

    public ShieldSensorHatchPartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.HV); // pick tier as you like
    }

    @Override
    public int getOutputSignal(@Nullable Direction direction) {
        if (direction == getFrontFacing().getOpposite()) {
            var controllers = getControllers().stream()
                    .filter(ShieldedMachine.class::isInstance)
                    .map(ShieldedMachine.class::cast)
                    .toList();

            if (controllers.isEmpty()) {
                return 0;
            } else {
                ShieldedMachine controller = controllers.get(0);
                ShieldTypes shield = controller.getShieldType();

                // scale shield health (0–1000) to redstone (0–15)
                int signal = (int) (15.0 * shield.shieldHealth / 1000.0);

                return signal;
            }
        }
        return 0;
    }

    @Override
    public void updateSignal() {
        super.updateSignal();
        var controllers = getControllers().stream()
                .filter(ShieldedMachine.class::isInstance)
                .map(ShieldedMachine.class::cast)
                .toList();

        if (controllers.isEmpty()) {
            setRenderShieldState(ShieldTypes.INACTIVE);
        } else {
            ShieldedMachine controller = controllers.get(0);
            setRenderShieldState(controller.getShieldType());
        }
    }

    private void setRenderShieldState(ShieldTypes type) {
        var oldRenderState = getRenderState();
        ShieldRenderProperty renderProp = switch (type) {
            case NORMAL -> ShieldRenderProperty.NORMAL;
            case INACTIVE -> ShieldRenderProperty.INACTIVE;
            case DECAYED -> ShieldRenderProperty.DECAYED;
        };
        var newRenderState = oldRenderState.setValue(ShieldRenderProperty.TYPE, renderProp);

        if (!Objects.equals(oldRenderState, newRenderState)) {
            setRenderState(newRenderState);
        }
    }

    @Override
    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        if (controller instanceof ShieldedMachine shielded) {
            setRenderShieldState(shielded.getShieldType());
        }
    }

    @Override
    public void removedFromController(@NotNull IMultiController controller) {
        super.removedFromController(controller);
        setRenderShieldState(ShieldTypes.INACTIVE);
    }
}
