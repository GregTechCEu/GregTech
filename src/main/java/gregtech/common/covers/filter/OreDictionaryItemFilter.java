package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.DrawableWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.ItemVariantMap;
import gregtech.api.unification.stack.MultiItemVariantMap;
import gregtech.api.unification.stack.SingleItemVariantMap;
import gregtech.api.util.function.BooleanConsumer;
import gregtech.api.util.oreglob.OreGlob;
import gregtech.api.util.oreglob.OreGlobCompileResult;
import gregtech.common.covers.filter.oreglob.impl.ImpossibleOreGlob;
import gregtech.common.gui.widget.HighlightedTextField;
import gregtech.common.gui.widget.orefilter.ItemOreFilterTestSlot;
import gregtech.common.gui.widget.orefilter.OreGlobCompileStatusWidget;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class OreDictionaryItemFilter extends ItemFilter {

    private final Map<Item, ItemVariantMap.Mutable<Boolean>> matchCache = new Object2ObjectOpenHashMap<>();
    private final SingleItemVariantMap<Boolean> noOreDictMatch = new SingleItemVariantMap<>();

    protected String expression = "";

    private OreGlob glob = ImpossibleOreGlob.getInstance();
    private boolean error;

    private boolean caseSensitive;
    /**
     * {@code false} requires any of the entry to be match in order for the match to be success, {@code true} requires
     * all entries to match
     */
    private boolean matchAll;

    @NotNull
    public String getExpression() {
        return expression;
    }

    @NotNull
    public OreGlob getGlob() {
        return this.glob;
    }

    protected void recompile(@Nullable Consumer<@Nullable OreGlobCompileResult> callback) {
        clearCache();
        String expr = this.expression;
        if (!expr.isEmpty()) {
            OreGlobCompileResult result = OreGlob.compile(expr, !this.caseSensitive);
            this.glob = result.getInstance();
            this.error = result.hasError();
            if (callback != null) callback.accept(result);
        } else {
            this.glob = ImpossibleOreGlob.getInstance();
            this.error = true;
            if (callback != null) callback.accept(null);
        }
    }

    protected void clearCache() {
        this.matchCache.clear();
        this.noOreDictMatch.clear();
    }

    @Override
    public void initUI(Consumer<Widget> widgetGroup) {
        ItemOreFilterTestSlot[] testSlot = new ItemOreFilterTestSlot[5];
        for (int i = 0; i < testSlot.length; i++) {
            ItemOreFilterTestSlot slot = new ItemOreFilterTestSlot(20 + 22 * i, 0);
            slot.setGlob(getGlob());
            slot.setMatchAll(this.matchAll);
            widgetGroup.accept(slot);
            testSlot[i] = slot;
        }
        OreGlobCompileStatusWidget compilationStatus = new OreGlobCompileStatusWidget(10, 10);

        Consumer<@Nullable OreGlobCompileResult> compileCallback = result -> {
            compilationStatus.setCompileResult(result);
            for (ItemOreFilterTestSlot slot : testSlot) {
                slot.setGlob(getGlob());
            }
        };

        HighlightedTextField textField = new HighlightedTextField(14, 26, 152, 14, () -> this.expression,
                s -> {
                    if (s.equals(this.expression)) return;
                    this.expression = s;
                    markDirty();
                    recompile(compileCallback);
                });
        compilationStatus.setTextField(textField);

        widgetGroup.accept(new ImageWidget(10, 0, 7, 7, GuiTextures.ORE_FILTER_INFO)
                .setTooltip("cover.ore_dictionary_filter.info"));
        widgetGroup.accept(compilationStatus);
        widgetGroup.accept(new DrawableWidget(10, 22, 156, 16)
                .setBackgroundDrawer((mouseX, mouseY, partialTicks, context, widget) -> {
                    Widget.drawGradientRect(widget.getPosition().x, widget.getPosition().y,
                            widget.getSize().width, widget.getSize().height,
                            0xFF808080, 0xFF808080, false);
                    Widget.drawGradientRect(widget.getPosition().x + 1, widget.getPosition().y + 1,
                            widget.getSize().width - 2, widget.getSize().height - 2,
                            0xFF000000, 0xFF000000, false);
                }));
        widgetGroup.accept(textField
                .setHighlightRule(h -> {
                    String t = h.getOriginalText();
                    for (int i = 0; i < t.length(); i++) {
                        switch (t.charAt(i)) {
                            case '|', '&', '^', '(', ')' -> h.format(i, TextFormatting.GOLD);
                            case '*', '?' -> h.format(i, TextFormatting.GREEN);
                            case '!' -> h.format(i, TextFormatting.RED);
                            case '\\' -> h.format(i++, TextFormatting.YELLOW);
                            case '$' -> { // TODO: remove this switch case in 2.9
                                h.format(i, TextFormatting.DARK_GREEN);
                                for (; i < t.length(); i++) {
                                    switch (t.charAt(i)) {
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
                        h.format(i + 1, TextFormatting.RESET);
                    }
                }).setMaxLength(64));
        widgetGroup.accept(new ForcedInitialSyncImageCycleButtonWidget(130, 38, 18, 18,
                GuiTextures.ORE_FILTER_BUTTON_CASE_SENSITIVE, () -> this.caseSensitive, caseSensitive -> {
                    if (this.caseSensitive == caseSensitive) return;
                    this.caseSensitive = caseSensitive;
                    markDirty();
                    recompile(compileCallback);
                }).setTooltipHoverString(
                        i -> "cover.ore_dictionary_filter.button.case_sensitive." + (i == 0 ? "disabled" : "enabled")));
        widgetGroup.accept(new ForcedInitialSyncImageCycleButtonWidget(148, 38, 18, 18,
                GuiTextures.ORE_FILTER_BUTTON_MATCH_ALL, () -> this.matchAll, matchAll -> {
                    if (this.matchAll == matchAll) return;
                    this.matchAll = matchAll;
                    markDirty();
                    clearCache();
                    for (ItemOreFilterTestSlot slot : testSlot) {
                        slot.setMatchAll(matchAll);
                    }
                }).setTooltipHoverString(
                        i -> "cover.ore_dictionary_filter.button.match_all." + (i == 0 ? "disabled" : "enabled")));
    }

    @Override
    public Object matchItemStack(ItemStack itemStack) {
        return matchesItemStack(itemStack) ?
                "wtf is this system?? i can put any non null object here and it i will work??? $arch" : null;
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
        boolean matches = this.matchAll ? this.glob.matchesAll(itemStack) : this.glob.matchesAny(itemStack);
        cacheEntry.put(itemStack, matches);
        return matches;
    }

    @Override
    public int getSlotTransferLimit(Object matchSlot, int globalTransferLimit) {
        return globalTransferLimit;
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return true;
    }

    @Override
    public int getTotalOccupiedHeight() {
        return 37;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setString("OreDictionaryFilter", expression);
        if (this.caseSensitive) tag.setBoolean("caseSensitive", true);
        if (this.matchAll) tag.setBoolean("matchAll", true);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        this.expression = tag.getString("OreDictionaryFilter");
        this.caseSensitive = tag.getBoolean("caseSensitive");
        this.matchAll = tag.getBoolean("matchAll");
        recompile(null);
    }

    public static class ForcedInitialSyncImageCycleButtonWidget extends ImageCycleButtonWidget {

        private final BooleanConsumer updater;

        public ForcedInitialSyncImageCycleButtonWidget(int xPosition, int yPosition, int width, int height,
                                                       TextureArea buttonTexture, BooleanSupplier supplier,
                                                       BooleanConsumer updater) {
            super(xPosition, yPosition, width, height, buttonTexture, supplier, updater);
            this.currentOption = 0;
            this.updater = updater;
        }

        @Override
        public void readUpdateInfo(int id, PacketBuffer buffer) {
            if (id == 1) {
                int currentOptionCache = this.currentOption;
                super.readUpdateInfo(id, buffer);
                if (this.currentOption != currentOptionCache) {
                    this.updater.apply(currentOption >= 1); // call updater to apply necessary state changes
                }
            } else {
                super.readUpdateInfo(id, buffer);
            }
        }
    }
}
