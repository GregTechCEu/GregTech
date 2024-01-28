package gregtech.common.gui.widget.orefilter;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.function.BooleanConsumer;
import gregtech.api.util.oreglob.OreGlob;
import gregtech.common.covers.filter.oreglob.impl.ImpossibleOreGlob;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.ItemSlot;
import it.unimi.dsi.fastutil.objects.Object2BooleanAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author brachy84
 */
public class OreFilterTestSlot extends ItemSlot {

    private final ItemOreFilterTestSlot slot;
    private Supplier<OreGlob> globSupplier = ImpossibleOreGlob::getInstance;
    private boolean expectedResult = true;
    @Nullable
    private BooleanConsumer onMatchChange;
    private Object2BooleanMap<String> testResult;
    private MatchType matchType = MatchType.INVALID;
    private boolean matchSuccess;

    private boolean matchAll;

    public OreFilterTestSlot() {
        this.slot = new ItemOreFilterTestSlot();
        this.slot.setParent(this);
        this.slot.setGlob(globSupplier.get());
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
        // todo add back match and no match overlays
        // this.match = new ImageWidget(18 - 5, -3, 9, 6, GuiTextures.ORE_FILTER_MATCH);
        // this.noMatch = new ImageWidget(18 - 5, -3, 7, 7, GuiTextures.ORE_FILTER_NO_MATCH);
        // child(this.match);
        // child(this.noMatch);
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

    public OreFilterTestSlot setExpectedResult(boolean expectedResult) {
        this.expectedResult = expectedResult;
        return getThis();
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
        if (oreDicts != null) {
            OreGlob glob = this.globSupplier.get();
            this.slot.setGlob(glob);
            if (oreDicts.isEmpty()) {
                // no oredict entries
                this.testResult = Object2BooleanMaps.singleton("", glob != null && glob.matches(""));
                this.matchType = MatchType.NO_ORE_DICT_MATCH;
            } else {
                this.testResult = new Object2BooleanAVLTreeMap<>();
                for (String oreDict : oreDicts) {
                    boolean matches = glob != null && glob.matches(oreDict);
                    this.testResult.put(oreDict, matches);
                }
                this.matchType = MatchType.ORE_DICT_MATCH;
            }
            boolean success = this.matchAll;
            for (var e : testResult.object2BooleanEntrySet()) {
                boolean result = e.getBooleanValue();
                if (result == !this.matchAll) {
                    success = !this.matchAll;
                    break;
                }
            }
            updateAndNotifyMatchSuccess(this.expectedResult == success);
            this.tooltip().markDirty();
            // todo add back match and no match overlays
            // this.match.setVisible(this.expectedResult == success);
            // this.noMatch.setVisible(this.expectedResult != success);
            return;
        }
        this.testResult = Object2BooleanMaps.emptyMap();
        this.matchType = MatchType.INVALID;
        updateAndNotifyMatchSuccess(false);
        // todo add back match and no match overlays
        // this.match.setVisible(false);
        // this.noMatch.setVisible(false);
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
