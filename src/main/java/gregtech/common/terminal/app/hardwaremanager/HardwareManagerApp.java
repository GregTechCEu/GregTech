package gregtech.common.terminal.app.hardwaremanager;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.*;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.hardware.Hardware;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.client.shader.Shaders;
import gregtech.common.items.MetaItems;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.atomic.AtomicInteger;

public class HardwareManagerApp extends AbstractApplication {

    private static final TextureArea CIRCUIT_LINE = TextureArea
            .fullImage("textures/gui/terminal/hardware_manager/circuit.png");
    @SideOnly(Side.CLIENT)
    private ShaderTexture circuit;
    private HardwareSlotWidget selected;
    private WidgetGroup apps;

    public HardwareManagerApp() {
        super("hardware");
    }

    @Override
    public IGuiTexture getIcon() {
        return new ItemStackTexture(MetaItems.INTEGRATED_CIRCUIT.getStackForm());
    }

    @Override
    public AbstractApplication initApp() {
        apps = new WidgetGroup();
        this.addWidget(apps);
        int x = 10;
        int y = 65;
        for (Hardware hardware : os.hardwareProvider.getProviders().values()) {
            HardwareSlotWidget hardwareSlotWidget = new HardwareSlotWidget(x, y, os, hardware);
            this.addWidget(hardwareSlotWidget);
            hardwareSlotWidget.setOnSelected(() -> {
                selected = hardwareSlotWidget;
                apps.clearAllWidgets();
                AtomicInteger index = new AtomicInteger(0);
                for (AbstractApplication installed : getOs().installedApps) {
                    TerminalRegistry
                            .getAppHardwareDemand(installed.getRegistryName(),
                                    getOs().tabletNBT.getCompoundTag(installed.getRegistryName()).getInteger("_tier"))
                            .stream()
                            .filter(hardware::isHardwareAdequate).findFirst()
                            .ifPresent(X -> {
                                apps.addWidget(new RectButtonWidget(162 + (index.get() % 4) * 25,
                                        122 + (index.get() / 4) * 30, 20, 20, 2)
                                                .setIcon(installed.getIcon())
                                                .setHoverText(installed.getUnlocalizedName())
                                                .setColors(0, TerminalTheme.COLOR_7.getColor(), 0));
                                index.getAndIncrement();
                            });
                }
            });
            x += 25;
        }
        return this;
    }

    @Override
    protected void hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        float time = (gui.entityPlayer.ticksExisted + partialTicks) / 20f;
        if (Shaders.allowedShader()) {
            if (circuit == null) {
                circuit = ShaderTexture.createShader("circuit.frag");
            }
            ResourceHelper.bindTexture("textures/gui/terminal/terminal_background.png");
            circuit.draw(x, y, width, height, uniformCache -> {
                uniformCache.glUniform1F("u_time", time);
                uniformCache.glUniform2F("u_mouse",
                        (float) (mouseX - x) * circuit.getResolution(),
                        (float) (mouseY - y) * circuit.getResolution());
            });
        } else {
            drawSolidRect(x, y, width, height, TerminalTheme.COLOR_B_2.getColor());
        }
        GlStateManager.color(1, 1, 1, 0.8f);
        CIRCUIT_LINE.draw(x, y, width, height);
        GlStateManager.color(1, 1, 1, 1);

        super.hookDrawInBackground(mouseX, mouseY, partialTicks, context);
        if (selected != null) {
            int sX = x + selected.getSelfPosition().x;
            int sY = y + selected.getSelfPosition().y;
            int sW = selected.getSize().width;
            int sH = selected.getSize().height;
            drawBorder(sX, sY, sW, sH, 0xff00af00, 1);
            drawSolidRect(sX + sW / 2, sY + sH, 1, 10, 0xff00af00);
            drawSolidRect(sX + sW / 2, sY + sH + 10, x + 210 - sX - sW / 2, 1, 0xff00af00);
            drawSolidRect(x + 210, sY + sH + 10, 1, y + 110 - sY - sH, 0xff00af00);
        }
        drawBorder(x + 160, y + 120, 25 * 4, 25 * 4, selected == null ? -1 : 0xff00af00, 1);
    }
}
