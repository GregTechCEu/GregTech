package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AdvanceRecipeMapMultiblockController;
import gregtech.api.util.TextFormattingUtil;

import mcjty.theoneprobe.api.*;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class MultiblockCoreProvider implements  IProbeInfoProvider {

    @Override
    public String getID() {
        return GTValues.MODID + ":multiblock_tread_provider";
    }
    private static IProbeInfo newVertical(final IProbeInfo probeInfo) {
        return probeInfo.vertical(probeInfo.defaultLayoutStyle().spacing(0));
    }

    private static IProbeInfo newBox(final IProbeInfo info) {
        return info.horizontal(info.defaultLayoutStyle().borderColor(0x801E90FF));
    }
    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world, IBlockState iBlockState, IProbeHitData iProbeHitData) {
        IProbeInfo horizontalPane = iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
        if (iBlockState.getBlock().hasTileEntity(iBlockState)) {
            TileEntity te = world.getTileEntity(iProbeHitData.getPos());
            if (te instanceof IGregTechTileEntity igtte) {
                MetaTileEntity mte = igtte.getMetaTileEntity();

                if (mte instanceof AdvanceRecipeMapMultiblockController controller) {

                    if(controller.getThread()==1)return;

                    horizontalPane.text(TextStyleClass.INFO + "{*gregtech.top.tread*}");
                    horizontalPane.text(TextStyleClass.INFO + " " +TextFormatting.RED+controller.getThread()+ " ");

                    IProbeInfo box;
                    IProbeInfo leftInfo;

                    for (MultiblockRecipeLogic multiblockRecipeLogic : controller.getRecipeMapWorkableList()) {

                        if(!multiblockRecipeLogic.isActive())continue;

                        box = newBox(iProbeInfo);
                        leftInfo = newVertical(box);

                        int currentProgress = multiblockRecipeLogic.getProgress();
                        int maxProgress = multiblockRecipeLogic.getMaxProgress();

                        String text;
                        if (maxProgress < 20) {
                            text = " / " + maxProgress + " t";
                        } else {
                            currentProgress = Math.round(currentProgress / 20.0F);
                            maxProgress = Math.round(maxProgress / 20.0F);
                            text = " / " + TextFormattingUtil.formatNumbers(maxProgress) + " s";
                        }

                        if (maxProgress > 0) {
                            int color = multiblockRecipeLogic.isWorkingEnabled() ? 0xFF4CBB17 : 0xFFBB1C28;
                            leftInfo.progress(currentProgress, maxProgress, leftInfo.defaultProgressStyle()
                                    .suffix(text)
                                    .filledColor(color)
                                    .alternateFilledColor(color)
                                    .borderColor(0xFF555555).numberFormat(NumberFormat.COMMAS));
                        }

                        leftInfo.text("{*gregtech.top.parallel*}" + TextFormatting.DARK_PURPLE + multiblockRecipeLogic.getParallelLimit());

                    }
                }
            }
        }
    }
}
