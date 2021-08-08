package gregtech.api.gui;

import com.google.common.base.Preconditions;
import gregtech.api.gui.widgets.WidgetUIAccess;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Widget is functional element of ModularUI
 * It can draw, perform actions, react to key press and mouse
 * It's information is also synced to client
 */
public abstract class Widget {

    protected ModularUI gui;
    protected ISizeProvider sizes;
    protected WidgetUIAccess uiAccess;
    private Position parentPosition = Position.ORIGIN;
    private Position selfPosition;
    private Position position;
    private Size size;

    public Widget(Position selfPosition, Size size) {
        Preconditions.checkNotNull(selfPosition, "selfPosition");
        Preconditions.checkNotNull(size, "size");
        this.selfPosition = selfPosition;
        this.size = size;
        this.position = this.parentPosition.add(selfPosition);
    }

    public void setGui(ModularUI gui) {
        this.gui = gui;
    }

    public void setSizes(ISizeProvider sizes) {
        this.sizes = sizes;
    }

    public void setUiAccess(WidgetUIAccess uiAccess) {
        this.uiAccess = uiAccess;
    }

    public void setParentPosition(Position parentPosition) {
        Preconditions.checkNotNull(parentPosition, "parentPosition");
        this.parentPosition = parentPosition;
        recomputePosition();
    }

    protected void setSelfPosition(Position selfPosition) {
        Preconditions.checkNotNull(selfPosition, "selfPosition");
        this.selfPosition = selfPosition;
        recomputePosition();
    }

    protected void setSize(Size size) {
        Preconditions.checkNotNull(size, "size");
        this.size = size;
        onSizeUpdate();
    }

    public final Position getPosition() {
        return position;
    }

    public final Size getSize() {
        return size;
    }

    public Rectangle toRectangleBox() {
        Position pos = getPosition();
        Size size = getSize();
        return new Rectangle(pos.x, pos.y, size.width, size.height);
    }

    private void recomputePosition() {
        this.position = this.parentPosition.add(selfPosition);
        onPositionUpdate();
    }

    public void applyScissor(final int parentX, final int parentY, final int parentWidth, final int parentHeight) {
    }

    protected void onPositionUpdate() {
    }

    protected void onSizeUpdate() {
    }

    public boolean isMouseOverElement(int mouseX, int mouseY, boolean correctPositionOnMouseWheelMoveEvent) {
        mouseX = correctPositionOnMouseWheelMoveEvent ? mouseX + getPosition().x : mouseX;
        return isMouseOverElement(mouseX, mouseY);
    }

    public boolean isMouseOverElement(int mouseX, int mouseY) {
        Position position = getPosition();
        Size size = getSize();
        return isMouseOver(position.x, position.y, size.width, size.height, mouseX, mouseY);
    }

    public static boolean isMouseOver(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && x + width > mouseX && y + height > mouseY;
    }

    /**
     * Called on both sides to initialize widget data
     */
    public void initWidget() {
    }

    /**
     * Called on serverside to detect changes and synchronize them with clients
     */
    public void detectAndSendChanges() {
    }

    /**
     * Called clientside every tick with this modular UI open
     */
    public void updateScreen() {
    }

    /**
     * Called each draw tick to draw this widget in GUI
     */
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
    }

    /**
     * Called each draw tick to draw this widget in GUI
     */
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
    }

    /**
     * Called when mouse wheel is moved in GUI
     * For some -redacted- reason mouseX position is relative against GUI not game window as in other mouse events
     */
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        return false;
    }

    /**
     * Called when mouse is clicked in GUI
     */
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return false;
    }

    /**
     * Called when mouse is pressed and hold down in GUI
     */
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        return false;
    }

    /**
     * Called when mouse is released in GUI
     */
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        return false;
    }

    /**
     * Called when key is typed in GUI
     */
    @SideOnly(Side.CLIENT)
    public boolean keyTyped(char charTyped, int keyCode) {
        return false;
    }

    /**
     * Read data received from server's {@link #writeUpdateInfo}
     */
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
    }

    public void handleClientAction(int id, PacketBuffer buffer) {
    }

    public List<INativeWidget> getNativeWidgets() {
        if (this instanceof INativeWidget) {
            return Collections.singletonList((INativeWidget) this);
        }
        return Collections.emptyList();
    }

    /**
     * Writes data to be sent to client's {@link #readUpdateInfo}
     */
    protected final void writeUpdateInfo(int id, Consumer<PacketBuffer> packetBufferWriter) {
        if (uiAccess != null && gui != null) {
            uiAccess.writeUpdateInfo(this, id, packetBufferWriter);
        }
    }

    @SideOnly(Side.CLIENT)
    protected final void writeClientAction(int id, Consumer<PacketBuffer> packetBufferWriter) {
        if (uiAccess != null) {
            uiAccess.writeClientAction(this, id, packetBufferWriter);
        }
    }

    @SideOnly(Side.CLIENT)
    protected void drawHoveringText(ItemStack itemStack, List<String> tooltip, int maxTextWidth, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiUtils.drawHoveringText(itemStack, tooltip, mouseX, mouseY,
                sizes.getScreenWidth(),
                sizes.getScreenHeight(), maxTextWidth, mc.fontRenderer);
    }

    @SideOnly(Side.CLIENT)
    protected void drawStringSized(String text, double x, double y, int color, boolean dropShadow, float scale, boolean center) {
        GlStateManager.pushMatrix();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        double scaledTextWidth = center ? fontRenderer.getStringWidth(text) * scale : 0.0;
        GlStateManager.translate(x + scaledTextWidth / 2.0, y, 0.0f);
        GlStateManager.scale(scale, scale, scale);
        fontRenderer.drawString(text, 0, 0, color, dropShadow);
        GlStateManager.popMatrix();
    }

    @SideOnly(Side.CLIENT)
    protected void drawStringFixedCorner(String text, double x, double y, int color, boolean dropShadow, float scale) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        double scaledWidth = fontRenderer.getStringWidth(text) * scale;
        double scaledHeight = fontRenderer.FONT_HEIGHT * scale;
        drawStringSized(text, x - scaledWidth, y - scaledHeight, color, dropShadow, scale, false);
    }

    @SideOnly(Side.CLIENT)
    protected static void drawItemStack(ItemStack itemStack, int x, int y, @Nullable String altTxt) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 32.0F);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        RenderHelper.enableGUIStandardItemLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem itemRender = mc.getRenderItem();
        itemRender.renderItemAndEffectIntoGUI(itemStack, x, y);
        itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, x, y, altTxt);
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
    }

    @SideOnly(Side.CLIENT)
    protected static List<String> getItemToolTip(ItemStack itemStack) {
        Minecraft mc = Minecraft.getMinecraft();
        ITooltipFlag flag = mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
        List<String> tooltip = itemStack.getTooltip(mc.player, flag);
        for (int i = 0; i < tooltip.size(); ++i) {
            if (i == 0) {
                tooltip.set(i, itemStack.getItem().getForgeRarity(itemStack).getColor() + tooltip.get(i));
            } else {
                tooltip.set(i, TextFormatting.GRAY + tooltip.get(i));
            }
        }
        return tooltip;
    }

    @SideOnly(Side.CLIENT)
    protected static void drawSelectionOverlay(int x, int y, int width, int height) {
        GlStateManager.disableDepth();
        GlStateManager.colorMask(true, true, true, false);
        drawGradientRect(x, y, width, height, -2130706433, -2130706433);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
    }

    @SideOnly(Side.CLIENT)
    protected static void drawSolidRect(int x, int y, int width, int height, int color) {
        Gui.drawRect(x, y, x + width, y + height, color);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableBlend();
    }

    @SideOnly(Side.CLIENT)
    protected static void drawGradientRect(int x, int y, int width, int height, int startColor, int endColor) {
        GuiUtils.drawGradientRect(0, x, y, x + width, y + height, startColor, endColor);
        GlStateManager.enableBlend();
    }

    @SideOnly(Side.CLIENT)
    protected void playButtonClickSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @SideOnly(Side.CLIENT)
    protected boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    @SideOnly(Side.CLIENT)
    protected boolean isCtrlDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }

    protected static boolean isClientSide() {
        return FMLCommonHandler.instance().getSide().isClient();
    }

    public static final class ClickData {
        public final int button;
        public final boolean isShiftClick;
        public final boolean isCtrlClick;

        public ClickData(int button, boolean isShiftClick, boolean isCtrlClick) {
            this.button = button;
            this.isShiftClick = isShiftClick;
            this.isCtrlClick = isCtrlClick;
        }

        public void writeToBuf(PacketBuffer buf) {
            buf.writeVarInt(button);
            buf.writeBoolean(isShiftClick);
            buf.writeBoolean(isCtrlClick);
        }

        public static ClickData readFromBuf(PacketBuffer buf) {
            int button = buf.readVarInt();
            boolean shiftClick = buf.readBoolean();
            boolean ctrlClick = buf.readBoolean();
            return new ClickData(button, shiftClick, ctrlClick);
        }
    }

    public static class ClientSideField<T> {
        @SideOnly(Side.CLIENT)
        private T fieldValue;

        public ClientSideField(Supplier<T> initializer) {
            if (isClientSide()) {
                this.fieldValue = initializer.get();
            }
        }

        public void useOnClient(Consumer<T> callback) {
            if (isClientSide()) {
                callback.accept(fieldValue);
            }
        }

        @SideOnly(Side.CLIENT)
        public T get() {
            return fieldValue;
        }
    }
}
