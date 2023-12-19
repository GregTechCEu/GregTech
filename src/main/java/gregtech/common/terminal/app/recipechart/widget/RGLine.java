package gregtech.common.terminal.app.recipechart.widget;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class RGLine extends WidgetGroup {

    protected final RGNode parent;
    protected final RGNode child;
    protected final ItemStack catalyst;
    protected int ratio;
    private boolean isSelected;
    private final List<Vec2f> points;
    private final RGContainer container;
    private final WidgetGroup infoGroup;
    private final WidgetGroup toolGroup;

    public RGLine(RGNode parent, RGNode child, RGContainer container) {
        super(0, 0, 0, 0);
        this.parent = parent;
        this.child = child;
        this.container = container;
        this.points = new ArrayList<>();
        this.catalyst = parent.catalyst;

        infoGroup = new WidgetGroup(0, 0, 0, 0);
        if (catalyst != null) {
            ItemStackHandler handler = new ItemStackHandler();
            handler.setStackInSlot(0, catalyst);
            infoGroup.addWidget(
                    new SlotWidget(handler, 0, 0, 0, false, false).setBackgroundTexture(new ColorRectTexture(0)));
            MetaTileEntity mte = GTUtility.getMetaTileEntity(catalyst);
            if (mte instanceof SimpleMachineMetaTileEntity) {
                infoGroup.addWidget(new LabelWidget(9, -10,
                        I18n.format("terminal.recipe_chart.tier") +
                                GTValues.VN[((SimpleMachineMetaTileEntity) mte).getTier()],
                        -1).setXCentered(true).setShadow(true));
            }
        }

        infoGroup.setVisible(false);
        infoGroup.setActive(false);
        this.addWidget(infoGroup);

        toolGroup = new WidgetGroup(0, 0, 0, 0);
        toolGroup.addWidget(new CircleButtonWidget(-8, 0, 8, 1, 12)
                .setColors(0, TerminalTheme.COLOR_7.getColor(), 0)
                .setIcon(GuiTextures.ICON_VISIBLE)
                .setHoverText("terminal.recipe_chart.visible")
                .setClickListener(cd -> {
                    infoGroup.setActive(!infoGroup.isActive());
                    infoGroup.setVisible(!infoGroup.isVisible());
                }));
        toolGroup.addWidget(new CircleButtonWidget(8, 0, 8, 1, 12)
                .setColors(0, TerminalTheme.COLOR_7.getColor(), 0)
                .setIcon(GuiTextures.ICON_CALCULATOR)
                .setHoverText("terminal.recipe_chart.ratio")
                .setClickListener(cd -> TerminalDialogWidget
                        .showTextFieldDialog(container.os, "terminal.recipe_chart.ratio", s -> {
                            try {
                                return Integer.parseInt(s) > 0;
                            } catch (Exception ignored) {
                                return false;
                            }
                        }, s -> {
                            if (s != null) {
                                ratio = Integer.parseInt(s);
                                parent.updateDemand(parent.getHeadDemand());
                            }
                        }).setClientSide().open()));
        toolGroup.addWidget(new SimpleTextWidget(0, -18, "", -1, () -> Integer.toString(ratio), true).setShadow(true));
        toolGroup.setVisible(false);
        this.addWidget(toolGroup);
        this.ratio = 1;
        updateLine();
    }

    public static RGLine deserializeLineNBT(NBTTagCompound nbt, RGContainer container) {
        RGLine line = new RGLine(container.nodes.get(nbt.getInteger("parent")),
                container.nodes.get(nbt.getInteger("child")), container);
        line.ratio = nbt.getInteger("ratio");
        boolean visible = nbt.getBoolean("visible");
        line.infoGroup.setVisible(visible);
        line.infoGroup.setActive(visible);
        return line;
    }

    public NBTTagCompound serializeLineNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("parent", container.nodes.indexOf(parent));
        nbt.setInteger("child", container.nodes.indexOf(child));
        nbt.setInteger("ratio", ratio);
        nbt.setBoolean("visible", infoGroup.isVisible());
        return nbt;
    }

    public RGNode getParent() {
        return parent;
    }

    public RGNode getChild() {
        return child;
    }

    public List<Vec2f> getPoints() {
        return points;
    }

    public ItemStack getCatalyst() {
        return catalyst;
    }

    public void updateSelected(boolean isSelected) {
        this.isSelected = isSelected;
        toolGroup.setVisible(this.isSelected);
    }

    public void updateLine() {
        this.points.clear();
        Position pos1 = parent.getNodePosition(child);
        Position pos2 = child.getNodePosition(null);
        int x1, x2, y1, y2;
        if (Math.abs(pos1.x - pos2.x) > Math.abs(pos1.y - pos2.y)) {
            if (pos1.x > pos2.x) {
                x1 = pos1.x;
                y1 = pos1.y + 9;
                x2 = pos2.x + 18;
            } else {
                x1 = pos1.x + 18;
                y1 = pos1.y + 9;
                x2 = pos2.x;
            }
            y2 = pos2.y + 9;
            points.addAll(genBezierPoints(new Vec2f(x1, y1), new Vec2f(x2, y2), true, 0.01f));
        } else {
            if (pos1.y > pos2.y) {
                x1 = pos1.x + 9;
                y1 = pos1.y;
                y2 = pos2.y + 18;
            } else {
                x1 = pos1.x + 9;
                y1 = pos1.y + 18;
                y2 = pos2.y;
            }
            x2 = pos2.x + 9;
            points.addAll(genBezierPoints(new Vec2f(x1, y1), new Vec2f(x2, y2), false, 0.01f));
        }
        Position position = pos2.subtract(child.getSelfPosition());
        this.setSelfPosition(new Position(Math.min(x1, x2), Math.min(y1, y2)).subtract(position));
        int width = Math.abs(x1 - x2);
        int height = Math.abs(y1 - y2);
        this.setSize(new Size(width, height));
        this.setVisible(true);
        this.setActive(true);
        toolGroup.setSelfPosition(new Position((width) / 2, 0));
        infoGroup.setSelfPosition(new Position((width - 18) / 2, (height - 18) / 2));
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        if (points == null || points.size() == 0) return false;
        float x = points.get(0).x;
        float y = points.get(0).y;
        float x2 = points.get(points.size() - 1).x;
        float y2 = points.get(points.size() - 1).y;
        if (mouseX >= Math.min(x, x2) && mouseY >= Math.min(y, y2) && Math.max(x, x2) > mouseX &&
                Math.max(y, y2) > mouseY) {
            for (Vec2f point : points) {
                if ((mouseX - point.x) * (mouseX - point.x) + (mouseY - point.y) * (mouseY - point.y) < 4) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (isSelected) {
            drawSolidRect(getPosition().x, getPosition().y, getSize().width, getSize().height, 0x2fffffff);
            drawLines(points, 0x2fff0000, 0xffff0000, 2);
        } else {
            drawLines(points, 0x2fffff00, 0xff00ff00, 2);
        }
        Vec2f point = points.get(points.size() - 1);
        drawSolidRect((int) (point.x - 1.5), (int) (point.y - 1.5), 3, 3, 0XFF00FF00);
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        } else if (isMouseOver(mouseX, mouseY)) {
            if (!isSelected) {
                container.setSelectedLine(this);
            }
        } else if (isSelected) {
            container.setSelectedLine(null);
        }
        return false;
    }
}
