//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.machine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRedstoneSignalMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ReactorRedstonePort extends MultiblockPartMachine implements IRedstoneSignalMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER;
    private int _signal;
    @Persisted
    @DescSynced
    private Type _type;

    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public ReactorRedstonePort(IMachineBlockEntity holder) {
        super(holder);
        this._type = ReactorRedstonePort.Type.HEAT;
    }

    public void trySetSignal(Type type, int signal) {
        if (type == this._type) {
            if (signal != this._signal) {
                this._signal = signal;
                this.updateSignal();
            }
        }
    }

    protected InteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide, BlockHitResult hitResult) {
        if (!playerIn.m_9236_().f_46443_) {
            this._type = this._type == ReactorRedstonePort.Type.HEAT ? ReactorRedstonePort.Type.FUELS : ReactorRedstonePort.Type.HEAT;
            playerIn.m_213846_(Component.m_237113_(this._type.m_7912_()));
        }

        return InteractionResult.m_19078_(playerIn.m_9236_().f_46443_);
    }

    public int getOutputSignal(@Nullable Direction side) {
        return this._signal;
    }

    public int getOutputDirectSignal(Direction direction) {
        return this._signal;
    }

    public int getAnalogOutputSignal() {
        return this._signal;
    }

    public boolean canConnectRedstone(Direction side) {
        return true;
    }

    static {
        MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ReactorRedstonePort.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);
    }

    public static enum Type implements StringRepresentable {
        HEAT("heat"),
        FUELS("rods");

        private final String name;
        private static final Map<String, Type> s_lookup = (Map)Stream.of(values()).collect(Collectors.toMap(Type::m_7912_, (e) -> e));

        private Type(String name) {
            this.name = name;
        }

        public @NotNull String m_7912_() {
            return this.name;
        }

        public static Type fromSerializedName(String name) {
            return (Type)s_lookup.getOrDefault(name, HEAT);
        }
    }
}
