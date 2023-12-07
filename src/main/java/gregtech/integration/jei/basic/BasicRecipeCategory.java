package gregtech.integration.jei.basic;

import gregtech.api.GTValues;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public abstract class BasicRecipeCategory<T, W extends IRecipeWrapper>
                                         implements IRecipeCategory<W>, IRecipeWrapperFactory<T> {

    public final String uniqueName;
    public final String localizedName;
    protected final IDrawable background;
    protected final int FONT_HEIGHT = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;

    public BasicRecipeCategory(String uniqueName, String localKey, IDrawable background, IGuiHelper guiHelper) {
        this.uniqueName = uniqueName;
        this.localizedName = I18n.format(localKey);
        this.background = background;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return null;
    }

    @NotNull
    @Override
    public String getUid() {
        return getModName() + ":" + uniqueName;
    }

    @NotNull
    @Override
    public String getTitle() {
        return localizedName;
    }

    @NotNull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(@NotNull Minecraft minecraft) {}

    @NotNull
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public String getModName() {
        return GTValues.MODID;
    }
}
