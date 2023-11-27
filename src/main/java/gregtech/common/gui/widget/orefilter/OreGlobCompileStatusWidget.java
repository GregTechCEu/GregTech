package gregtech.common.gui.widget.orefilter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.util.oreglob.OreGlobCompileResult;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OreGlobCompileStatusWidget extends Widget {

    @Nullable
    private OreGlobCompileResult result;

    private TextFieldWidget2 textField;

    public OreGlobCompileStatusWidget(int x, int y) {
        super(x, y, 7, 7);
    }

    public void setCompileResult(@Nullable OreGlobCompileResult result) {
        this.result = result;
    }

    public void setTextField(TextFieldWidget2 textField) {
        this.textField = textField;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        TextureArea texture;
        if (this.result == null || this.textField.isFocused()) {
            texture = GuiTextures.ORE_FILTER_WAITING;
        } else if (this.result.getReports().length == 0) {
            texture = GuiTextures.ORE_FILTER_SUCCESS;
        } else if (this.result.hasError()) {
            texture = GuiTextures.ORE_FILTER_ERROR;
        } else {
            texture = GuiTextures.ORE_FILTER_WARN;
        }
        texture.draw(this.getPosition().x, this.getPosition().y, this.getSize().width, this.getSize().height);
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (this.result != null && !this.textField.isFocused() && isMouseOverElement(mouseX, mouseY)) {
            List<String> list = new ArrayList<>();
            int error = 0, warn = 0;
            for (OreGlobCompileResult.Report report : this.result.getReports()) {
                if (report.isError()) error++;
                else warn++;
                list.add((report.isError() ? TextFormatting.RED : TextFormatting.GOLD) + report.toString());
            }
            if (error > 0) {
                if (warn > 0) {
                    list.add(0, I18n.format("cover.ore_dictionary_filter.status.err_warn", error, warn));
                } else {
                    list.add(0, I18n.format("cover.ore_dictionary_filter.status.err", error));
                }
            } else {
                if (warn > 0) {
                    list.add(0, I18n.format("cover.ore_dictionary_filter.status.warn", warn));
                } else {
                    list.add(I18n.format("cover.ore_dictionary_filter.status.no_issues"));
                }
                list.add("");
                list.add(I18n.format("cover.ore_dictionary_filter.status.explain"));
                list.add("");
                list.addAll(this.result.getInstance().toFormattedString());
            }

            drawHoveringText(ItemStack.EMPTY, list, 300, mouseX, mouseY);
        }
    }
}
