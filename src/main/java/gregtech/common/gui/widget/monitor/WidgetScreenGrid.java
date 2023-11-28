package gregtech.common.gui.widget.monitor;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Mouse;

public class WidgetScreenGrid extends Widget {

    public int x, y;
    public static final int width = 20;
    public static final int height = 20;
    MetaTileEntityMonitorScreen monitorScreen;
    private int color = 0XFF000000;

    public WidgetScreenGrid(int xPosition, int yPosition, int x, int y) {
        super(new Position(xPosition + x * width, yPosition + y * height), new Size(width, height));
        this.x = x;
        this.y = y;
    }

    public void setScreen(MetaTileEntityMonitorScreen monitorScreen) {
        this.monitorScreen = monitorScreen;
        color = (monitorScreen != null && monitorScreen.isActive()) ? 0XFF4F66FF : 0XFF000000;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = this.getPosition().x;
        int y = this.getPosition().y;
        int width = this.getSize().width;
        int height = this.getSize().height;
        int color = (monitorScreen != null && monitorScreen.isActive()) ? monitorScreen.frameColor : this.color;
        Gui.drawRect(x + 1, y + 1, x + width - 2, y + height - 2, color);
        // if (monitorScreen.scale > 1) {
        // width = (int) (monitorScreen.scale * width);
        // height = (int) (monitorScreen.scale * height);
        // Gui.drawRect(x, y, x + width - 1, y + 1, color);
        // Gui.drawRect(x, y + height - 1, x + width - 1, y + height, color);
        // Gui.drawRect(x, y, x + 1, y + height, color);
        // Gui.drawRect(x + width - 1, y, x + width, y + height, color);
        // }
        // if (this.isMouseOverElement(mouseX, mouseY)) {
        // Gui.drawRect(x, y, x + width, y + height, 0x4B6C6C6C);
        // }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            if (monitorScreen != null && monitorScreen.isActive()) {
                int x = this.getPosition().x;
                int y = this.getPosition().y;
                int width = this.getSize().width;
                int height = this.getSize().height;
                GlStateManager.pushMatrix();
                GlStateManager.translate(x + (width / 2F), y + (height / 2F), 100);
                GlStateManager.scale(width, height, 1);
                RenderUtil.renderRect(-0.5f, -0.5f, this.monitorScreen.scale, this.monitorScreen.scale, 0, 0XFF000000);
                monitorScreen.renderScreen(0, null);
                GlStateManager.popMatrix();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            ClickData clickData = new ClickData(Mouse.getEventButton(), Widget.isShiftDown(), Widget.isCtrlDown());
            this.writeClientAction(1, clickData::writeToBuf);
            Widget.playButtonClickSound();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            if (monitorScreen != null) {
                MetaTileEntityUIFactory.INSTANCE.openUI(monitorScreen.getHolder(),
                        (EntityPlayerMP) this.gui.entityPlayer);
            }
        }
    }
}
