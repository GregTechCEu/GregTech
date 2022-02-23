package gregtech.api.gui.widgets;

import com.google.common.collect.Lists;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Position;
import gregtech.client.utils.RenderUtil;
import gregtech.api.util.Size;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.*;

/**
 * Class Designed for the Quantum Tank. Could be used elsewhere, but is very specialized.
 */
public class PhantomTankWidget extends TankWidget implements IGhostIngredientTarget {

    private final FluidTank phantomTank;

    protected FluidStack lastPhantomStack;

    public PhantomTankWidget(IFluidTank fluidTank, int x, int y, int width, int height, FluidTank phantomTank) {
        super(fluidTank, x, y, width, height);
        this.phantomTank = phantomTank;
    }

    @Override
    public PhantomTankWidget setClient() {
        super.setClient();
        this.lastPhantomStack = phantomTank != null ? phantomTank.getFluid() != null ? phantomTank.getFluid().copy() : null : null;
        return this;
    }

    @Override
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        if (lastFluidInTank != null || (!(ingredient instanceof FluidStack) && drainFrom(ingredient) == null)) {
            return Collections.emptyList();
        }

        Rectangle rectangle = toRectangleBox();
        return Lists.newArrayList(new Target<Object>() {

            @Nonnull
            @Override
            public Rectangle getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                FluidStack stack;
                if (ingredient instanceof FluidStack) {
                    stack = (FluidStack) ingredient;
                } else {
                    stack = drainFrom(ingredient);
                }

                if (stack != null) {
                    NBTTagCompound compound = stack.writeToNBT(new NBTTagCompound());
                    writeClientAction(LOAD_PHANTOM_FLUID_STACK_FROM_NBT, buf -> buf.writeCompoundTag(compound));
                }

                if (isClient) {
                    phantomTank.setFluid(stack);
                }
            }
        });
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buf) {
        super.handleClientAction(id, buf);
        if (id == VOID_PHANTOM_FLUID) {
            ItemStack stack = gui.entityPlayer.inventory.getItemStack().copy();
            if (!stack.isEmpty()) {
                stack.setCount(1);
                IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandler != null) {
                    FluidStack resultStack = fluidHandler.drain(Integer.MAX_VALUE, false);
                    phantomTank.setFluid(resultStack);
                }
            } else {
                phantomTank.setFluid(null);
            }
        } else if (id == LOAD_PHANTOM_FLUID_STACK_FROM_NBT) {
            FluidStack stack;
            try {
                stack = FluidStack.loadFluidStackFromNBT(buf.readCompoundTag());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            phantomTank.setFluid(stack);
        }
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            return lastFluidInTank == null ? lastPhantomStack : lastFluidInTank;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            writeClientAction(VOID_PHANTOM_FLUID, buf -> {});
            if (isClient) {
                phantomTank.setFluid(null);
            }
            return true;
        }
        return false;
    }

    private FluidStack drainFrom(Object ingredient) {
        if (ingredient instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) ingredient;
            IFluidHandlerItem fluidHandler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandler != null)
                return fluidHandler.drain(Integer.MAX_VALUE, false);
        }
        return null;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (this.lastFluidInTank != null) {
            super.drawInBackground(mouseX, mouseY, partialTicks, context);
            return;
        }
        Position pos = getPosition();
        Size size = getSize();
        if (lastPhantomStack != null && !gui.isJEIHandled) {
            GlStateManager.disableBlend();
            FluidStack stackToDraw = lastPhantomStack;
            if (stackToDraw.amount == 0) {
                stackToDraw = GTUtility.copyAmount(1, stackToDraw);
            }
            RenderUtil.drawFluidForGui(stackToDraw, 1,
                    pos.x + fluidRenderOffset, pos.y + fluidRenderOffset,
                    size.width - fluidRenderOffset, size.height - fluidRenderOffset);
            GlStateManager.enableBlend();
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (this.lastFluidInTank == null) return;
        super.drawInForeground(mouseX, mouseY);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (isClient) {
            FluidStack stack = phantomTank.getFluid();
            if (stack == null && lastPhantomStack != null) {
                lastPhantomStack = null;
            } else if (stack != null) {
                if (!stack.isFluidEqual(lastPhantomStack)) {
                    lastPhantomStack = stack.copy();
                } else if (stack.amount != 0) {
                    lastPhantomStack.amount = 0;
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        FluidStack stack = phantomTank.getFluid();
        if (stack == null && lastPhantomStack != null) {
            lastPhantomStack = null;
            writeUpdateInfo(REMOVE_PHANTOM_FLUID_TYPE, buf -> {});
        } else if (stack != null) {
            if (!stack.isFluidEqual(lastPhantomStack)) {
                lastPhantomStack = stack.copy();
                NBTTagCompound stackTag = stack.writeToNBT(new NBTTagCompound());
                writeUpdateInfo(CHANGE_PHANTOM_FLUID, buf -> buf.writeCompoundTag(stackTag));
            } else if (stack.amount != 0) {
                lastPhantomStack.amount = 0;
                writeUpdateInfo(VOID_PHANTOM_FLUID, buf -> {});
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buf) {
        super.readUpdateInfo(id, buf);
        if (id == REMOVE_PHANTOM_FLUID_TYPE) {
            lastPhantomStack = null;
        } else if (id == CHANGE_PHANTOM_FLUID) {
            NBTTagCompound stackTag;
            try {
                stackTag = buf.readCompoundTag();
            } catch (IOException ignored) {
                return;
            }
            lastPhantomStack = FluidStack.loadFluidStackFromNBT(stackTag);
        } else if (id == VOID_PHANTOM_FLUID) {
            lastPhantomStack.amount = 0;
        }
    }

    public String getFormattedFluidAmount() {
        if (lastFluidInTank == null) {
            return "0";
        }
        return super.getFormattedFluidAmount();
    }

    public String getFluidLocalizedName() {
        if (lastFluidInTank == null) {
            return lastPhantomStack == null ? "" : lastPhantomStack.getLocalizedName();
        }
        return super.getFluidLocalizedName();
    }
}
