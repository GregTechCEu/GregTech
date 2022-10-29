package gregtech.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.cover.IFacadeCover;
import gregtech.client.renderer.handler.FacadeRenderer;
import gregtech.common.covers.facade.FacadeHelper;
import gregtech.common.items.behaviors.FacadeItem;
import net.minecraft.block.state.IBlockState;
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

public class CoverFacade extends CoverBehavior implements IFacadeCover {

    private ItemStack facadeStack = ItemStack.EMPTY;
    private IBlockState facadeState = Blocks.STONE.getDefaultState();

    public CoverFacade(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    public void setFacadeStack(ItemStack facadeStack) {
        this.facadeStack = facadeStack.copy();
        updateFacadeState();
    }

    @Override
    public boolean canAttach() {
        return true;
    }

    @Override
    public void onAttached(ItemStack itemStack) {
        super.onAttached(itemStack);
        setFacadeStack(FacadeItem.getFacadeStack(itemStack));
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 plateBox, BlockRenderLayer layer) {
        BlockRenderLayer oldLayer = MinecraftForgeClient.getRenderLayer();
        ForgeHooksClient.setRenderLayer(layer);
        FacadeRenderer.renderBlockCover(renderState, translation, coverHolder.getWorld(), coverHolder.getPos(), attachedSide.getIndex(), facadeState, plateBox, layer);
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
    public ItemStack getPickItem() {
        ItemStack dropStack = getCoverDefinition().getDropItemStack();
        FacadeItem.setFacadeStack(dropStack, facadeStack);
        return dropStack;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        super.writeInitialSyncData(packetBuffer);
        packetBuffer.writeShort(Item.getIdFromItem(facadeStack.getItem()));
        packetBuffer.writeShort(Items.FEATHER.getDamage(facadeStack));
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        super.readInitialSyncData(packetBuffer);
        Item item = Item.getItemById(packetBuffer.readShort());
        int itemDamage = packetBuffer.readShort();
        this.facadeStack = new ItemStack(item, 1, itemDamage);
        updateFacadeState();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setTag("Facade", facadeStack.writeToNBT(new NBTTagCompound()));

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
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
    public boolean shouldAutoConnect() {
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
