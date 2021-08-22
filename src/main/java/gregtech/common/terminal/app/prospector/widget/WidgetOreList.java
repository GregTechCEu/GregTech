package gregtech.common.terminal.app.prospector.widget;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.terminal.gui.IDraggable;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.Position;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import java.util.*;
import java.util.function.Consumer;

import static gregtech.api.gui.impl.ModularUIGui.bColorForOverlay;
import static gregtech.api.gui.impl.ModularUIGui.gColorForOverlay;
import static gregtech.api.gui.impl.ModularUIGui.rColorForOverlay;

public class WidgetOreList extends DraggableScrollableWidgetGroup {
    private WidgetGroup selected;
    private final Map<WidgetGroup, String> widgetMap;
    public Consumer<String> onSelected = null;
    public Set<String> ores;
    @SideOnly(Side.CLIENT)
    private int tickCounter;

    public WidgetOreList(int xPosition, int yPosition, int width, int slotSize) {
        super(xPosition, yPosition, width, slotSize);
        widgetMap = new HashMap<>();
        ores = new HashSet<>();
        this.setYScrollBarWidth(5);
        this.setYBarStyle(null, TerminalTheme.COLOR_F_1);
        clear();
    }

    public void addOres(Set<String> ores, int mode) {
        switch (mode) {
            case 0:
                ores.stream().sorted().forEach(this::addOre);
                break;
            case 1:
                ores.stream().sorted().forEach(this::addOil);
                break;
            default:
                break;
        }
    }

    private void addOre(String orePrefix) {
        if (ores.contains(orePrefix)) {
            return;
        } else {
            ores.add(orePrefix);
        }
        ItemStack itemStack = OreDictUnifier.get(orePrefix);
        MaterialStack materialStack = OreDictUnifier.getMaterial(OreDictUnifier.get(orePrefix));
        ItemStackHandler itemStackHandler = new ItemStackHandler(1);
        itemStackHandler.insertItem(0, itemStack, false);
        WidgetGroup widgetGroup = new WidgetGroup(0, 0, getSize().width - 5, 18);
        widgetGroup.addWidget(new SlotWidget(itemStackHandler, 0, 0, 0, false, false));
        widgetGroup.addWidget(new LabelWidget(20, 5, itemStack.getDisplayName(), materialStack==null? orePrefix.hashCode():materialStack.material.getMaterialRGB() | 0XFF000000));
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
        if (ores.contains(orePrefix)) {
            return;
        } else {
            ores.add(orePrefix);
        }
        FluidStack fluidStack = FluidRegistry.getFluidStack(orePrefix, 1);
        if (fluidStack == null) return;
        FluidTank fluidTank = new FluidTank(1);
        fluidTank.setCanFill(false);
        fluidTank.fillInternal(fluidStack, true);
        WidgetGroup widgetGroup = new WidgetGroup(0, 0, getSize().width - 5, 18);
        widgetGroup.addWidget(new TankWidget(fluidTank, 0, 0, 18, 18)
                .setAlwaysShowFull(true)
                .setHideTooltip(true)
                .setContainerClicking(false, false));
        widgetGroup.addWidget(new LabelWidget(20, 5, fluidStack.getLocalizedName(), getFluidColor(fluidStack.getFluid())));
        addOrePrefix(orePrefix, widgetGroup);
    }

    public void clear() {
        this.clearAllWidgets();
        widgetMap.clear();
        WidgetGroup widgetGroup = new WidgetGroup();
        widgetGroup.addWidget(new ImageWidget(0, 0, 18, 18, GuiTextures.LOCK));
        widgetGroup.addWidget(new LabelWidget(20, 9, "metaitem.tool.prospect.gui.list"));
        selected = widgetGroup;
        widgetMap.put(widgetGroup, "[all]");
        this.addWidget(widgetGroup);
    }

    @Override
    protected boolean hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        for (Widget widget : widgets) {
            if (widget.isVisible()) {
                widget.drawInBackground(mouseX, mouseY, partialTicks, context);
            }
        }
        GlStateManager.color(rColorForOverlay, gColorForOverlay, bColorForOverlay, 1.0F);
        if (selected != null) {
            drawSolidRect(selected.getPosition().x, selected.getPosition().y, selected.getSize().width, 18, 0x4BFFFFFF);
        }
        return true;
    }

    @Override
    protected boolean checkClickedDragged(int mouseX, int mouseY, int button) {
        draggedWidget = null;
        if (isMouseOverElement(mouseX, mouseY)) {
            for (int i = widgets.size() - 1; i >= 0; i--) {
                Widget widget = widgets.get(i);
                if(widget.isVisible() && widget instanceof WidgetGroup) {
                    if(widget.isMouseOverElement(mouseX, mouseY) && this.selected != widget) {
                        this.selected = (WidgetGroup) widget;
                        if (this.onSelected != null) {
                            onSelected.accept(widgetMap.get(this.selected));
                        }
                        return true;
                    }
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
                    if (widget1 instanceof SlotWidget){
                        SlotWidget slotWidget = (SlotWidget) widget1;
                        List<ItemStack> list = OreDictUnifier.getAllWithOreDictionaryName(widgetMap.get(widget));
                        if (list.size() > 0 ) {
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

}
