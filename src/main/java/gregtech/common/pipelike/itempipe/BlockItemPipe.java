package gregtech.common.pipelike.itempipe;

import com.google.common.base.Preconditions;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.MaterialRegistry;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.common.pipelike.itempipe.net.ItemPipeNet;
import gregtech.common.pipelike.itempipe.net.WorldItemPipeNet;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipeTickable;
import gregtech.common.render.ItemPipeRenderer;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BlockItemPipe extends BlockMaterialPipe<ItemPipeType, ItemPipeProperties, WorldItemPipeNet> {

    private final Map<Material, ItemPipeProperties> enabledMaterials = new HashMap<>();

    public BlockItemPipe(ItemPipeType itemPipeType) {
        super(itemPipeType);
        setHarvestLevel("pickaxe", 1);
    }

    public void addPipeMaterial(Material material, ItemPipeProperties properties) {
        Preconditions.checkNotNull(material, "material");
        Preconditions.checkNotNull(properties, "itemPipeProperties");
        Preconditions.checkArgument(MaterialRegistry.MATERIAL_REGISTRY.getNameForObject(material) != null, "material is not registered");
        this.enabledMaterials.put(material, properties);
    }

    @Override
    public TileEntityPipeBase<ItemPipeType, ItemPipeProperties> createNewTileEntity(boolean supportsTicking) {
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

    /*@Override
    public int getActiveNodeConnections(IBlockAccess world, BlockPos nodePos, IPipeTile<ItemPipeType, ItemPipeProperties> selfTileEntity) {
        return getPipeTileEntity(world, nodePos).getBlockedConnections();
        /*int activeNodeConnections = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            BlockPos offsetPos = nodePos.offset(side);
            TileEntity tileEntity = world.getTileEntity(offsetPos);
            if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite())) {
                activeNodeConnections |= 1 << side.getIndex();
            }
        }
        return activeNodeConnections;
    }*/

    @Override
    public Class<ItemPipeType> getPipeTypeClass() {
        return ItemPipeType.class;
    }

    @Override
    protected ItemPipeProperties getFallbackType() {
        return enabledMaterials.values().iterator().next();
    }

    @Override
    public WorldItemPipeNet getWorldPipeNet(World world) {
        return WorldItemPipeNet.getWorldPipeNet(world);
    }

    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return ItemPipeRenderer.INSTANCE.getParticleTexture((TileEntityItemPipe) world.getTileEntity(blockPos));
    }

    @Override
    protected ItemPipeProperties createProperties(ItemPipeType itemPipeType, Material material) {
        return itemPipeType.modifyProperties(enabledMaterials.getOrDefault(material, getFallbackType()));
    }

    public Collection<Material> getEnabledMaterials() {
        return Collections.unmodifiableSet(enabledMaterials.keySet());
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (Material material : enabledMaterials.keySet()) {
            for (ItemPipeType itemPipeType : ItemPipeType.values()) {
                if (!itemPipeType.getOrePrefix().isIgnored(material)) {
                    items.add(getItem(material));
                }
            }
        }
    }

    @Override
    public ItemPipeType getItemPipeType(ItemStack itemStack) {
        return super.getItemPipeType(itemStack);
    }

    @Override
    public boolean canPipesConnect(IPipeTile<ItemPipeType, ItemPipeProperties> selfTile, EnumFacing side, IPipeTile<ItemPipeType, ItemPipeProperties> sideTile) {
        return selfTile instanceof TileEntityItemPipe && sideTile instanceof TileEntityItemPipe;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<ItemPipeType, ItemPipeProperties> selfTile, EnumFacing side, TileEntity tile) {
        return tile != null && tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()) != null;
    }

    /*@Override
    public int getActiveVisualConnections(IPipeTile<ItemPipeType, ItemPipeProperties> selfTile) {
        return selfTile.getBlockedConnections();
        /*int activeNodeConnections = 0;
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
    }*/

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

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return ItemPipeRenderer.BLOCK_RENDER_TYPE;
    }


}
