package gregtech.common.covers.filter;

import gregtech.api.cover.CoverWithUI;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.ItemVariantMap;
import gregtech.api.unification.stack.MultiItemVariantMap;
import gregtech.api.unification.stack.SingleItemVariantMap;
import gregtech.api.util.oreglob.OreGlobCompileResult;
import gregtech.common.covers.filter.readers.OreDictFilterReader;
import gregtech.common.mui.widget.HighlightedTextField;
import gregtech.common.mui.widget.orefilter.OreFilterTestSlot;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OreDictionaryItemFilter extends BaseFilter {

    private final Map<Item, ItemVariantMap.Mutable<Boolean>> matchCache = new Object2ObjectOpenHashMap<>();
    private final SingleItemVariantMap<Boolean> noOreDictMatch = new SingleItemVariantMap<>();
    private final OreDictFilterReader filterReader;

    public OreDictionaryItemFilter(ItemStack stack) {
        this.filterReader = new OreDictFilterReader(stack);
        recompile();
    }

    @Override
    public OreDictFilterReader getFilterReader() {
        return filterReader;
    }

    @NotNull
    public String getExpression() {
        return this.filterReader.getExpression();
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
    public @NotNull ModularPanel createPopupPanel(PanelSyncManager syncManager, String panelName) {
        return GTGuis.createPopupPanel(panelName, 188, 76, false)
                .padding(7)
                .child(CoverWithUI.createTitleRow(getContainerStack()))
                .child(createWidgets(syncManager).top(22));
    }

    @Override
    public @NotNull ModularPanel createPanel(PanelSyncManager syncManager) {
        return GTGuis.createPanel("ore_dict_filter", 100, 100);
    }

    @Override
    public @NotNull Widget<?> createWidgets(PanelSyncManager syncManager) {
        List<OreFilterTestSlot> oreSlots = new ArrayList<>();
        var expression = new StringSyncValue(this.filterReader::getExpression, this.filterReader::setExpression);

        BooleanConsumer setCaseSensitive = b -> {
            this.filterReader.setCaseSensitive(b);
            if (!syncManager.isClient()) return;
            for (var slot : oreSlots) {
                slot.updatePreview();
            }
        };

        BooleanConsumer setMatchAll = b -> {
            this.clearCache();
            this.filterReader.setMatchAll(b);
            if (!syncManager.isClient()) return;
            for (var slot : oreSlots) {
                slot.setMatchAll(b);
            }
        };

        var caseSensitive = new BooleanSyncValue(this.filterReader::isCaseSensitive, setCaseSensitive);
        var matchAll = new BooleanSyncValue(this.filterReader::shouldMatchAll, setMatchAll);

        return Flow.column().widthRel(1f).coverChildrenHeight()
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
                .child(Flow.row().coverChildrenHeight()
                        .widthRel(1f)
                        .child(Flow.column().height(18)
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
                                            .setGlobSupplier(this.filterReader::getGlob);
                                    slot.setMatchAll(this.filterReader.shouldMatchAll());
                                    oreSlots.add(slot);
                                    return slot;
                                })
                                .build().marginRight(2))
                        .child(new CycleButtonWidget()
                                .size(18).value(caseSensitive)
                                .marginRight(2)
                                .stateBackground(0, GTGuiTextures.BUTTON_CASE_SENSITIVE[0])
                                .stateBackground(1, GTGuiTextures.BUTTON_CASE_SENSITIVE[1])
                                .addTooltip(0,
                                        IKey.lang("cover.ore_dictionary_filter.button.case_sensitive.disabled"))
                                .addTooltip(1,
                                        IKey.lang("cover.ore_dictionary_filter.button.case_sensitive.enabled")))
                        .child(new CycleButtonWidget()
                                .size(18).value(matchAll)
                                .marginRight(2)
                                .stateBackground(0, GTGuiTextures.BUTTON_MATCH_ALL[0])
                                .stateBackground(1, GTGuiTextures.BUTTON_MATCH_ALL[1])
                                .addTooltip(0,
                                        IKey.lang("cover.ore_dictionary_filter.button.match_all.disabled"))
                                .addTooltip(1,
                                        IKey.lang("cover.ore_dictionary_filter.button.match_all.enabled")))
                        .child(createBlacklistUI()));
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

    protected void createStatusTooltip(RichTooltip tooltip) {
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

    @Override
    public MatchResult matchItem(ItemStack itemStack) {
        // "wtf is this system?? i can put any non null object here and it i will work??? $arch"
        // not anymore :thanosdaddy: -ghzdude
        var match = matchesItemStack(itemStack);
        return MatchResult.create(match != isBlacklistFilter(), match ? itemStack.copy() : ItemStack.EMPTY, -1);
    }

    @Override
    public boolean testItem(ItemStack toTest) {
        return matchesItemStack(toTest);
    }

    @Override
    public FilterType getType() {
        return FilterType.ITEM;
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
    public boolean showGlobalTransferLimitSlider() {
        return true;
    }
}
