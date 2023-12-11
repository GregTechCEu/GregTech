package gregtech.common.gui.widget.monitor;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ScrollableListWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.pipenet.tile.PipeCoverableImplementation;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.renderer.handler.BlockPosHighlightRenderer;
import gregtech.client.utils.RenderUtil;
import gregtech.common.covers.CoverDigitalInterface;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class WidgetCoverList extends ScrollableListWidget {

    private static final Pattern PARENTHESIS_PATTERN = Pattern.compile("[,() ]");

    private WidgetGroup selected;
    private final Map<WidgetGroup, CoverDigitalInterface> widgetMap;
    public Consumer<CoverDigitalInterface> onSelected;

    public WidgetCoverList(int xPosition, int yPosition, int width, int slotSize, List<CoverDigitalInterface> covers,
                           CoverDigitalInterface bindCover, Consumer<CoverDigitalInterface> onSelected) {
        super(xPosition, yPosition, width, slotSize * 18);
        widgetMap = new HashMap<>();
        this.onSelected = onSelected;
        for (CoverDigitalInterface cover : covers) {
            ItemStack itemStack = cover.getPickItem();
            BlockPos pos = cover.getPos();
            if (cover.getCoverableView() instanceof PipeCoverableImplementation) {
                itemStack = null;
                pos = pos.offset(cover.getAttachedSide());
                TileEntity tileEntity = cover.getWorld().getTileEntity(pos);
                IBlockState state = cover.getWorld().getBlockState(pos);
                if (tileEntity != null) {
                    itemStack = tileEntity.getBlockType().getItem(cover.getWorld(), pos, state);
                }
                if (itemStack == null) continue;
            }
            ItemStackHandler itemStackHandler = new ItemStackHandler(1);
            itemStackHandler.insertItem(0, itemStack, false);
            WidgetGroup widgetGroup = new WidgetGroup();
            widgetGroup.addWidget(new SlotWidget(itemStackHandler, 0, 0, 0, false, false));
            widgetGroup.addWidget(new LabelWidget(20, 5,
                    String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ()), 0XFFFFFFFF));
            widgetMap.put(widgetGroup, cover);
            if (widgetGroup.getSize().width + scrollPaneWidth > this.getSize().width)
                this.setSize(new Size(widgetGroup.getSize().width + scrollPaneWidth, this.getSize().height));
            this.addWidget(widgetGroup);
            if (cover == bindCover) {
                selected = widgetGroup;
            }
        }
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (this.isMouseOverElement(mouseX - this.getPosition().x + this.gui.getGuiLeft(), mouseY)) {
            int direction = -MathHelper.clamp(wheelDelta, -2, 2);
            int moveDelta = direction * (this.slotHeight / 2);
            addScrollOffset(moveDelta);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        if (!result && mouseY >= this.getPosition().y && mouseY <= this.getPosition().y + this.getSize().height) {
            Widget widget = this.widgets.stream().filter(it -> it.isMouseOverElement(mouseX, mouseY)).findFirst()
                    .orElse(null);
            if (widget instanceof WidgetGroup) {
                List<Widget> children = ((WidgetGroup) widget).getContainedWidgets(true);
                if (children.get(0).isMouseOverElement(mouseX, mouseY)) {
                    try {
                        String posString = ObfuscationReflectionHelper.getPrivateValue(LabelWidget.class,
                                (LabelWidget) children.get(1), "text");
                        String[] posSplit = PARENTHESIS_PATTERN.split(posString);
                        BlockPosHighlightRenderer.renderBlockBoxHighLight(
                                new BlockPos(Integer.parseInt(posSplit[1]), Integer.parseInt(posSplit[3]),
                                        Integer.parseInt(posSplit[5])),
                                5000);
                        Minecraft.getMinecraft().player.closeScreen();
                        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(
                                ((SlotWidget) children.get(0)).getHandle().getStack().getDisplayName() +
                                        ": " + posString));
                        return false;
                    } catch (Throwable e) {
                        GTLog.logger.error("Could not reflect GregTech WidgetLabel text", e);
                    }
                }
                if (widget == this.selected) {
                    this.selected = null;
                } else {
                    this.selected = (WidgetGroup) widget;
                }
                writeClientAction(2, buf -> {
                    if (this.selected == null) {
                        buf.writeInt(-1);
                    } else {
                        buf.writeInt(this.getContainedWidgets(true).indexOf(this.selected));
                    }
                });
            }
        }
        return result;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            int index = buffer.readInt();
            if (index == -1) {
                this.selected = null;
            } else {
                this.selected = (WidgetGroup) this.getContainedWidgets(true).get(index);
            }
            if (onSelected != null) {
                onSelected.accept(widgetMap.getOrDefault(this.selected, null));
            }
        }
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        int mouseDelta = (mouseY - lastMouseY) * 5;
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (draggedOnScrollBar) {
            addScrollOffset(mouseDelta);
            return true;
        }
        if (isPositionInsideScissor(mouseX, mouseY)) {
            return super.mouseDragged(mouseX, mouseY, button, timeDragged);
        }
        return false;
    }

    private void addScrollOffset(int offset) {
        this.scrollOffset = MathHelper.clamp(scrollOffset + offset, 0, totalListHeight - getSize().height);
        onPositionUpdate();
    }

    private boolean isPositionInsideScissor(int mouseX, int mouseY) {
        return isMouseOverElement(mouseX, mouseY) && !isOnScrollPane(mouseX, mouseY);
    }

    private boolean isOnScrollPane(int mouseX, int mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        return isMouseOver(pos.x + size.width - scrollPaneWidth, pos.y, scrollPaneWidth, size.height, mouseX, mouseY);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        RenderUtil.useScissor(this.getPosition().x, this.getPosition().y, this.getSize().width, this.getSize().height,
                () -> {
                    widgets.forEach(widget -> {
                        if (widget instanceof WidgetGroup) {
                            Widget widget1 = ((WidgetGroup) widget).getContainedWidgets(true).get(0);
                            SlotWidget slotWidget = (SlotWidget) widget1;
                            slotWidget.setActive(widget.getPosition().y >= this.getPosition().y - 9 &&
                                    widget.getPosition().y <= this.getPosition().y + this.getSize().height - 9);
                        }
                    });
                    if (selected != null) {
                        Gui.drawRect(selected.getPosition().x, selected.getPosition().y,
                                selected.getPosition().x + this.getSize().width - this.scrollPaneWidth,
                                selected.getPosition().y + selected.getSize().height, 0x4BFFFFFF);
                    }
                    super.drawInBackground(mouseX, mouseY, partialTicks, context);
                });
    }
}
