package gregtech.api.pipenet.block.material;

import gregtech.api.GregTechAPI;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.item.ItemStack;

public abstract class BlockMaterialPipe<PipeType extends Enum<PipeType> & IPipeType<NodeDataType> & IMaterialPipeType<NodeDataType>, NodeDataType, WorldPipeNetType extends WorldPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>>> extends BlockPipe<PipeType, NodeDataType, WorldPipeNetType> {

    protected final PipeType pipeType;

    public BlockMaterialPipe(PipeType pipeType) {
        this.pipeType = pipeType;
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
        int materialId = GregTechAPI.MATERIAL_REGISTRY.getIDForObject(material);
        return new ItemStack(this, 1, materialId);
    }

    public Material getItemMaterial(ItemStack itemStack) {
        return GregTechAPI.MATERIAL_REGISTRY.getObjectById(itemStack.getMetadata());
    }

    @Override
    public void setTileEntityData(TileEntityPipeBase<PipeType, NodeDataType> pipeTile, ItemStack itemStack) {
        ((TileEntityMaterialPipeBase<PipeType, NodeDataType>) pipeTile).setPipeData(this, pipeType, getItemMaterial(itemStack));
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
}
