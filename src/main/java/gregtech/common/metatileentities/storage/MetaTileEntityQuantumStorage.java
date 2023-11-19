package gregtech.common.metatileentities.storage;

import gregtech.api.capability.IQuantumController;
import gregtech.api.capability.IQuantumStorage;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_CONTROLLER_POS;
import static gregtech.api.capability.GregtechDataCodes.REMOVE_CONTROLLER;

public abstract class MetaTileEntityQuantumStorage<T> extends MetaTileEntity implements IQuantumStorage<T> {

    private WeakReference<IQuantumController> controller; // not synced, server only. lazily initialized from pos
    private BlockPos controllerPos; // synced, server and client

    public MetaTileEntityQuantumStorage(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public void setConnected(IQuantumController controller) {
        if (getWorld().isRemote) return;
        if (!controller.getPos().equals(controllerPos)) {
            this.controller = new WeakReference<>(controller);
            this.controllerPos = controller.getPos();
            if (!getWorld().isRemote) {
                writeCustomData(UPDATE_CONTROLLER_POS, buf -> buf.writeBlockPos(controllerPos));
                markDirty();
            }
        }
    }

    @Override
    public void setDisconnected() {
        if (getWorld().isRemote) return;
        if (controllerPos != null && !tryFindNewNetwork()) {
            // first try to find a new adjacent network
            controller = null;
            controllerPos = null;

            writeCustomData(REMOVE_CONTROLLER, buf -> {});
            markDirty();
        }
    }

    // use this to make sure controller is properly initialized
    @Override
    public IQuantumController getController() {
        if (getWorld().isRemote) return null; // quick safety check
        if (isConnected()) {
            if (controller != null && controller.get() != null) return controller.get();
            MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), controllerPos);
            if (mte instanceof IQuantumController) {
                controller = new WeakReference<>((IQuantumController) mte);
                return (IQuantumController) mte;
            } else {
                // controller is no longer there for some reason, need to disconnect
                controller = null;
                controllerPos = null;
                writeCustomData(REMOVE_CONTROLLER, buf -> {});
                markDirty();
            }
        }
        return null;
    }

    @Override
    public boolean isConnected() {
        // use controllerPos here because it is synced
        // on both sides, where controller is not
        return controllerPos != null;
    }

    @Override
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    @Override
    public void onRemoval() {
        if (!getWorld().isRemote && isConnected()) {
            IQuantumController controller = getController();
            if (controller != null) controller.rebuildNetwork();
        }
    }

    // todo might not be good time to do this
    // todo need to make sure it is AFTER nbt has been read
    @Override
    public void onPlacement() {
        // add to the network if an adjacent block is part of a network
        // use whatever we find first, merging networks is not supported
        if (!getWorld().isRemote) {
            tryFindNewNetwork();
        }
    }

    private boolean tryFindNewNetwork() {
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (getWorld().getBlockState(getPos().offset(facing)).getBlock() == Blocks.AIR) continue;
            MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), getPos().offset(facing));
            IQuantumController candidate = null;
            if (mte instanceof IQuantumStorage<?> storage) {
                if (storage.isConnected()) {
                    IQuantumController controller = storage.getController();
                    if (controller == null || controller.getPos().equals(controllerPos)) continue;
                    if (controller.canConnect(this)) {
                        candidate = controller;
                    }
                }
            } else if (mte instanceof IQuantumController quantumController) {
                if (quantumController.canConnect(this)) {
                    candidate = quantumController;
                }
            }
            if (candidate != null) {
                candidate.rebuildNetwork();
                return true;
            }
        }
        return false;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(controllerPos != null);
        if (controllerPos != null) {
            buf.writeBlockPos(controllerPos);
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            controllerPos = buf.readBlockPos();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_CONTROLLER_POS) {
            this.controllerPos = buf.readBlockPos();
        } else if (dataId == REMOVE_CONTROLLER) {
            this.controllerPos = null;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tagCompound = super.writeToNBT(data);
        tagCompound.setBoolean("HasController", controllerPos != null);
        if (controllerPos != null) {
            tagCompound.setLong("ControllerPos", controllerPos.toLong());
        }
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.getBoolean("HasController")) {
            this.controllerPos = BlockPos.fromLong(data.getLong("ControllerPos"));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.quantum_chest.tooltip"));
    }
}
