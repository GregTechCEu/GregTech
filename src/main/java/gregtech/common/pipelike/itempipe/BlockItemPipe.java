package gregtech.common.pipelike.itempipe;

import com.google.common.base.Preconditions;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.block.simple.EmptyNodeData;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.type.Material;
import gregtech.common.pipelike.itempipe.net.ItemPipeNet;
import gregtech.common.pipelike.itempipe.net.WorldItemPipeNet;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipeTickable;
import gregtech.common.render.InvPipeRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BlockItemPipe extends BlockMaterialPipe<ItemPipeType, EmptyNodeData, WorldItemPipeNet> {

    static {
        //TickableWorldPipeNetEventHandler.registerTickablePipeNet(WorldItemPipeNet::getWorldPipeNet);
    }

    private final List<Material> enabledMaterials = new ArrayList<>();

    public BlockItemPipe() {
        setHarvestLevel("pickaxe", 1);
    }

    public void addPipeMaterial(Material material) {
        Preconditions.checkNotNull(material, "material");
        Preconditions.checkArgument(Material.MATERIAL_REGISTRY.getNameForObject(material) != null, "material is not registered");
        this.enabledMaterials.add(material);
    }

    @Override
    public TileEntityPipeBase<ItemPipeType, EmptyNodeData> createNewTileEntity(boolean supportsTicking) {
        return supportsTicking ? new TileEntityItemPipeTickable() : new TileEntityItemPipe();
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (!worldIn.isRemote) {
            ItemPipeNet itemPipeNet = getWorldPipeNet(worldIn).getNetFromPos(pos);
            if (itemPipeNet != null) {
                itemPipeNet.nodeNeighbourChanged(pos);
            }
        }
    }

    @Override
    public int getActiveNodeConnections(IBlockAccess world, BlockPos nodePos, IPipeTile<ItemPipeType, EmptyNodeData> selfTileEntity) {
        int activeNodeConnections = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            BlockPos offsetPos = nodePos.offset(side);
            TileEntity tileEntity = world.getTileEntity(offsetPos);
            if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite())) {
                activeNodeConnections |= 1 << side.getIndex();
            }
        }
        return activeNodeConnections;
    }

    @Override
    public Class<ItemPipeType> getPipeTypeClass() {
        return ItemPipeType.class;
    }

    @Override
    protected EmptyNodeData getFallbackType() {
        return EmptyNodeData.INSTANCE;
    }

    @Override
    public WorldItemPipeNet getWorldPipeNet(World world) {
        return WorldItemPipeNet.getWorldPipeNet(world);
    }

    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return InvPipeRenderer.INSTANCE.getParticleTexture((TileEntityItemPipe) world.getTileEntity(blockPos));
    }

    @Override
    protected EmptyNodeData createProperties(ItemPipeType itemPipeType, Material material) {
        return EmptyNodeData.INSTANCE;
    }

    public Collection<Material> getEnabledMaterials() {
        return Collections.unmodifiableList(enabledMaterials);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (Material material : enabledMaterials) {
            for (ItemPipeType fluidPipeType : ItemPipeType.values()) {
                items.add(getItem(fluidPipeType, material));
            }
        }
    }

    @Override
    public ItemPipeType getItemPipeType(ItemStack itemStack) {
        return super.getItemPipeType(itemStack);
    }

    @Override
    protected boolean canPipesConnect(IPipeTile<ItemPipeType, EmptyNodeData> selfTile, EnumFacing side, IPipeTile<ItemPipeType, EmptyNodeData> sideTile) {
        return selfTile.getNodeData().equals(sideTile.getNodeData());
    }

    @Override
    protected int getActiveVisualConnections(IPipeTile<ItemPipeType, EmptyNodeData> selfTile) {
        int activeNodeConnections = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            BlockPos offsetPos = selfTile.getPipePos().offset(side);
            TileEntity tileEntity = selfTile.getPipeWorld().getTileEntity(offsetPos);
            if (tileEntity != null) {
                EnumFacing opposite = side.getOpposite();
                IItemHandler sourceHandler = selfTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
                IItemHandler receivedHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, opposite);
                if (sourceHandler != null && receivedHandler != null) {
                    activeNodeConnections |= 1 << side.getIndex();
                }
            }
        }
        return activeNodeConnections;
    }

    @Override
    protected void onActiveModeChange(World world, BlockPos pos, boolean isActiveNow, boolean isInitialChange) {
        TileEntityItemPipe oldTileEntity = (TileEntityItemPipe) world.getTileEntity(pos);
        if (!(oldTileEntity instanceof TileEntityItemPipeTickable) && isActiveNow) {
            TileEntityItemPipeTickable newTileEntity = new TileEntityItemPipeTickable();
            newTileEntity.transferDataFrom(oldTileEntity);
            newTileEntity.setActive(true);
            world.setTileEntity(pos, newTileEntity);
        } else if (oldTileEntity instanceof TileEntityItemPipeTickable) {
            ((TileEntityItemPipeTickable) oldTileEntity).setActive(isActiveNow);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return InvPipeRenderer.BLOCK_RENDER_TYPE;
    }


}
