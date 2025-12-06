//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.compat;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.placeholder.MultiLineComponent;
import com.gregtechceu.gtceu.api.placeholder.Placeholder;
import com.gregtechceu.gtceu.api.placeholder.PlaceholderContext;
import com.gregtechceu.gtceu.api.placeholder.exceptions.NotSupportedException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.PlaceholderException;
import fi.dea.mc.deafission.api.IReactorApi;
import fi.dea.mc.deafission.common.data.machine.FissionReactorMachine;
import java.util.List;
import java.util.function.Function;

class FissionGtPlaceholders$2 extends Placeholder {
    FissionGtPlaceholders$2(String name, Function var2) {
        super(name);
        this.val$impl = var2;
    }

    public MultiLineComponent apply(PlaceholderContext ctx, List<MultiLineComponent> args) throws PlaceholderException {
        MetaMachine var4 = MetaMachine.getMachine(ctx.level(), ctx.pos());
        if (var4 instanceof FissionReactorMachine reactor) {
            IReactorApi var5 = reactor.getApi();
            return (MultiLineComponent)this.val$impl.apply(var5.getMetrics());
        } else {
            throw new NotSupportedException();
        }
    }
}
