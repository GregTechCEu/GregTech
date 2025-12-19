package gregtech.api.mui;

import gregtech.api.GTValues;
import gregtech.integration.jei.JustEnoughItemsModule;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerRecipeTransferHandler;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;

@SuppressWarnings("UnstableApiUsage")
@SideOnly(Side.CLIENT)
public class GregTechGuiScreen extends ModularScreen implements RecipeViewerRecipeTransferHandler {

    private static final Object2ObjectMap<String, IJEIRecipeReceiver> knownRecipeReceivers = new Object2ObjectOpenHashMap<>();

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
        for (IJEIRecipeReceiver handler : knownRecipeReceivers.values()) {
            IRecipeTransferError error = handler.receiveRecipe(recipeLayout, maxTransfer, simulate);
            if (error == null) return null;
        }
        return JustEnoughItemsModule.transferHelper.createInternalError();
    }

    public static void registerRecipeReceiver(String key, IJEIRecipeReceiver receiver) {
        knownRecipeReceivers.put(key, receiver);
    }

    public static void removeRecipeReceiver(String key) {
        knownRecipeReceivers.remove(key);
    }
}
