package gregtech.api.items.gui;

import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.BooleanSupplier;

public class PlayerInventoryHolder implements IUIHolder {

    public static void openHandItemUI(EntityPlayer player, EnumHand hand) {
        PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
        holder.openUI();
    }

    public final EntityPlayer player;

    final EnumHand hand;

    ItemStack sampleItem;
    BooleanSupplier validityCheck;

    @SideOnly(Side.CLIENT)
    public PlayerInventoryHolder(EntityPlayer player, EnumHand hand, ItemStack sampleItem) {
        this.player = player;
        this.hand = hand;
        this.sampleItem = sampleItem;
        this.validityCheck = () -> ItemStack.areItemsEqual(sampleItem, player.getHeldItem(hand));
    }

    public PlayerInventoryHolder(EntityPlayer entityPlayer, EnumHand hand) {
        this.player = entityPlayer;
        this.hand = hand;
        this.sampleItem = player.getHeldItem(hand);
        this.validityCheck = () -> ItemStack.areItemsEqual(sampleItem, player.getHeldItem(hand));
    }

    public PlayerInventoryHolder setCustomValidityCheck(BooleanSupplier validityCheck) {
        this.validityCheck = validityCheck;
        return this;
    }

    ModularUI createUI(EntityPlayer entityPlayer) {
        ItemUIFactory uiFactory = (ItemUIFactory) sampleItem.getItem();
        return uiFactory.createUI(this, entityPlayer);
    }

    public void openUI() {
        PlayerInventoryUIFactory.INSTANCE.openUI(this, (EntityPlayerMP) player);
    }

    @Override
    public boolean isValid() {
        return validityCheck.getAsBoolean();
    }

    @Override
    public boolean isRemote() {
        return player.getEntityWorld().isRemote;
    }

    public ItemStack getCurrentItem() {
        ItemStack itemStack = player.getHeldItem(hand);
        if (!ItemStack.areItemsEqual(sampleItem, itemStack))
            return null;
        return itemStack;
    }

    /**
     * Will replace current item in hand with the given one
     * will also update sample item to this item
     */
    public void setCurrentItem(ItemStack item) {
        this.sampleItem = item;
        player.setHeldItem(hand, item);
    }

    @Override
    public void markAsDirty() {
        player.inventory.markDirty();
        player.inventoryContainer.detectAndSendChanges();
    }
}
