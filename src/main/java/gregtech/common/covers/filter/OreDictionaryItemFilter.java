package gregtech.common.covers.filter;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.ItemVariantMap;
import gregtech.api.unification.stack.MultiItemVariantMap;
import gregtech.api.unification.stack.SingleItemVariantMap;
import gregtech.api.util.oreglob.OreGlob;
import gregtech.api.util.oreglob.OreGlobCompileResult;
import gregtech.common.covers.filter.oreglob.impl.ImpossibleOreGlob;
import gregtech.common.covers.filter.readers.OreDictFilterReader;
import gregtech.common.gui.widget.HighlightedTextField;
import gregtech.common.gui.widget.orefilter.OreFilterTestSlot;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class OreDictionaryItemFilter extends ItemFilter {

    private final Map<Item, ItemVariantMap.Mutable<Boolean>> matchCache = new Object2ObjectOpenHashMap<>();
    private final SingleItemVariantMap<Boolean> noOreDictMatch = new SingleItemVariantMap<>();
    private final OreDictFilterReader filterReader;

    public OreDictionaryItemFilter(ItemStack stack) {
        this.filterReader = new OreDictFilterReader(stack);
        setFilterReader(this.filterReader);
        recompile();
    }

    @NotNull
    public String getExpression() {
        return this.filterReader.getExpression();
    }

    @NotNull
    public OreGlob getGlob() {
        return this.filterReader.getGlob();
    }

    protected void recompile() {
        clearCache();
        this.filterReader.recompile();
    }

    protected void clearCache() {
        this.matchCache.clear();
        this.noOreDictMatch.clear();
    }

    @Override
    public void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup) {}

    @Override
    public @NotNull ModularPanel createPopupPanel(GuiSyncManager syncManager) {
        return GTGuis.createPopupPanel("ore_dict_filter", 188, 76)
                .padding(7)
                .child(CoverWithUI.createTitleRow(getContainerStack()))
                .child(createWidgets(syncManager).top(22));
    }

    @Override
    public @NotNull ModularPanel createPanel(GuiSyncManager syncManager) {
        return GTGuis.createPanel("ore_dict_filter", 100, 100);
    }

    @Override
    public @NotNull Widget<?> createWidgets(GuiSyncManager syncManager) {
        var expression = new StringSyncValue(this.filterReader::getExpression, this.filterReader::setExpression);
        var caseSensitive = new BooleanSyncValue(this.filterReader::isCaseSensitive,
                this.filterReader::setCaseSensitive);
        var matchAll = new BooleanSyncValue(this.filterReader::shouldMatchAll, this.filterReader::setMatchAll);

        List<OreFilterTestSlot> oreSlots = new ArrayList<>();

        return new Column().widthRel(1f).coverChildrenHeight()
                .child(new HighlightedTextField()
                        .setHighlightRule(this::highlightRule)
                        .onUnfocus(() -> {
                            for (var slot : oreSlots) {
                                slot.updatePreview();
                            }
                        })
                        .setTextColor(Color.WHITE.darker(1))
                        .value(expression).marginBottom(4)
                        .height(18).widthRel(1f))
                .child(new Row().coverChildrenHeight()
                        .widthRel(1f)
                        .child(new Column().height(18)
                                .coverChildrenWidth().marginRight(2)
                                .child(GTGuiTextures.OREDICT_INFO.asWidget()
                                        .size(8).top(0)
                                        .addTooltipLine(IKey.lang("cover.ore_dictionary_filter.info")))
                                .child(new Widget<>()
                                        .size(8).bottom(0)
                                        .onUpdateListener(this::getStatusIcon)
                                        .tooltipBuilder(this::createStatusTooltip)
                                        .tooltip(tooltip -> tooltip.setAutoUpdate(true))))
                        .child(SlotGroupWidget.builder()
                                .row("XXXXX")
                                .key('X', i -> {
                                    var slot = new OreFilterTestSlot()
                                            .setGlobSupplier(this::getGlob);
                                    oreSlots.add(slot);
                                    return slot;
                                })
                                .build().marginRight(2))
                        .child(new ToggleButton()
                                .size(18).value(caseSensitive)
                                .background(GTGuiTextures.BUTTON_CASE_SENSITIVE[1])
                                .hoverBackground(GTGuiTextures.BUTTON_CASE_SENSITIVE[1])
                                .selectedBackground(GTGuiTextures.BUTTON_CASE_SENSITIVE[0])
                                .selectedHoverBackground(GTGuiTextures.BUTTON_CASE_SENSITIVE[0])
                                .marginRight(2)
                                .tooltip(tooltip -> tooltip.setAutoUpdate(true)))
                        .child(new ToggleButton()
                                .size(18).value(matchAll)
                                .background(GTGuiTextures.BUTTON_MATCH_ALL[1])
                                .hoverBackground(GTGuiTextures.BUTTON_MATCH_ALL[1])
                                .selectedHoverBackground(GTGuiTextures.BUTTON_MATCH_ALL[0])
                                .selectedBackground(GTGuiTextures.BUTTON_MATCH_ALL[0])
                                .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                                .marginRight(2))
                        .child(super.createWidgets(syncManager)));
    }

    protected void getStatusIcon(Widget<?> widget) {
        UITexture texture;
        var result = this.filterReader.getResult();

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

    protected void createStatusTooltip(Tooltip tooltip) {
        var result = this.filterReader.getResult();
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

    protected String highlightRule(StringBuilder h) {
        for (int i = 0; i < h.length(); i++) {
            switch (h.charAt(i)) {
                case '|', '&', '^', '(', ')' -> {
                    h.insert(i, TextFormatting.GOLD);
                    i += 2;
                }
                case '*', '?' -> {
                    h.insert(i, TextFormatting.GREEN);
                    i += 2;
                }
                case '!' -> {
                    h.insert(i, TextFormatting.RED);
                    i += 2;
                }
                case '\\' -> {
                    h.insert(i++, TextFormatting.YELLOW);
                    i += 2;
                }
                case '$' -> { // TODO: remove this switch case in 2.9
                    h.insert(i, TextFormatting.DARK_GREEN);
                    for (; i < h.length(); i++) {
                        switch (h.charAt(i)) {
                            case ' ', '\t', '\n', '\r' -> {}
                            case '\\' -> {
                                i++;
                                continue;
                            }
                            default -> {
                                continue;
                            }
                        }
                        break;
                    }
                }
                default -> {
                    continue;
                }
            }
            h.insert(i + 1, TextFormatting.RESET);
        }
        return h.toString();
    }

    @Override
    public MatchResult<ItemStack> match(ItemStack itemStack) {
        // "wtf is this system?? i can put any non null object here and it i will work??? $arch"
        // not anymore :thanosdaddy: -ghzdude
        var match = matchesItemStack(itemStack);
        return createResult(match, match ? itemStack.copy() : ItemStack.EMPTY, -1);
    }

    @Override
    public boolean test(ItemStack toTest) {
        return matchesItemStack(toTest);
    }

    public boolean matchesItemStack(@NotNull ItemStack itemStack) {
        var result = this.filterReader.getResult();
        if (result == null || result.hasError()) return false;
        Item item = itemStack.getItem();
        ItemVariantMap<Set<String>> oreDictEntry = OreDictUnifier.getOreDictionaryEntry(item);

        if (oreDictEntry == null) {
            // no oredict entries associated
            Boolean cached = this.noOreDictMatch.getEntry();
            if (cached == null) {
                cached = this.filterReader.getGlob().matches("");
            }
            this.matchCache.put(item, this.noOreDictMatch);
            return cached;
        }

        ItemVariantMap.Mutable<Boolean> cacheEntry = this.matchCache.get(item);
        if (cacheEntry != null) {
            Boolean cached = cacheEntry.get(itemStack);
            if (cached != null) return cached;
        }

        if (cacheEntry == null) {
            if (oreDictEntry.isEmpty()) {
                // no oredict entries associated
                Boolean cached = this.noOreDictMatch.getEntry();
                if (cached == null) {
                    cached = this.filterReader.getGlob().matches("");
                    this.noOreDictMatch.put(cached);
                }
                this.matchCache.put(item, this.noOreDictMatch);
                return cached;
            } else if (!item.getHasSubtypes() || !oreDictEntry.hasNonWildcardEntry()) {
                cacheEntry = new SingleItemVariantMap<>(); // we can just ignore metadata and use shared cache
            } else {
                cacheEntry = new MultiItemVariantMap<>(); // variant items
            }
            this.matchCache.put(item, cacheEntry);
        }
        boolean matches = this.filterReader.shouldMatchAll() ?
                this.filterReader.getGlob().matchesAll(itemStack) :
                this.filterReader.getGlob().matchesAny(itemStack);
        cacheEntry.put(itemStack, matches);
        return matches;
    }

    @Override
    public int getTransferLimit(int matchSlot, int globalTransferLimit) {
        return globalTransferLimit;
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return true;
    }
}
