package gregtech.common.mui.widget;

import net.minecraft.client.gui.FontRenderer;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IHoverable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.drawable.text.RichText;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerIngredientProvider;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.TextFieldTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.HoveredWidgetList;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.scroll.ScrollArea;
import com.cleanroommc.modularui.widget.scroll.ScrollData;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Box;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ScrollableTextWidget extends Widget<ScrollableTextWidget>
                                  implements IRichTextBuilder<ScrollableTextWidget>, Interactable, IViewport,
                                  RecipeViewerIngredientProvider {

    private final RichText text = new RichText();
    private Consumer<IRichTextBuilder<?>> builder;
    private boolean dirty = false;
    private boolean autoUpdate = false;
    private Object lastIngredient;

    private final ScrollArea scroll = new ScrollArea();
    private final TextRenderer renderer = new ScrollingTextRenderer();

    public ScrollableTextWidget() {
        listenGuiAction((IGuiAction.MouseReleased) mouseButton -> {
            this.scroll.mouseReleased(getContext());
            return false;
        });
    }

    @Override
    public void onInit() {
        this.scroll.setScrollData(ScrollData.of(GuiAxis.Y, false, 4));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.scroll.drag(getContext().getAbsMouseX(), getContext().getAbsMouseY());
    }

    public void markDirty() {
        this.dirty = true;
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        super.drawForeground(context);
        if (getHoveredElement() instanceof IHoverable hoverable) {
            hoverable.onHover();
            RichTooltip tooltip = hoverable.getTooltip();
            if (tooltip != null) {
                tooltip.draw(context);
            }
        }
        if (getHoveredElement() instanceof IIcon icon &&
                icon.getRootDrawable() instanceof RecipeViewerIngredientProvider provider) {
            lastIngredient = provider.getIngredient();
        } else {
            lastIngredient = null;
        }
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        ModularGuiContext context = getContext();
        if (this.scroll.mouseClicked(context)) {
            return Result.STOP;
        }
        if (getHoveredElement() instanceof Interactable interactable) {
            return interactable.onMousePressed(mouseButton);
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        if (this.scroll.mouseScroll(getContext())) {
            return true;
        }
        if (getHoveredElement() instanceof Interactable interactable) {
            return interactable.onMouseScroll(scrollDirection, amount);
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        this.scroll.mouseReleased(getContext());
        if (getHoveredElement() instanceof Interactable interactable) {
            return interactable.onMouseRelease(mouseButton);
        }
        return false;
    }

    @Nullable
    public Object getHoveredElement() {
        if (!isHovering()) return null;
        FontRenderer fr = getContext().getFontRenderer();
        int x = getContext().getMouseX(), y = getContext().getMouseY();
        return this.text.getHoveringElement(fr, x, y + getScrollY()); // undo scrolling
    }

    @Override
    public Area getArea() {
        return getScrollArea();
    }

    public ScrollArea getScrollArea() {
        return this.scroll;
    }

    @Override
    public void transformChildren(IViewportStack stack) {
        stack.translate(0, -getScrollY());
    }

    @Override
    public void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (isInside(stack, x, y)) {
            widgets.add(this, stack, getAdditionalHoverInfo(stack, x, y));
        }
    }

    @Override
    public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {}

    @Override
    public void onResized() {
        this.scroll.getScrollY().clamp(this.scroll);
    }

    @Override
    public boolean canHover() {
        return super.canHover() ||
                this.scroll.isInsideScrollbarArea(getContext().getMouseX(), getContext().getMouseY());
    }

    @Override
    public void preDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.applyAtZero(this.scroll, context);
        } else {
            drawText(context);
        }
    }

    private void drawText(ModularGuiContext context) {
        if (this.autoUpdate || this.dirty) {
            if (this.builder != null) {
                this.text.clearText();
                this.builder.accept(this.text);
            }
            this.dirty = false;
        }

        Alignment alignment = this.text.getAlignment();
        Area area = getArea();
        Box padding = area.getPadding();
        WidgetThemeEntry<TextFieldTheme> textThemeEntry = context.getTheme().getTextFieldTheme();
        TextFieldTheme textTheme = textThemeEntry.getTheme();

        this.text.compileAndDraw(this.renderer, context, true);

        // this isn't perfect, but i hope it's good enough
        int diff = (int) Math.ceil((this.renderer.getLastTrimmedHeight() - area.h()) / 2);
        this.scroll.getScrollY().setScrollSize(area.h() + Math.max(0, diff));

        // this is responsible for centering the text if there's not enough to scroll
        int x = padding.getLeft();
        int y = (int) (area.h() * alignment.y);
        y -= (int) (this.renderer.getLastTrimmedHeight() * alignment.y);
        y = Math.min(Math.max(padding.getTop(), y), area.h() - padding.getBottom());
        this.text.setupRenderer(this.renderer, x, y - getScrollY(), area.paddedWidth(), area.paddedHeight(),
                textTheme.getTextColor(), textTheme.getTextShadow());

        this.text.compileAndDraw(this.renderer, context, false);
    }

    @Override
    public void postDraw(ModularGuiContext context, boolean transformed) {
        if (!transformed) {
            Stencil.remove();
            WidgetThemeEntry<WidgetTheme> theme = context.getTheme().getScrollbarTheme();
            this.scroll.drawScrollbar(context, theme.getTheme(isHovering()), theme.getTheme().getBackground());
        }
    }

    public int getScrollY() {
        return this.scroll.getScrollY() != null ? this.scroll.getScrollY().getScroll() : 0;
    }

    @Override
    public IRichTextBuilder<?> getRichText() {
        return this.text;
    }

    /**
     * Sets the auto update property. If auto update is true the text will be deleted each time it is drawn.
     * If {@link #builder} is not null, it will then be called.
     *
     * @param autoUpdate auto update
     * @return this
     */
    public ScrollableTextWidget autoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        return this;
    }

    /**
     * A builder which is called every time before drawing when {@link #dirty} is true.
     *
     * @param builder text builder
     * @return this
     */
    public ScrollableTextWidget textBuilder(Consumer<IRichTextBuilder<?>> builder) {
        this.builder = builder;
        markDirty();
        return this;
    }

    @Override
    public @Nullable Object getIngredient() {
        return this.lastIngredient;
    }

    public static class ScrollingTextRenderer extends TextRenderer {

        @Override
        protected int getStartX(float maxWidth, float lineWidth) {
            return super.getStartX(this.maxWidth, lineWidth);
        }

        public int getLastY() {
            return (int) lastY;
        }

        public int getLastX() {
            return (int) lastX;
        }

        @Override
        protected int getStartY(float height) {
            return this.y; // always draw at the top
        }
    }
}
