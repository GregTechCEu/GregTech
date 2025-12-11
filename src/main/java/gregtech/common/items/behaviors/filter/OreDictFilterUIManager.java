package gregtech.common.items.behaviors.filter;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.oreglob.OreGlobCompileResult;
import gregtech.common.covers.filter.BaseFilter;
import gregtech.common.covers.filter.OreDictionaryItemFilter;
import gregtech.common.covers.filter.readers.OreDictFilterReader;
import gregtech.common.mui.widget.HighlightedTextField;
import gregtech.common.mui.widget.orefilter.OreFilterTestSlot;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.utils.BooleanConsumer;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OreDictFilterUIManager extends BaseFilterUIManager {

    @Override
    protected ModularPanel createBasePanel(ItemStack stack) {
        return super.createBasePanel(stack)
                .height(160);
    }

    @Override
    public @NotNull ModularPanel createPopupPanel(ItemStack stack, PanelSyncManager syncManager, String panelName) {
        return super.createPopupPanel(stack, syncManager, panelName)
                .size(188, 76)
                .padding(7);
    }

    @Override
    public @NotNull ModularPanel createPanel(ItemStack stack, PanelSyncManager syncManager) {
        return GTGuis.createPanel("ore_dict_filter", 100, 100);
    }

    @Override
    public @NotNull Widget<?> createWidgets(ItemStack stack, PanelSyncManager syncManager) {
        OreDictionaryItemFilter filter = (OreDictionaryItemFilter) BaseFilter.getFilterFromStack(stack);
        OreDictFilterReader filterReader = filter.getFilterReader();
        filterReader.readStack(stack);

        List<OreFilterTestSlot> oreSlots = new ArrayList<>();
        var expression = new StringSyncValue(filterReader::getExpression, filterReader::setExpression);

        BooleanConsumer setCaseSensitive = b -> {
            filterReader.setCaseSensitive(b);
            if (!syncManager.isClient()) return;
            for (var slot : oreSlots) {
                slot.updatePreview();
            }
        };

        BooleanConsumer setMatchAll = b -> {
            filter.clearCache();
            filterReader.setMatchAll(b);
            if (!syncManager.isClient()) return;
            for (var slot : oreSlots) {
                slot.setMatchAll(b);
            }
        };

        var caseSensitive = new BooleanSyncValue(filterReader::isCaseSensitive, setCaseSensitive);
        var matchAll = new BooleanSyncValue(filterReader::shouldMatchAll, setMatchAll);

        return Flow.column().coverChildren()
                .name("root.widget.col")
                .child(new HighlightedTextField()
                        .setHighlightRule(this::highlightRule)
                        .onUnfocus(() -> {
                            for (var slot : oreSlots) {
                                slot.updatePreview();
                            }
                        })
                        .name("oredict.text_field")
                        .setTextColor(Color.WHITE.darker(1))
                        .value(expression).marginBottom(4)
                        .height(18).widthRel(1f))
                .child(Flow.row().coverChildrenHeight()
                        .name("oredict.info.row")
                        .widthRel(1f)
                        .child(Flow.column().height(18)
                                .name("oredict.info.status.col")
                                .coverChildrenWidth().marginRight(2)
                                .child(GTGuiTextures.OREDICT_INFO.asWidget()
                                        .name("oredict.info.icon")
                                        .size(8).top(0)
                                        .addTooltipLine(IKey.lang("cover.ore_dictionary_filter.info")))
                                .child(new Widget<>()
                                        .name("oredict.status.icon")
                                        .size(8).bottom(0)
                                        .onUpdateListener(widget -> getStatusIcon(filterReader.getResult(), widget))
                                        .tooltipBuilder(richTooltip -> createStatusTooltip(filterReader.getResult(),
                                                richTooltip))
                                        .tooltip(tooltip -> tooltip.setAutoUpdate(true))))
                        .child(SlotGroupWidget.builder()
                                .row("XXXXX")
                                .key('X', i -> {
                                    var slot = new OreFilterTestSlot()
                                            .setGlobSupplier(filterReader::getGlob);
                                    slot.setMatchAll(filterReader.shouldMatchAll());
                                    oreSlots.add(slot);
                                    return slot.name("oredict.test_slot." + i);
                                })
                                .build().name("oredict.test.slot_group").marginRight(2))
                        .child(new CycleButtonWidget()
                                .name("oredict.button.case_sensitive")
                                .size(18).value(caseSensitive)
                                .marginRight(2)
                                .stateBackground(0, GTGuiTextures.BUTTON_CASE_SENSITIVE[0])
                                .stateBackground(1, GTGuiTextures.BUTTON_CASE_SENSITIVE[1])
                                .addTooltip(0,
                                        IKey.lang("cover.ore_dictionary_filter.button.case_sensitive.disabled"))
                                .addTooltip(1,
                                        IKey.lang("cover.ore_dictionary_filter.button.case_sensitive.enabled")))
                        .child(new CycleButtonWidget()
                                .name("oredict.button.match_all")
                                .size(18).value(matchAll)
                                .marginRight(2)
                                .stateBackground(0, GTGuiTextures.BUTTON_MATCH_ALL[0])
                                .stateBackground(1, GTGuiTextures.BUTTON_MATCH_ALL[1])
                                .addTooltip(0,
                                        IKey.lang("cover.ore_dictionary_filter.button.match_all.disabled"))
                                .addTooltip(1,
                                        IKey.lang("cover.ore_dictionary_filter.button.match_all.enabled")))
                        .child(createBlacklistUI(stack)));
    }

    protected void getStatusIcon(OreGlobCompileResult result, Widget<?> widget) {
        UITexture texture;

        if (result == null) {
            texture = GTGuiTextures.OREDICT_WAITING;
        } else if (result.getReports().length == 0) {
            texture = GTGuiTextures.OREDICT_SUCCESS;
        } else if (result.hasError()) {
            texture = GTGuiTextures.OREDICT_ERROR;
        } else {
            texture = GTGuiTextures.OREDICT_WARN;
        }
        widget.background(texture);
    }

    protected void createStatusTooltip(OreGlobCompileResult result, RichTooltip tooltip) {
        if (result == null) return;
        List<String> list = new ArrayList<>();

        int error = 0, warn = 0;
        for (OreGlobCompileResult.Report report : result.getReports()) {
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
            list.addAll(result.getInstance().toFormattedString());
        }
        tooltip.addStringLines(list);
    }

    protected String highlightRule(String text) {
        StringBuilder builder = new StringBuilder(text);
        for (int i = 0; i < builder.length(); i++) {
            switch (builder.charAt(i)) {
                case '|', '&', '^', '(', ')' -> {
                    builder.insert(i, TextFormatting.GOLD);
                    i += 2;
                }
                case '*', '?' -> {
                    builder.insert(i, TextFormatting.GREEN);
                    i += 2;
                }
                case '!' -> {
                    builder.insert(i, TextFormatting.RED);
                    i += 2;
                }
                case '\\' -> {
                    builder.insert(i++, TextFormatting.YELLOW);
                    i += 2;
                }
                default -> {
                    continue;
                }
            }
            builder.insert(i + 1, TextFormatting.RESET);
        }
        return builder.toString();
    }
}
