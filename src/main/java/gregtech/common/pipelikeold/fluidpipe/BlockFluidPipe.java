package gregtech.common.pipelikeold.fluidpipe;

import gregtech.api.fluids.FluidConstants;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.graphnet.pipenetold.PipeNetNode;
import gregtech.api.graphnet.pipenetold.block.material.BlockMaterialPipe;
import gregtech.api.graphnet.edge.NetFlowEdge;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.graphnet.pipenetold.tile.IPipeTile;
import gregtech.api.graphnet.pipenetold.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.util.EntityDamageUtil;
import gregtech.client.renderer.pipeold.FluidPipeRenderer;
import gregtech.client.renderer.pipeold.PipeRenderer;
import gregtech.common.creativetab.GTCreativeTabs;
import gregtech.common.pipelikeold.fluidpipe.net.WorldFluidPipeNet;
import gregtech.common.pipelikeold.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelikeold.fluidpipe.tile.TileEntityFluidPipeTickable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class BlockFluidPipe extends
                            BlockMaterialPipe<FluidPipeType, FluidPipeProperties, NetFlowEdge, WorldFluidPipeNet> {

    private final SortedMap<Material, FluidPipeProperties> enabledMaterials = new TreeMap<>();

    public BlockFluidPipe(FluidPipeType pipeType, MaterialRegistry registry) {
        super(pipeType, registry);
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_PIPES);
        setHarvestLevel(ToolClasses.WRENCH, 1);
    }

    public void addPipeMaterial(Material material, FluidPipeProperties fluidPipeProperties) {
        Preconditions.checkNotNull(material, "material");
        Preconditions.checkNotNull(fluidPipeProperties, "material %s fluidPipeProperties was null", material);
        Preconditions.checkArgument(material.getRegistry().getNameForObject(material) != null,
                "material %s is not registered", material);
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

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    public PipeRenderer getPipeRenderer() {
        return FluidPipeRenderer.INSTANCE;
    }

    @Override
    protected FluidPipeProperties getFallbackType() {
        return enabledMaterials.values().iterator().next();
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs itemIn, @NotNull NonNullList<ItemStack> items) {
        for (Material material : enabledMaterials.keySet()) {
            items.add(getItem(material));
        }
    }

    @Override
    public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        // TODO insert to neighbours
    }

    @Override
    public boolean canPipesConnect(IPipeTile<FluidPipeType, FluidPipeProperties, NetFlowEdge> selfTile, EnumFacing side,
                                   IPipeTile<FluidPipeType, FluidPipeProperties, NetFlowEdge> sideTile) {
        return selfTile instanceof TileEntityFluidPipe && sideTile instanceof TileEntityFluidPipe;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<FluidPipeType, FluidPipeProperties, NetFlowEdge> selfTile,
                                         EnumFacing side,
                                         TileEntity tile) {
        return tile != null &&
                tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()) != null;
    }

    @Override
    public boolean isHoldingPipe(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        ItemStack stack = player.getHeldItemMainhand();
        return stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlockFluidPipe;
    }

    @Override
    public void onEntityCollision(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                  @NotNull Entity entityIn) {
        super.onEntityCollision(worldIn, pos, state, entityIn);
        if (worldIn.isRemote) return;
        TileEntityFluidPipe pipe = (TileEntityFluidPipe) getPipeTileEntity(worldIn, pos);
        if (pipe.getFrameMaterial() == null && pipe.getOffsetTimer() % 10 == 0) {
            if (entityIn instanceof EntityLivingBase living) {
                PipeNetNode<FluidPipeType, FluidPipeProperties, NetFlowEdge> node = pipe.getNode();
                var net = node.getNet();
                Set<FluidTestObject> fluids = new ObjectOpenHashSet<>();
                for (NetFlowEdge edge : net.getGraph().edgesOf(node)) {
                    for (Object obj : edge.getActiveChannels(null,
                            FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter())) {
                        if (obj instanceof FluidTestObject tester) fluids.add(tester);
                    }
                }
                int maxTemp = FluidConstants.ROOM_TEMPERATURE;
                int minTemp = FluidConstants.ROOM_TEMPERATURE;
                for (FluidTestObject fluid : fluids) {
                    int temp = fluid.fluid.getTemperature(fluid.recombine());
                    maxTemp = Math.max(temp, maxTemp);
                    minTemp = Math.min(temp, minTemp);
                }
                EntityDamageUtil.applyTemperatureDamage(living, maxTemp, 1.0F, 5);
                EntityDamageUtil.applyTemperatureDamage(living, minTemp, 1.0F, 5);
            }
        }
    }

    @Override
    public TileEntityPipeBase<FluidPipeType, FluidPipeProperties, NetFlowEdge> createNewTileEntity(boolean supportsTicking) {
        return supportsTicking ? new TileEntityFluidPipeTickable() : new TileEntityFluidPipe();
    }

    @Override
    @NotNull
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return FluidPipeRenderer.INSTANCE.getBlockRenderType();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return FluidPipeRenderer.INSTANCE.getParticleTexture((IPipeTile<?, ?, ?>) world.getTileEntity(blockPos));
    }
}
