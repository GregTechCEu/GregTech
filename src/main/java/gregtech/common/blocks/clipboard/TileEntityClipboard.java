package gregtech.common.blocks.clipboard;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.itemhandlers.InaccessibleItemStackHandler;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.common.items.behaviors.ClipboardBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.Optional;

import static gregtech.common.items.MetaItems.CLIPBOARD;

public class TileEntityClipboard extends TileEntity, implements IUIHolder {

    private ItemStack clipboardStack;
    protected EnumFacing frontFacing = EnumFacing.NORTH;

    protected ModularUI createUI(EntityPlayer entityPlayer) {
        if(clipboardStack == ItemStack.EMPTY) {
           clipboardStack = CLIPBOARD.getStackForm();
        }
        if(clipboardStack.isItemEqual(CLIPBOARD.getStackForm())) {
            List<IItemBehaviour> behaviours = ((MetaItem<?>) clipboardStack.getItem()).getBehaviours(clipboardStack);
            Optional<IItemBehaviour> clipboardBehaviour = behaviours.stream().filter((x) -> x instanceof ClipboardBehaviour).findFirst();
            if(!clipboardBehaviour.isPresent())
                return null;
            if(clipboardBehaviour.get() instanceof ClipboardBehaviour) {
                return ((ClipboardBehaviour) clipboardBehaviour.get()).createUI(new PlayerInventoryHolder(entityPlayer, entityPlayer.getActiveHand(), clipboardStack), entityPlayer);
            }
        }
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public void markAsDirty() {

    }
}
