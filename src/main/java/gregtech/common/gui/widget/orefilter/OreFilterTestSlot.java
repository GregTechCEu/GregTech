package gregtech.common.gui.widget.orefilter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.Position;
import gregtech.api.util.function.BooleanConsumer;
import gregtech.api.util.oreglob.OreGlob;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2BooleanAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author brachy84
 */
public abstract class OreFilterTestSlot extends WidgetGroup {

    @Nullable
    private OreGlob glob;
    private boolean expectedResult = true;

    @Nullable
    private TextureArea slotIcon = GuiTextures.SLOT;

    @Nullable
    private BooleanConsumer onMatchChange;

    private Object2BooleanMap<String> testResult;
    private MatchType matchType = MatchType.INVALID;
    private boolean matchSuccess;

    private boolean initialized = false;

    private final ImageWidget match;
    private final ImageWidget noMatch;

    public OreFilterTestSlot(int xPosition, int yPosition) {
        super(xPosition, yPosition, 18, 18);
        this.match = new ImageWidget(18 - 5, -3, 9, 6, GuiTextures.ORE_FILTER_MATCH);
        this.noMatch = new ImageWidget(18 - 5, -3, 7, 7, GuiTextures.ORE_FILTER_NO_MATCH);
        addWidget(this.match);
        addWidget(this.noMatch);
    }

    @Override
    public void initWidget() {
        this.initialized = true;
        updatePreview();
        super.initWidget();
    }

    public boolean isMatchSuccess() {
        return matchSuccess;
    }

    public OreFilterTestSlot setSlotIcon(@Nullable TextureArea slotIcon) {
        this.slotIcon = slotIcon;
        return this;
    }

    public OreFilterTestSlot setExpectedResult(boolean expectedResult) {
        this.expectedResult = expectedResult;
        return this;
    }

    public OreFilterTestSlot onMatchChange(@Nullable BooleanConsumer onMatchChange) {
        this.onMatchChange = onMatchChange;
        return this;
    }

    public void setGlob(@Nullable OreGlob glob) {
        this.glob = glob;
        updatePreview();
    }

    protected void updatePreview() {
        if (!this.initialized) return;
        Set<String> oreDicts = getTestCandidates();
        if (oreDicts != null) {
            boolean success;
            OreGlob glob = this.glob;
            if (oreDicts.isEmpty()) {
                // no oredict entries
                this.testResult = Object2BooleanMaps.singleton("", success = glob != null && glob.matches(""));
                this.matchType = MatchType.NO_ORE_DICT_MATCH;
            } else {
                this.testResult = new Object2BooleanAVLTreeMap<>();
                success = false;
                for (String oreDict : oreDicts) {
                    boolean matches = glob != null && glob.matches(oreDict);
                    this.testResult.put(oreDict, matches);
                    success |= matches;
                }
                this.matchType = MatchType.ORE_DICT_MATCH;
            }
            updateAndNotifyMatchSuccess(this.expectedResult == success);
            this.match.setVisible(this.expectedResult == success);
            this.noMatch.setVisible(this.expectedResult != success);
            return;
        }
        this.testResult = Object2BooleanMaps.emptyMap();
        this.matchType = MatchType.INVALID;
        updateAndNotifyMatchSuccess(false);
        this.match.setVisible(false);
        this.noMatch.setVisible(false);
    }

    private void updateAndNotifyMatchSuccess(boolean newValue) {
        if (this.matchSuccess == newValue) return;
        this.matchSuccess = newValue;
        if (this.onMatchChange != null) {
            this.onMatchChange.apply(newValue);
        }
    }

    /**
     * Get each test candidate for current state of test slot. An empty collection indicates that the
     * match is for items without any ore dictionary entry. A {@code null} value indicates that the
     * input state is invalid or empty.
     *
     * @return each test candidate for current state of test slot
     */
    @Nullable
    protected abstract Set<String> getTestCandidates();

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        if (this.slotIcon != null) {
            this.slotIcon.draw(pos.x, pos.y, 18, 18);
        }

        renderSlotContents(partialTicks, context);

        if (isActive() && isMouseOverElement(mouseX, mouseY)) {
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            drawSolidRect(getPosition().x + 1, getPosition().y + 1, 16, 16, 0x80ffffff);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableBlend();
        }

        GlStateManager.disableDepth();
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        GlStateManager.enableDepth();
    }

    protected abstract void renderSlotContents(float partialTicks, IRenderContext context);

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (isActive() && isMouseOverElement(mouseX, mouseY)) {
            List<String> list;
            switch (this.matchType) {
                case NO_ORE_DICT_MATCH:
                    list = Collections.singletonList(I18n.format(this.matchSuccess ?
                            "cover.ore_dictionary_filter.test_slot.no_oredict.matches" :
                            "cover.ore_dictionary_filter.test_slot.no_oredict.matches_not"));
                    break;
                case ORE_DICT_MATCH:
                    list = this.testResult.object2BooleanEntrySet().stream().map(
                            e -> I18n.format(e.getBooleanValue() ?
                                    "cover.ore_dictionary_filter.test_slot.matches" :
                                    "cover.ore_dictionary_filter.test_slot.matches_not", e.getKey()))
                            .collect(Collectors.toList());
                    break;
                case INVALID:
                default:
                    list = Arrays.asList(LocalizationUtils.formatLines("cover.ore_dictionary_filter.test_slot.info"));
                    break;
            }
            drawHoveringText(ItemStack.EMPTY, list, 300, mouseX, mouseY);
        }
    }

    private enum MatchType {
        NO_ORE_DICT_MATCH,
        ORE_DICT_MATCH,
        INVALID
    }
}
