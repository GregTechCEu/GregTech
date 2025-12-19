package gregtech.api.pipenet.block.material;

import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.client.renderer.pipe.PipeRenderProperties;

import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class BlockMaterialPipe<
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType> & IMaterialPipeType<NodeDataType>, NodeDataType,
        WorldPipeNetType extends WorldPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>>>
                                       extends BlockPipe<PipeType, NodeDataType, WorldPipeNetType> {

    protected final PipeType pipeType;
    protected final Map<Material, NodeDataType> enabledMaterials;
    private final MaterialRegistry registry;

    public BlockMaterialPipe(@NotNull PipeType pipeType, @NotNull MaterialRegistry registry) {
        this.pipeType = pipeType;
        this.enabledMaterials = new TreeMap<>();
        this.registry = registry;
    }

    public boolean isValidPipeMaterial(Material material) {
        return !getPipeType().getOrePrefix().isIgnored(material);
    }

    public void addPipeMaterial(Material material, NodeDataType pipeProperties) {
        Preconditions.checkNotNull(material, "material was null");
        Preconditions.checkNotNull(pipeProperties, "the %s of material %s was null", getPipeTypeClass().getSimpleName(),
                material);
        Preconditions.checkArgument(material.getRegistry().getNameForObject(material) != null,
                "material %s is not registered", material);
        this.enabledMaterials.put(material, pipeProperties);
    }

    public Collection<Material> getEnabledMaterials() {
        return Collections.unmodifiableSet(enabledMaterials.keySet());
    }

    @Override
    public NodeDataType createProperties(IPipeTile<PipeType, NodeDataType> pipeTile) {
        PipeType pipeType = pipeTile.getPipeType();
        Material material = ((IMaterialPipeTile<PipeType, NodeDataType>) pipeTile).getPipeMaterial();
        if (pipeType == null || material == null) {
            return getFallbackType();
        }
        return createProperties(pipeType, material);
    }

    @Override
    public NodeDataType createItemProperties(ItemStack itemStack) {
        Material material = getItemMaterial(itemStack);
        if (pipeType == null || material == null) {
            return getFallbackType();
        }
        return createProperties(pipeType, material);
    }

    public ItemStack getItem(Material material) {
        if (material == null) return ItemStack.EMPTY;
        int materialId = registry.getIDForObject(material);
        return new ItemStack(this, 1, materialId);
    }

    public Material getItemMaterial(ItemStack itemStack) {
        return registry.getObjectById(itemStack.getMetadata());
    }

    @Override
    public void setTileEntityData(TileEntityPipeBase<PipeType, NodeDataType> pipeTile, ItemStack itemStack) {
        ((TileEntityMaterialPipeBase<PipeType, NodeDataType>) pipeTile).setPipeData(this, pipeType,
                getItemMaterial(itemStack));
    }

    @Override
    public ItemStack getDropItem(IPipeTile<PipeType, NodeDataType> pipeTile) {
        Material material = ((IMaterialPipeTile<PipeType, NodeDataType>) pipeTile).getPipeMaterial();
        return getItem(material);
    }

    protected NodeDataType createProperties(PipeType pipeType, Material material) {
        return pipeType.modifyProperties(enabledMaterials.getOrDefault(material, getFallbackType()));
    }

    @Override
    protected NodeDataType getFallbackType() {
        return enabledMaterials.values().iterator().next();
    }

    @Override
    public void getSubBlocks(@NotNull CreativeTabs itemIn, @NotNull NonNullList<ItemStack> items) {
        for (Material material : enabledMaterials.keySet()) {
            items.add(getItem(material));
        }
    }

    public OrePrefix getPrefix() {
        return pipeType.getOrePrefix();
    }

    public PipeType getPipeType() {
        return pipeType;
    }

    @NotNull
    public MaterialRegistry getMaterialRegistry() {
        return registry;
    }

    @NotNull
    @Override
    protected BlockStateContainer.Builder constructState(BlockStateContainer.@NotNull Builder builder) {
        return super.constructState(builder).add(PipeRenderProperties.MATERIAL_PROPERTY);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        var tile = (TileEntityMaterialPipeBase<?, ?>) getPipeTileEntity(world, blockPos);
        if (tile != null) {
            return getPipeType().getModel().getParticleTexture(tile.getPaintingColor(), tile.getPipeMaterial());
        }
        return null;
    }
}
