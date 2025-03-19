package gregtech.api.capability;

import gregtech.api.metatileentity.multiblock.IMultiblockPart;

import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * intended for use in conjunction with {@link gregtech.api.capability.impl.DistillationTowerLogicHandler}
 * use with distillation tower type multiblocks
 */
public interface IDistillationTower {

    List<IMultiblockPart> getMultiblockParts();

    BlockPos getPos();

    void invalidateStructure();

    boolean allowSameFluidFillForOutputs();
}
