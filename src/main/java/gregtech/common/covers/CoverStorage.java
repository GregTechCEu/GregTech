package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.layout.Grid;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

public class CoverStorage extends CoverBehavior implements CoverWithUI {

    private final ItemStackHandler storageHandler = new ItemStackHandler(9);
    private static final int MAX_WIDTH = 176;
    private static final int MAX_HEIGHT = 126;
    private static final int SLOT_SIZE = 18;

    public CoverStorage(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return true;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        Textures.STORAGE.renderSided(attachedSide, plateBox, renderState, pipeline, translation);
    }

    @Override
    public void onRemoved() {
        NonNullList<ItemStack> drops = NonNullList.create();
        MetaTileEntity.clearInventory(drops, storageHandler);
        for (ItemStack itemStack : drops) {
            Block.spawnAsEntity(coverHolder.getWorld(), coverHolder.getPos(), itemStack);
        }
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        if (!coverHolder.getWorld().isRemote) {
            openUI((EntityPlayerMP) playerIn);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        ModularUI.Builder builder = new ModularUI.Builder(GuiTextures.BACKGROUND, MAX_WIDTH, MAX_HEIGHT);
        builder.label(5, 5, "cover.storage.title");
        for (int index = 0; index < storageHandler.getSlots(); index++) {
            builder.slot(storageHandler, index, (index * SLOT_SIZE) + 7, (MAX_HEIGHT - SLOT_SIZE * 5) / 2, true, true, GuiTextures.SLOT);
        }

        builder.bindPlayerInventory(player.inventory, (MAX_HEIGHT - SLOT_SIZE * 2) / 2 - 1);

        return builder.build(this, player);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel createUIPanel(GuiContext context, EntityPlayer player) {
        int rowSize = this.storageHandler.getSlots();
        List<List<IWidget>> widgets = new ArrayList<>();
        widgets.add(new ArrayList<>());
        for (int j = 0; j < rowSize; j++) {
            widgets.get(0).add(new ItemSlot().setSynced(j));
        }
        return ModularPanel.defaultPanel(context, MAX_WIDTH, MAX_HEIGHT)
                .child(IKey.lang("cover.storage.title").asWidget().pos(5, 5))
                .bindPlayerInventory()
                .child(new Grid()
                        .top((MAX_HEIGHT - SLOT_SIZE * 5) / 2).left(7).right(7).height(18)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .matrix(widgets));
    }

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer entityPlayer) {
        for (int i = 0; i < this.storageHandler.getSlots(); i++) {
            guiSyncHandler.syncValue(i, SyncHandlers.itemSlot(this.storageHandler, i).slotGroup("item_inv"));
        }
        guiSyncHandler.registerSlotGroup("item_inv", this.storageHandler.getSlots());
    }

    @Override
    public <T> T getCapability(Capability<T> capability, T defaultValue) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (defaultValue == null) {
                return null;
            }
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(storageHandler);
        }

        return defaultValue;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setTag("Storage", this.storageHandler.serializeNBT());
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.storageHandler.deserializeNBT(tagCompound.getCompoundTag("Storage"));
    }
}
