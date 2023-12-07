package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.util.Constants.NBT;

public class AbstractUsableBehaviour implements IItemBehaviour {

    public final int totalUses;

    public AbstractUsableBehaviour(int totalUses) {
        this.totalUses = totalUses;
    }

    public boolean useItemDurability(EntityPlayer player, EnumHand hand, ItemStack stack, ItemStack replacementStack) {
        int usesLeft = getUsesLeft(stack);
        if (!player.capabilities.isCreativeMode) {
            if (--usesLeft <= 0) {
                if (replacementStack.isEmpty()) {
                    // if replacement stack is empty, just shrink resulting stack
                    stack.shrink(1);
                } else {
                    // otherwise, update held item to replacement stack
                    player.setHeldItem(hand, replacementStack);
                }
                return true;
            }
            setUsesLeft(stack, usesLeft);
        }
        return true;
    }

    public final int getUsesLeft(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null || !tagCompound.hasKey("GT.UsesLeft", NBT.TAG_INT))
            return totalUses;
        return tagCompound.getInteger("GT.UsesLeft");
    }

    public static void setUsesLeft(ItemStack itemStack, int usesLeft) {
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            itemStack.setTagCompound(tagCompound);
        }
        tagCompound.setInteger("GT.UsesLeft", usesLeft);
    }
}
