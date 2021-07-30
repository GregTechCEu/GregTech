package gregtech.api.terminal.gui.widgets.guide;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.istack.internal.Nullable;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import javafx.geometry.Pos;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.List;

public abstract class GuideWidget extends Widget implements IGuideWidget {
    //config
    public String ref;
    public int fill;
    public int stroke;
    public int stroke_width = 1;
    public String link;
    public List<String> hover_text;

    private static final Gson GSON = new Gson();
    protected transient GuidePageWidget page;

    public GuideWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public GuideWidget(){
        super(Position.ORIGIN, Size.ZERO);
    }

    public void updateValue(String field, JsonElement value) {
        try {
            Field f = this.getClass().getDeclaredField(field);
            f.set(this, new Gson().fromJson(value, f.getType()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSize(Size size) {
        Size oldSize = this.getSize();
        super.setSize(size);
        if (page != null) {
            page.onSizeUpdate(this, oldSize);
        }
    }

    @Override
    protected void recomputePosition() {
        Position oldPosition = getPosition();
        super.recomputePosition();
        if (page != null) {
            page.onPositionUpdate(this, oldPosition);
        }
    }

    @Override
    public void setSelfPosition(Position selfPosition) {
        super.setSelfPosition(selfPosition);
    }

    @Override
    public JsonObject getTemplate(boolean isFixed) {
        JsonObject template = new JsonObject();
        if (isFixed) {
            template.addProperty("x", 0);
            template.addProperty("y", 0);
            template.addProperty("width", 100);
            template.addProperty("height", 100);
        }
        template.addProperty("ref", ref);
        template.addProperty("stroke", stroke);
        template.addProperty("stroke_width", stroke_width);
        template.addProperty("fill", fill);
        template.addProperty("link", link);
        template.add("hover_text", new Gson().toJsonTree(hover_text));
        return template;
    }

    @Override
    public String getRef() {
        return ref;
    }

    @Override
    public Widget createStreamWidget(int x, int y, int pageWidth, JsonObject config) {
        GuideWidget widget = GSON.fromJson(config, this.getClass());
        widget.setSelfPosition(new Position(x, y));
        widget.setSize(new Size(pageWidth, 0));
        return widget.initStream(x, y, pageWidth, config);
    }

    @Override
    public Widget createFixedWidget(int x, int y, int width, int height, JsonObject config) {
        GuideWidget widget = GSON.fromJson(config, this.getClass());
        widget.setSelfPosition(new Position(x, y));
        widget.setSize(new Size(width, height));
        return widget.initFixed(x, y, width, height, config);
    }

    protected Widget initStream(int x, int y, int pageWidth, @Nullable JsonObject config) {
        return initFixed(x, y, pageWidth, 0, config);
    }

    protected Widget initFixed(int x, int y, int width, int height, @Nullable JsonObject config) {
        return this;
    }

    @Override
    public void setPage(GuidePageWidget page) {
        this.page = page;
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (link != null && isMouseOverElement(mouseX, mouseY)) {
            Position position = getPosition();
            Size size = getSize();
            drawBorder(position.x, position.y, size.width, size.height, 0xff0000ff, stroke_width);
        }
        if (hover_text != null && isMouseOverElement(mouseX, mouseY)) {
            int scrollYOffset = page.getScrollYOffset();
            GlStateManager.translate(0, scrollYOffset, 0);
            drawHoveringText(ItemStack.EMPTY, hover_text, 100, mouseX, mouseY - scrollYOffset);
            GlStateManager.translate(0, -scrollYOffset, 0);
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position position = getPosition();
        Size size = getSize();
        if(stroke != 0) {
            drawBorder(position.x, position.y, size.width, size.height, stroke, stroke_width);
        }
        if (fill != 0) {
            drawGradientRect(position.x, position.y, size.width, size.height, fill, fill);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (link != null && isMouseOverElement(mouseX, mouseY)) {
           page.jumpToRef(link);
           return true;
        }
        return false;
    }
}
