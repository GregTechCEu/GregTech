package gregtech.common.pipelike.cable;

import com.google.common.base.Preconditions;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.tool.ICutterItem;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.tile.AttachmentType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.pipelike.cable.net.EnergyNet;
import gregtech.common.pipelike.cable.net.WorldENet;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import gregtech.common.pipelike.cable.tile.TileEntityCableTickable;
import gregtech.common.render.CableRenderer;
import gregtech.common.tools.DamageValues;
import net.minecraft.block.ITileEntityProvider;
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
import net.minecraft.world.IBlockAccess;
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
        Preconditions.checkArgument(Material.MATERIAL_REGISTRY.getNameForObject(material) != null, "material is not registered");
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
    public int getActiveNodeConnections(IBlockAccess world, BlockPos nodePos, IPipeTile<Insulation, WireProperties> selfTileEntity) {
        int activeNodeConnections = 0;
        for (EnumFacing side : EnumFacing.VALUES) {
            BlockPos offsetPos = nodePos.offset(side);
            TileEntity tileEntity = world.getTileEntity(offsetPos);
            //do not connect to null cables and ignore cables
            if (tileEntity == null || getPipeTileEntity(tileEntity) != null) continue;
            EnumFacing opposite = side.getOpposite();
            IEnergyContainer energyContainer = tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, opposite);
            if (energyContainer != null) {
                activeNodeConnections |= 1 << side.getIndex();
            }
        }
        return activeNodeConnections;
    }

    @Override
    public int onPipeToolUsed(ItemStack stack, EnumFacing coverSide, IPipeTile<Insulation, WireProperties> pipeTile, EntityPlayer entityPlayer) {
        ICutterItem cutterItem = stack.getCapability(GregtechCapabilities.CAPABILITY_CUTTER, null);
        if (cutterItem != null) {
            if (cutterItem.damageItem(DamageValues.DAMAGE_FOR_CUTTER, true)) {
                if (!entityPlayer.world.isRemote) {
                    boolean isBlocked = pipeTile.isConnectionBlocked(AttachmentType.PIPE, coverSide);
                    pipeTile.setConnectionBlocked(AttachmentType.PIPE, coverSide, !isBlocked, false);
                    cutterItem.damageItem(DamageValues.DAMAGE_FOR_CUTTER, false);
                }
                return 1;
            }
            return 0;
        }
        return -1;
    }

    @Override
    public void onEntityCollision(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entityIn) {
        if (worldIn.isRemote) return;
        Insulation insulation = getPipeTileEntity(worldIn, pos).getPipeType();
        boolean damageOnLossless = ConfigHolder.doLosslessWiresDamage;
        if (!worldIn.isRemote && insulation.insulationLevel == -1 && entityIn instanceof EntityLivingBase) {
            EntityLivingBase entityLiving = (EntityLivingBase) entityIn;
            EnergyNet energyNet = getWorldPipeNet(worldIn).getNetFromPos(pos);
            if (energyNet != null && (damageOnLossless || energyNet.getAllNodes().get(pos).data.lossPerBlock > 0)) {
                long voltage = energyNet.getLastMaxVoltage();
                long amperage = energyNet.getLastAmperage();
                if (voltage > 0L && amperage > 0L) {
                    float damageAmount = (GTUtility.getTierByVoltage(voltage) + 1) * amperage * 4;
                    entityLiving.attackEntityFrom(DamageSources.getElectricDamage(), damageAmount);
                }
            }
        }
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return CableRenderer.BLOCK_RENDER_TYPE;
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
