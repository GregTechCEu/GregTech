package gregtech.worldgen.impl;

import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.wood.BlockRubberLog;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WorldGenRubberTree extends WorldGenAbstractTree {

    public static final WorldGenRubberTree INSTANCE = new WorldGenRubberTree(false);
    public static final WorldGenRubberTree INSTANCE_NOTIFY = new WorldGenRubberTree(true);

    protected WorldGenRubberTree(boolean notify) {
        super(notify);
    }

    @Override
    public boolean generate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos pos) {
        int trunkHeight = rand.nextInt(3) + 5; // 5-7 logs

        final int maxWorldHeight = world.getHeight();
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();

        if (posY <= 1) {
            return false;
        }

        final int topLeafHeight = trunkHeight + 3;
        final int ySpaceRequired = posY + topLeafHeight + 1;
        final int leafStartY = ySpaceRequired - 2;

        // check if there is enough room to fit the whole tree
        if (ySpaceRequired >= maxWorldHeight) {
            return false;
        }
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = posY; y < ySpaceRequired; y++) {
            int radius;
            if (y == posY) {
                radius = 0;
            } else if (y < leafStartY) {
                radius = 1;
            } else {
                radius = 2;
            }

            final int xLimit = posX + radius;
            final int zLimit = posZ + radius;
            for (int x = posX - radius; x <= xLimit; x++) {
                for (int z = posZ - radius; z < zLimit; z++) {
                    mutable.setPos(x, y, z);
                    if (!isReplaceable(world, mutable)) {
                        return false;
                    }
                }
            }
        }

        // check for valid soil
        mutable.setPos(posX, posY - 1, posZ);
        IBlockState soilState = world.getBlockState(mutable);
        Block soilBlock = soilState.getBlock();
        if (!soilBlock.canSustainPlant(soilState, world, mutable, EnumFacing.UP, MetaBlocks.RUBBER_SAPLING)) {
            return false;
        }

        soilBlock.onPlantGrow(soilState, world, mutable, pos);

        // leaves
        final int leavesOnTrunk = 4;
        final int slimmingPoint = trunkHeight - 2;
        int leafRadius = 2;
        for (int yOffset = trunkHeight - leavesOnTrunk; yOffset < topLeafHeight; yOffset++) {
            if (yOffset == slimmingPoint) {
                leafRadius = 1;
            } else if (yOffset == trunkHeight) {
                leafRadius = 0;
            }

            int y = posY + yOffset;
            for (int xOffset = -leafRadius; xOffset <= leafRadius; xOffset++) {
                int x = posX + xOffset;
                for (int zOffset = -leafRadius; zOffset <= leafRadius; zOffset++) {
                    int z = posZ + zOffset;
                    if (y <= trunkHeight && xOffset == 0 && zOffset == 0) {
                        // skip the trunk
                        continue;
                    }
                    if (leafRadius == 0 || Math.abs(xOffset) < leafRadius || Math.abs(zOffset) < leafRadius ||
                            (yOffset <= slimmingPoint && rand.nextBoolean())) {
                        mutable.setPos(x, y, z);
                        IBlockState existing = world.getBlockState(mutable);
                        if (existing.getBlock().isAir(existing, world, mutable)) {
                            setBlockAndNotifyAdequately(world, mutable, MetaBlocks.RUBBER_LEAVES.getDefaultState());
                        }
                    }
                }
            }
        }

        // trunk
        IBlockState logState = MetaBlocks.RUBBER_LOG.getDefaultState().withProperty(BlockRubberLog.NATURAL, true);
        mutable.setPos(posX, posY, posZ);
        for (int y = 0; y < trunkHeight; y++) {
            mutable.setY(posY + y);
            IBlockState existing = world.getBlockState(mutable);
            if (existing.getBlock().isAir(existing, world, mutable) ||
                    existing.getBlock().isLeaves(existing, world, mutable)) {
                setBlockAndNotifyAdequately(world, mutable, logState);
            }
        }

        return true;
    }
}
