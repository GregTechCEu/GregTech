package gregtech.common.terminal.app.recipechart.widget;

import gregtech.api.gui.Widget;
import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.gui.widgets.PhantomFluidWidget;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.common.inventory.handlers.SingleItemStackHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PhantomWidget extends WidgetGroup implements IGhostIngredientTarget {
    private final IItemHandlerModifiable itemHandler;
    private FluidStack fluidStack;
    private PhantomFluidWidget fluidWidget;
    private PhantomSlotWidget slotWidget;
    private Consumer<Object> onChanged;

    public PhantomWidget(int x, int y, Object defaultObj) {
        super(x, y, 18, 18);
        itemHandler = new SingleItemStackHandler(1);
        fluidStack = null;
        fluidWidget = new PhantomFluidWidget(0, 0, 18, 18, null, null)
        .setFluidStackUpdater(fluid -> {
            fluidStack = fluid.copy();
            if (fluidStack != null && fluidStack.amount > 0) {
                itemHandler.setStackInSlot(0, ItemStack.EMPTY);
                slotWidget.setVisible(false);
                fluidWidget.setVisible(true);
                if (onChanged != null) {
                    onChanged.accept(fluidStack);
                }
            }
        }, true).setBackgroundTexture(TerminalTheme.COLOR_B_2).showTip(true)
        .setFluidStackSupplier(() -> fluidStack,true);
        slotWidget = new PhantomSlotWidget(itemHandler, 0, 0, 0) {
            @Override
            public boolean isEnabled() {
                return isActive();
            }
        };
        slotWidget.setChangeListener(()-> {
            if (!itemHandler.getStackInSlot(0).isEmpty()) {
                fluidStack = null;
                fluidWidget.setVisible(false);
                slotWidget.setVisible(true);
                if (onChanged != null) {
                    onChanged.accept(itemHandler.getStackInSlot(0));
                }
            }
        }).setBackgroundTexture(TerminalTheme.COLOR_B_2);
        this.addWidget(fluidWidget);
        this.addWidget(slotWidget);

        if (defaultObj instanceof ItemStack) {
            itemHandler.setStackInSlot(0, (ItemStack) defaultObj);
            fluidWidget.setVisible(false);
            slotWidget.setVisible(true);
        } else if (defaultObj instanceof FluidStack) {
            fluidStack = (FluidStack) defaultObj;
            slotWidget.setVisible(false);
            fluidWidget.setVisible(true);
        }
    }

    public PhantomWidget setChangeListener(Consumer<Object> onChanged) {
        this.onChanged = onChanged;
        return this;
    }

    public void setObject(FluidStack fluid) {
        if (fluid != null) {
            fluidStack = fluid.copy();
            if (fluidStack != null && fluidStack.amount > 0) {
                itemHandler.setStackInSlot(0, ItemStack.EMPTY);
                slotWidget.setVisible(false);
                fluidWidget.setVisible(true);
                if (onChanged != null) {
                    onChanged.accept(fluidStack);
                }
            }
        }
    }

    public void setObject(ItemStack item) {
        if (item != null && !item.isEmpty()) {
            ItemStack copy = item.copy();
            copy.setCount(1);
            itemHandler.setStackInSlot(0, copy);
            fluidStack = null;
            fluidWidget.setVisible(false);
            slotWidget.setVisible(true);
            if (onChanged != null) {
                onChanged.accept(copy);
            }
        }
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        if (!isVisible()) {
            return Collections.emptyList();
        }
        ArrayList<IGhostIngredientHandler.Target<?>> targets = new ArrayList<>();
        for (Widget widget : widgets) {
            if (widget instanceof IGhostIngredientTarget) {
                targets.addAll(((IGhostIngredientTarget) widget).getPhantomTargets(ingredient));
            }
        }
        return targets;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            ItemStack itemStack = gui.entityPlayer.inventory.getItemStack();
            if (!itemStack.isEmpty()) {
                IFluidHandler handlerItem = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (handlerItem != null && handlerItem.getTankProperties().length > 0) {
                    FluidStack fluidStack = handlerItem.getTankProperties()[0].getContents();
                    if (fluidStack != null) {
                        this.setObject(fluidStack);
                        return true;
                    }
                }
                this.setObject(itemStack);
                return true;
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
