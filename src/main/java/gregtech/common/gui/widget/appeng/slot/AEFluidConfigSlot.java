package gregtech.common.gui.widget.appeng.slot;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.common.gui.widget.appeng.AEConfigWidget;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.data.IAEFluidStack;
import com.google.common.collect.Lists;
import mezz.jei.api.gui.IGhostIngredientHandler;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.LOAD_PHANTOM_FLUID_STACK_FROM_NBT;
import static gregtech.api.util.GTUtility.getFluidFromContainer;

/**
 * @Author GlodBlock
 * @Description A configurable slot for {@link IAEFluidStack}
 * @Date 2023/4/21-0:50
 */
public class AEFluidConfigSlot extends AEConfigSlot<IAEFluidStack> {

    public AEFluidConfigSlot(int x, int y, AEConfigWidget<IAEFluidStack> widget, int index) {
        super(new Position(x, y), new Size(18, 18 * 2), widget, index);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        Position position = getPosition();
        IConfigurableSlot<IAEFluidStack> slot = this.parentWidget.getDisplay(this.index);
        IAEFluidStack config = slot.getConfig();
        IAEFluidStack stock = slot.getStock();
        GuiTextures.FLUID_SLOT.draw(position.x, position.y, 18, 18);
        GuiTextures.FLUID_SLOT.draw(position.x, position.y + 18, 18, 18);
        GuiTextures.CONFIG_ARROW.draw(position.x, position.y, 18, 18);
        if (this.select) {
            GuiTextures.SELECT_BOX.draw(position.x, position.y, 18, 18);
        }
        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (config != null) {
            RenderUtil.drawFluidForGui(config.getFluidStack(), config.getFluidStack().amount, stackX, stackY, 17, 17);
            String amountStr = TextFormattingUtil.formatLongToCompactString(config.getStackSize(), 4) + "L";
            drawStringFixedCorner(amountStr, stackX + 17, stackY + 17, 16777215, true, 0.5f);
        }
        if (stock != null) {
            RenderUtil.drawFluidForGui(stock.getFluidStack(), stock.getFluidStack().amount, stackX, stackY + 18, 17,
                    17);
            String amountStr = TextFormattingUtil.formatLongToCompactString(stock.getStackSize(), 4) + "L";
            drawStringFixedCorner(amountStr, stackX + 17, stackY + 18 + 17, 16777215, true, 0.5f);
        }
        if (mouseOverConfig(mouseX, mouseY)) {
            drawSelectionOverlay(stackX, stackY, 16, 16);
        } else if (mouseOverStock(mouseX, mouseY)) {
            drawSelectionOverlay(stackX, stackY + 18, 16, 16);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        IAEFluidStack fluid = null;
        boolean displayAmt = false;
        IConfigurableSlot<IAEFluidStack> slot = this.parentWidget.getDisplay(this.index);
        if (mouseOverConfig(mouseX, mouseY)) {
            fluid = slot.getConfig();
        } else if (mouseOverStock(mouseX, mouseY)) {
            fluid = slot.getStock();
            displayAmt = true;
        }
        if (fluid != null) {
            List<String> hoverStringList = new ArrayList<>();
            hoverStringList.add(fluid.getFluidStack().getLocalizedName());
            if (displayAmt) {
                hoverStringList.add(String.format("%,d L", fluid.getStackSize()));
            }
            List<String> formula = FluidTooltipUtil.getFluidTooltip(fluid.getFluidStack());
            if (formula != null) {
                for (String s : formula) {
                    if (s.isEmpty()) continue;
                    hoverStringList.add(s);
                }
            }
            drawHoveringText(ItemStack.EMPTY, hoverStringList, -1, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (mouseOverConfig(mouseX, mouseY)) {
            if (button == 1) {
                // Right click to clear
                this.parentWidget.disableAmount();
                writeClientAction(REMOVE_ID, buf -> {});
            } else if (button == 0) {
                // Left click to set/select
                ItemStack hold = this.gui.entityPlayer.inventory.getItemStack();
                FluidStack fluid = FluidUtil.getFluidContained(hold);

                if (fluid != null) {
                    writeClientAction(UPDATE_ID, buf -> {
                        buf.writeString(fluid.getFluid().getName());
                        buf.writeVarInt(fluid.amount);
                    });
                }
                this.parentWidget.enableAmount(this.index);
                this.select = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        IConfigurableSlot<IAEFluidStack> slot = this.parentWidget.getConfig(this.index);
        if (id == REMOVE_ID) {
            slot.setConfig(null);
            this.parentWidget.disableAmount();
            writeUpdateInfo(REMOVE_ID, buf -> {});
        }
        if (id == UPDATE_ID) {
            FluidStack fluid = FluidRegistry.getFluidStack(buffer.readString(Integer.MAX_VALUE / 16),
                    buffer.readVarInt());
            slot.setConfig(WrappedFluidStack.fromFluidStack(fluid));
            this.parentWidget.enableAmount(this.index);
            if (fluid != null) {
                writeUpdateInfo(UPDATE_ID, buf -> {
                    buf.writeString(fluid.getFluid().getName());
                    buf.writeVarInt(fluid.amount);
                });
            }
        }
        if (id == AMOUNT_CHANGE_ID) {
            if (slot.getConfig() != null) {
                int amt = buffer.readInt();
                slot.getConfig().setStackSize(amt);
                writeUpdateInfo(AMOUNT_CHANGE_ID, buf -> buf.writeInt(amt));
            }
        }
        if (id == LOAD_PHANTOM_FLUID_STACK_FROM_NBT) {
            try {
                FluidStack fluid = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
                slot.setConfig(WrappedFluidStack.fromFluidStack(fluid));
                this.parentWidget.enableAmount(this.index);
                if (fluid != null) {
                    writeUpdateInfo(UPDATE_ID, buf -> {
                        buf.writeString(fluid.getFluid().getName());
                        buf.writeVarInt(fluid.amount);
                    });
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        IConfigurableSlot<IAEFluidStack> slot = this.parentWidget.getDisplay(this.index);
        if (id == REMOVE_ID) {
            slot.setConfig(null);
        }
        if (id == UPDATE_ID) {
            FluidStack fluid = FluidRegistry.getFluidStack(buffer.readString(Integer.MAX_VALUE / 16),
                    buffer.readVarInt());
            slot.setConfig(WrappedFluidStack.fromFluidStack(fluid));
        }
        if (id == AMOUNT_CHANGE_ID) {
            if (slot.getConfig() != null) {
                int amt = buffer.readInt();
                slot.getConfig().setStackSize(amt);
            }
        }
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        if (getFluidFromContainer(ingredient) == null) {
            return Collections.emptyList();
        }
        Rectangle rectangle = toRectangleBox();
        rectangle.height /= 2;
        return Lists.newArrayList(new IGhostIngredientHandler.Target<>() {

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
            }
        });
    }

    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        IConfigurableSlot<IAEFluidStack> slot = this.parentWidget.getDisplay(this.index);
        Rectangle rectangle = toRectangleBox();
        rectangle.height /= 2;
        if (slot.getConfig() == null || wheelDelta == 0 || !rectangle.contains(mouseX, mouseY)) {
            return false;
        }
        FluidStack fluid = slot.getConfig().getFluidStack();
        long amt;
        if (isCtrlDown()) {
            amt = wheelDelta > 0 ? fluid.amount * 2L : fluid.amount / 2L;
        } else {
            amt = wheelDelta > 0 ? fluid.amount + 1L : fluid.amount - 1L;
        }

        if (amt > 0 && amt < Integer.MAX_VALUE + 1L) {
            int finalAmt = (int) amt;
            writeClientAction(AMOUNT_CHANGE_ID, buf -> buf.writeInt(finalAmt));
            return true;
        }
        return false;
    }
}
