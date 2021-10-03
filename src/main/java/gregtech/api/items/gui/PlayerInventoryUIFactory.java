package gregtech.api.items.gui;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.UIFactory;
import gregtech.api.items.metaitem.MetaItem;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

/**
 * {@link UIFactory} implementation for {@link MetaItem}s
 */
public class PlayerInventoryUIFactory extends UIFactory<PlayerInventoryHolder> {

    public static final PlayerInventoryUIFactory INSTANCE = new PlayerInventoryUIFactory();

    private PlayerInventoryUIFactory() {
    }

    public void init() {
        GregTechAPI.UI_FACTORY_REGISTRY.register(1, new ResourceLocation(GTValues.MODID, "player_inventory_factory"), this);
    }

    @Override
    protected ModularUI createUITemplate(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        return holder.createUI(entityPlayer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected PlayerInventoryHolder readHolderFromSyncData(PacketBuffer syncData) {
        EntityPlayer entityPlayer = Minecraft.getMinecraft().player;
        EnumHand enumHand = EnumHand.values()[syncData.readByte()];
        ItemStack itemStack;
        try {
            itemStack = syncData.readItemStack();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        return new PlayerInventoryHolder(entityPlayer, enumHand, itemStack);
    }

    @Override
    protected void writeHolderToSyncData(PacketBuffer syncData, PlayerInventoryHolder holder) {
        syncData.writeByte(holder.hand.ordinal());
        syncData.writeItemStack(holder.getCurrentItem());
    }

}
