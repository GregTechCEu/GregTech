package gregtech.api.pipenet.block.material;

import gregtech.api.GTValues;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.client.renderer.pipe.PipeRenderer;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class BlockMaterialPipe<
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType> & IMaterialPipeType<NodeDataType>, NodeDataType,
        WorldPipeNetType extends WorldPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>>>
                                       extends BlockPipe<PipeType, NodeDataType, WorldPipeNetType> {

    protected final PipeType pipeType;
    private final MaterialRegistry registry;

    public BlockMaterialPipe(@NotNull PipeType pipeType, @NotNull MaterialRegistry registry) {
        this.pipeType = pipeType;
        this.registry = registry;
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

    protected abstract NodeDataType createProperties(PipeType pipeType, Material material);

    public OrePrefix getPrefix() {
        return pipeType.getOrePrefix();
    }

    public PipeType getItemPipeType(ItemStack is) {
        return pipeType;
    }

    @NotNull
    public MaterialRegistry getMaterialRegistry() {
        return registry;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    public abstract PipeRenderer getPipeRenderer();

    public void onModelRegister() {
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(this), stack -> getPipeRenderer().getModelLocation());
        for (IBlockState state : this.getBlockState().getValidStates()) {
            ModelResourceLocation resourceLocation = new ModelResourceLocation(
                    new ResourceLocation(GTValues.MODID, // force pipe models to always be GT's
                            Objects.requireNonNull(this.getRegistryName()).getPath()),
                    MetaBlocks.statePropertiesToString(state.getProperties()));
            // noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this),
                    this.getMetaFromState(state), resourceLocation);
        }
    }
}
