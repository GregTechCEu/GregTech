package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.cover.IFacadeCover;
import gregtech.client.renderer.handler.FacadeRenderer;
import gregtech.common.covers.facade.FacadeHelper;
import gregtech.common.items.behaviors.FacadeItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoverFacade extends CoverBase implements IFacadeCover {

    private ItemStack facadeStack = ItemStack.EMPTY;
    private IBlockState facadeState = Blocks.STONE.getDefaultState();

    public CoverFacade(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                       @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    public void setFacadeStack(ItemStack facadeStack) {
        this.facadeStack = facadeStack.copy();
        updateFacadeState();
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return true;
    }

    @Override
    public void onAttachment(@NotNull CoverableView coverableView, @NotNull EnumFacing side, @Nullable EntityPlayer player, @NotNull ItemStack itemStack) {
        super.onAttachment(coverableView, side, player, itemStack);
        setFacadeStack(FacadeItem.getFacadeStack(itemStack));
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        BlockRenderLayer oldLayer = MinecraftForgeClient.getRenderLayer();
        ForgeHooksClient.setRenderLayer(layer);
        FacadeRenderer.renderBlockCover(renderState, translation, getCoverableView().getWorld(), getCoverableView().getPos(), getAttachedSide().getIndex(), facadeState, plateBox, layer);
        ForgeHooksClient.setRenderLayer(oldLayer);
    }

    @Override
    public boolean canRenderInLayer(BlockRenderLayer renderLayer) {
        return true;
    }

    @Override
    public IBlockState getVisualState() {
        return facadeState;
    }

    @Override
    public @NotNull ItemStack getPickItem() {
        ItemStack dropStack = getDefinition().getDropItemStack();
        FacadeItem.setFacadeStack(dropStack, facadeStack);
        return dropStack;
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeShort(Item.getIdFromItem(facadeStack.getItem()));
        packetBuffer.writeShort(Items.FEATHER.getDamage(facadeStack));
    }

    @Override
    public void readInitialSyncData(@NotNull PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        Item item = Item.getItemById(packetBuffer.readShort());
        int itemDamage = packetBuffer.readShort();
        this.facadeStack = new ItemStack(item, 1, itemDamage);
        updateFacadeState();
    }

    @Override
    public void writeToNBT(@NotNull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setTag("Facade", facadeStack.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.facadeStack = new ItemStack(tagCompound.getCompoundTag("Facade"));
        this.updateFacadeState();
    }

    @Override
    public boolean canPipePassThrough() {
        return true;
    }

    private void updateFacadeState() {
        this.facadeState = FacadeHelper.lookupBlockForItem(facadeStack);
    }

    @Override
    public boolean shouldAutoConnectToPipes() {
        return false;
    }

    @Override
    public boolean canRenderBackside() {
        return false;
    }

    @Override
    public void renderCoverPlate(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
    }
}
