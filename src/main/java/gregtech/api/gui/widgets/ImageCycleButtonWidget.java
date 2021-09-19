package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.SizedTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.function.BooleanConsumer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;
import java.util.function.*;

public class ImageCycleButtonWidget extends Widget {

    protected TextureArea buttonTexture;
    private int textColor = 0xFFFFFF;
    private final IntSupplier currentOptionSupplier;
    private final IntConsumer setOptionExecutor;
    private final int optionCount;
    private static final int RIGHT_MOUSE = 1;
    protected int currentOption;
    protected String tooltipHoverString;
    protected long hoverStartTime = -1L;
    protected boolean isMouseHovered;

    public ImageCycleButtonWidget(int xPosition, int yPosition, int width, int height, TextureArea buttonTexture, int optionCount, IntSupplier currentOptionSupplier, IntConsumer setOptionExecutor) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.buttonTexture = buttonTexture;
        this.currentOptionSupplier = currentOptionSupplier;
        this.setOptionExecutor = setOptionExecutor;
        this.optionCount = optionCount;
    }


    public ImageCycleButtonWidget(int xPosition, int yPosition, int width, int height, TextureArea buttonTexture, BooleanSupplier supplier, BooleanConsumer updater) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.buttonTexture = buttonTexture;
        this.currentOptionSupplier = () -> supplier.getAsBoolean() ? 1 : 0;
        this.setOptionExecutor = (value) -> updater.apply(value >= 1);
        this.optionCount = 2;
    }

    public ImageCycleButtonWidget setTooltipHoverString(String hoverString) {
        this.tooltipHoverString = hoverString;
        return this;
    }

    public ImageCycleButtonWidget setButtonTexture(TextureArea texture) {
        this.buttonTexture = texture;
        return this;
    }

    public ImageCycleButtonWidget setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (buttonTexture instanceof SizedTextureArea) {
            ((SizedTextureArea) buttonTexture).drawHorizontalCutSubArea(pos.x, pos.y, size.width, size.height, (float)currentOption / optionCount, (float)1 / optionCount);
        } else {
            buttonTexture.drawSubArea(pos.x, pos.y, size.width, size.height, 0.0, (float)currentOption / optionCount, 1.0, (float)1 / optionCount);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        boolean isHovered = isMouseOverElement(mouseX, mouseY);
        boolean wasHovered = isMouseHovered;
        if (isHovered && !wasHovered) {
            this.isMouseHovered = true;
            this.hoverStartTime = System.currentTimeMillis();
        } else if (!isHovered && wasHovered) {
            this.isMouseHovered = false;
            this.hoverStartTime = 0L;
        } else if (isHovered) {
            long timeSinceHover = System.currentTimeMillis() - hoverStartTime;
            if (timeSinceHover > 1000L && tooltipHoverString != null) {
                List<String> hoverList = Arrays.asList(I18n.format(tooltipHoverString).split("/n"));
                drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (currentOptionSupplier.getAsInt() != currentOption) {
            this.currentOption = currentOptionSupplier.getAsInt();
            writeUpdateInfo(1, buf -> buf.writeVarInt(currentOption));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 1) {
            this.currentOption = buffer.readVarInt();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (isMouseOverElement(mouseX, mouseY)) {
            //Allow only the RMB to reverse cycle
            if(button == RIGHT_MOUSE) {
                //Wrap from the first option to the last if needed
                this.currentOption = currentOption == 0 ? optionCount - 1 : currentOption - 1;
            } else {
                this.currentOption = (currentOption + 1) % optionCount;
            }
            writeClientAction(1, buf -> buf.writeVarInt(currentOption));
            playButtonClickSound();
            return true;
        }
        return false;
    }


    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            this.currentOption = MathHelper.clamp(buffer.readVarInt(), 0, optionCount);
            setOptionExecutor.accept(currentOption);
        }
    }

}
