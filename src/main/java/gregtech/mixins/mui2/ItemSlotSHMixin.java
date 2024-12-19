package gregtech.mixins.mui2;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemHandlerHelper;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(value = ItemSlotSH.class, remap = false)
public abstract class ItemSlotSHMixin extends SyncHandler {

    @Unique
    private boolean gregTech$registered;

    @Shadow
    protected abstract void phantomScroll(MouseData mouseData);

    @Shadow
    public abstract void setEnabled(boolean enabled, boolean sync);

    @Shadow
    public abstract boolean isPhantom();

    @Shadow
    private ItemStack lastStoredPhantomItem;

    @Shadow
    public abstract ModularSlot getSlot();

    @Shadow
    public abstract void incrementStackCount(int amount);

    @WrapOperation(method = "init",
                   at = @At(value = "INVOKE",
                            target = "Lcom/cleanroommc/modularui/screen/ModularContainer;registerSlot(Ljava/lang/String;Lcom/cleanroommc/modularui/widgets/slot/ModularSlot;)V"))
    protected void wrapRegister(ModularContainer instance, String slotGroup, ModularSlot modularSlot,
                                Operation<Void> original) {
        if (!gregTech$registered) {
            original.call(instance, slotGroup, modularSlot);
            gregTech$registered = true;
        }
    }

    /**
     * @author GTCEu - Ghzdude
     * @reason Implement <a href="https://github.com/CleanroomMC/ModularUI/pull/90">MUI2 PR#90</a>
     */
    @Overwrite
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == 2) {
            phantomClick(MouseData.readPacket(buf));
        } else if (id == 3) {
            phantomScroll(MouseData.readPacket(buf));
        } else if (id == 4) {
            setEnabled(buf.readBoolean(), false);
        } else if (id == 5) {
            if (!isPhantom()) return;
            gregTech$phantomClick(MouseData.create(0), buf.readItemStack());
        }
    }

    @Inject(method = "readOnClient",
            at = @At(value = "INVOKE",
                     target = "Lcom/cleanroommc/modularui/widgets/slot/ModularSlot;onSlotChangedReal(Lnet/minecraft/item/ItemStack;ZZZ)V"))
    protected void asdf(int id, PacketBuffer buf, CallbackInfo ci) {
        if (id == 3) {
            this.lastStoredPhantomItem = NetworkUtils.readItemStack(buf);
            getSlot().putStack(this.lastStoredPhantomItem.copy());
        }
    }

    @Inject(method = "phantomScroll", at = @At("TAIL"))
    protected void asdf(MouseData mouseData, CallbackInfo ci) {
        syncToClient(3, buffer -> NetworkUtils.writeItemStack(buffer, this.lastStoredPhantomItem));
    }

    /**
     * @author GTCEu - Ghzdude
     * @reason Implement <a href="https://github.com/CleanroomMC/ModularUI/pull/90">MUI2 PR#90</a>
     */
    @Overwrite
    protected void phantomClick(MouseData mouseData) {
        gregTech$phantomClick(mouseData, getSyncManager().getCursorItem());
    }

    @Unique
    protected void gregTech$phantomClick(MouseData mouseData, ItemStack cursorStack) {
        ItemStack slotStack = getSlot().getStack();
        ItemStack stackToPut;
        if (!cursorStack.isEmpty() && !slotStack.isEmpty() &&
                !ItemHandlerHelper.canItemStacksStack(cursorStack, slotStack)) {
            stackToPut = cursorStack.copy();
            if (mouseData.mouseButton == 1) {
                stackToPut.setCount(1);
            }
            stackToPut.setCount(Math.min(stackToPut.getCount(), getSlot().getItemStackLimit(stackToPut)));
            getSlot().putStack(stackToPut);
            this.lastStoredPhantomItem = stackToPut.copy();
        } else if (slotStack.isEmpty()) {
            if (cursorStack.isEmpty()) {
                if (mouseData.mouseButton == 1 && !this.lastStoredPhantomItem.isEmpty()) {
                    stackToPut = this.lastStoredPhantomItem.copy();
                } else {
                    return;
                }
            } else {
                stackToPut = cursorStack.copy();
            }
            if (mouseData.mouseButton == 1) {
                stackToPut.setCount(1);
            }
            stackToPut.setCount(Math.min(stackToPut.getCount(), getSlot().getItemStackLimit(stackToPut)));
            getSlot().putStack(stackToPut);
            this.lastStoredPhantomItem = stackToPut.copy();
        } else {
            if (mouseData.mouseButton == 0) {
                if (mouseData.shift) {
                    getSlot().putStack(ItemStack.EMPTY);
                } else {
                    incrementStackCount(-1);
                }
            } else if (mouseData.mouseButton == 1) {
                incrementStackCount(1);
            }
        }
    }
}
