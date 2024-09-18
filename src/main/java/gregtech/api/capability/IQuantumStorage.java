package gregtech.api.capability;

import gregtech.api.cover.CoverableView;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Nullable;

public interface IQuantumStorage<T> extends CoverableView {

    Type getType();

    void setConnected(IQuantumController controller);

    void setDisconnected();

    BlockPos getControllerPos();

    @Nullable
    IQuantumController getQuantumController();

    BlockPos getPos();

    default boolean isConnected() {
        // use controllerPos here because it is synced
        // on both sides, where controller is not
        return getControllerPos() != null;
    }

    default void tryFindNetwork() {
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
                    IQuantumController controller = storage.getQuantumController();
                    if (controller != null && controller.canConnect(this)) {
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

    T getTypeValue();

    enum Type {
        ITEM,
        FLUID,
        EXTENDER,
        PROXY,
        ENERGY
    }
}
