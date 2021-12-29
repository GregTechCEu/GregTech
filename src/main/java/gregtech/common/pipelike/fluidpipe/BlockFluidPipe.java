package gregtech.common.pipelike.fluidpipe;

import com.google.common.base.Preconditions;
import gregtech.api.GregTechAPI;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.client.renderer.pipe.FluidPipeRenderer;
import gregtech.common.advancement.GTTriggers;
import gregtech.common.pipelike.fluidpipe.net.FluidPipeNet;
import gregtech.common.pipelike.fluidpipe.net.WorldFluidPipeNet;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;

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
    public void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items) {
        for (Material material : enabledMaterials.keySet()) {
            for (FluidPipeType fluidPipeType : FluidPipeType.values()) {
                if (!fluidPipeType.getOrePrefix().isIgnored(material)) {
                    items.add(getItem(material));
                }
            }
        }
    }

    /*@Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TileEntityFluidPipe pipeTile = (TileEntityFluidPipe) getPipeTileEntity(worldIn, pos);
        if (pipeTile != null && !worldIn.isRemote) {
            pipeTile.checkNeighbours();
        }
    }*/

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        FluidPipeNet net = getWorldPipeNet(worldIn).getNetFromPos(pos);
        TileEntityFluidPipe pipe = (TileEntityFluidPipe) getPipeTileEntity(worldIn, pos);
        // get and remove all fluids of the pipe from the net

    }

    /*@Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (!worldIn.isRemote) {
            TileEntityFluidPipe pipe = (TileEntityFluidPipe) getPipeTileEntity(worldIn, pos);
            if (pipe != null)
                pipe.checkNeighbours();
        }
    }*/

    @Override
    public boolean canPipesConnect(IPipeTile<FluidPipeType, FluidPipeProperties> selfTile, EnumFacing side, IPipeTile<FluidPipeType, FluidPipeProperties> sideTile) {
        if (selfTile instanceof TileEntityFluidPipe && sideTile instanceof TileEntityFluidPipe) {
            TileEntityFluidPipe selfPipe = (TileEntityFluidPipe) selfTile, sidePipe = (TileEntityFluidPipe) selfTile;
            // yes if one pipe is empty
            //if(selfPipe.areTanksEmpty() || sidePipe.areTanksEmpty())
            //    return true;
            // get content of one pipe
            Set<FluidStack> fluids = new HashSet<>();
            for (FluidTank tank : selfPipe.getFluidTanks()) {
                FluidStack fluid = tank.getFluid();
                if (fluid != null && fluid.amount > 0) {
                    fluids.add(fluid);
                }
            }
            // if a fluid of the side pipe is not in this pipe return false
            for (FluidTank tank : sidePipe.getFluidTanks()) {
                FluidStack fluid = tank.getFluid();
                if (fluid != null && fluid.amount > 0 && !fluids.contains(fluid)) {
                    return false;
                }
            }
            return true;
        }
        return false;
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
            TileEntityFluidPipe pipe = (TileEntityFluidPipe) getPipeTileEntity(worldIn, pos);
            List<Integer> temps = new ArrayList<>();
            for (FluidTank tank : pipe.getFluidTanks()) {
                if (tank.getFluid() != null && tank.getFluid().amount > 0) {
                    temps.add(tank.getFluid().getFluid().getTemperature(tank.getFluid()));
                }
            }
            if (temps.size() == 0)
                return;
            float fluidTemperature = (float) temps.stream().mapToInt(i -> i).average().getAsDouble();
            if (fluidTemperature >= 373) {
                //100C, temperature of boiling water
                float damageAmount = (fluidTemperature - 363) / 4.0f;
                entityLiving.attackEntityFrom(DamageSources.getHeatDamage(), damageAmount);
                if (entityLiving instanceof EntityPlayerMP)
                    GTTriggers.FLUID_PIPE_DEATH_HEAT.trigger((EntityPlayerMP) entityLiving);

            } else if (fluidTemperature <= 183) {
                //-90C, temperature of freezing of most gaseous elements
                float damageAmount = fluidTemperature / 4.0f;
                entityLiving.attackEntityFrom(DamageSources.getFrostDamage(), damageAmount);
                if (entityLiving instanceof EntityPlayerMP)
                    GTTriggers.FLUID_PIPE_DEATH_COLD.trigger((EntityPlayerMP) entityLiving);
            }
        }
    }

    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        boolean b = super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);

        if (!worldIn.isRemote) {
            TileEntityFluidPipe pipe = (TileEntityFluidPipe) getPipeTileEntity(worldIn, pos);
            FluidTank[] tanks = pipe.getFluidTanks();
            StringBuilder builder = new StringBuilder().append("Content: ");
            for (FluidTank tank : tanks) {
                FluidStack fluid = tank.getFluid();
                if (fluid == null)
                    builder.append("-, ");
                else
                    builder.append(fluid.getFluid().getName()).append(" * ").append(fluid.amount).append(", ");
            }
            builder.delete(builder.length() - 2, builder.length());
            playerIn.sendMessage(new TextComponentString(builder.toString()));
        }


        return b;
    }

    @Override
    public TileEntityPipeBase<FluidPipeType, FluidPipeProperties> createNewTileEntity(boolean supportsTicking) {
        return supportsTicking ? new TileEntityFluidPipeTickable() : new TileEntityFluidPipe();
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return FluidPipeRenderer.BLOCK_RENDER_TYPE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return FluidPipeRenderer.INSTANCE.getParticleTexture((TileEntityFluidPipe) world.getTileEntity(blockPos));
    }
}
