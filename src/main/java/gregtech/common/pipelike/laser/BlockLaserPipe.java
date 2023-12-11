package gregtech.common.pipelike.laser;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.client.renderer.pipe.LaserPipeRenderer;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.pipelike.laser.net.WorldLaserPipeNet;
import gregtech.common.pipelike.laser.tile.TileEntityLaserPipe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockLaserPipe extends BlockPipe<LaserPipeType, LaserPipeProperties, WorldLaserPipeNet> {

    private final LaserPipeType pipeType;
    private final LaserPipeProperties properties;

    public BlockLaserPipe(@NotNull LaserPipeType pipeType) {
        this.pipeType = pipeType;
        this.properties = LaserPipeProperties.INSTANCE;
        setCreativeTab(GregTechAPI.TAB_GREGTECH_PIPES);
        setHarvestLevel(ToolClasses.WIRE_CUTTER, 1);
    }

    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return LaserPipeRenderer.INSTANCE.getParticleTexture((TileEntityLaserPipe) world.getTileEntity(blockPos));
    }

    @Override
    public Class<LaserPipeType> getPipeTypeClass() {
        return LaserPipeType.class;
    }

    @Override
    public WorldLaserPipeNet getWorldPipeNet(World world) {
        return WorldLaserPipeNet.getWorldPipeNet(world);
    }

    @Override
    public TileEntityPipeBase<LaserPipeType, LaserPipeProperties> createNewTileEntity(boolean supportsTicking) {
        return new TileEntityLaserPipe();
    }

    @Override
    public LaserPipeProperties createProperties(IPipeTile<LaserPipeType, LaserPipeProperties> pipeTile) {
        LaserPipeType pipeType = pipeTile.getPipeType();
        if (pipeType == null) return getFallbackType();
        return this.pipeType.modifyProperties(properties);
    }

    @Override
    public LaserPipeProperties createItemProperties(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemBlockLaserPipe pipe) {
            return ((BlockLaserPipe) pipe.getBlock()).properties;
        }
        return null;
    }

    @Override
    public ItemStack getDropItem(IPipeTile<LaserPipeType, LaserPipeProperties> pipeTile) {
        return new ItemStack(this, 1, pipeType.ordinal());
    }

    @Override
    protected LaserPipeProperties getFallbackType() {
        return LaserPipeProperties.INSTANCE;
    }

    @Override
    public LaserPipeType getItemPipeType(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemBlockLaserPipe pipe) {
            return ((BlockLaserPipe) pipe.getBlock()).pipeType;
        }
        return null;
    }

    @Override
    public void setTileEntityData(TileEntityPipeBase<LaserPipeType, LaserPipeProperties> pipeTile,
                                  ItemStack itemStack) {
        pipeTile.setPipeData(this, pipeType);
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs itemIn, @NotNull NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, this.pipeType.ordinal()));
    }

    @Override
    protected boolean isPipeTool(@NotNull ItemStack stack) {
        return ToolHelper.isTool(stack, ToolClasses.WIRE_CUTTER);
    }

    @Override
    public boolean canPipesConnect(IPipeTile<LaserPipeType, LaserPipeProperties> selfTile, EnumFacing side,
                                   IPipeTile<LaserPipeType, LaserPipeProperties> sideTile) {
        return selfTile instanceof TileEntityLaserPipe && sideTile instanceof TileEntityLaserPipe;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<LaserPipeType, LaserPipeProperties> selfTile, EnumFacing side,
                                         @Nullable TileEntity tile) {
        return tile != null &&
                tile.getCapability(GregtechTileCapabilities.CAPABILITY_LASER, side.getOpposite()) != null;
    }

    @Override
    public boolean isHoldingPipe(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        ItemStack stack = player.getHeldItemMainhand();
        return stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlockLaserPipe;
    }

    @Override
    @NotNull
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return LaserPipeRenderer.INSTANCE.getBlockRenderType();
    }

    @Override
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        if (layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.CUTOUT) return true;
        return layer == BloomEffectUtil.getEffectiveBloomLayer();
    }
}
