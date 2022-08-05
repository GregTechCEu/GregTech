package gregtech.integration.jei.recipe.primitive;

import gregtech.api.GTValues;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

//TODO, move this and all inheritors into integration.jei.basic
public abstract class BasicRecipeCategory<T, W extends IRecipeWrapper> implements IRecipeCategory<W>, IRecipeWrapperFactory<T> {

    public final String uniqueName;
    public final String localizedName;
    protected final IDrawable background;

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

    @Nonnull
    @Override
    public String getUid() {
        return getModName() + ":" + uniqueName;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return localizedName;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
    }

    @Nonnull
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public String getModName() {
        return GTValues.MODID;
    }
}
