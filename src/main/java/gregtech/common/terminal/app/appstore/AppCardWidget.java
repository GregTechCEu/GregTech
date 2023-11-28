package gregtech.common.terminal.app.appstore;

import gregtech.api.GTValues;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.*;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.AnimaWidgetGroup;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.interpolate.Eases;
import gregtech.api.util.interpolate.Interpolator;
import gregtech.client.shader.Shaders;
import gregtech.client.utils.RenderUtil;
import gregtech.common.items.behaviors.TerminalBehaviour;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.List;

public class AppCardWidget extends AnimaWidgetGroup {

    private final AbstractApplication application;
    private final AppStoreApp store;
    @SideOnly(Side.CLIENT)
    private ImageWidget stateWidget;
    @SideOnly(Side.CLIENT)
    private ImageWidget bgWidget;
    @SideOnly(Side.CLIENT)
    private IGuiTexture banner;
    @SideOnly(Side.CLIENT)
    private int offset;
    @SideOnly(Side.CLIENT)
    private int alpha;

    public AppCardWidget(int x, int y, AbstractApplication application, AppStoreApp store) {
        super(x, y, 100, 100);
        this.application = application;
        this.store = store;
        TerminalOSWidget os = store.getOs();
        if (os.isRemote()) {
            this.addWidget(new CircleButtonWidget(15, 17)
                    .setColors(TerminalTheme.COLOR_B_2.getColor(),
                            application.getThemeColor(),
                            TerminalTheme.COLOR_B_2.getColor())
                    .setHoverText(application.getUnlocalizedName())
                    .setIcon(application.getIcon()));
            this.addWidget(new ImageWidget(30, 0, 65, 34,
                    new TextTexture(application.getUnlocalizedName(), -1).setDropShadow(true)
                            .setWidth(65)));
            offset = Math.abs(GTValues.RNG.nextInt()) % 200;
            banner = application.getBanner();
            if (os.installedApps.contains(application)) {
                if (TerminalBehaviour.isCreative(os.itemStack) || application.getMaxTier() ==
                        Math.min(os.tabletNBT.getCompoundTag(application.getRegistryName()).getInteger("_tier"),
                                application.getMaxTier())) {
                    updateState(0);
                } else {
                    updateState(1);
                }
            } else {
                updateState(2);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void updateState(int state) {
        String text = "Latest";
        int bg = 0xff4B4C4C;
        if (state == 1) {
            text = "Upgrade";
            bg = 0xffF7911F;
        } else if (state == 2) {
            text = "Install";
            bg = 0xff67F74B;
        }
        if (stateWidget != null) {
            removeWidget(stateWidget);
            removeWidget(bgWidget);
        }
        stateWidget = new ImageWidget(15, 85, 70, 15,
                new TextTexture(text, -1).setWidth(70).setDropShadow(true).setType(TextTexture.TextType.HIDE));
        bgWidget = new ImageWidget(15, 85, 70, 13, new ColorRectTexture(bg));
        this.addWidget(bgWidget);
        this.addWidget(stateWidget);
    }

    private void openDialog() {
        new AppPageWidget(application, store, this).open();
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -1) { // open page
            if (buffer.readBoolean()) {
                openDialog();
            }
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            writeClientAction(-1, buffer -> buffer.writeBoolean(true));
            openDialog();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void hookDrawInForeground(int mouseX, int mouseY) {
        // hover
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        if (isMouseOverElement(mouseX, mouseY) &&
                store.getOs().desktop.widgets.stream().noneMatch(app -> app instanceof TerminalDialogWidget)) {
            int dur = 7;
            int maxAlpha = 100; // 0-255!!!!!
            float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
            if (alpha != maxAlpha && interpolator == null) {
                interpolator = new Interpolator(0, maxAlpha, dur, Eases.LINEAR,
                        value -> alpha = value.intValue(),
                        value -> interpolator = null);
                interpolator.start();
            }
            // smooth
            int color;
            if (alpha == maxAlpha) {
                color = TerminalTheme.COLOR_B_2.getColor() & 0x00ffffff | ((alpha) << 24);
            } else {
                color = TerminalTheme.COLOR_B_2.getColor() & 0x00ffffff |
                        ((alpha + (int) (maxAlpha * partialTicks / dur)) << 24);
            }
            int finalColor = color;
            RenderUtil.useScissor(store.getPosition().x, store.getPosition().y, store.getSize().width,
                    store.getSize().height, () -> {
                        drawSolidRect(0, 0, gui.getScreenWidth(), y, finalColor);
                        drawSolidRect(0, y + height, gui.getScreenWidth(), gui.getScreenHeight(), finalColor);
                        drawSolidRect(0, y, x, height, finalColor);
                        drawSolidRect(x + width, y, gui.getScreenWidth(), height, finalColor);

                        drawBorder(x, y, width, height, application.getThemeColor(), -1);
                    });
        } else {
            alpha = 0;
        }
        super.hookDrawInForeground(mouseX, mouseY);
    }

    @Override
    public void hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        Color color = new Color(application.getThemeColor());
        drawSolidRect(x, y, width, height, color.getRGB() & 0x00ffffff | 0x4f000000);
        if (banner != null) {
            banner.draw(x, y, width, 34);
        } else {
            if (Shaders.allowedShader()) {
                float time = offset + (gui.entityPlayer.ticksExisted + partialTicks) / 20f;
                ShaderTexture.createShader("banner.frag").draw(x, y, width, 34, uniformCache -> {
                    uniformCache.glUniform1F("u_time", time);
                    uniformCache.glUniform3F("b_color", color.getRed() / 255f, color.getGreen() / 255f,
                            color.getBlue() / 255f);
                });
            } else {
                drawSolidRect(x, y, width, 34, color.getRGB());
            }
        }
        super.hookDrawInBackground(mouseX, mouseY, partialTicks, context);

        // description
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        List<String> description = fr.listFormattedStringToWidth(application.getDescription(), 90);
        int fColor = store.darkMode ? -1 : 0xff333333;
        for (int i = 0; i < Math.min(description.size(), 4); i++) {
            fr.drawString(description.get(i), x + 5, y + 40 + i * fr.FONT_HEIGHT, fColor, store.darkMode);
        }
        if (description.size() > 4) {
            fr.drawString("...", x + 5, y + 40 + 4 * fr.FONT_HEIGHT, fColor, store.darkMode);
        }

        // shadow
        drawGradientRect(x, y + 34, width, 3, 0x6f000000, 0, false);
        drawRectShadow(x, y, width, height, 5);
    }
}
