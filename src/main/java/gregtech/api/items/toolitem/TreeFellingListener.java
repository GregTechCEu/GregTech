package gregtech.api.items.toolitem;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TreeFellingListener {

    private final EntityPlayerMP player;
    private final ItemStack tool;
    private final Deque<BlockPos> orderedBlocks;
    private final BlockPos samplePos;
    private final int minY;

    private int minX, maxX, minZ, maxZ;
    private boolean purgeLeaves;
    private Block targetLeaves;
    private Iterator<BlockPos.MutableBlockPos> leavesToPurge;

    private TreeFellingListener(EntityPlayerMP player, ItemStack tool, Deque<BlockPos> orderedBlocks) {
        this.player = player;
        this.tool = tool;
        this.orderedBlocks = orderedBlocks;
        this.samplePos = orderedBlocks.getFirst();
        this.minY = orderedBlocks.getLast().getY();
        this.minX = this.maxX = this.samplePos.getX();
        this.minZ = this.maxZ = this.samplePos.getZ();
    }

    public static void start(@Nonnull IBlockState state, ItemStack tool, BlockPos start, @Nonnull EntityPlayerMP player) {
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
    public void onWorldTick(@Nonnull TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.world == player.world) {
            if (purgeLeaves) {
                if (targetLeaves == null) {
                    targetLeaves = Arrays.stream(EnumFacing.VALUES)
                            .map(facing -> player.world.getBlockState(this.samplePos).getBlock())
                            // Cannot use fastutil map::new here as setValue throws UOE
                            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                            .entrySet()
                            .stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(Blocks.AIR);
                    BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(this.samplePos);
                    int topY = mutablePos.getY();
                    int tries = 2;
                    while (tries > 0) {
                        IBlockState state;
                        do {
                            topY = mutablePos.getY() + 1;
                            mutablePos.setY(topY);
                        } while (targetLeaves == Blocks.AIR ?
                                (state = player.world.getBlockState(mutablePos)).getBlock().isLeaves(state, player.world, mutablePos) :
                                player.world.getBlockState(mutablePos).getBlock() == targetLeaves);
                        tries--;
                    }
                    int offsetMinX = 3;
                    int offsetMaxX = 3;
                    int offsetMinZ = 3;
                    int offsetMaxZ = 3;
                    for (BlockPos.MutableBlockPos check : BlockPos.getAllInBoxMutable(this.minX - offsetMinX, this.minY, this.minZ - offsetMinZ, this.maxX + offsetMaxX, this.minY, this.maxZ + offsetMaxZ)) {
                        if (check.getX() == this.samplePos.getX() && check.getZ() == this.samplePos.getZ()) {
                            continue;
                        }
                        if (player.world.getBlockState(check).getBlock().isWood(player.world, check)) {
                            int diff = this.samplePos.getX() - check.getX();
                            if (diff > 0 && diff < offsetMaxX) {
                                offsetMaxX = diff;
                            } else if (Math.abs(diff) < offsetMinX) {
                                offsetMinX = Math.abs(diff);
                            }
                            diff = this.samplePos.getZ() - check.getZ();
                            if (diff > 0 && diff < offsetMaxZ) {
                                offsetMaxZ = diff;
                            } else if (Math.abs(diff) < offsetMinZ) {
                                offsetMinZ = Math.abs(diff);
                            }
                        }
                    }
                    leavesToPurge = BlockPos.getAllInBoxMutable(this.minX - offsetMinX, this.minY, this.minZ - offsetMinZ, this.maxX + offsetMaxX, topY, this.maxZ + offsetMaxZ).iterator();
                    return;
                }
                while (leavesToPurge.hasNext()) {
                    BlockPos.MutableBlockPos check = leavesToPurge.next();
                    IBlockState state = player.world.getBlockState(check);
                    if (targetLeaves == Blocks.AIR ? state.getBlock().isLeaves(state, player.world, check) : state.getBlock() == targetLeaves) {
                        state.getBlock().dropBlockAsItem(player.world, check, state, 0);
                        player.world.setBlockToAir(check);
                    }
                }
                MinecraftForge.EVENT_BUS.unregister(this);
                return;
            }
            if (tool.isEmpty()) {
                MinecraftForge.EVENT_BUS.unregister(this);
                return;
            }
            if (!orderedBlocks.isEmpty()) {
                BlockPos posToBreak = orderedBlocks.removeLast();
                int x = posToBreak.getX();
                if (x > this.maxX) {
                    this.maxX = x;
                } else if (x < this.minX) {
                    this.minX = x;
                }
                int z = posToBreak.getZ();
                if (z > this.maxZ) {
                    this.maxZ = z;
                } else if (z < this.minZ) {
                    this.minZ = z;
                }
                if (!ToolHelper.breakBlockRoutine(player, tool, posToBreak)) {
                    purgeLeaves = true;
                }
            } else {
                purgeLeaves = true;
            }
        }
    }
}
