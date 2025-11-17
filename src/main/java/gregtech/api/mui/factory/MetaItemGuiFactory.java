package gregtech.api.mui.factory;

import gregtech.api.items.metaitem.MetaItem;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MetaItemGuiFactory extends AbstractUIFactory<PlayerInventoryGuiData> {

    public static final MetaItemGuiFactory INSTANCE = new MetaItemGuiFactory();

    private MetaItemGuiFactory() {
        super("gregtech:meta_item");
    }

    public static void open(EntityPlayer player, EnumHand hand) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(hand);
        int index = hand == EnumHand.OFF_HAND ? 40 : player.inventory.currentItem;
        PlayerInventoryGuiData guiData = new PlayerInventoryGuiData(player, InventoryTypes.PLAYER, index);
        GuiManager.open(INSTANCE, guiData, verifyServerSide(player));
    }

    @Override
    public @NotNull IGuiHolder<PlayerInventoryGuiData> getGuiHolder(PlayerInventoryGuiData data) {
        ItemStack stack = data.getUsedItemStack();
        if (!(stack.getItem() instanceof MetaItem<?>metaItem)) {
            throw new IllegalArgumentException("Found item is not a valid MetaItem!");
        }
        MetaItem<?>.MetaValueItem valueItem = metaItem.getItem(stack);
        if (valueItem == null || valueItem.getUIManager() == null) {
            throw new IllegalArgumentException("Found MetaItem is not a gui holder!");
        }
        return valueItem.getUIManager();
    }

    @Override
    public void writeGuiData(PlayerInventoryGuiData guiData, PacketBuffer buffer) {
        buffer.writeByte(guiData.getSlotIndex());
    }

    @Override
    public @NotNull PlayerInventoryGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new PlayerInventoryGuiData(player, InventoryTypes.PLAYER, buffer.readByte());
    }
}
