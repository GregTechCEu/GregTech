package gregtech.common.terminal.app.prospector.widget;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.Position;
import gregtech.common.terminal.app.prospector.ProspectorMode;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.ItemStackHandler;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.*;
import java.util.function.Consumer;

public class WidgetOreList extends DraggableScrollableWidgetGroup {

    protected WidgetGroup selected;
    protected final BiMap<WidgetGroup, String> widgetMap;
    protected Consumer<String> onSelected = null;
    public Map<String, String> ores;
    private int tickCounter;

    public WidgetOreList(int xPosition, int yPosition, int width, int slotSize) {
        super(xPosition, yPosition, width, slotSize);
        widgetMap = HashBiMap.create();
        ores = new HashMap<>();
        this.setYScrollBarWidth(5);
        this.setYBarStyle(null, TerminalTheme.COLOR_F_1);
        clear();
    }

    public void setSelected(String oreName) {
        WidgetGroup widget = widgetMap.inverse().get(oreName);
        if (widget != null) {
            this.selected = widget;
            if (this.onSelected != null) {
                onSelected.accept(widgetMap.get(this.selected));
            }
        }
    }

    public void addOres(Set<String> ores, ProspectorMode mode) {
        switch (mode) {
            case ORE:
                ores.stream().sorted().forEach(this::addOre);
                break;
            case FLUID:
                ores.stream().sorted().forEach(this::addOil);
                break;
            default:
                break;
        }
    }

    private void addOre(String orePrefix) {
        if (ores.containsKey(orePrefix)) {
            return;
        }
        ItemStack itemStack = OreDictUnifier.get(orePrefix);
        if (itemStack == null || itemStack.isEmpty()) return;
        ores.put(orePrefix, itemStack.getDisplayName());
        MaterialStack materialStack = OreDictUnifier.getMaterial(OreDictUnifier.get(orePrefix));
        ItemStackHandler itemStackHandler = new ItemStackHandler(1);
        itemStackHandler.insertItem(0, itemStack, false);
        WidgetGroup widgetGroup = new WidgetGroup(0, 0, getSize().width - 5, 18);
        widgetGroup.addWidget(new SlotWidget(itemStackHandler, 0, 0, 0, false, false));
        widgetGroup.addWidget(new LabelWidget(20, 5, itemStack.getDisplayName(),
                materialStack == null ? orePrefix.hashCode() : materialStack.material.getMaterialRGB() | 0XFF000000));
        addOrePrefix(orePrefix, widgetGroup);
    }

    private void addOrePrefix(String orePrefix, WidgetGroup widgetGroup) {
        widgetMap.put(widgetGroup, orePrefix);
        this.addWidget(widgetGroup);
        this.widgets.sort(Comparator.comparing(widgetMap::get));
        int y = 0;
        for (Widget widget : this.widgets) {
            widget.setSelfPosition(new Position(0, y - scrollYOffset));
            y += 18;
        }
        computeMax();
    }

    private void addOil(String orePrefix) {
        if (ores.containsKey(orePrefix)) {
            return;
        }
        FluidStack fluidStack = FluidRegistry.getFluidStack(orePrefix, 1);
        if (fluidStack == null) return;
        ores.put(orePrefix, fluidStack.getLocalizedName());
        FluidTank fluidTank = new FluidTank(1);
        fluidTank.setCanFill(false);
        fluidTank.fillInternal(fluidStack, true);
        WidgetGroup widgetGroup = new WidgetGroup(0, 0, getSize().width - 5, 18);
        widgetGroup.addWidget(new TankWidget(fluidTank, 0, 0, 18, 18)
                .setAlwaysShowFull(true)
                .setClient()
                .setHideTooltip(true)
                .setContainerClicking(false, false));
        widgetGroup
                .addWidget(new LabelWidget(20, 5, fluidStack.getLocalizedName(), getFluidColor(fluidStack.getFluid())));
        addOrePrefix(orePrefix, widgetGroup);
    }

    public void clear() {
        this.clearAllWidgets();
        widgetMap.clear();
        WidgetGroup widgetGroup = new WidgetGroup(0, 0, getSize().width - 5, 18);
        widgetGroup.addWidget(new ImageWidget(0, 0, 18, 18, GuiTextures.LOCK));
        widgetGroup.addWidget(new LabelWidget(20, 9, "terminal.prospector.list", -1));
        selected = widgetGroup;
        widgetMap.put(widgetGroup, "[all]");
        this.addWidget(widgetGroup);
    }

    @Override
    protected boolean hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (selected != null) {
            drawSolidRect(selected.getPosition().x, selected.getPosition().y, selected.getSize().width, 18, 0x4BFFFFFF);
        }
        for (Widget widget : widgets) {
            if (widget.isVisible()) {
                widget.drawInBackground(mouseX, mouseY, partialTicks, context);

                GlStateManager.color(gui.getRColorForOverlay(), gui.getGColorForOverlay(), gui.getBColorForOverlay(),
                        1.0F);
            }
        }
        return true;
    }

    @Override
    protected boolean checkClickedDragged(int mouseX, int mouseY, int button) {
        draggedWidget = null;
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget widget = widgets.get(i);
            if (widget.isVisible() && widget instanceof WidgetGroup) {
                if (widget.isMouseOverElement(mouseX, mouseY)) {
                    if (isMouseOverElement(mouseX, mouseY) && this.selected != widget) {
                        this.setSelected(widgetMap.get(widget));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        tickCounter++;
        if (tickCounter % 20 == 0) {
            widgets.forEach(widget -> {
                if (widget instanceof WidgetGroup) {
                    Widget widget1 = ((WidgetGroup) widget).getContainedWidgets(true).get(0);
                    if (widget1 instanceof SlotWidget) {
                        SlotWidget slotWidget = (SlotWidget) widget1;
                        List<ItemStack> list = OreDictUnifier.getAllWithOreDictionaryName(widgetMap.get(widget));
                        if (list.size() > 0) {
                            slotWidget.getHandle().decrStackSize(64);
                            slotWidget.getHandle().putStack(list.get(Math.floorMod(tickCounter / 20, list.size())));
                        }
                    }
                }

            });
        }
    }

    public static int getFluidColor(Fluid fluid) {
        if (fluid == FluidRegistry.WATER) {
            return 3183823;
        } else {
            return fluid == FluidRegistry.LAVA ? 16766720 : fluid.getColor();
        }
    }

    @Override
    protected void writeClientAction(int id, Consumer<PacketBuffer> packetBufferWriter) {}
}
