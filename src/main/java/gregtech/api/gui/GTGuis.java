package gregtech.api.gui;

import com.cleanroommc.modularui.manager.GuiInfo;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;

public class GTGuis {

    public static final GuiInfo MTE = GuiInfo.builder()
            .clientGui(context -> {
                MetaTileEntity mte = GTUtility.getMetaTileEntity(context.getWorld(), context.getBlockPos());
                if (mte != null) {
                    return mte.createClientGui(context.getPlayer());
                }
                throw new UnsupportedOperationException();
            })
            .serverGui((context, syncHandler) -> {
                MetaTileEntity mte = GTUtility.getMetaTileEntity(context.getWorld(), context.getBlockPos());
                if (mte != null) {
                    mte.buildSyncHandler(syncHandler, context.getPlayer());
                }
            })
            .build();
}
