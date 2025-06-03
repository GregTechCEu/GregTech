package gregtech.api.mui;

import gregtech.api.GTValues;
import gregtech.mixins.mui2.ModularSyncManagerAccessor;
import gregtech.mixins.mui2.PanelSyncHandlerAccessor;
import gregtech.mixins.mui2.PanelSyncManagerAccessor;

import net.minecraft.client.Minecraft;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
@SideOnly(Side.CLIENT)
public class GregTechGuiScreen extends ModularScreen implements JeiRecipeTransferHandler {

    public static final IRecipeTransferError DEFAULT_JEI_ERROR = new IRecipeTransferError() {

        @Override
        public @NotNull Type getType() {
            return Type.INTERNAL;
        }

        @Override
        public void showError(@NotNull Minecraft minecraft, int mouseX, int mouseY,
                              @NotNull IRecipeLayout recipeLayout, int recipeX, int recipeY) {
            // no error, just hide the + button
        }
    };

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
            panelToSyncMap.put(psm.getPanelName(), new ArrayList<>(getSyncHandlers(psm)));

            if (hasSubPanels(psm)) {
                for (PanelSyncHandler psh : getSubPanels(psm)) {
                    if (hasSyncManager(psh)) {
                        panels.add(getPanelSyncManager(psh));
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

    private static boolean hasSubPanels(PanelSyncManager panelSyncManager) {
        return !((PanelSyncManagerAccessor) panelSyncManager).getSubPanels().isEmpty();
    }

    private static List<PanelSyncHandler> getSubPanels(PanelSyncManager panelSyncManager) {
        if (!hasSubPanels(panelSyncManager)) {
            return Collections.emptyList();
        }

        List<PanelSyncHandler> subPanels = new ArrayList<>();
        Collection<SyncHandler> syncHandlers = ((PanelSyncManagerAccessor) panelSyncManager).getSubPanels().values();

        for (SyncHandler syncHandler : syncHandlers) {
            if (syncHandler instanceof PanelSyncHandler panelSyncHandler) {
                subPanels.add(panelSyncHandler);
            }
        }

        return subPanels;
    }

    private static Collection<SyncHandler> getSyncHandlers(PanelSyncManager panelSyncManager) {
        return ((PanelSyncManagerAccessor) panelSyncManager).getSyncHandlers().values();
    }

    private static boolean hasSyncManager(PanelSyncHandler panelSyncHandler) {
        return ((PanelSyncHandlerAccessor) (Object) panelSyncHandler).getPanelSyncManager() != null;
    }

    private static PanelSyncManager getPanelSyncManager(PanelSyncHandler panelSyncHandler) {
        return ((PanelSyncHandlerAccessor) (Object) panelSyncHandler).getPanelSyncManager();
    }
}
