package gregtech.common.items.tool;

import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class HarvestIceBehavior implements IToolBehavior {

    // ice harvesting is handled in an event elsewhere

    @Override
    public void addBehaviorNBT(@Nonnull ItemStack stack, @Nonnull NBTTagCompound tag) {
        tag.setBoolean(ToolHelper.HARVEST_ICE_KEY, true);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(" " + I18n.format("item.gt.tool.behavior.silk_ice"));
    }
}
