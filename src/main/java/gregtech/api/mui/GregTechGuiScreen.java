package gregtech.api.mui;

import gregtech.api.GTValues;
import gregtech.api.util.MUIUtil;
import gregtech.mixins.mui2.ModularSyncManagerAccessor;
import gregtech.mixins.mui2.PanelSyncHandlerAccessor;
import gregtech.mixins.mui2.PanelSyncManagerAccessor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.integration.jei.JeiRecipeTransferHandler;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.ObjectList;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gregtech.api.mui.IJEIRecipeReceiver.DEFAULT_JEI_ERROR;

@SuppressWarnings("UnstableApiUsage")
@SideOnly(Side.CLIENT)
public class GregTechGuiScreen extends ModularScreen implements JeiRecipeTransferHandler {

    public GregTechGuiScreen(ModularPanel mainPanel) {
        this(mainPanel, GTGuiTheme.STANDARD);
    }

    public GregTechGuiScreen(ModularPanel mainPanel, GTGuiTheme theme) {
        this(GTValues.MODID, mainPanel, theme);
    }

    public GregTechGuiScreen(String owner, ModularPanel mainPanel, GTGuiTheme theme) {
        this(owner, mainPanel, theme.getId());
    }

    public GregTechGuiScreen(String owner, ModularPanel mainPanel, String themeId) {
        super(owner, mainPanel);
        useTheme(themeId);
    }

    @Override
    public IRecipeTransferError transferRecipe(IRecipeLayout recipeLayout, boolean maxTransfer, boolean simulate) {
        Map<String, List<SyncHandler>> panelToSyncMap = new HashMap<>();
        ObjectList<PanelSyncManager> panels = ObjectList.create();
        panels.add(((ModularSyncManagerAccessor) getSyncManager()).getMainPanelSyncManager());
        while (!panels.isEmpty()) {
            PanelSyncManager psm = panels.removeFirst();
            panelToSyncMap.put(psm.getPanelName(), new ArrayList<>(MUIUtil.getSyncHandlers(psm)));

            if (MUIUtil.hasSubPanels(psm)) {
                for (PanelSyncHandler psh : MUIUtil.getSubPanels(psm)) {
                    if (MUIUtil.hasSyncManager(psh)) {
                        panels.add(MUIUtil.getPanelSyncManager(psh));
                    }
                }
            }
        }

        for (SyncHandler syncHandler : panelToSyncMap.get(getPanelManager().getTopMostPanel().getName())) {
            if (syncHandler instanceof IJEIRecipeReceiver recipeReceiver) {
                return recipeReceiver.receiveRecipe(recipeLayout, maxTransfer, simulate);
            }
        }

        // Hide the + button by default if no recipe receiver was found.
        return DEFAULT_JEI_ERROR;
    }
}
