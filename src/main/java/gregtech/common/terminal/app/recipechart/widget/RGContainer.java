package gregtech.common.terminal.app.recipechart.widget;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class RGContainer extends DraggableScrollableWidgetGroup {

    protected TerminalOSWidget os;
    private RGNode selectedNode;
    private RGLine selectedLine;
    protected final List<RGNode> nodes;
    protected final List<RGLine> lines;

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

    public void addOrUpdateLine(RGNode parent, RGNode child) {
        Optional<RGLine> optional = lines.stream()
                .filter(line -> line.getParent() == parent && line.getChild() == child).findFirst();
        if (!optional.isPresent()) {
            RGLine line = new RGLine(parent, child, this);
            lines.add(line);
            this.addWidget(0, line);
        } else {
            optional.get().updateLine();
        }
    }

    public RGLine getLine(RGNode parent, RGNode child) {
        Optional<RGLine> optional = lines.stream()
                .filter(line -> line.getParent() == parent && line.getChild() == child).findFirst();
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

    public void loadFromNBT(NBTTagCompound nbt) {
        try {
            this.clearAllWidgets();
            this.nodes.clear();
            this.lines.clear();
            NBTTagList nodesList = nbt.getTagList("nodes", Constants.NBT.TAG_COMPOUND);
            for (NBTBase node : nodesList) { // build nodes
                nodes.add(RGNode.deserializeNodeNBT((NBTTagCompound) node, this));
            }
            Iterator<NBTBase> iterator = nodesList.iterator(); // build relations
            for (RGNode node : nodes) {
                NBTTagCompound nodeTag = (NBTTagCompound) iterator.next();
                node.deserializeRelationNBT(nodeTag.getTagList("parents", Constants.NBT.TAG_INT_ARRAY),
                        nodeTag.getTagList("children", Constants.NBT.TAG_INT_ARRAY));
                this.addWidget(node);
            }
            for (RGLine line : lines) {
                removeWidget(line);
            }
            lines.clear();
            NBTTagList linesList = nbt.getTagList("lines", Constants.NBT.TAG_COMPOUND);
            for (NBTBase node : linesList) { // build nodes
                RGLine line = RGLine.deserializeLineNBT((NBTTagCompound) node, this);
                lines.add(line);
                this.addWidget(0, line);
            }
        } catch (Exception e) {
            TerminalDialogWidget.showInfoDialog(os, "ERROR", e.getMessage()).setClientSide().open();
        }
    }

    public NBTTagCompound saveAsNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList nodesTag = new NBTTagList();
        for (RGNode node : nodes) {
            nodesTag.appendTag(node.serializeNodeNBT());
        }
        nbt.setTag("nodes", nodesTag);
        NBTTagList linesTag = new NBTTagList();
        for (RGLine line : lines) {
            linesTag.appendTag(line.serializeLineNBT());
        }
        nbt.setTag("lines", linesTag);
        return nbt;
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
