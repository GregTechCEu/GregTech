package gregtech.api.mui;

import gregtech.api.GTValues;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.integration.jei.JeiRecipeTransferHandler;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
@SideOnly(Side.CLIENT)
public class GregTechGuiScreen extends ModularScreen implements JeiRecipeTransferHandler {

    public static final IRecipeTransferError DEFAULT_ERROR = new IRecipeTransferError() {

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
        // Hide the + button by default if this recipe isn't valid for insertion
        return DEFAULT_ERROR;
    }
}
