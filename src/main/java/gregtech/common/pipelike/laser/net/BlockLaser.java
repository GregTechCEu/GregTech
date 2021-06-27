package gregtech.common.pipelike.laser.net;
import com.google.common.base.Preconditions;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.type.Material;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.pipelike.laser.net.LaserPipeNet;
import gregtech.common.pipelike.laser.net.WorldLaserNet;
import gregtech.common.pipelike.laser.tile.LaserSize;
import gregtech.common.pipelike.laser.tile.TileEntityLaser;
import gregtech.common.pipelike.laser.tile.TileEntityLaserTickable;
import gregtech.common.render.CableRenderer;
import gregtech.common.pipelike.laser.tile.LaserProperties;
import gregtech.common.render.LaserRenderer;
import net.minecraft.block.ITileEntityProvider;
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import gregtech.api.pipenet.block.BlockPipe;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;


public class BlockLaser extends BlockMaterialPipe<LaserSize,LaserProperties,WorldLaserNet>implements ITileEntityProvider {

    private final Map<Material, LaserProperties> enabledMaterials2 = new TreeMap<>();

    public BlockLaser() {
        setHarvestLevel("cutter", 1);
    }

    public void addlaserMaterial(Material material, LaserProperties LaserProperties) {
        Preconditions.checkNotNull(material, "material");
        Preconditions.checkNotNull(LaserProperties, "LaserProperties");
        Preconditions.checkArgument(Material.MATERIAL_REGISTRY.getNameForObject(material) != null, "material is not registered");
        this.enabledMaterials2.put(material, LaserProperties);


    }
    public Collection<Material> getEnabledMaterials2() {
        return Collections.unmodifiableSet(enabledMaterials2.keySet());
    }
    @Override
    public Class<LaserSize> getPipeTypeClass() {
        return LaserSize.class;
    }

    @Override
    public WorldLaserNet getWorldPipeNet(World world) {
        return WorldLaserNet.getWorldENet(world);
    }



    @Override
    protected LaserProperties createProperties(LaserSize insulation, Material material) {
        return insulation.modifyProperties(enabledMaterials2.getOrDefault(material, getFallbackType()));

    }

    @Override
    protected LaserProperties getFallbackType() {
        return enabledMaterials2.values().iterator().next();
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (Material material : enabledMaterials2.keySet()) {
            for (LaserSize laserSize : LaserSize.values()) {
                items.add(getItem(laserSize, material));
            }
        }
    }
    @Override
    public int getActiveNodeConnections(IBlockAccess world, BlockPos nodePos, IPipeTile<LaserSize, LaserProperties> selfTileEntity) {
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
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
    }
    @Override
    @SideOnly(Side.CLIENT)
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return LaserRenderer.BLOCK_RENDER_TYPE;
    }

    @Override
    public TileEntityPipeBase<LaserSize, LaserProperties> createNewTileEntity(boolean supportsTicking) {
        return supportsTicking ? new TileEntityLaser() : new TileEntityLaser();
    }


    @Override
    @SideOnly(Side.CLIENT)
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        return LaserRenderer.INSTANCE.getParticleTexture((TileEntityLaser) world.getTileEntity(blockPos));
    }
}
