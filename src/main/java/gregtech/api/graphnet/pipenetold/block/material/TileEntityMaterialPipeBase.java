package gregtech.api.graphnet.pipenetold.block.material;

import gregtech.api.graphnet.pipenetold.IPipeNetData;
import gregtech.api.graphnet.pipenetold.block.BlockPipe;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.pipenetold.tile.IPipeTile;
import gregtech.api.graphnet.pipenetold.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.registry.MaterialRegistry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_PIPE_MATERIAL;

public abstract class TileEntityMaterialPipeBase<PipeType extends Enum<PipeType> & IMaterialPipeType<NodeDataType>,
        NodeDataType extends IPipeNetData<NodeDataType>, Edge extends NetEdge>
                                                extends TileEntityPipeBase<PipeType, NodeDataType, Edge>
                                                implements IMaterialPipeTile<PipeType, NodeDataType, Edge> {

    private Material pipeMaterial = Materials.Aluminium;

    @Override
    public Material getPipeMaterial() {
        return pipeMaterial;
    }

    public void setPipeData(BlockPipe<PipeType, NodeDataType, Edge, ?> pipeBlock, PipeType pipeType,
                            Material pipeMaterial) {
        this.pipeMaterial = pipeMaterial;
        super.setPipeData(pipeBlock, pipeType);
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_PIPE_MATERIAL, this::writePipeMaterial);
        }
    }

    @Override
    public void setPipeData(BlockPipe<PipeType, NodeDataType, Edge, ?> pipeBlock, PipeType pipeType) {
        throw new UnsupportedOperationException("Unsupported for TileEntityMaterialMaterialPipeBase");
    }

    @Override
    public int getDefaultPaintingColor() {
        return pipeMaterial == null ? super.getDefaultPaintingColor() : pipeMaterial.getMaterialRGB();
    }

    @Override
    public BlockMaterialPipe<PipeType, NodeDataType, Edge, ?> getPipeBlock() {
        return (BlockMaterialPipe<PipeType, NodeDataType, Edge, ?>) super.getPipeBlock();
    }

    @Override
    public void transferDataFrom(IPipeTile<PipeType, NodeDataType, Edge> tileEntity) {
        super.transferDataFrom(tileEntity);
        this.pipeMaterial = ((IMaterialPipeTile<PipeType, NodeDataType, Edge>) tileEntity).getPipeMaterial();
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("PipeMaterial", pipeMaterial.toString());
        return compound;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        MaterialRegistry registry = getPipeBlock().getMaterialRegistry();
        this.pipeMaterial = registry.getObject(compound.getString("PipeMaterial"));
        if (this.pipeMaterial == null) {
            this.pipeMaterial = registry.getFallbackMaterial();
        }
        this.getNode().setData(getPipeBlock().createProperties(this));
        if (!compound.hasKey("PipeNetVersion")) markAsNeedingOldNetSetup();
    }

    private void writePipeMaterial(@NotNull PacketBuffer buf) {
        buf.writeVarInt(getPipeBlock().getMaterialRegistry().getIDForObject(pipeMaterial));
    }

    private void readPipeMaterial(@NotNull PacketBuffer buf) {
        this.pipeMaterial = getPipeBlock().getMaterialRegistry().getObjectById(buf.readVarInt());
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        buf.writeVarInt(getPipeBlock().getMaterialRegistry().getIDForObject(pipeMaterial));
        super.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        this.pipeMaterial = getPipeBlock().getMaterialRegistry().getObjectById(buf.readVarInt());
        super.receiveInitialSyncData(buf);
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        super.receiveCustomData(discriminator, buf);
        if (discriminator == UPDATE_PIPE_MATERIAL) {
            readPipeMaterial(buf);
            scheduleRenderUpdate();
        }
    }
}
