package gregtech.api.pipes;

import gregtech.api.pipes.net.ConnectionInfo;
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
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class BlockPipe extends Block implements ITileEntityProvider {
    protected ArrayList<PipeNetwork> networks = new ArrayList<>();

    public BlockPipe() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("pipe");
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        System.out.println(this.networks.toString());

        PipeTE tileEntity = (PipeTE) worldIn.getTileEntity(pos);

        if (tileEntity != null) {
            if (tileEntity.hasNode()) {
                System.out.println(tileEntity.getNode().getParent());
            }
        }

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
            PipeNetwork newNet = new PipeNetwork();
            newNet.addNode(pos, tileEntity.getNode());
            networks.add(newNet);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        PipeTE tileEntity = (PipeTE) worldIn.getTileEntity(pos);

        if (tileEntity != null) {
            if (tileEntity.hasNode()) {
                this.networks.remove(tileEntity.getNode().getParent());
                System.out.println(this.networks.toString());
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
