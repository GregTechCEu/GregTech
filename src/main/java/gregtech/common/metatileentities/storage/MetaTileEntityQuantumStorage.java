package gregtech.common.metatileentities.storage;

import gregtech.api.capability.IQuantumController;
import gregtech.api.capability.IQuantumStorage;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
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

import static gregtech.api.capability.GregtechDataCodes.REMOVE_CONTROLLER;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_CONTROLLER_POS;

public abstract class MetaTileEntityQuantumStorage<T> extends MetaTileEntity implements IQuantumStorage<T> {

    /** not synced, server only. lazily initialized from pos */
    private WeakReference<IQuantumController> controller = new WeakReference<>(null);

    /** synced, server and client */
    private BlockPos controllerPos;

    private ImageWidget connectedIcon;

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
        if (!getWorld().isRemote) {
            controller.clear();
            controllerPos = null;
            writeCustomData(REMOVE_CONTROLLER, buf -> {});
            tryFindNetwork();
            markDirty();
        }
    }

    // use this to make sure controller is properly initialized
    @Override
    public IQuantumController getController() {
        if (isConnected()) {
            if (controller.get() != null) return controller.get();
            MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), controllerPos);
            if (mte instanceof IQuantumController) {
                controller = new WeakReference<>((IQuantumController) mte);
                return (IQuantumController) mte;
            } else {
                // controller is no longer there for some reason, need to disconnect
                setDisconnected();
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

    @Override
    public void onPlacement() {
        // add to the network if an adjacent block is part of a network
        // use whatever we find first, merging networks is not supported
        if (!getWorld().isRemote) {
            tryFindNetwork();
        }
    }

    private void tryFindNetwork() {
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (getWorld().getBlockState(getPos().offset(facing)).getBlock() == Blocks.AIR) continue;
            MetaTileEntity mte;
            if (getNeighbor(facing) instanceof IGregTechTileEntity gtte) {
                mte = gtte.getMetaTileEntity();
            } else {
                continue;
            }

            IQuantumController candidate = null;
            if (mte instanceof IQuantumStorage<?>storage) {
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
                return;
            }
        }
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
            scheduleRenderUpdate();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_CONTROLLER_POS) {
            this.controllerPos = buf.readBlockPos();
            this.controller.clear();

            if (this.connectedIcon != null) {
                this.connectedIcon.setImage(GuiTextures.GREGTECH_LOGO);
                String pos = String.format("X=%d, Z=%d, Y=%d", controllerPos.getX(), controllerPos.getZ(),
                        controllerPos.getY());
                this.connectedIcon.setTooltip("Connected to Quantum Controller at/n" + pos);
            }
            scheduleRenderUpdate();
        } else if (dataId == REMOVE_CONTROLLER) {
            this.controllerPos = null;
            this.controller.clear();
            if (this.connectedIcon != null) {
                this.connectedIcon.setImage(GuiTextures.GREGTECH_LOGO_DARK);
                this.connectedIcon.setTooltip(null);
            }
            scheduleRenderUpdate();
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

    protected ImageWidget createConnectedGui(int y) {
        // todo do something for rendering a highlight at the controller
        connectedIcon = new ImageWidget(151, y, 18, 18,
                isConnected() ? GuiTextures.GREGTECH_LOGO : GuiTextures.GREGTECH_LOGO_DARK);

        if (isConnected()) {
            String pos = String.format("X=%d, Z=%d, Y=%d", controllerPos.getX(), controllerPos.getZ(),
                    controllerPos.getY());
            connectedIcon.setTooltip("Connected to Quantum Controller at/n" + pos);
        }

        return connectedIcon;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.quantum_chest.tooltip"));
    }
}
