package gregtech.common.covers;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.ICoverable;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.BlockPosFace;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.items.behaviors.CoverDigitalInterfaceWirelessPlaceBehaviour;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityCentralMonitor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class CoverDigitalInterfaceWireless extends CoverDigitalInterface{
    private BlockPos remote;

    public CoverDigitalInterfaceWireless(ICoverable coverHolder, EnumFacing attachedSide) {
        super(coverHolder, attachedSide);
    }

    @Override
    public void setMode(MODE mode, int slot, EnumFacing spin) {
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        if (this.remote != null) {
            tagCompound.setTag("cdiRemote", NBTUtil.createPosTag(this.remote));
        }

        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.remote = tagCompound.hasKey("cdiRemote") ? NBTUtil.getPosFromTag(tagCompound.getCompoundTag("cdiRemote")) : null;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer packetBuffer) {
        packetBuffer.writeBoolean(remote != null);
        if (remote != null) {
            packetBuffer.writeBlockPos(remote);
        }
        super.writeInitialSyncData(packetBuffer);
    }

    @Override
    public void readInitialSyncData(PacketBuffer packetBuffer) {
        if (packetBuffer.readBoolean()) {
            this.remote = packetBuffer.readBlockPos();
        }
        super.readInitialSyncData(packetBuffer);
    }

    @Override
    public void onAttached(ItemStack itemStack, EntityPlayer player) {
        remote = CoverDigitalInterfaceWirelessPlaceBehaviour.getRemotePos(itemStack);
    }

    @Override
    public void update() {
        super.update();
        if (remote != null && !isRemote() && coverHolder.getOffsetTimer() % 20 == 0) {
            TileEntity te = coverHolder.getWorld().getTileEntity(remote);
            if (te instanceof IGregTechTileEntity && ((IGregTechTileEntity) te).getMetaTileEntity() instanceof MetaTileEntityCentralMonitor) {
                ((MetaTileEntityCentralMonitor) ((IGregTechTileEntity) te).getMetaTileEntity()).addRemoteCover(new BlockPosFace(coverHolder.getPos(), attachedSide));
            }
        }
    }

    @Override
    public EnumActionResult onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, CuboidRayTraceResult hitResult) {
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ItemStack getPickItem() {
        ItemStack drop = super.getPickItem();
        if (remote != null) {
            drop.setTagCompound(NBTUtil.createPosTag(remote));
        }
        return drop;
    }

    @Override
    public void renderCover(CCRenderState ccRenderState, Matrix4 translation, IVertexOperation[] ops, Cuboid6 cuboid6, BlockRenderLayer blockRenderLayer) {
        Textures.COVER_INTERFACE_WIRELESS.renderSided(this.attachedSide, cuboid6, ccRenderState, ops, translation);
    }
}
