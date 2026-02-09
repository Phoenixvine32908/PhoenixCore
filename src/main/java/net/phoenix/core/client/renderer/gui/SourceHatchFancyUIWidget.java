package net.phoenix.core.client.renderer.gui;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.phoenix.core.common.machine.multiblock.part.special.SourceHatchPartMachine;

import javax.annotation.Nonnull;

public class SourceHatchFancyUIWidget extends FancyMachineUIWidget {

    private final SourceHatchPartMachine hatch;

    private static final int BG_COLOR_A = 0xFF0c0c12;
    private static final int BG_COLOR_B = 0xFF060608;

    private static final int BUTTON_BG = 0xB0101018;
    private static final int BUTTON_BORDER = 0xFF6A3DFF;

    // “realm mist” tint
    private static final int MIST_RGB = 0x6A3DFF;

    public SourceHatchFancyUIWidget(IFancyUIProvider mainPage, int width, int height) {
        super(mainPage, width, height);
        this.hatch = (SourceHatchPartMachine) mainPage;

        setBackground((IGuiTexture) null);
    }

    @Override
    public void initWidget() {
        super.initWidget();

        addExitButton();
    }

    private void addExitButton() {
        int bw = 40, bh = 14;
        int bx = getPosition().x + getSize().width - 46;
        int by = getPosition().y + 6;

        IGuiTexture bg = new GuiTextureGroup(
                new ColorRectTexture(BUTTON_BG),
                new ColorBorderTexture(1, BUTTON_BORDER));

        // Clickable background
        addWidget(new ButtonWidget(bx, by, bw, bh, bg, cd -> closeUIClientSide()));

        // Centered text on top of the button
        var font = Minecraft.getInstance().font;
        int tx = bx + (bw - font.width("Exit")) / 2;
        int ty = by + (bh - font.lineHeight) / 2;

        addWidget(new LabelWidget(tx, ty, "Exit"));
    }

    @OnlyIn(Dist.CLIENT)
    private void closeUIClientSide() {
        // Your ModularUI build doesn't expose close(), so close the current screen.
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawFullBackground(graphics);
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        drawOverlayHUD(graphics);
    }

    @OnlyIn(Dist.CLIENT)
    private void drawFullBackground(GuiGraphics graphics) {
        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        // Base gradient
        DrawerHelper.drawGradientRect(graphics, x, y, w, h, BG_COLOR_A, BG_COLOR_B, false);

        // Subtle grid
        drawGridPattern(graphics, x, y, w, h);

        // Animated mist
        drawMist(graphics, x, y, w, h);

        // Border tint based on IO
        int accent = getAccentColor();
        DrawerHelper.drawBorder(graphics, x, y, w, h, accent & 0x55FFFFFF, 1);
    }

    @OnlyIn(Dist.CLIENT)
    private void drawGridPattern(GuiGraphics graphics, int x, int y, int w, int h) {
        int gridColor = 0x08FFFFFF;
        int spacing = 16;

        for (int gx = x + spacing; gx < x + w; gx += spacing) {
            graphics.fill(gx, y, gx + 1, y + h, gridColor);
        }
        for (int gy = y + spacing; gy < y + h; gy += spacing) {
            graphics.fill(x, gy, x + w, gy + 1, gridColor);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void drawMist(GuiGraphics graphics, int x, int y, int w, int h) {
        long t = System.currentTimeMillis();
        int ox = (int) ((t / 20) & 255);
        int oy = (int) ((t / 33) & 255);

        int step = 6; // lower = prettier, higher = faster

        for (int py = 0; py < h; py += step) {
            for (int px = 0; px < w; px += step) {
                int nx = (px + ox) & 255;
                int ny = (py + oy) & 255;

                // cheap hash noise 0..255
                int v = ((nx * 734287 + ny * 912271) ^ (nx * 31 + ny * 17)) & 255;

                int a = (v * 60) / 255; // 0..60 alpha
                int col = (a << 24) | MIST_RGB;

                graphics.fill(x + px, y + py, x + px + step, y + py + step, col);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void drawOverlayHUD(GuiGraphics graphics) {
        var font = Minecraft.getInstance().font;

        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        int cur = hatch.getSourceContainer().getSource();
        int max = Math.max(1, hatch.getSourceContainer().getMaxSource());
        int rate = hatch.getSourceContainer().getTransferRate();
        int pct = (int) ((cur * 100L) / max);

        int accent = getAccentColor();
        int value = 0xFFE6E6FF;
        int label = 0xFF8080A0;

        graphics.drawString(font, "INSIDE: SOURCE HATCH", x + 10, y + 20, accent, false);

        graphics.drawString(font, "MODE:", x + 10, y + 40, label, false);
        graphics.drawString(font, hatch.getIo().name(), x + 60, y + 40, value, false);

        graphics.drawString(font, "SOURCE:", x + 10, y + 54, label, false);
        graphics.drawString(font, cur + " / " + max + " (" + pct + "%)", x + 60, y + 54, value, false);

        graphics.drawString(font, "RATE:", x + 10, y + 68, label, false);
        graphics.drawString(font, rate + "/s", x + 60, y + 68, value, false);

        // Accent divider
        graphics.fill(x + 10, y + 84, x + w - 10, y + 85, accent);

        // Simple orb gauge on the right
        int orbR = 38;
        int orbCx = x + w - 58;
        int orbCy = y + h / 2 + 18;
        drawOrb(graphics, orbCx, orbCy, orbR, pct, accent);
    }

    @OnlyIn(Dist.CLIENT)
    private void drawOrb(GuiGraphics graphics, int cx, int cy, int r, int pct, int accent) {
        pct = Math.max(0, Math.min(100, pct));

        // outer shadow
        graphics.fill(cx - r - 2, cy - r - 2, cx + r + 2, cy + r + 2, 0x66000000);

        // orb body
        graphics.fill(cx - r, cy - r, cx + r, cy + r, 0xAA0C0D22);

        // fill (vertical)
        int fillH = (int) ((2f * r) * (pct / 100f));
        int y0 = cy + r - fillH;
        graphics.fill(cx - r, y0, cx + r, cy + r, (0x90 << 24) | (accent & 0x00FFFFFF));

        // highlights
        graphics.fill(cx - r, cy - r, cx - r + 5, cy + r, 0x22000000);
        graphics.fill(cx - r, cy - r, cx + r, cy - r + 5, 0x22000000);

        // percent text
        var font = Minecraft.getInstance().font;
        String txt = pct + "%";
        int tw = font.width(txt);
        graphics.drawString(font, txt, cx - tw / 2, cy - font.lineHeight / 2, 0xFFFFFFFF, true);
    }

    @OnlyIn(Dist.CLIENT)
    private int getAccentColor() {
        return switch (hatch.getIo()) {
            case IN -> 0xFF66AAFF;
            case OUT -> 0xFFAA66FF;
            case BOTH -> 0xFF66FFCC;
            default -> 0xFF8080A0;
        };
    }
}
