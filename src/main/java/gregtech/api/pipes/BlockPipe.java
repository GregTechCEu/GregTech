package gregtech.api.pipes;

import gregtech.api.pipes.net.ConnectionInfo;
import gregtech.api.pipes.net.NetworkController;
import gregtech.api.pipes.net.Node;
import gregtech.api.pipes.net.PipeNetwork;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockPipe extends Block implements ITileEntityProvider {
    public BlockPipe() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("pipe");
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        PipeTE tileEntity = (PipeTE) worldIn.getTileEntity(pos);

        if (tileEntity != null) {
            if (tileEntity.hasNode()) {
                System.out.println(tileEntity.getNode().getParent());
            }
        }

        System.out.println(NetworkController.INSTANCE.networks);

        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        PipeTE tileEntity = (PipeTE) worldIn.getTileEntity(pos);

        if (tileEntity != null) {
            // Initializing this like this will, hopefully,
            // be less ugly in the future. For now, spit and glue.
            ConnectionInfo conInf[] = new ConnectionInfo[6];

            tileEntity.setNode(new Node(conInf));
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        PipeTE tileEntity = (PipeTE) worldIn.getTileEntity(pos);
        PipeTE neighborTileEntity = (PipeTE) worldIn.getTileEntity(fromPos);

        if (tileEntity != null && neighborTileEntity != null) {
            // Set network to be shared
            neighborTileEntity.getNode().setParent(tileEntity.getNode().getParent());
            tileEntity.getNode().getParent().addNode(fromPos, neighborTileEntity.getNode());
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        PipeTE tileEntity = (PipeTE) worldIn.getTileEntity(pos);

        if (tileEntity != null) {
            if (tileEntity.hasNode()) {
                // Remove this entities node from its network.
                tileEntity.getNode().getParent().removeNode(pos);
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new PipeTE();
    }
}
