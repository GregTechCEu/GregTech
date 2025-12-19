package gregtech.api.mui.drawable;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.recipes.chance.boost.BoostableChanceEntry;
import gregtech.api.util.TextFormattingUtil;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerIngredientProvider;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.SlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

public class GTObjectDrawable implements IDrawable, RecipeViewerIngredientProvider {

    private static final TextRenderer renderer = new TextRenderer();

    private final Object object;
    private final long amount;
    private Function<BoostableChanceEntry<?>, Integer> boostFunction;
    @NotNull
    private Predicate<Object> drawBackground = $ -> true;

    public GTObjectDrawable(Object object, long amount) {
        this.object = object;
        this.amount = amount;
    }

    public GTObjectDrawable setBoostFunction(Function<BoostableChanceEntry<?>, Integer> boostFunction) {
        this.boostFunction = boostFunction;
        return this;
    }

    public void setDrawBackground(@NotNull Predicate<Object> drawBackground) {
        this.drawBackground = drawBackground;
    }

    static {
        renderer.setScale(0.5f);
        renderer.setShadow(true);
        renderer.setColor(Color.WHITE.main);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (!(context instanceof ModularGuiContext modularGuiContext)) return;
        renderer.setAlignment(Alignment.BottomRight, width - 1, height - 1);
        drawObject(object, modularGuiContext, x, y, width, height);
        if (amount > 0) {
            renderer.setPos(x + 1, y + 1);
            String amount = TextFormattingUtil.formatLongToCompactString(this.amount, 3);
            if (object instanceof FluidStack) amount += "L";
            renderer.draw(amount);
        }
    }

    private void drawObject(Object object, ModularGuiContext context, int x, int y, int width, int height) {
        if (object instanceof ItemStack stack) {
            SlotTheme theme = context.getTheme().getItemSlotTheme().getTheme();
            IDrawable background = theme.getBackground();
            if (drawBackground.test(object)) {
                if (background == null) background = GTGuiTextures.SLOT;
                background.draw(context, x, y, width, height, theme);
            }
            GuiDraw.drawItem(stack, x + 1, y + 1, width - 2, height - 2, 100);
        } else if (object instanceof FluidStack stack) {
            SlotTheme theme = context.getTheme().getFluidSlotTheme().getTheme();
            IDrawable background = theme.getBackground();
            if (drawBackground.test(object)) {
                if (background == null) background = GTGuiTextures.FLUID_SLOT;
                background.draw(context, x, y, width, height, theme);
            }
            GuiDraw.drawFluidTexture(stack, x + 1, y + 1, width - 2, height - 2, 0);
        } else if (object instanceof BoostableChanceEntry<?>entry) {
            drawObject(entry.getIngredient(), context, x, y, width, height);
            String chance = "~" + this.boostFunction.apply(entry) / 100 + "%";
            if (amount > 0) y -= 4;
            renderer.setPos(x + 1, y + 1);
            renderer.draw(chance);
        }
    }

    @Override
    public Icon asIcon() {
        return IDrawable.super.asIcon().size(18);
    }

    @Override
    public Widget<?> asWidget() {
        return IDrawable.super.asWidget().size(18);
    }

    @Override
    public @Nullable Object getIngredient() {
        if (object instanceof BoostableChanceEntry<?>entry) {
            return entry.getIngredient();
        }
        return object;
    }
}
