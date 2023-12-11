package gregtech.api.pipenet.longdist;

import gregtech.api.GregTechAPI;
import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockLongDistancePipe extends Block implements ILDNetworkPart {

    private final LongDistancePipeType pipeType;

    public BlockLongDistancePipe(LongDistancePipeType pipeType) {
        super(Material.IRON);
        this.pipeType = pipeType;
        setTranslationKey("long_distance_" + pipeType.getName() + "_pipeline");
        setCreativeTab(GregTechAPI.TAB_GREGTECH);
        setHarvestLevel(ToolClasses.WRENCH, 1);
    }

    @Override
    public void onBlockPlacedBy(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                @NotNull EntityLivingBase placer, @NotNull ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (worldIn.isRemote) return;
        // first find all neighbouring networks
        List<LongDistanceNetwork> networks = new ArrayList<>();
        BlockPos.PooledMutableBlockPos offsetPos = BlockPos.PooledMutableBlockPos.retain();
        for (EnumFacing facing : EnumFacing.VALUES) {
            offsetPos.setPos(pos).move(facing);
            IBlockState neighborState = worldIn.getBlockState(offsetPos);
            ILDNetworkPart networkPart = ILDNetworkPart.tryGet(worldIn, offsetPos, neighborState);
            if (networkPart != null && networkPart.getPipeType() == getPipeType()) {
                // neighbor is a valid pipe block
                LongDistanceNetwork network = LongDistanceNetwork.get(worldIn, offsetPos);
                if (network == null) {
                    // if for some reason there is not a network at the neighbor, create one
                    network = networkPart.getPipeType().createNetwork(worldIn);
                    network.recalculateNetwork(Collections.singleton(offsetPos.toImmutable()));
                    return;
                }
                if (!network.getPipeType().isValidPart(networkPart)) {
                    throw new IllegalStateException("NetworkPart " + networkPart + " pipeType " +
                            network.getPipeType() + " is not valid for network type " + network.getPipeType());
                }
                ILDEndpoint endpoint = ILDEndpoint.tryGet(worldIn, offsetPos);
                // only count the network as connected if it's not an endpoint or the endpoints input or output face is
                // connected
                if (endpoint == null || endpoint.getFrontFacing().getAxis() == facing.getAxis()) {
                    networks.add(network);
                }
            }
        }
        offsetPos.release();
        if (networks.isEmpty()) {
            // create network
            LongDistanceNetwork network = this.pipeType.createNetwork(worldIn);
            network.onPlacePipe(pos);
        } else if (networks.size() == 1) {
            // add to connected network
            networks.get(0).onPlacePipe(pos);
        } else {
            // merge all connected networks together
            LongDistanceNetwork main = networks.get(0);
            main.onPlacePipe(pos);
            networks.remove(0);
            for (LongDistanceNetwork network : networks) {
                main.mergePipeNet(network);
            }
        }
    }

    @Override
    public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if (worldIn.isRemote) return;
        LongDistanceNetwork network = LongDistanceNetwork.get(worldIn, pos);
        if (network != null) {
            network.onRemovePipe(pos);
        }
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs itemIn, @NotNull NonNullList<ItemStack> items) {
        if (itemIn == GregTechAPI.TAB_GREGTECH) {
            items.add(new ItemStack(this));
        }
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(I18n.format("gregtech.block.tooltip.no_mob_spawning"));
    }

    public @NotNull LongDistancePipeType getPipeType() {
        return pipeType;
    }
}
