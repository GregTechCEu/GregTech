package gregtech.api.gui.widgets;

import gregtech.api.fluids.GTFluid;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.utils.RenderUtil;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import com.google.common.collect.Lists;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static gregtech.api.capability.GregtechDataCodes.*;
import static gregtech.api.util.GTUtility.getFluidFromContainer;

/**
 * Class Designed for the Quantum Tank. Could be used elsewhere, but is very specialized.
 */
public class PhantomTankWidget extends TankWidget implements IGhostIngredientTarget {

    private final Supplier<FluidStack> phantomFluidGetter;
    private final Consumer<FluidStack> phantomFluidSetter;

    @Nullable
    protected FluidStack lastPhantomStack;

    public PhantomTankWidget(IFluidTank fluidTank, int x, int y, int width, int height,
                             Supplier<FluidStack> phantomFluidGetter, Consumer<FluidStack> phantomFluidSetter) {
        super(fluidTank, x, y, width, height);
        this.phantomFluidGetter = phantomFluidGetter;
        this.phantomFluidSetter = phantomFluidSetter;
        setLastPhantomStack(this.phantomFluidGetter.get());
    }

    protected void setLastPhantomStack(FluidStack fluid) {
        if (fluid != null) {
            this.lastPhantomStack = fluid.copy();
            this.lastPhantomStack.amount = 1;
        } else {
            this.lastPhantomStack = null;
        }
    }

    @Override
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        if (lastFluidInTank != null || getFluidFromContainer(ingredient) == null) {
            return Collections.emptyList();
        }

        Rectangle rectangle = toRectangleBox();
        return Lists.newArrayList(new Target<Object>() {

            @NotNull
            @Override
            public Rectangle getArea() {
                return rectangle;
            }

            @Override
            public void accept(@NotNull Object ingredient) {
                FluidStack stack = getFluidFromContainer(ingredient);

                if (stack != null) {
                    NBTTagCompound compound = stack.writeToNBT(new NBTTagCompound());
                    writeClientAction(LOAD_PHANTOM_FLUID_STACK_FROM_NBT, buf -> buf.writeCompoundTag(compound));
                }

                phantomFluidSetter.accept(stack);
            }
        });
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buf) {
        if (id == SET_PHANTOM_FLUID) {
            ItemStack stack = gui.entityPlayer.inventory.getItemStack().copy();
            if (stack.isEmpty()) {
                phantomFluidSetter.accept(null);
            } else {
                stack.setCount(1);
                IFluidHandlerItem fluidHandler = stack
                        .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandler != null) {
                    phantomFluidSetter.accept(fluidHandler.drain(Integer.MAX_VALUE, false));
                }
            }
        } else if (id == LOAD_PHANTOM_FLUID_STACK_FROM_NBT) {
            FluidStack stack;
            try {
                stack = FluidStack.loadFluidStackFromNBT(buf.readCompoundTag());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            phantomFluidSetter.accept(stack);
        } else super.handleClientAction(id, buf);
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            return lastFluidInTank == null ? phantomFluidGetter.get() : lastFluidInTank;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            writeClientAction(SET_PHANTOM_FLUID, buf -> {});
            return true;
        }
        return false;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (this.lastFluidInTank != null) {
            super.drawInBackground(mouseX, mouseY, partialTicks, context);
            return;
        }
        Position pos = getPosition();
        Size size = getSize();
        FluidStack fluid = phantomFluidGetter.get();
        if (fluid != null && !gui.isJEIHandled) {
            GlStateManager.disableBlend();
            RenderUtil.drawFluidForGui(fluid, fluid.amount,
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
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        FluidStack stack = phantomFluidGetter.get();
        if (stack == null) {
            if (lastPhantomStack != null) {
                setLastPhantomStack(null);
                writeUpdateInfo(REMOVE_PHANTOM_FLUID_TYPE, buf -> {});
            }
        } else if (lastPhantomStack == null || !stack.isFluidEqual(lastPhantomStack)) {
            setLastPhantomStack(stack);
            NBTTagCompound stackTag = stack.writeToNBT(new NBTTagCompound());
            writeUpdateInfo(CHANGE_PHANTOM_FLUID, buf -> buf.writeCompoundTag(stackTag));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buf) {
        if (id == REMOVE_PHANTOM_FLUID_TYPE) {
            phantomFluidSetter.accept(null);
        } else if (id == CHANGE_PHANTOM_FLUID) {
            NBTTagCompound stackTag;
            try {
                stackTag = buf.readCompoundTag();
            } catch (IOException ignored) {
                return;
            }
            phantomFluidSetter.accept(FluidStack.loadFluidStackFromNBT(stackTag));
        } else super.readUpdateInfo(id, buf);
    }

    @Override
    public String getFluidLocalizedName() {
        if (lastFluidInTank != null) {
            return lastFluidInTank.getLocalizedName();
        }
        FluidStack fluid = phantomFluidGetter.get();
        return fluid == null ? "" : fluid.getLocalizedName();
    }

    @Nullable
    public TextComponentTranslation getFluidTextComponent() {
        if (lastFluidInTank != null && lastFluidInTank.getFluid() instanceof GTFluid.GTMaterialFluid materialFluid) {
            return materialFluid.toTextComponentTranslation();
        }

        FluidStack stack = phantomFluidGetter.get();
        if (stack == null) return null;
        if (stack.getFluid() instanceof GTFluid.GTMaterialFluid materialFluid) {
            return materialFluid.toTextComponentTranslation();
        }
        return new TextComponentTranslation(stack.getUnlocalizedName());
    }
}
