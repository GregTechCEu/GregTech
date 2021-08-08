package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.common.items.behaviors.ModeSwitchBehavior.ILocalizationKey;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class ModeSwitchBehavior<T extends Enum<T> & ILocalizationKey> implements IItemBehaviour {

    public final Class<T> enumClass;
    private final T[] enumConstants;

    public ModeSwitchBehavior(Class<T> enumClass) {
        this.enumClass = enumClass;
        this.enumConstants = enumClass.getEnumConstants();
    }

    public T getModeFromItemStack(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) {
            return enumConstants[0];
        }
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        return enumConstants[tagCompound.getInteger("Mode")];
    }

    public void setModeForItemStack(ItemStack itemStack, T newMode) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        tagCompound.setInteger("Mode", ArrayUtils.indexOf(enumConstants, newMode));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            T currentMode = getModeFromItemStack(itemStack);
            int currentModeIndex = ArrayUtils.indexOf(enumConstants, currentMode);
            T nextMode = enumConstants[(currentModeIndex + 1) % enumConstants.length];
            setModeForItemStack(itemStack, nextMode);
            ITextComponent newModeComponent = new TextComponentTranslation(nextMode.getUnlocalizedName());
            ITextComponent textComponent = new TextComponentTranslation("metaitem.behavior.mode_switch.mode_switched", newModeComponent);
            player.sendStatusMessage(textComponent, true);
            return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);
        }
        return ActionResult.newResult(EnumActionResult.PASS, itemStack);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        T currentMode = getModeFromItemStack(itemStack);
        lines.add(I18n.format("metaitem.behavior.mode_switch.tooltip"));
        lines.add(I18n.format("metaitem.behavior.mode_switch.current_mode", I18n.format(currentMode.getUnlocalizedName())));
    }

    public interface ILocalizationKey {
        String getUnlocalizedName();
    }
}
