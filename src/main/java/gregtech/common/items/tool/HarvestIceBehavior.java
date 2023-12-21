package gregtech.common.items.tool;

import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.behavior.IToolBehavior;

import gregtech.api.util.TaskScheduler;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
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

    private HarvestIceBehavior() {/**/}

    @Override
    public void convertBlockDrops(@NotNull ItemStack stack, @NotNull EntityPlayer player,
                                  @NotNull List<ItemStack> drops, BlockEvent.@NotNull HarvestDropsEvent event) {
        Block block = event.getState().getBlock();
        if (!event.isSilkTouching() && (block == Blocks.ICE || block == Blocks.PACKED_ICE)) {
            Item iceBlock = Item.getItemFromBlock(block);
            if (drops.stream().noneMatch(drop -> drop.getItem() == iceBlock)) {
                final World world = event.getWorld();
                final BlockPos pos = event.getPos();
                drops.add(new ItemStack(iceBlock));
                TaskScheduler.scheduleTask(world, () -> {
                    IBlockState flowingState = world.getBlockState(pos);
                    if (flowingState == Blocks.FLOWING_WATER.getDefaultState()) {
                        world.setBlockToAir(pos);
                    }
                    // only try once, so future water placement does not get eaten too
                    return false;
                });
                ((IGTTool) stack.getItem()).playSound(player);
            }
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.gt.tool.behavior.silk_ice"));
    }
}
