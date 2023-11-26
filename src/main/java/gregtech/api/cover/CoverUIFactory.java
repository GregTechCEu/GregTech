package gregtech.api.cover;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.UIFactory;
import gregtech.api.util.GTUtility;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public final class CoverUIFactory extends UIFactory<CoverWithUI> {

    public static final CoverUIFactory INSTANCE = new CoverUIFactory();

    private CoverUIFactory() {}

    public void init() {
        GregTechAPI.UI_FACTORY_REGISTRY.register(2, GTUtility.gregtechId("cover_behavior_factory"), this);
    }

    @Override
    protected ModularUI createUITemplate(CoverWithUI holder, EntityPlayer entityPlayer) {
        return holder.createUI(entityPlayer);
    }

    @Override
    protected CoverWithUI readHolderFromSyncData(PacketBuffer syncData) {
        BlockPos blockPos = syncData.readBlockPos();
        EnumFacing attachedSide = EnumFacing.VALUES[syncData.readByte()];
        TileEntity tileEntity = Minecraft.getMinecraft().world.getTileEntity(blockPos);
        CoverableView coverable = tileEntity == null ? null :
                tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER, attachedSide);
        if (coverable != null) {
            Cover cover = coverable.getCoverAtSide(attachedSide);
            if (cover instanceof CoverWithUI coverWithUI) {
                return coverWithUI;
            }
        }
        return null;
    }

    @Override
    protected void writeHolderToSyncData(PacketBuffer syncData, CoverWithUI cover) {
        syncData.writeBlockPos(cover.getPos());
        syncData.writeByte(cover.getAttachedSide().ordinal());
    }
}
