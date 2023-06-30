package gregtech.common.pipelike.optical;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.ICoverable;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.OpticalPipeRenderer;
import gregtech.common.pipelike.optical.net.WorldOpticalPipeNet;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
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
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockOpticalPipe extends BlockPipe<OpticalPipeType, OpticalPipeProperties, WorldOpticalPipeNet> {

    private final OpticalPipeType pipeType;
    private final OpticalPipeProperties properties;

    public BlockOpticalPipe(@Nonnull OpticalPipeType pipeType) {
        this.pipeType = pipeType;
        this.properties = new OpticalPipeProperties();
        setCreativeTab(GregTechAPI.TAB_GREGTECH_PIPES);
        setHarvestLevel(ToolClasses.WIRE_CUTTER, 1);
    }

    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(@Nonnull World world, BlockPos blockPos) {
        return OpticalPipeRenderer.INSTANCE.getParticleTexture((TileEntityOpticalPipe) world.getTileEntity(blockPos));
    }

    @Override
    public Class<OpticalPipeType> getPipeTypeClass() {
        return OpticalPipeType.class;
    }

    @Override
    public WorldOpticalPipeNet getWorldPipeNet(World world) {
        return WorldOpticalPipeNet.getWorldPipeNet(world);
    }

    @Override
    public TileEntityPipeBase<OpticalPipeType, OpticalPipeProperties> createNewTileEntity(boolean supportsTicking) {
        return new TileEntityOpticalPipe();
    }

    @Override
    public OpticalPipeProperties createProperties(@Nonnull IPipeTile<OpticalPipeType, OpticalPipeProperties> pipeTile) {
        OpticalPipeType pipeType = pipeTile.getPipeType();
        if (pipeType == null) return getFallbackType();
        return this.pipeType.modifyProperties(properties);
    }

    @Override
    public OpticalPipeProperties createItemProperties(@Nonnull ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemBlockOpticalPipe pipe) {
            return ((BlockOpticalPipe) pipe.getBlock()).properties;
        }
        return null;
    }

    @Override
    public ItemStack getDropItem(IPipeTile<OpticalPipeType, OpticalPipeProperties> pipeTile) {
        return new ItemStack(this, 1, pipeType.ordinal());
    }

    @Override
    protected OpticalPipeProperties getFallbackType() {
        return new OpticalPipeProperties();
    }

    @Override
    public OpticalPipeType getItemPipeType(@Nonnull ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemBlockOpticalPipe pipe) {
            return ((BlockOpticalPipe) pipe.getBlock()).pipeType;
        }
        return null;
    }

    @Override
    public void setTileEntityData(@Nonnull TileEntityPipeBase<OpticalPipeType, OpticalPipeProperties> pipeTile, ItemStack itemStack) {
        pipeTile.setPipeData(this, pipeType);
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, this.pipeType.ordinal()));
    }

    @Override
    protected boolean isPipeTool(@Nonnull ItemStack stack) {
        return ToolHelper.isTool(stack, ToolClasses.WIRE_CUTTER);
    }

    @Override
    public boolean canPipesConnect(IPipeTile<OpticalPipeType, OpticalPipeProperties> selfTile, EnumFacing side, IPipeTile<OpticalPipeType, OpticalPipeProperties> sideTile) {
        return selfTile instanceof TileEntityOpticalPipe && sideTile instanceof TileEntityOpticalPipe;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<OpticalPipeType, OpticalPipeProperties> selfTile, EnumFacing side, @Nullable TileEntity tile) {
        return tile != null && tile.getCapability(GregtechTileCapabilities.CAPABILITY_DATA_ACCESS, side.getOpposite()) != null;
    }

    @Override
    public boolean isHoldingPipe(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        ItemStack stack = player.getHeldItemMainhand();
        return stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlockOpticalPipe;
    }

    @Override
    public boolean hasPipeCollisionChangingItem(IBlockAccess world, BlockPos pos, ItemStack stack) {
        return ToolHelper.isTool(stack, ToolClasses.WIRE_CUTTER) ||
                GTUtility.isCoverBehaviorItem(stack, () -> hasCover(getPipeTileEntity(world, pos)),
                        coverDef -> ICoverable.canPlaceCover(coverDef, getPipeTileEntity(world, pos).getCoverableImplementation()));
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return OpticalPipeRenderer.INSTANCE.getBlockRenderType();
    }
}
