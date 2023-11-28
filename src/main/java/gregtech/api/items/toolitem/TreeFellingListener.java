package gregtech.api.items.toolitem;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public final class TreeFellingListener {

    private final EntityPlayerMP player;
    private final ItemStack tool;
    private final Deque<BlockPos> orderedBlocks;

    private TreeFellingListener(EntityPlayerMP player, ItemStack tool, Deque<BlockPos> orderedBlocks) {
        this.player = player;
        this.tool = tool;
        this.orderedBlocks = orderedBlocks;
    }

    public static void start(@NotNull IBlockState state, ItemStack tool, BlockPos start,
                             @NotNull EntityPlayerMP player) {
        World world = player.world;
        Block block = state.getBlock();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        Queue<BlockPos> checking = new ArrayDeque<>();
        Set<BlockPos> visited = new ObjectOpenHashSet<>();
        checking.add(start);

        while (!checking.isEmpty()) {
            BlockPos check = checking.remove();
            if (check != start) {
                visited.add(check);
            }
            for (int y = 0; y <= 1; y++) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x != 0 || y != 0 || z != 0) {
                            mutablePos.setPos(check.getX() + x, check.getY() + y, check.getZ() + z);
                            if (!visited.contains(mutablePos)) {
                                // Check that the found block matches the original block state, which is wood.
                                if (block == world.getBlockState(mutablePos).getBlock()) {
                                    if (!checking.contains(mutablePos)) {
                                        BlockPos immutablePos = mutablePos.toImmutable();
                                        checking.add(immutablePos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!visited.isEmpty()) {
            Deque<BlockPos> orderedBlocks = visited.stream()
                    .sorted(Comparator.comparingInt(pos -> start.getY() - pos.getY()))
                    .collect(Collectors.toCollection(ArrayDeque::new));
            MinecraftForge.EVENT_BUS.register(new TreeFellingListener(player, tool, orderedBlocks));
        }
    }

    @SubscribeEvent
    public void onWorldTick(@NotNull TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.world == player.world && event.side == Side.SERVER) {
            if (orderedBlocks.isEmpty() || tool.isEmpty()) {
                MinecraftForge.EVENT_BUS.unregister(this);
                return;
            }
            ToolHelper.breakBlockRoutine(player, tool, orderedBlocks.removeLast());
        }
    }
}
