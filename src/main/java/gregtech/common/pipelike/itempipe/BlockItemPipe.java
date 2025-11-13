package gregtech.common.pipelike.itempipe;

import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.client.renderer.pipe.ItemPipeRenderer;
import gregtech.client.renderer.pipe.PipeRenderer;
import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.common.pipelike.itempipe.net.WorldItemPipeNet;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipeTickable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class BlockItemPipe extends BlockMaterialPipe<ItemPipeType, ItemPipeProperties, WorldItemPipeNet> {

    public BlockItemPipe(ItemPipeType itemPipeType, MaterialRegistry registry) {
        super(itemPipeType, registry);
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_PIPES);
        setHarvestLevel(ToolClasses.WRENCH, 1);
    }

    @Override
    public TileEntityPipeBase<ItemPipeType, ItemPipeProperties> createNewTileEntity(boolean supportsTicking) {
        return supportsTicking ? new TileEntityItemPipeTickable() : new TileEntityItemPipe();
    }

    @Override
    public Class<ItemPipeType> getPipeTypeClass() {
        return ItemPipeType.class;
    }

    @Override
    public WorldItemPipeNet getWorldPipeNet(World world) {
        return WorldItemPipeNet.getWorldPipeNet(world);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return ItemPipeRenderer.INSTANCE.getParticleTexture((TileEntityItemPipe) world.getTileEntity(blockPos));
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    public PipeRenderer getPipeRenderer() {
        return ItemPipeRenderer.INSTANCE;
    }

    @Override
    public boolean canPipesConnect(IPipeTile<ItemPipeType, ItemPipeProperties> selfTile, EnumFacing side,
                                   IPipeTile<ItemPipeType, ItemPipeProperties> sideTile) {
        return selfTile instanceof TileEntityItemPipe && sideTile instanceof TileEntityItemPipe;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<ItemPipeType, ItemPipeProperties> selfTile, EnumFacing side,
                                         TileEntity tile) {
        return tile != null &&
                tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()) != null;
    }

    @Override
    public boolean isHoldingPipe(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        ItemStack stack = player.getHeldItemMainhand();
        return stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlockItemPipe;
    }

    @Override
    @NotNull
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return ItemPipeRenderer.INSTANCE.getBlockRenderType();
    }
}
