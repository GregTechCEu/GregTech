package gregtech.api.mui.drawable;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.widget.Widget;

public class GTItemDrawable implements IDrawable {

    private ItemStack stack;
    private long amount;
    private final TextRenderer renderer = new TextRenderer();

    public GTItemDrawable(ItemStack stack, long amount) {
        this.stack = stack;
        this.amount = amount;
    }

    public GTItemDrawable(ItemStack stack) {
        this.stack = stack;
    }

    public GTItemDrawable() {}

    {
        renderer.setScale(0.5f);
        renderer.setShadow(true);
        renderer.setColor(Color.WHITE.main);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiDraw.drawItem(this.stack, x, y, width, height);
        String amountText = NumberFormat.formatWithMaxDigits(amount, 3);
        renderer.setAlignment(Alignment.BottomRight, width - 1, height - 1);
        renderer.setPos(x + 1, y + 1);
        renderer.draw(amountText);
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public Icon asIcon() {
        return IDrawable.super.asIcon().size(16);
    }

    @Override
    public Widget<?> asWidget() {
        return IDrawable.super.asWidget().size(16);
    }
}
