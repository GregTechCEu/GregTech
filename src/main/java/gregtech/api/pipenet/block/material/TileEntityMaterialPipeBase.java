package gregtech.api.pipenet.block.material;

import gregtech.api.GregTechAPI;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_PIPE_MATERIAL;

public abstract class TileEntityMaterialPipeBase<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> extends TileEntityPipeBase<PipeType, NodeDataType> implements IMaterialPipeTile<PipeType, NodeDataType> {

    private Material pipeMaterial = Materials.Aluminium;

    @Override
    public Material getPipeMaterial() {
        return pipeMaterial;
    }

    public void setPipeData(BlockPipe<PipeType, NodeDataType, ?> pipeBlock, PipeType pipeType, Material pipeMaterial) {
        super.setPipeData(pipeBlock, pipeType);
        this.pipeMaterial = pipeMaterial;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_PIPE_MATERIAL, this::writePipeMaterial);
        }
    }

    @Override
    public void setPipeData(BlockPipe<PipeType, NodeDataType, ?> pipeBlock, PipeType pipeType) {
        throw new UnsupportedOperationException("Unsupported for TileEntityMaterialMaterialPipeBase");
    }

    @Override
    public int getDefaultPaintingColor() {
        return pipeMaterial == null ? super.getDefaultPaintingColor() : pipeMaterial.getMaterialRGB();
    }

    @Override
    public void transferDataFrom(IPipeTile<PipeType, NodeDataType> tileEntity) {
        super.transferDataFrom(tileEntity);
        this.pipeMaterial = ((IMaterialPipeTile<PipeType, NodeDataType>) tileEntity).getPipeMaterial();
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("PipeMaterial", pipeMaterial.toString());
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.pipeMaterial = GregTechAPI.MATERIAL_REGISTRY.getObject(compound.getString("PipeMaterial"));
        if (this.pipeMaterial == null) {
            this.pipeMaterial = Materials.Aluminium; // fallback
        }
    }

    private void writePipeMaterial(PacketBuffer buf) {
        buf.writeVarInt(GregTechAPI.MATERIAL_REGISTRY.getIDForObject(pipeMaterial));
    }

    private void readPipeMaterial(PacketBuffer buf) {
        this.pipeMaterial = GregTechAPI.MATERIAL_REGISTRY.getObjectById(buf.readVarInt());
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeVarInt(GregTechAPI.MATERIAL_REGISTRY.getIDForObject(pipeMaterial));
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.pipeMaterial = GregTechAPI.MATERIAL_REGISTRY.getObjectById(buf.readVarInt());
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        super.receiveCustomData(discriminator, buf);
        if (discriminator == UPDATE_PIPE_MATERIAL) {
            readPipeMaterial(buf);
            scheduleChunkForRenderUpdate();
        }
    }
}
