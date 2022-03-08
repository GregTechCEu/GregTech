package gregtech.api.metatileentity;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.guiOld.ModularUI;
import gregtech.api.guiOld.UIFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * {@link UIFactory} implementation for {@link MetaTileEntity}
 */
public class MetaTileEntityUIFactory extends UIFactory<MetaTileEntityHolder> {

    public static final MetaTileEntityUIFactory INSTANCE = new MetaTileEntityUIFactory();

    private MetaTileEntityUIFactory() {
    }

    public void init() {
        GregTechAPI.UI_FACTORY_REGISTRY.register(0, new ResourceLocation(GTValues.MODID, "meta_tile_entity_factory"), this);
    }

    @Override
    protected ModularUI createUITemplate(MetaTileEntityHolder holder, EntityPlayer entityPlayer) {
        return holder.getMetaTileEntity().createUI(entityPlayer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected MetaTileEntityHolder readHolderFromSyncData(PacketBuffer syncData) {
        return (MetaTileEntityHolder) Minecraft.getMinecraft().world.getTileEntity(syncData.readBlockPos());
    }

    @Override
    protected void writeHolderToSyncData(PacketBuffer syncData, MetaTileEntityHolder holder) {
        syncData.writeBlockPos(holder.getPos());
    }

}
