package gregtech.api.terminal.app.recipechart.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RGContainer extends DraggableScrollableWidgetGroup {
    protected TerminalOSWidget os;
    private RGNode selectedNode;
    private RGLine selectedLine;
    private final List<RGNode> nodes;
    private final List<RGLine> lines;

    public RGContainer(int x, int y, int width, int height, TerminalOSWidget os) {
        super(x, y, width, height);
        this.os = os;
        this.setDraggable(true);
        this.setXScrollBarHeight(4);
        this.setYScrollBarWidth(4);
        this.setXBarStyle(null, TerminalTheme.COLOR_F_1);
        this.setYBarStyle(null, TerminalTheme.COLOR_F_1);
        nodes = new ArrayList<>();
        lines = new ArrayList<>();
    }

    public RGNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(RGNode selectedNode) {
        if (this.selectedNode != null) {
            this.selectedNode.updateSelected(false);
        }
        this.selectedNode = selectedNode;
        if (this.selectedNode != null) {
            this.selectedNode.updateSelected(true);
        }
    }

    public RGLine getSelectedLine() {
        return selectedLine;
    }

    public void setSelectedLine(RGLine selectedLine) {
        if (this.selectedLine != null) {
            this.selectedLine.updateSelected(false);
        }
        this.selectedLine = selectedLine;
        if (this.selectedLine != null) {
            this.selectedLine.updateSelected(true);
        }
    }

    public RGNode addNode(int x, int y) {
        RGNode node = new RGNode(x + getScrollXOffset(), y + getScrollYOffset(), this, null, true);
        nodes.add(node);
        this.addWidget(node);
        return node;
    }

    public RGNode addNode(int x, int y, Object object) {
        RGNode node = new RGNode(x + getScrollXOffset(), y + getScrollYOffset(), this, object, false);
        nodes.add(node);
        this.addWidget(node);
        return node;
    }

    public void removeNode(RGNode node) {
        nodes.remove(node);
        this.waitToRemoved(node);
    }

    public void updateLine(RGNode parent, RGNode child) {
        this.addLine(parent, child, null);
    }

    public void addLine(RGNode parent, RGNode child, ItemStack catalyst) {
        Optional<RGLine> optional = lines.stream().filter(line -> line.getParent() == parent && line.getChild() == child).findFirst();
        if (!optional.isPresent()) {
            RGLine line = new RGLine(parent, child, this, catalyst);
            lines.add(line);
            this.addWidget(0, line);
        } else {
            optional.get().updateLine();
        }
    }

    public RGLine getLine(RGNode parent, RGNode child) {
        Optional<RGLine> optional = lines.stream().filter(line -> line.getParent() == parent && line.getChild() == child).findFirst();
        return optional.orElse(null);
    }

    public void removeLine(RGNode parent, RGNode child) {
        lines.removeIf(line -> {
            if (line.getParent() == parent && line.getChild() == child) {
                RGContainer.this.waitToRemoved(line);
                return true;
            }
            return false;
        });
    }

    public void loadNBT(NBTTagCompound nbt) {
        this.nodes.clear();
        NBTTagList nodesList = nbt.getTagList("nodes", Constants.NBT.TAG_COMPOUND);
        for (NBTBase node : nodesList) { // build nodes
            NBTTagCompound nodeTag = (NBTTagCompound)node;
            NBTTagCompound headTag = nodeTag.getCompoundTag("head");
            byte type = headTag.getByte("type"); // 0-null 1-itemstack 2-fluidstack
            Object head = null;
            if (type == 1) {
                head = new ItemStack(headTag.getCompoundTag("nbt"));
            } else if (type == 2) {
                head = FluidStack.loadFluidStackFromNBT(headTag.getCompoundTag("nbt"));
            }
            if (nodeTag.getBoolean("phantom")) {
                this.nodes.add(new RGNode(nodeTag.getInteger("x"), nodeTag.getInteger("y"), this, head, true));
            } else {
                this.nodes.add(new RGNode(nodeTag.getInteger("x"), nodeTag.getInteger("y"), this, head, false));
            }
        }
    }

    public NBTTagCompound saveAsNBT() {

        return null;
    }

    @Override
    protected int getMaxHeight() {
        return super.getMaxHeight() + 20;
    }

    @Override
    protected int getMaxWidth() {
        return super.getMaxWidth() + 20;
    }

    @Override
    protected boolean hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (draggedWidget != null && draggedWidget == selectedNode) {
            for (RGNode node : nodes) {
                if (node != selectedNode && node.canMerge(selectedNode)) {
                    drawBorder(node.getPosition().x, node.getPosition().y, 18, 18, 0XFF0000FF, 2);
                    break;
                }
            }
        }
        return super.hookDrawInBackground(mouseX, mouseY, partialTicks, context);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        if (draggedWidget != null && draggedWidget == selectedNode) {
            for (RGNode node : nodes) {
                if (node != selectedNode && node.canMerge(selectedNode)) {
                    node.mergeNode(selectedNode);
                    break;
                }
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (widget.isVisible() && widget.isActive() && widget.mouseWheelMove(mouseX, mouseY, wheelDelta)) {
                return true;
            }
        }
        return false;
    }

}
