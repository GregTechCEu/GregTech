package gregtech.common.pipelike.cable;

import com.google.common.base.Preconditions;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.tool.ICutterItem;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.items.toolitem.IToolStats;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.CableRenderer;
import gregtech.common.advancement.GTTriggers;
import gregtech.common.pipelike.cable.net.EnergyNet;
import gregtech.common.pipelike.cable.net.WorldENet;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import gregtech.common.pipelike.cable.tile.TileEntityCableTickable;
import gregtech.common.tools.DamageValues;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class BlockCable extends BlockMaterialPipe<Insulation, WireProperties, WorldENet> implements ITileEntityProvider {

    private final Map<Material, WireProperties> enabledMaterials = new TreeMap<>();

    public BlockCable(Insulation cableType) {
        super(cableType);
        setHarvestLevel("cutter", 1);
    }

    public void addCableMaterial(Material material, WireProperties wireProperties) {
        Preconditions.checkNotNull(material, "material");
        Preconditions.checkNotNull(wireProperties, "wireProperties");
        Preconditions.checkArgument(GregTechAPI.MATERIAL_REGISTRY.getNameForObject(material) != null, "material is not registered");
        if (!pipeType.orePrefix.isIgnored(material)) {
            this.enabledMaterials.put(material, wireProperties);
        }
    }

    public Collection<Material> getEnabledMaterials() {
        return Collections.unmodifiableSet(enabledMaterials.keySet());
    }

    @Override
    public Class<Insulation> getPipeTypeClass() {
        return Insulation.class;
    }

    @Override
    protected WireProperties createProperties(Insulation insulation, Material material) {
        return insulation.modifyProperties(enabledMaterials.getOrDefault(material, getFallbackType()));
    }

    @Override
    protected WireProperties getFallbackType() {
        return enabledMaterials.values().iterator().next();
    }

    @Override
    public WorldENet getWorldPipeNet(World world) {
        return WorldENet.getWorldENet(world);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (Material material : enabledMaterials.keySet()) {
            items.add(getItem(material));
        }
    }

    @Override
    public int onPipeToolUsed(World world, BlockPos pos, ItemStack stack, EnumFacing coverSide, IPipeTile<Insulation, WireProperties> pipeTile, EntityPlayer entityPlayer) {
        ICutterItem cutterItem = stack.getCapability(GregtechCapabilities.CAPABILITY_CUTTER, null);
        if (cutterItem != null) {
            if (cutterItem.damageItem(DamageValues.DAMAGE_FOR_CUTTER, true)) {
                if (!entityPlayer.world.isRemote) {
                    boolean isOpen = pipeTile.isConnected(coverSide);
                    pipeTile.setConnection(coverSide, !isOpen, false);
                    cutterItem.damageItem(DamageValues.DAMAGE_FOR_CUTTER, false);
                    IToolStats.onOtherUse(stack, world, pos);
                }
                return 1;
            }
            return 0;
        }
        return -1;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (!worldIn.isRemote) {
            EnergyNet enet = getWorldPipeNet(worldIn).getNetFromPos(pos);
            if (enet != null) {
                enet.nodeNeighbourChanged(pos);
            }
        }
    }

    @Override
    public boolean canPipesConnect(IPipeTile<Insulation, WireProperties> selfTile, EnumFacing side, IPipeTile<Insulation, WireProperties> sideTile) {
        return selfTile instanceof TileEntityCable && sideTile instanceof TileEntityCable;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<Insulation, WireProperties> selfTile, EnumFacing side, TileEntity tile) {
        return tile != null && tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side.getOpposite()) != null;
    }

    @Override
    public boolean isHoldingPipe(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        ItemStack stack = player.getHeldItemMainhand();
        return stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlockCable;
    }

    @Override
    public void onEntityCollision(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entityIn) {
        if (worldIn.isRemote) return;
        Insulation insulation = getPipeTileEntity(worldIn, pos).getPipeType();
        if (insulation.insulationLevel == -1 && entityIn instanceof EntityLivingBase) {
            EntityLivingBase entityLiving = (EntityLivingBase) entityIn;
            TileEntityCable cable = (TileEntityCable) getPipeTileEntity(worldIn, pos);
            if (cable != null && cable.getNodeData().getLossPerBlock() > 0) {
                long voltage = cable.getCurrentMaxVoltage();
                double amperage = cable.getAverageAmperage();
                if (voltage > 0L && amperage > 0L) {
                    float damageAmount = (float) ((GTUtility.getTierByVoltage(voltage) + 1) * amperage * 4);
                    entityLiving.attackEntityFrom(DamageSources.getElectricDamage(), damageAmount);
                    if (entityLiving instanceof EntityPlayerMP) {
                        GTTriggers.ELECTROCUTION_DEATH.trigger((EntityPlayerMP) entityLiving);
                    }
                }
            }
        }
    }

    @Override
    protected boolean doDrawGrid(ItemStack stack) {
        return stack.hasCapability(GregtechCapabilities.CAPABILITY_CUTTER, null);
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return CableRenderer.INSTANCE.getBlockRenderType();
    }

    @Override
    public TileEntityPipeBase<Insulation, WireProperties> createNewTileEntity(boolean supportsTicking) {
        return supportsTicking ? new TileEntityCableTickable() : new TileEntityCable();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return CableRenderer.INSTANCE.getParticleTexture((TileEntityCable) world.getTileEntity(blockPos));
    }
}
