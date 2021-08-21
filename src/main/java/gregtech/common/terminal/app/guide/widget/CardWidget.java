package gregtech.common.terminal.app.guide.widget;

import com.google.gson.JsonObject;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.common.terminal.app.guideeditor.widget.configurator.BooleanConfigurator;
import gregtech.common.terminal.app.guideeditor.widget.configurator.NumberConfigurator;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

public class CardWidget extends GuideWidget{
    public final static String NAME = "card";

    //config
    public int width;
    public int height;
    public boolean isShadow;

    @Override
    public String getRegistryName() {
        return NAME;
    }

    @Override
    public JsonObject getTemplate(boolean isFixed) {
        JsonObject template = super.getTemplate(isFixed);
        template.addProperty("fill", -3745585);
        template.addProperty("width", 120);
        template.addProperty("height", 60);
        template.addProperty("isShadow", true);
        return template;
    }

    @Override
    public void loadConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, boolean isFixed, Consumer<String> needUpdate) {
        super.loadConfigurator(group, config, isFixed, needUpdate);
        if (!isFixed) {
            group.addWidget(new NumberConfigurator(group, config, "width").setOnUpdated(needUpdate));
            group.addWidget(new NumberConfigurator(group, config, "height").setOnUpdated(needUpdate));
        }
        group.addWidget(new BooleanConfigurator(group, config, "isShadow", true).setOnUpdated(needUpdate));
    }

    @Override
    protected Widget initStream() {
        int pageWidth = getSize().width;
        int x = getSelfPosition().x;
        int y = getSelfPosition().y;
        if (page != null) {
            x = page.getMargin();
            pageWidth = page.getPageWidth() - 2 * x;
        }
        this.setSelfPosition(new Position(x + (pageWidth - width) / 2, y));
        return initFixed();
    }

    @Override
    protected Widget initFixed() {
        this.setSize(new Size(width, height));
        return this;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (isShadow) {
            int x = getPosition().x;
            int y = getPosition().y;
            int width = getSize().width;
            int height = getSize().height;
            drawGradientRect(x + 5, y + height, width - 5, 5, 0x4f000000, 0, false);
            drawGradientRect(x + width, y + 5, 5, height - 5, 0x4f000000, 0, true);

            float startAlpha = (float) (0x4f) / 255.0F;
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
            x += width;
            y += height;
            width = 5;
            height = 5;
            buffer.pos(x, y, 0).color(0, 0, 0, startAlpha).endVertex();
            buffer.pos(x, y + height, 0).color(0, 0, 0, 0).endVertex();
            buffer.pos(x + width, y + height, 0).color(0, 0, 0, 0).endVertex();

            buffer.pos(x, y, 0).color(0, 0, 0, startAlpha).endVertex();
            buffer.pos(x + width, y + height, 0).color(0, 0, 0, 0).endVertex();
            buffer.pos(x + width, y, 0).color(0, 0, 0, 0).endVertex();
            tessellator.draw();
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
        }
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }
}
