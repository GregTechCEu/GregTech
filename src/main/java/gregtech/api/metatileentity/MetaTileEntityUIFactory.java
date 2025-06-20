package gregtech.api.metatileentity;

import gregtech.api.GregTechAPI;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.UIFactory;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * {@link UIFactory} implementation for {@link MetaTileEntity}
 */
public class MetaTileEntityUIFactory extends UIFactory<IGregTechTileEntity> {

    public static final MetaTileEntityUIFactory INSTANCE = new MetaTileEntityUIFactory();

    private MetaTileEntityUIFactory() {}

    public void init() {
        GregTechAPI.UI_FACTORY_REGISTRY.register(0, GTUtility.gregtechId("meta_tile_entity_factory"), this);
    }

    @Override
    protected ModularUI createUITemplate(IGregTechTileEntity tileEntity, EntityPlayer entityPlayer) {
        return tileEntity.getMetaTileEntity().createUI(entityPlayer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected IGregTechTileEntity readHolderFromSyncData(PacketBuffer syncData) {
        return (IGregTechTileEntity) Minecraft.getMinecraft().world.getTileEntity(syncData.readBlockPos());
    }

    @Override
    protected void writeHolderToSyncData(PacketBuffer syncData, IGregTechTileEntity holder) {
        syncData.writeBlockPos(holder.pos());
    }
}
