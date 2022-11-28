package gregtech.common.items.tool;

import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * The Tree Felling Behavior must be handled in a special way in {@link gregtech.api.items.toolitem.IGTTool#definition$onBlockStartBreak(ItemStack, BlockPos, EntityPlayer)}
 *
 * @see gregtech.api.items.toolitem.ToolHelper#treeFellingRoutine(EntityPlayerMP, ItemStack, BlockPos)
 */
public class TreeFellingBehavior implements IToolBehavior {

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(" " + I18n.format("item.gt.tool.behavior.tree_felling"));
    }

    @Override
    public void addBehaviorNBT(@Nonnull ItemStack stack, @Nonnull NBTTagCompound tag) {
        tag.setBoolean(ToolHelper.TREE_FELLING_KEY, true);
    }
}
