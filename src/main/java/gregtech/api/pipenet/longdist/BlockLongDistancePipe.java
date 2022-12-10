package gregtech.api.pipenet.longdist;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BlockLongDistancePipe extends Block {

    private final LongDistancePipeType pipeType;

    public BlockLongDistancePipe(LongDistancePipeType pipeType) {
        super(Material.IRON);
        this.pipeType = pipeType;
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (worldIn.isRemote) return;
        List<LongDistanceNetwork> networks = new ArrayList<>();
        BlockPos.PooledMutableBlockPos offsetPos = BlockPos.PooledMutableBlockPos.retain();
        for (EnumFacing facing : EnumFacing.VALUES) {
            offsetPos.setPos(pos).move(facing);
            LongDistanceNetwork network = LongDistanceNetwork.get(worldIn, offsetPos);
            if (network != null && pipeType == network.getPipeType()) {
                networks.add(network);
            }
        }
        offsetPos.release();
        if (networks.isEmpty()) {
            LongDistanceNetwork network = this.pipeType.createNetwork(worldIn);
            network.onPlacePipe(pos);
        } else if (networks.size() == 1) {
            networks.get(0).onPlacePipe(pos);
        } else {
            LongDistanceNetwork main = networks.get(0);
            main.onPlacePipe(pos);
            networks.remove(0);
            for (LongDistanceNetwork network : networks) {
                main.mergePipeNet(network);
            }
        }
    }

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if (worldIn.isRemote) return;
        LongDistanceNetwork network = LongDistanceNetwork.get(worldIn, pos);
        network.onRemovePipe(pos);
    }
}
