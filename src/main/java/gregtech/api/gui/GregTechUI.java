package gregtech.api.gui;

import com.cleanroommc.modularui.api.ITileWithModularUI;
import com.cleanroommc.modularui.common.builder.UIBuilder;
import com.cleanroommc.modularui.common.builder.UIInfo;
import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.internal.ModularWindow;
import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import com.cleanroommc.modularui.common.internal.wrapper.ModularUIContainer;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class GregTechUI {

    private static final UIInfo<?, ?>[] SIDED_COVER_UI = new UIInfo[6];

    public static UIInfo<?, ?> getCoverUi(EnumFacing facing) {
        return SIDED_COVER_UI[facing.getIndex()];
    }

    public static final UIInfo<?, ?> MTE_UI = UIBuilder.of()
            .gui(((player, world, x, y, z) -> {
                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if (te instanceof MetaTileEntityHolder && !te.isInvalid()) {
                    MetaTileEntity mte = ((MetaTileEntityHolder) te).getMetaTileEntity();
                    if (mte != null) {
                        UIBuildContext buildContext = new UIBuildContext(player);
                        ModularWindow window = mte.createWindow(buildContext);
                        return new ModularGui(new ModularUIContainer(new ModularUIContext(buildContext), window));
                    }
                }
                return null;
            }))
            .container((player, world, x, y, z) -> {
                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if (te instanceof MetaTileEntityHolder && !te.isInvalid()) {
                    MetaTileEntity mte = ((MetaTileEntityHolder) te).getMetaTileEntity();
                    if (mte != null) {
                        UIBuildContext buildContext = new UIBuildContext(player);
                        ModularWindow window = mte.createWindow(buildContext);
                        return new ModularUIContainer(new ModularUIContext(buildContext), window);
                    }
                }
                return null;
            })
            .build();

    static {
        for (EnumFacing facing : EnumFacing.VALUES) {
            SIDED_COVER_UI[facing.getIndex()] = UIBuilder.of()
                    .gui(((player, world, x, y, z) -> {
                        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                        if (te == null) {
                            return null;
                        }
                        ICoverable coverable = te.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, facing);
                        if (coverable == null) {
                            return null;
                        }
                        CoverBehavior cover = coverable.getCoverAtSide(facing);
                        if (cover instanceof CoverWithUI) {
                            UIBuildContext buildContext = new UIBuildContext(player);
                            ModularWindow window = ((CoverWithUI) cover).createWindow(buildContext);
                            return new ModularGui(new ModularUIContainer(new ModularUIContext(buildContext), window));
                        }
                        return null;
                    }))
                    .container((player, world, x, y, z) -> {
                        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                        if (te == null) {
                            return null;
                        }
                        ICoverable coverable = te.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, facing);
                        if (coverable == null) {
                            return null;
                        }
                        CoverBehavior cover = coverable.getCoverAtSide(facing);
                        if (cover instanceof CoverWithUI) {
                            UIBuildContext buildContext = new UIBuildContext(player);
                            ModularWindow window = ((CoverWithUI) cover).createWindow(buildContext);
                            return new ModularUIContainer(new ModularUIContext(buildContext), window);
                        }
                        return null;
                    })
                    .build();
        }
    }


}
