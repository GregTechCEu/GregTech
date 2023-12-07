package gregtech.common.items.tool;

import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @see gregtech.common.ToolEventHandlers#onHarvestDrops(BlockEvent.HarvestDropsEvent)
 */
public class HarvestIceBehavior implements IToolBehavior {

    public static final HarvestIceBehavior INSTANCE = new HarvestIceBehavior();

    protected HarvestIceBehavior() {/**/}

    // ice harvesting is handled in an event elsewhere

    @Override
    public void addBehaviorNBT(@NotNull ItemStack stack, @NotNull NBTTagCompound tag) {
        tag.setBoolean(ToolHelper.HARVEST_ICE_KEY, true);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.silk_ice"));
    }
}
