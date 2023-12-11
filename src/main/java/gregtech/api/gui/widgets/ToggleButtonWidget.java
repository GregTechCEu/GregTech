package gregtech.api.gui.widgets;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.SizedTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.function.BooleanConsumer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ToggleButtonWidget extends Widget {

    private BooleanSupplier predicate;
    private boolean isVisible = true;
    protected TextureArea buttonTexture;
    private final BooleanSupplier isPressedCondition;
    private final BooleanConsumer setPressedExecutor;
    private String tooltipText;
    private Object[] tooltipArgs;
    protected boolean isPressed;
    private boolean shouldUseBaseBackground;

    public ToggleButtonWidget(int xPosition, int yPosition, int width, int height, BooleanSupplier isPressedCondition,
                              BooleanConsumer setPressedExecutor) {
        this(xPosition, yPosition, width, height, GuiTextures.VANILLA_BUTTON, isPressedCondition, setPressedExecutor);
    }

    public ToggleButtonWidget(int xPosition, int yPosition, int width, int height, TextureArea buttonTexture,
                              BooleanSupplier isPressedCondition, BooleanConsumer setPressedExecutor) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        Preconditions.checkNotNull(buttonTexture, "texture");
        this.buttonTexture = buttonTexture;
        this.isPressedCondition = isPressedCondition;
        this.setPressedExecutor = setPressedExecutor;
    }

    public ToggleButtonWidget setButtonTexture(TextureArea texture) {
        Preconditions.checkNotNull(texture, "texture");
        this.buttonTexture = texture;
        return this;
    }

    public ToggleButtonWidget setTooltipText(String tooltipText, Object... args) {
        Preconditions.checkNotNull(tooltipText, "tooltipText");
        this.tooltipText = tooltipText;
        this.tooltipArgs = args;
        return this;
    }

    public ToggleButtonWidget setPredicate(BooleanSupplier predicate) {
        this.predicate = predicate;
        this.isVisible = false;
        return this;
    }

    public ToggleButtonWidget shouldUseBaseBackground() {
        this.shouldUseBaseBackground = true;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (!isVisible) return;
        Position pos = getPosition();
        Size size = getSize();
        if (shouldUseBaseBackground) {
            GuiTextures.TOGGLE_BUTTON_BACK.drawSubArea(pos.x, pos.y, size.width, size.height, 0.0,
                    isPressed ? 0.5 : 0.0, 1.0, 0.5);
            GlStateManager.color(1, 1, 1, 1);
            buttonTexture.draw(pos.x, pos.y, size.width, size.height);
        } else {
            if (buttonTexture instanceof SizedTextureArea) {
                ((SizedTextureArea) buttonTexture).drawHorizontalCutSubArea(pos.x, pos.y, size.width, size.height,
                        isPressed ? 0.5 : 0.0, 0.5);
            } else {
                buttonTexture.drawSubArea(pos.x, pos.y, size.width, size.height, 0.0, isPressed ? 0.5 : 0.0, 1.0, 0.5);
            }
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (!isVisible) return;
        if (isMouseOverElement(mouseX, mouseY) && tooltipText != null) {
            String postfix = isPressed ? ".enabled" : ".disabled";
            String tooltipHoverString = tooltipText + postfix;
            List<String> hoverList = Arrays.asList(LocalizationUtils.formatLines(tooltipHoverString, tooltipArgs));
            drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (isPressedCondition.getAsBoolean() != isPressed) {
            this.isPressed = isPressedCondition.getAsBoolean();
            writeUpdateInfo(1, buf -> buf.writeBoolean(isPressed));
        }
        if (predicate != null && predicate.getAsBoolean() != isVisible) {
            this.isVisible = predicate.getAsBoolean();
            writeUpdateInfo(2, buf -> buf.writeBoolean(isVisible));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 1) {
            this.isPressed = buffer.readBoolean();
        } else if (id == 2) {
            this.isVisible = buffer.readBoolean();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (isVisible && isMouseOverElement(mouseX, mouseY)) {
            this.isPressed = !this.isPressed;
            writeClientAction(1, buf -> buf.writeBoolean(isPressed));
            playButtonClickSound();
            return true;
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            this.isPressed = buffer.readBoolean();
            setPressedExecutor.apply(isPressed);
        }
    }
}
