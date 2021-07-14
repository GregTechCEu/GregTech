package gregtech.common.metatileentities;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.FluidHandlerProxy;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.itemhandlers.InaccessibleItemStackHandler;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.common.items.MetaItems;
import gregtech.common.items.behaviors.ClipboardBehaviour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Optional;

import static gregtech.common.items.MetaItems.CLIPBOARD;

public class MetaTileEntityClipboard extends MetaTileEntity {

    public MetaTileEntityClipboard(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityClipboard(metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        if(itemInventory.getStackInSlot(0) == ItemStack.EMPTY) {
            ((InaccessibleItemStackHandler) itemInventory).setStackInSlot(0, CLIPBOARD.getStackForm());
        }
        if(itemInventory.getStackInSlot(0).isItemEqual(CLIPBOARD.getStackForm())) {
            List<IItemBehaviour> behaviours = ((MetaItem<?>) itemInventory.getStackInSlot(0).getItem()).getBehaviours(itemInventory.getStackInSlot(0));
            Optional<IItemBehaviour> clipboardBehaviour = behaviours.stream().filter((x) -> x instanceof ClipboardBehaviour).findFirst();
            if(!clipboardBehaviour.isPresent())
                return null;
            if(clipboardBehaviour.get() instanceof ClipboardBehaviour) {
                return ((ClipboardBehaviour) clipboardBehaviour.get()).createUI(new PlayerInventoryHolder(entityPlayer, entityPlayer.getActiveHand(), itemInventory.getStackInSlot(0)), entityPlayer);
            }
        }
        return null;
    }

    @Override
    protected void initializeInventory() {
        this.itemInventory = new InaccessibleItemStackHandler();

        this.importItems = createImportItemHandler();
        this.exportItems = createExportItemHandler();
        this.importFluids = createImportFluidHandler();
        this.exportFluids = createExportFluidHandler();
        this.fluidInventory = new FluidHandlerProxy(importFluids, exportFluids);
    }
}
