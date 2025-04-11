package gregtech.worldgen.impl;

import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.wood.BlockRubberLog;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class WorldGenRubberTreeBig extends WorldGenAbstractTree {

    public static final WorldGenRubberTreeBig INSTANCE = new WorldGenRubberTreeBig(false);

    protected WorldGenRubberTreeBig(boolean notify) {
        super(notify);
    }
    @Override
    public boolean generate(@NotNull World world, @NotNull Random rand, @NotNull BlockPos pos) {

        IBlockState state = world.getBlockState(pos.add(0,-1,0));
        if (!(state.getMaterial().isSolid() && (state.getBlock() instanceof BlockGrass || state.getBlock() instanceof BlockDirt))) {
            return false; // 返回基座上方位置
        }

        int trunkHeight = rand.nextInt(6) + 12; // 12-18 logs

        final int maxWorldHeight = world.getHeight();
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();

        if (posY <= 1) {
            return false;
        }

        final int topLeafHeight = trunkHeight + 3;
        final int ySpaceRequired = posY + topLeafHeight + 1;

        // check if there is enough room to fit the whole tree
        if (ySpaceRequired >= maxWorldHeight) {
            return false;
        }
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        // ======== 叶子生成部分重构 ========
        // 云杉特征参数
        int leafLayers = 4 + rand.nextInt(2); // 4-6层叶子
        int baseLeafY = posY + trunkHeight - leafLayers; // 叶子起始高度

        // 生成锥形叶子层
        for (int layer = 0; layer < leafLayers; ++layer) {
            int currentRadius = 1 + (leafLayers - layer); // 底层半径最大

            // 每层生成范围
            for (int xOffset = -currentRadius; xOffset <= currentRadius; ++xOffset) {
                for (int zOffset = -currentRadius; zOffset <= currentRadius; ++zOffset) {
                    // 形成圆形轮廓
                    int manhattanDist = Math.abs(xOffset) + Math.abs(zOffset);
                    if (manhattanDist > currentRadius + 1) continue;

                    // 随机稀疏化外层
                    if (manhattanDist == currentRadius + 1 && rand.nextFloat() < 0.5f) continue;

                    // 生成高度计算
                    int yPos = baseLeafY + layer;
                    mutable.setPos(posX + xOffset, yPos, posZ + zOffset);

                    if (isReplaceable(world, mutable)) {
                        setBlockAndNotifyAdequately(world, mutable, MetaBlocks.RUBBER_LEAVES.getDefaultState());
                    }
                }
            }
        }

        // ======== 树干生成优化 ========
        // 生成主干（延伸至叶子顶部）
        IBlockState logState = MetaBlocks.RUBBER_LOG.getDefaultState().withProperty(BlockRubberLog.NATURAL, true);
        for (int y = 0; y < trunkHeight - 1; ++y) { // 延长1格到叶子层
            mutable.setPos(posX, posY + y, posZ);
            if (isReplaceable(world, mutable)) {
                setBlockAndNotifyAdequately(world, mutable, logState);
            }
        }

        // ======== 添加小枝干 ========
        // 在顶部两层生成随机分支
        for (int layer = 0; layer < 2; ++layer) {
            int branchY = posY + trunkHeight - leafLayers - layer;
            EnumFacing[] directions = EnumFacing.HORIZONTALS;
            Collections.shuffle(Arrays.asList(directions), rand);

            // 每个方向生成分支
            for (int i = 0; i < 2; ++i) { // 随机选择2个方向
                EnumFacing dir = directions[i];
                mutable.setPos(posX, branchY, posZ).move(dir);

                if (isReplaceable(world, mutable)) {
                    setBlockAndNotifyAdequately(world, mutable, logState);

                    // 添加末端叶子
                    mutable.move(dir);
                    if (isReplaceable(world, mutable)) {
                        setBlockAndNotifyAdequately(world, mutable, MetaBlocks.RUBBER_LEAVES.getDefaultState());
                    }
                }
            }
        }

        return true;
    }
}
