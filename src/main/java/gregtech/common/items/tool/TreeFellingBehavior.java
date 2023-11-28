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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The Tree Felling Behavior must be handled in a special way in
 * {@link gregtech.api.items.toolitem.IGTTool#definition$onBlockStartBreak(ItemStack, BlockPos, EntityPlayer)}
 *
 * @see gregtech.api.items.toolitem.ToolHelper#treeFellingRoutine(EntityPlayerMP, ItemStack, BlockPos)
 */
public class TreeFellingBehavior implements IToolBehavior {

    public static final TreeFellingBehavior INSTANCE = new TreeFellingBehavior();

    protected TreeFellingBehavior() {/**/}

    @Override
    public void addBehaviorNBT(@NotNull ItemStack stack, @NotNull NBTTagCompound tag) {
        tag.setBoolean(ToolHelper.TREE_FELLING_KEY, true);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.tree_felling"));
    }
}
