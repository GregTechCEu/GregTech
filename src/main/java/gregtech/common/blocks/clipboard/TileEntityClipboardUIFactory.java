package gregtech.common.blocks.clipboard;

import gregtech.api.GTValues;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.UIFactory;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityClipboardUIFactory extends UIFactory<TileEntityClipboard> {
    public static final TileEntityClipboardUIFactory INSTANCE = new TileEntityClipboardUIFactory();

    public void init() {
        UIFactory.FACTORY_REGISTRY.register(3, new ResourceLocation(GTValues.MODID, "clipboard_factory"), this);
    }

    @Override
    protected ModularUI createUITemplate(TileEntityClipboard clipboard, EntityPlayer entityPlayer) {
        return clipboard.createUI(entityPlayer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected TileEntityClipboard readHolderFromSyncData(PacketBuffer syncData) {
        return (TileEntityClipboard) Minecraft.getMinecraft().world.getTileEntity(syncData.readBlockPos());
    }

    @Override
    protected void writeHolderToSyncData(PacketBuffer syncData, TileEntityClipboard holder) {
        syncData.writeBlockPos(holder.getPos());
    }
}
