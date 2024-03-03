package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.IControllable;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IControlRodPort;
import gregtech.api.metatileentity.multiblock.IFissionReactorHatch;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.blocks.BlockFissionCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class MetaTileEntityControlRodPort extends MetaTileEntityMultiblockNotifiablePart
                                          implements IControllable, IFissionReactorHatch,
                                          IMultiblockAbilityPart<IControlRodPort>, IControlRodPort {

    private boolean workingEnabled;
    private boolean valid;

    private byte insertion;

    public MetaTileEntityControlRodPort(ResourceLocation metaTileEntityId, boolean isExportHatch) {
        super(metaTileEntityId, 4, false);
        this.frontFacing = EnumFacing.UP;
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.workingEnabled = isWorkingAllowed;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityControlRodPort(metaTileEntityId, false);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(EnumFacing.UP);
    }

    @Override
    public boolean checkValidity(int depth) {
        // Export ports are always considered valid
        BlockPos pos = this.getPos();
        for (int i = 1; i < depth; i++) {
            if (getWorld().getBlockState(pos.offset(EnumFacing.DOWN, i)) !=
                    MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.CONTROL_ROD_CHANNEL)) {
                return false;
            }
        }
        return getWorld().getBlockState(pos.offset(EnumFacing.DOWN, depth)) ==
                MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.REACTOR_VESSEL);
    }

    @Override
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public MultiblockAbility<IControlRodPort> getAbility() {
        return MultiblockAbility.CONTROL_ROD_PORT;
    }

    @Override
    public void registerAbilities(List<IControlRodPort> abilityList) {
        abilityList.add(this);
    }

    @Override
    public byte getInsertionAmount() {
        return this.insertion;
    }
}
