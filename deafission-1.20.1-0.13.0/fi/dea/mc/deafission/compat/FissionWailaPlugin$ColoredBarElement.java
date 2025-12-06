//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.compat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElement.Align;

public class FissionWailaPlugin$ColoredBarElement implements IElement {
    private Vec2 size;
    private Vec2 layoutSize;
    private Vec2 fullSize;
    private Vec2 pos;
    private int color;
    private ResourceLocation tag;

    public FissionWailaPlugin$ColoredBarElement(Vec2 size, Vec2 layoutSize, Vec2 fullSize, int color) {
        this.size = Vec2.f_82462_;
        this.layoutSize = Vec2.f_82462_;
        this.fullSize = Vec2.f_82462_;
        this.pos = Vec2.f_82462_;
        this.size = size;
        this.layoutSize = layoutSize;
        this.fullSize = fullSize;
        this.color = color;
    }

    public IElement size(@Nullable Vec2 vec2) {
        this.layoutSize = vec2;
        return this;
    }

    public Vec2 getSize() {
        return this.layoutSize;
    }

    public Vec2 getCachedSize() {
        return this.layoutSize;
    }

    public void render(GuiGraphics guiGraphics, float x, float y, float maxx, float maxy) {
        Vec2 pos2 = this.size.m_165910_(new Vec2(x, y));
        Vec2 pos3 = this.fullSize.m_165910_(new Vec2(x, y));
        float dy = this.fullSize.f_82471_ - this.size.f_82471_;
        guiGraphics.m_280509_((int)x, (int)y, (int)pos3.f_82470_, (int)pos3.f_82471_, 1145324714);
        guiGraphics.m_280509_((int)x, (int)(y + dy), (int)pos2.f_82470_, (int)(pos2.f_82471_ + dy), this.color);
    }

    public IElement align(IElement.Align align) {
        return this;
    }

    public IElement.Align getAlignment() {
        return Align.LEFT;
    }

    public IElement translate(Vec2 vec2) {
        this.pos = this.pos.m_165910_(vec2);
        return this;
    }

    public Vec2 getTranslation() {
        return this.pos;
    }

    public IElement tag(ResourceLocation resourceLocation) {
        this.tag = resourceLocation;
        return this;
    }

    public ResourceLocation getTag() {
        return this.tag;
    }

    public @Nullable String getCachedMessage() {
        return "";
    }

    public IElement clearCachedMessage() {
        return this;
    }

    public IElement message(@Nullable String s) {
        return this;
    }
}
