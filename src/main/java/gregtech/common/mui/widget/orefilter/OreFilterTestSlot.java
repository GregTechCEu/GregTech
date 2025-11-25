package gregtech.common.mui.widget.orefilter;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.function.BooleanConsumer;
import gregtech.api.util.oreglob.OreGlob;
import gregtech.common.covers.filter.oreglob.impl.ImpossibleOreGlob;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import it.unimi.dsi.fastutil.objects.Object2BooleanAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author brachy84
 */
public class OreFilterTestSlot extends PhantomItemSlot {

    private final ItemOreFilterTestSlot slot;
    private Supplier<OreGlob> globSupplier = ImpossibleOreGlob::getInstance;
    @Nullable
    private BooleanConsumer onMatchChange;
    private final Object2BooleanMap<String> testResult = new Object2BooleanAVLTreeMap<>();
    private MatchType matchType = MatchType.INVALID;
    private boolean matchSuccess;
    private boolean matchAll;

    public OreFilterTestSlot() {
        this.slot = new ItemOreFilterTestSlot();
        this.slot.setParent(this);
        slot(this.slot);
        tooltipBuilder(tooltip -> {
            if (!isEnabled()) return;
            tooltip.addDrawableLines(switch (this.matchType) {
                case NO_ORE_DICT_MATCH -> Collections.singletonList(IKey.lang(this.matchSuccess ?
                        "cover.ore_dictionary_filter.test_slot.no_oredict.matches" :
                        "cover.ore_dictionary_filter.test_slot.no_oredict.matches_not"));
                case ORE_DICT_MATCH -> this.testResult.object2BooleanEntrySet()
                        .stream().map(e -> IKey.lang(e.getBooleanValue() ?
                                "cover.ore_dictionary_filter.test_slot.matches" :
                                "cover.ore_dictionary_filter.test_slot.matches_not", e.getKey()))
                        .collect(Collectors.toList());
                default -> Collections.singletonList(IKey.lang("cover.ore_dictionary_filter.test_slot.info"));
            });
        });
    }

    public OreFilterTestSlot setGlobSupplier(Supplier<OreGlob> supplier) {
        this.globSupplier = supplier;
        this.updatePreview();
        return getThis();
    }

    @Override
    public OreFilterTestSlot getThis() {
        return this;
    }

    public boolean isMatchSuccess() {
        return matchSuccess;
    }

    public OreFilterTestSlot onMatchChange(@Nullable BooleanConsumer onMatchChange) {
        this.onMatchChange = onMatchChange;
        return getThis();
    }

    public void setMatchAll(boolean matchAll) {
        if (this.matchAll == matchAll) return;
        this.matchAll = matchAll;
        updatePreview();
    }

    public void updatePreview() {
        Set<String> oreDicts = getTestCandidates();
        this.testResult.clear();
        if (oreDicts == null) {
            this.matchType = MatchType.INVALID;
            updateAndNotifyMatchSuccess(false);
            return;
        }
        OreGlob glob = this.globSupplier.get();
        int success = 0;
        if (oreDicts.isEmpty()) {
            // no oredict entries
            this.testResult.put("", glob != null && glob.matches(""));
            this.matchType = MatchType.NO_ORE_DICT_MATCH;
        } else {
            for (String oreDict : oreDicts) {
                boolean matches = glob != null && glob.matches(oreDict);
                if (matches) success++;
                this.testResult.put(oreDict, matches);
            }
            this.matchType = MatchType.ORE_DICT_MATCH;
        }
        updateAndNotifyMatchSuccess(this.matchAll ? success == testResult.size() : success > 0);
        this.tooltip().markDirty();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        if (this.matchSuccess) {
            GTGuiTextures.OREDICT_MATCH.draw(context, 12, -2, 9, 6, widgetTheme.getTheme());
        } else if (!testResult.isEmpty()) {
            GTGuiTextures.OREDICT_NO_MATCH.draw(context, 12, -3, 7, 7, widgetTheme.getTheme());
        }
    }

    private void updateAndNotifyMatchSuccess(boolean newValue) {
        if (this.matchSuccess == newValue) return;
        this.matchSuccess = newValue;
        if (this.onMatchChange != null) {
            this.onMatchChange.apply(newValue);
        }
    }

    /**
     * Get each test candidate for current state of test slot. An empty collection indicates that the match is for items
     * without any ore dictionary entry. A {@code null} value indicates that the input state is invalid or empty.
     *
     * @return each test candidate for current state of test slot
     */
    @Nullable
    protected Set<String> getTestCandidates() {
        return this.slot.getStack().isEmpty() ? null : OreDictUnifier.getOreDictionaryNames(this.slot.getStack());
    }

    private enum MatchType {
        NO_ORE_DICT_MATCH,
        ORE_DICT_MATCH,
        INVALID
    }
}
