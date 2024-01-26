package gregtech.api.mui;

import com.cleanroommc.modularui.integration.jei.JeiRecipeTransferHandler;
import com.cleanroommc.modularui.screen.ModularPanel;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;

@SuppressWarnings("UnstableApiUsage")
public class GregTechGuiTransferrableScreen extends GregTechGuiScreen implements JeiRecipeTransferHandler {

    public GregTechGuiTransferrableScreen(ModularPanel mainPanel) {
        super(mainPanel);
    }

    public GregTechGuiTransferrableScreen(ModularPanel mainPanel, GTGuiTheme theme) {
        super(mainPanel, theme);
    }

    public GregTechGuiTransferrableScreen(String owner, ModularPanel mainPanel, GTGuiTheme theme) {
        super(owner, mainPanel, theme);
    }

    public GregTechGuiTransferrableScreen(String owner, ModularPanel mainPanel, String themeId) {
        super(owner, mainPanel, themeId);
    }

    @Override
    public IRecipeTransferError transferRecipe(IRecipeLayout recipeLayout, boolean maxTransfer, boolean simulate) {
        return null;
    }
}
