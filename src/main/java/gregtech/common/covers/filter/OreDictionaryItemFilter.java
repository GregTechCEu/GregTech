package gregtech.common.covers.filter;

import com.cleanroommc.modularui.api.drawable.IKey;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.ItemVariantMap;
import gregtech.api.unification.stack.MultiItemVariantMap;
import gregtech.api.unification.stack.SingleItemVariantMap;
import gregtech.api.util.oreglob.OreGlob;
import gregtech.api.util.oreglob.OreGlobCompileResult;
import gregtech.common.covers.filter.oreglob.impl.ImpossibleOreGlob;
import gregtech.common.gui.widget.HighlightedTextField;
import gregtech.common.gui.widget.orefilter.OreFilterTestSlot;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
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
import java.util.function.Supplier;

public class OreDictionaryItemFilter extends ItemFilter {

    private final Map<Item, ItemVariantMap.Mutable<Boolean>> matchCache = new Object2ObjectOpenHashMap<>();
    private final SingleItemVariantMap<Boolean> noOreDictMatch = new SingleItemVariantMap<>();

    private OreGlob glob = ImpossibleOreGlob.getInstance();
    private boolean error;
    private final OreDictionaryFilterReader filterReader;

    public OreDictionaryItemFilter(ItemStack stack) {
        this.filterReader = new OreDictionaryFilterReader(stack, 0);
        setFilterReader(this.filterReader);
        recompile();
    }

    @NotNull
    public String getExpression() {
        return this.filterReader.getExpression();
    }

    @NotNull
    public OreGlob getGlob() {
        return this.glob;
    }

    protected void recompile() {
        clearCache();
        String expr = this.filterReader.getExpression();
        if (!expr.isEmpty()) {
            OreGlobCompileResult result = OreGlob.compile(expr, !this.filterReader.isCaseSensitive());
            this.glob = result.getInstance();
            this.error = result.hasError();
        } else {
            this.glob = ImpossibleOreGlob.getInstance();
            this.error = true;
        }
    }

    protected void clearCache() {
        this.matchCache.clear();
        this.noOreDictMatch.clear();
    }

    @Override
    public void initUI(Consumer<gregtech.api.gui.Widget> widgetGroup) {
//        ItemOreFilterTestSlot[] testSlot = new ItemOreFilterTestSlot[5];
//        for (int i = 0; i < testSlot.length; i++) {
//            ItemOreFilterTestSlot slot = new ItemOreFilterTestSlot(20 + 22 * i, 0);
//            slot.setGlob(getGlob());
//            slot.setMatchAll(this.filterReader.shouldMatchAll());
//            widgetGroup.accept(slot);
//            testSlot[i] = slot;
//        }
//        OreGlobCompileStatusWidget compilationStatus = new OreGlobCompileStatusWidget(10, 10);
//
//
//        HighlightedTextField textField = new HighlightedTextField(14, 26, 152, 14,
//                filterReader::getExpression,
//                s -> {
//                    this.filterReader.setExpression(s);
//                    recompile(compileCallback);
//                });
//        compilationStatus.setTextField(textField);
//
//        widgetGroup.accept(new ImageWidget(10, 0, 7, 7, gregtech.api.gui.GuiTextures.ORE_FILTER_INFO)
//                .setTooltip("cover.ore_dictionary_filter.info"));
//        widgetGroup.accept(compilationStatus);
//        widgetGroup.accept(new DrawableWidget(10, 22, 156, 16)
//                .setBackgroundDrawer((mouseX, mouseY, partialTicks, context, widget) -> {
//                    gregtech.api.gui.Widget.drawGradientRect(widget.getPosition().x, widget.getPosition().y,
//                            widget.getSize().width, widget.getSize().height,
//                            0xFF808080, 0xFF808080, false);
//                    gregtech.api.gui.Widget.drawGradientRect(widget.getPosition().x + 1, widget.getPosition().y + 1,
//                            widget.getSize().width - 2, widget.getSize().height - 2,
//                            0xFF000000, 0xFF000000, false);
//                }));
//        widgetGroup.accept(textField
//                .setHighlightRule(h -> {
//                    String t = h.getOriginalText();
//                    for (int i = 0; i < t.length(); i++) {
//                        switch (t.charAt(i)) {
//                            case '|', '&', '^', '(', ')' -> h.format(i, TextFormatting.GOLD);
//                            case '*', '?' -> h.format(i, TextFormatting.GREEN);
//                            case '!' -> h.format(i, TextFormatting.RED);
//                            case '\\' -> h.format(i++, TextFormatting.YELLOW);
//                            case '$' -> { // TODO: remove this switch case in 2.9
//                                h.format(i, TextFormatting.DARK_GREEN);
//                                for (; i < t.length(); i++) {
//                                    switch (t.charAt(i)) {
//                                        case ' ', '\t', '\n', '\r' -> {}
//                                        case '\\' -> {
//                                            i++;
//                                            continue;
//                                        }
//                                        default -> {
//                                            continue;
//                                        }
//                                    }
//                                    break;
//                                }
//                            }
//                            default -> {
//                                continue;
//                            }
//                        }
//                        h.format(i + 1, TextFormatting.RESET);
//                    }
//                }).setMaxLength(64));
//        widgetGroup.accept(new ForcedInitialSyncImageCycleButtonWidget(130, 38, 18, 18,
//                gregtech.api.gui.GuiTextures.ORE_FILTER_BUTTON_CASE_SENSITIVE, filterReader::isCaseSensitive,
//                caseSensitive -> {
//                    this.filterReader.setCaseSensitive(caseSensitive);
//                    recompile(compileCallback);
//                }).setTooltipHoverString(
//                        i -> "cover.ore_dictionary_filter.button.case_sensitive." + (i == 0 ? "disabled" : "enabled")));
//        widgetGroup.accept(new ForcedInitialSyncImageCycleButtonWidget(148, 38, 18, 18,
//                gregtech.api.gui.GuiTextures.ORE_FILTER_BUTTON_MATCH_ALL, filterReader::shouldMatchAll,
//                matchAll -> {
//                    this.filterReader.setMatchAll(matchAll);
//                    clearCache();
//                    for (ItemOreFilterTestSlot slot : testSlot) {
//                        slot.setMatchAll(matchAll);
//                    }
//                }).setTooltipHoverString(
//                        i -> "cover.ore_dictionary_filter.button.match_all." + (i == 0 ? "disabled" : "enabled")));
    }

    @Override
    public @NotNull ModularPanel createPopupPanel(GuiSyncManager syncManager) {
        return GTGuis.createPopupPanel("ore_dict_filter", 100, 100);
    }

    @Override
    public @NotNull ModularPanel createPanel(GuiSyncManager syncManager) {
        return GTGuis.createPanel("ore_dict_filter", 100, 100);
    }

    @Override
    @NotNull
    public ParentWidget<?> createWidgets(GuiSyncManager syncManager) {
        var expression = new StringSyncValue(this.filterReader::getExpression, this.filterReader::setExpression);
        var caseSensitive = new BooleanSyncValue(this.filterReader::isCaseSensitive, this.filterReader::setCaseSensitive);
        var matchAll = new BooleanSyncValue(this.filterReader::shouldMatchAll, this.filterReader::setMatchAll);

        List<OreFilterTestSlot> oreSlots = new ArrayList<>();

        return new Column().widthRel(1f).coverChildrenHeight()
                .top(22).margin(7)
                .child(new HighlightedTextField()
                        .setHighlightRule(this::highlightRule)
                        .onUnfocus(() -> {
                            for (var slot : oreSlots) {
                                slot.updatePreview();
                            }
                        })
                        .value(expression).marginBottom(4)
                        .height(18).widthRel(1f))
                .child(new Row().coverChildrenHeight()
                        .widthRel(1f)
                        .child(GTGuiTextures.OREDICT_INFO.asWidget().marginRight(4)
                                .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                                .tooltipBuilder(tooltip -> tooltip.addLine(glob.toString())))
                        .child(SlotGroupWidget.builder()
                                .row("XXXXX")
                                .key('X', i -> {
                                    var slot = new OreFilterTestSlot()
                                            .setGlobSupplier(this::getGlob);
                                    oreSlots.add(slot);
                                    return slot;
                                })
                                .build().marginRight(4))
                        .child(new ToggleButton()
                                .size(18).value(caseSensitive)
                                // todo fix the textures for hovering
                                .background(GTGuiTextures.MC_BUTTON_DISABLED)
                                .selectedBackground(GTGuiTextures.MC_BUTTON)
                                .marginRight(4)
                                .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                                .addTooltipLine(IKey.lang("cover.ore_dictionary_filter.case_sensitive",
                                        caseSensitive.getBoolValue())))
                        .child(new ToggleButton()
                                .size(18).value(matchAll)
                                .background(GTGuiTextures.MC_BUTTON_DISABLED)
                                .selectedBackground(GTGuiTextures.MC_BUTTON)
                                .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                                .addTooltipLine(IKey.lang("cover.ore_dictionary_filter.match_all",
                                        matchAll.getBoolValue()))));
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
    public MatchResult<Integer> matchItemStack(ItemStack itemStack) {
        // "wtf is this system?? i can put any non null object here and it i will work??? $arch"
        // not anymore :thanosdaddy: -ghzdude
        var match = matchesItemStack(itemStack) ? Match.SUCCEED : Match.FAIL;
        return ItemFilter.createResult(match, -1);
    }

    public boolean matchesItemStack(@NotNull ItemStack itemStack) {
        if (this.error) return false;
        Item item = itemStack.getItem();
        ItemVariantMap<Set<String>> oreDictEntry = OreDictUnifier.getOreDictionaryEntry(item);

        if (oreDictEntry == null) {
            // no oredict entries associated
            Boolean cached = this.noOreDictMatch.getEntry();
            if (cached == null) {
                cached = this.glob.matches("");
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
                    cached = this.glob.matches("");
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
        boolean matches = this.filterReader.shouldMatchAll() ? this.glob.matchesAll(itemStack) : this.glob.matchesAny(itemStack);
        cacheEntry.put(itemStack, matches);
        return matches;
    }

    @Override
    public int getSlotTransferLimit(int matchSlot, int globalTransferLimit) {
        return globalTransferLimit;
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return true;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
//        tag.setString("OreDictionaryFilter", expression);
//        if (this.caseSensitive) tag.setBoolean("caseSensitive", true);
//        if (this.matchAll) tag.setBoolean("matchAll", true);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
//        this.expression = tag.getString("OreDictionaryFilter");
//        this.caseSensitive = tag.getBoolean("caseSensitive");
//        this.matchAll = tag.getBoolean("matchAll");
        recompile();
    }

//    public static class ForcedInitialSyncImageCycleButtonWidget extends ImageCycleButtonWidget {
//
//        private final BooleanConsumer updater;
//
//        public ForcedInitialSyncImageCycleButtonWidget(int xPosition, int yPosition, int width, int height,
//                                                       TextureArea buttonTexture, BooleanSupplier supplier,
//                                                       BooleanConsumer updater) {
//            super(xPosition, yPosition, width, height, buttonTexture, supplier, updater);
//            this.currentOption = 0;
//            this.updater = updater;
//        }
//
//        @Override
//        public void readUpdateInfo(int id, PacketBuffer buffer) {
//            if (id == 1) {
//                int currentOptionCache = this.currentOption;
//                super.readUpdateInfo(id, buffer);
//                if (this.currentOption != currentOptionCache) {
//                    this.updater.apply(currentOption >= 1); // call updater to apply necessary state changes
//                }
//            } else {
//                super.readUpdateInfo(id, buffer);
//            }
//        }
//    }

    protected class OreDictionaryFilterReader extends BaseFilterReader {

        private static final String EXPRESSION = "expression";
        private static final String CASE_SENSITIVE = "case_sensitive";
        private static final String MATCH_ALL = "match_all";

        public OreDictionaryFilterReader(ItemStack container, int slots) {
            super(container, slots);
//            setExpression("");
            setCaseSensitive(true);
            setMatchAll(true);
        }

        @Override
        public Supplier<Integer> getMaxStackSizer() {
            return () -> 1;
        }

        public void setExpression(String expression) {
            if (this.getExpression().equals(expression)) return;
            getStackTag().setString(EXPRESSION, expression);
            recompile();
            markDirty();
        }

        public String getExpression() {
            return getStackTag().getString(EXPRESSION);
        }

        public void setCaseSensitive(boolean caseSensitive) {
            if (this.isCaseSensitive() == caseSensitive) return;
            getStackTag().setBoolean(CASE_SENSITIVE, caseSensitive);
            markDirty();
        }

        public boolean isCaseSensitive() {
            return getStackTag().getBoolean(CASE_SENSITIVE);
        }

        public void setMatchAll(boolean matchAll) {
            if (this.shouldMatchAll() == matchAll) return;
            getStackTag().setBoolean(MATCH_ALL, matchAll);
            markDirty();
        }

        /**
         * {@code false} requires any of the entry to be match in order for the match to be success, {@code true} requires
         * all entries to match
         */
        public boolean shouldMatchAll() {
            return getStackTag().getBoolean(MATCH_ALL);
        }
    }

}
