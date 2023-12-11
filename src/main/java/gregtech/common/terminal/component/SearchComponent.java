package gregtech.common.terminal.component;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.TextFieldWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.os.menu.IMenuComponent;
import gregtech.api.terminal.util.ISearch;
import gregtech.api.terminal.util.SearchEngine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class SearchComponent<T> extends WidgetGroup implements IMenuComponent {

    private final static TextureArea SEARCHING = TextureArea.fullImage("textures/gui/terminal/icon/search_hover.png");
    private final static int SIZE = 10;
    private final SearchEngine<T> engine;
    private final List<Tuple<T, String>> results;
    private final IWidgetSearch<T> search;
    private boolean isUp;
    private int offset;

    public SearchComponent(IWidgetSearch<T> search) {
        super(0, 0, 280, 20);
        this.search = search;
        results = new ArrayList<>();
        engine = new SearchEngine<>(search, r -> results.add(new Tuple<>(r, search.resultDisplay(r))));
        this.addWidget(new TextFieldWidget(0, 5, 280, 20, new ColorRectTexture(0xcf000000), null, null)
                .setValidator(s -> true)
                .setTextResponder(s -> {
                    results.clear();
                    engine.searchWord(s);
                    offset = 0;
                }, true));
    }

    @Override
    public IGuiTexture buttonIcon() {
        return SEARCHING;
    }

    @Override
    public String hoverText() {
        return "terminal.component.searching";
    }

    public void setUp(boolean up) {
        isUp = up;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        int x = getPosition().x;
        int width = getSize().width;
        int height = getSize().height;
        int y = (isUp ? -results.size() : 1) * height + getPosition().y;
        int max = Math.min(offset + SIZE, results.size());
        for (int i = offset; i < max; i++) {
            Tuple<T, String> result = results.get(i);
            drawSolidRect(x, y, width, height, 0xAA000000);
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            fontRenderer.drawString(result.getSecond(), x + 4, y + (height - fontRenderer.FONT_HEIGHT) / 2 + 1, -1);
            if (isMouseOver(x, y, width, height, mouseX, mouseY)) {
                drawBorder(x + 1, y + 1, width - 2, height - 2, -1, 1);
            }
            y += height;
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        int x = getPosition().x;
        int width = getSize().width;
        int height = getSize().height;
        int y = (isUp ? -results.size() : 1) * height + getPosition().y;
        int max = Math.min(offset + SIZE, results.size());
        for (int i = offset; i < max; i++) {
            Tuple<T, String> result = results.get(i);
            if (isMouseOver(x, y, width, height, mouseX, mouseY)) {
                search.selectResult(result.getFirst());
                engine.dispose();
                return true;
            }
            y += height;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        int x = getPosition().x;
        int width = getSize().width;
        int height = getSize().height;
        int y = (isUp ? -results.size() : 1) * height + getPosition().y;
        int max = Math.min(offset + SIZE, results.size());
        if (isMouseOver(x, y, width, height * (max - offset), mouseX, mouseY)) {
            offset = MathHelper.clamp(offset + (wheelDelta > 0 ? -1 : +1), 0, Math.max(0, results.size() - SIZE));
            return true;
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    public interface IWidgetSearch<T> extends ISearch<T> {

        String resultDisplay(T result);

        void selectResult(T result);
    }
}
