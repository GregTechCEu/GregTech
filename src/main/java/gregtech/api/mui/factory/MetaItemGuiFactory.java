package gregtech.api.mui.factory;

import gregtech.api.items.metaitem.MetaItem;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.HandGuiData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MetaItemGuiFactory extends AbstractUIFactory<HandGuiData> {

    public static final MetaItemGuiFactory INSTANCE = new MetaItemGuiFactory();

    private MetaItemGuiFactory() {
        super("gregtech:meta_item");
    }

    public static void open(EntityPlayer player, EnumHand hand) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(hand);
        HandGuiData guiData = new HandGuiData(player, hand);
        GuiManager.open(INSTANCE, guiData, (EntityPlayerMP) player);
    }

    @Override
    public @NotNull IGuiHolder<HandGuiData> getGuiHolder(HandGuiData data) {
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
    public void writeGuiData(HandGuiData guiData, PacketBuffer buffer) {
        buffer.writeByte(guiData.getHand().ordinal());
    }

    @Override
    public @NotNull HandGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new HandGuiData(player, EnumHand.values()[buffer.readByte()]);
    }
}
