package gregtech.common.pipelike.fluidpipe;

import com.google.common.base.Preconditions;
import gregtech.api.GregTechAPI;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.pipenet.PipeGatherer;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.common.advancement.GTTriggers;
import gregtech.common.pipelike.fluidpipe.net.FluidPipeNet;
import gregtech.common.pipelike.fluidpipe.net.WorldFluidPipeNet;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
import gregtech.common.render.FluidPipeRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public class BlockFluidPipe extends BlockMaterialPipe<FluidPipeType, FluidPipeProperties, WorldFluidPipeNet> {

    private final SortedMap<Material, FluidPipeProperties> enabledMaterials = new TreeMap<>();

    public BlockFluidPipe(FluidPipeType pipeType) {
        super(pipeType);
        setHarvestLevel("wrench", 1);
    }

    public void addPipeMaterial(Material material, FluidPipeProperties fluidPipeProperties) {
        Preconditions.checkNotNull(material, "material");
        Preconditions.checkNotNull(fluidPipeProperties, "fluidPipeProperties");
        Preconditions.checkArgument(GregTechAPI.MATERIAL_REGISTRY.getNameForObject(material) != null, "material is not registered");
        this.enabledMaterials.put(material, fluidPipeProperties);
    }

    public Collection<Material> getEnabledMaterials() {
        return Collections.unmodifiableSet(enabledMaterials.keySet());
    }

    @Override
    public Class<FluidPipeType> getPipeTypeClass() {
        return FluidPipeType.class;
    }

    @Override
    public WorldFluidPipeNet getWorldPipeNet(World world) {
        return WorldFluidPipeNet.getWorldPipeNet(world);
    }

    @Override
    protected FluidPipeProperties createProperties(FluidPipeType fluidPipeType, Material material) {
        return fluidPipeType.modifyProperties(enabledMaterials.getOrDefault(material, getFallbackType()));
    }

    @Override
    protected FluidPipeProperties getFallbackType() {
        return enabledMaterials.values().iterator().next();
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (Material material : enabledMaterials.keySet()) {
            for (FluidPipeType fluidPipeType : FluidPipeType.values()) {
                if (!fluidPipeType.getOrePrefix().isIgnored(material)) {
                    items.add(getItem(material));
                }
            }
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (!worldIn.isRemote) {
            FluidPipeNet itemPipeNet = getWorldPipeNet(worldIn).getNetFromPos(pos);
            if (itemPipeNet != null) {
                itemPipeNet.nodeNeighbourChanged(pos);
            }
        }
    }

    @Override
    public boolean canPipesConnect(IPipeTile<FluidPipeType, FluidPipeProperties> selfTile, EnumFacing side, IPipeTile<FluidPipeType, FluidPipeProperties> sideTile) {
        return selfTile instanceof TileEntityFluidPipe && sideTile instanceof TileEntityFluidPipe;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<FluidPipeType, FluidPipeProperties> selfTile, EnumFacing side, TileEntity tile) {
        return tile != null && tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()) != null;
    }

    @Override
    public void onEntityCollision(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entityIn) {
        if (worldIn.isRemote) return;
        if (entityIn instanceof EntityLivingBase && entityIn.world.getWorldTime() % 20 == 0L) {
            EntityLivingBase entityLiving = (EntityLivingBase) entityIn;
            FluidPipeNet pipeNet = getWorldPipeNet(worldIn).getNetFromPos(pos);
            if (pipeNet == null) return;
            TileEntityFluidPipeTickable pipeTile = (TileEntityFluidPipeTickable) PipeGatherer.findFirstMatching(pipeNet, worldIn, pos, pipe -> pipe instanceof TileEntityFluidPipeTickable);
            if (pipeTile == null || pipeTile.getNodeData().tanks > 1) return;
            FluidStack fluidStack = pipeTile.getContainedFluid(0);
            if (fluidStack == null)
                return; //pipe network is empty
            int fluidTemperature = fluidStack.getFluid().getTemperature(fluidStack);
            boolean wasDamaged = false;
            if (fluidTemperature >= 373) {
                //100C, temperature of boiling water
                float damageAmount = (fluidTemperature - 363) / 4.0f;
                entityLiving.attackEntityFrom(DamageSources.getHeatDamage(), damageAmount);
                wasDamaged = true;

            } else if (fluidTemperature <= 183) {
                //-90C, temperature of freezing of most gaseous elements
                float damageAmount = fluidTemperature / 4.0f;
                entityLiving.attackEntityFrom(DamageSources.getFrostDamage(), damageAmount);
                wasDamaged = true;
            }
            if (wasDamaged && entityLiving instanceof EntityPlayerMP) {
                GTTriggers.FLUID_PIPE_DEATH.trigger((EntityPlayerMP) entityLiving);
            }
        }
    }

    @Override
    public TileEntityPipeBase<FluidPipeType, FluidPipeProperties> createNewTileEntity(boolean supportsTicking) {
        return supportsTicking ? new TileEntityFluidPipeTickable() : new TileEntityFluidPipe();
    }

    @Nonnull
    @SuppressWarnings("deprecation")
    @Override
    public int getVisualConnections(IPipeTile<FluidPipeType, FluidPipeProperties> selfTile) {
        int connections = selfTile.getOpenConnections();
        float selfTHICCness = selfTile.getPipeType().getThickness();
        for (EnumFacing facing : EnumFacing.values()) {
            CoverBehavior cover = selfTile.getCoverableImplementation().getCoverAtSide(facing);
            if (cover != null) {
                // adds side to open connections of it isn't already open & has a cover
                connections |= 1 << facing.getIndex();
                continue;
            }
            // check if neighbour is a smaller item pipe
            TileEntity neighbourTile = selfTile.getPipeWorld().getTileEntity(selfTile.getPipePos().offset(facing));
            if (neighbourTile instanceof TileEntityFluidPipe &&
                    ((TileEntityFluidPipe) neighbourTile).isConnectionOpenAny(facing.getOpposite()) &&
                    ((TileEntityFluidPipe) neighbourTile).getPipeType().getThickness() < selfTHICCness) {
                connections |= 1 << (facing.getIndex() + 6);
            }
        }
        return connections;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return FluidPipeRenderer.BLOCK_RENDER_TYPE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return FluidPipeRenderer.INSTANCE.getParticleTexture((TileEntityFluidPipe) world.getTileEntity(blockPos));
    }
}
