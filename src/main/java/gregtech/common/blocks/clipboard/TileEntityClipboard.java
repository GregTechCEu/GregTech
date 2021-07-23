package gregtech.common.blocks.clipboard;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverBehaviorUIFactory;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.itemhandlers.InaccessibleItemStackHandler;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.Material;
import gregtech.common.items.behaviors.ClipboardBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.Optional;

import static gregtech.common.items.MetaItems.CLIPBOARD;

public class TileEntityClipboard extends TileEntity implements IUIHolder {

    private ItemStackHandler clipboardStackHandler = new ItemStackHandler(1);
    protected EnumFacing frontFacing = EnumFacing.NORTH;



    public void openUI(EntityPlayerMP player) {
        TileEntityClipboardUIFactory.INSTANCE.openUI( this, player);
    }

    protected ModularUI createUI(EntityPlayer entityPlayer) {
        if(clipboardStackHandler.getStackInSlot(0) == ItemStack.EMPTY) {
            clipboardStackHandler.setStackInSlot(0, CLIPBOARD.getStackForm());
        }
        if(clipboardStackHandler.getStackInSlot(0).isItemEqual(CLIPBOARD.getStackForm())) {
            List<IItemBehaviour> behaviours = ((MetaItem<?>) clipboardStackHandler.getStackInSlot(0).getItem()).getBehaviours(clipboardStackHandler.getStackInSlot(0));
            Optional<IItemBehaviour> clipboardBehaviour = behaviours.stream().filter((x) -> x instanceof ClipboardBehaviour).findFirst();
            if(!clipboardBehaviour.isPresent())
                return null;
            if(clipboardBehaviour.get() instanceof ClipboardBehaviour) {
                return ((ClipboardBehaviour) clipboardBehaviour.get()).createUI(new PlayerInventoryHolder(entityPlayer, entityPlayer.getActiveHand(), clipboardStackHandler.getStackInSlot(0)), entityPlayer);
            }
        }
        return null;
    }

    public ItemStack getClipboard() {
        return this.clipboardStackHandler.getStackInSlot(0).copy();
    }

    public void setClipboard(ItemStack stack) {
        this.clipboardStackHandler.setStackInSlot(0, stack);
    }

    public EnumFacing getFrontFacing() {
        return frontFacing;
    }

    public void setFrontFacing(EnumFacing facing) {
        if(facing == EnumFacing.UP || facing == EnumFacing.DOWN)
            throw new IllegalArgumentException("Clipboards can't face up or down!");
        frontFacing = facing;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isRemote() {
        return this.getWorld().isRemote;
    }

    @Override
    public void markAsDirty() {
        super.markDirty();
    }
}
