package gregtech.common.pipelike.fluidpipe;

import com.google.common.base.Preconditions;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.MaterialRegistry;
import gregtech.api.unification.material.properties.FluidPipeProperty;
import gregtech.api.unification.material.Material;
import gregtech.common.pipelike.fluidpipe.net.FluidPipeNet;
import gregtech.common.pipelike.fluidpipe.net.WorldFluidPipeNet;
import gregtech.common.pipelike.fluidpipe.tile.FluidPipeFluidHandler;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
import gregtech.common.render.FluidPipeRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public class BlockFluidPipe extends BlockMaterialPipe<FluidPipeType, FluidPipeProperty, WorldFluidPipeNet> {

    private final SortedMap<Material, FluidPipeProperty> enabledMaterials = new TreeMap<>();

    public BlockFluidPipe(FluidPipeType pipeType) {
        super(pipeType);
        setHarvestLevel("wrench", 1);
    }

    public void addPipeMaterial(Material material, FluidPipeProperty fluidPipeProperties) {
        Preconditions.checkNotNull(material, "material");
        Preconditions.checkNotNull(fluidPipeProperties, "fluidPipeProperties");
        Preconditions.checkArgument(MaterialRegistry.MATERIAL_REGISTRY.getNameForObject(material) != null, "material is not registered");
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
    protected FluidPipeProperty createProperties(FluidPipeType fluidPipeType, Material material) {
        return fluidPipeType.modifyProperties(enabledMaterials.getOrDefault(material, getFallbackType()));
    }

    @Override
    protected FluidPipeProperty getFallbackType() {
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
    public boolean canPipesConnect(IPipeTile<FluidPipeType, FluidPipeProperty> selfTile, EnumFacing side, IPipeTile<FluidPipeType, FluidPipeProperty> sideTile) {
        return selfTile.getNodeData().equals(sideTile.getNodeData());
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<FluidPipeType, FluidPipeProperty> selfTile, EnumFacing side, TileEntity tile) {
        return tile != null && tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()) != null;
    }

    /*@Override
    public int getActiveVisualConnections(IPipeTile<FluidPipeType, FluidPipeProperties> selfTile) {
        return selfTile.getBlockedConnections();
        int activeNodeConnections = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            BlockPos offsetPos = selfTile.getPipePos().offset(side);
            TileEntity tileEntity = selfTile.getPipeWorld().getTileEntity(offsetPos);
            if(tileEntity != null) {
                EnumFacing opposite = side.getOpposite();
                IFluidHandler sourceHandler = selfTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
                IFluidHandler receivedHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opposite);
                if (sourceHandler != null && receivedHandler != null) {
                    activeNodeConnections |= 1 << side.getIndex();
                }
            }
        }
        return activeNodeConnections;
    }*/

    /*@Override
    public int getActiveNodeConnections(IBlockAccess world, BlockPos nodePos, IPipeTile<FluidPipeType, FluidPipeProperties> selfTileEntity) {
        return getPipeTileEntity(world, nodePos).getBlockedConnections();
        /*int activeNodeConnections = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            BlockPos offsetPos = nodePos.offset(side);
            TileEntity tileEntity = world.getTileEntity(offsetPos);
            if(tileEntity != null) {
                EnumFacing opposite = side.getOpposite();
                IFluidHandler sourceHandler = selfTileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
                IFluidHandler receivedHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opposite);
                if (sourceHandler != null && receivedHandler != null && canPushIntoFluidHandler(selfTileEntity, tileEntity, sourceHandler, receivedHandler)) {
                    activeNodeConnections |= 1 << side.getIndex();
                }
            }
        }
        return activeNodeConnections;
    }*/

    public boolean canPushIntoFluidHandler(IPipeTile<FluidPipeType, FluidPipeProperty> selfTileEntity, TileEntity otherTileEntity, IFluidHandler sourceHandler, IFluidHandler destinationHandler) {
        boolean isSourcePipe = sourceHandler instanceof FluidPipeFluidHandler;
        boolean isDestPipe = destinationHandler instanceof FluidPipeFluidHandler;
        if(isSourcePipe && isDestPipe) {
            float sourceThickness = selfTileEntity.getPipeType().getThickness();
            IPipeTile<FluidPipeType, FluidPipeProperty> otherPipe = getPipeTileEntity(otherTileEntity);
            if (otherPipe == null) {
                return false;
            }
            float destThickness = otherPipe.getPipeType().getThickness();
            return sourceThickness > destThickness;
        }
        return true;
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (worldIn.isRemote) return;
        if (entityIn instanceof EntityLivingBase && entityIn.world.getWorldTime() % 20 == 0L) {
            EntityLivingBase entityLiving = (EntityLivingBase) entityIn;
            FluidPipeNet pipeNet = getWorldPipeNet(worldIn).getNetFromPos(pos);
            if (pipeNet != null) {
                FluidStack fluidStack = pipeNet.getFluidNetTank().getFluid();
                if (fluidStack == null) {
                    return; //pipe network is empty
                }
                int fluidTemperature = fluidStack.getFluid().getTemperature(fluidStack);
                if (fluidTemperature >= 373) {
                    //100C, temperature of boiling water
                    float damageAmount = (fluidTemperature - 363) / 4.0f;
                    entityLiving.attackEntityFrom(DamageSources.getHeatDamage(), damageAmount);

                } else if (fluidTemperature <= 183) {
                    //-90C, temperature of freezing of most gaseous elements
                    float damageAmount = fluidTemperature / 4.0f;
                    entityLiving.attackEntityFrom(DamageSources.getFrostDamage(), damageAmount);

                }
            }
        }
    }

    @Override
    public TileEntityPipeBase<FluidPipeType, FluidPipeProperty> createNewTileEntity(boolean supportsTicking) {
        return supportsTicking ? new TileEntityFluidPipeTickable() : new TileEntityFluidPipe();
    }

    @Override
    protected void onActiveModeChange(World world, BlockPos pos, boolean isActiveNow, boolean isInitialChange) {
        TileEntityFluidPipe oldTileEntity = (TileEntityFluidPipe) world.getTileEntity(pos);
        if (!(oldTileEntity instanceof TileEntityFluidPipeTickable) && isActiveNow) {
            TileEntityFluidPipeTickable newTileEntity = new TileEntityFluidPipeTickable();
            newTileEntity.transferDataFrom(oldTileEntity);
            newTileEntity.setActive(true);
            world.setTileEntity(pos, newTileEntity);
        } else if (oldTileEntity instanceof TileEntityFluidPipeTickable) {
            ((TileEntityFluidPipeTickable) oldTileEntity).setActive(isActiveNow);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return FluidPipeRenderer.BLOCK_RENDER_TYPE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return FluidPipeRenderer.INSTANCE.getParticleTexture((TileEntityFluidPipe) world.getTileEntity(blockPos));
    }
}
