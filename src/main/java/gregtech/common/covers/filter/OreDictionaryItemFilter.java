package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.DrawableWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.api.util.oreglob.OreGlob;
import gregtech.api.util.oreglob.OreGlobCompileResult;
import gregtech.common.covers.filter.oreglob.impl.ImpossibleOreGlob;
import gregtech.common.gui.widget.HighlightedTextField;
import gregtech.common.gui.widget.orefilter.ItemOreFilterTestSlot;
import gregtech.common.gui.widget.orefilter.OreGlobCompileStatusWidget;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenCustomHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

import java.util.function.Consumer;

public class OreDictionaryItemFilter extends ItemFilter {

    private static final Hash.Strategy<ItemStack> HASH_STRATEGY = ItemStackHashStrategy.builder().compareItem(true).compareDamage(true).build();

    protected String expression = "";
    private OreGlob glob = ImpossibleOreGlob.getInstance();
    private boolean error;

    private final Object2BooleanMap<ItemStack> matchCache = new Object2BooleanOpenCustomHashMap<>(HASH_STRATEGY);

    public String getExpression() {
        return expression;
    }

    @Override
    public void initUI(Consumer<Widget> widgetGroup) {
        ItemOreFilterTestSlot[] testSlot = new ItemOreFilterTestSlot[5];
        for (int i = 0; i < testSlot.length; i++) {
            testSlot[i] = new ItemOreFilterTestSlot(20 + 22 * i, 0);
            widgetGroup.accept(testSlot[i]);
        }
        OreGlobCompileStatusWidget compilationStatus = new OreGlobCompileStatusWidget(10, 10);
        HighlightedTextField textField = new HighlightedTextField(14, 26, 152, 14, () -> this.expression,
                s -> {
                    if (s.equals(this.expression)) return;
                    this.expression = s;
                    if (!s.isEmpty()) {
                        OreGlobCompileResult result = OreGlob.compile(s);
                        this.glob = result.getInstance();
                        this.error = result.hasError();
                        compilationStatus.setCompileResult(result);
                    } else {
                        this.glob = ImpossibleOreGlob.getInstance();
                        this.error = true;
                        compilationStatus.setCompileResult(null);
                    }
                    this.matchCache.clear();
                    markDirty();
                    for (ItemOreFilterTestSlot slot : testSlot) {
                        slot.setGlob(this.error ? null : this.glob);
                    }
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
                            case '|': case '&': case '^': case '(': case ')':
                                h.format(i, TextFormatting.GOLD);
                                break;
                            case '*': case '?':
                                h.format(i, TextFormatting.GREEN);
                                break;
                            case '!':
                                h.format(i, TextFormatting.RED);
                                break;
                            case '\\':
                                h.format(i++, TextFormatting.YELLOW);
                                break;
                            case '$': {
                                h.format(i, TextFormatting.DARK_GREEN);
                                for (; i < t.length(); i++) {
                                    switch (t.charAt(i)) {
                                        case ' ': case '\t': case '\n': case '\r':
                                            break;
                                        case '\\':
                                            i++;
                                        default:
                                            continue;
                                    }
                                    break;
                                }
                                break;
                            }
                            default:
                                continue;
                        }
                        h.format(i + 1, TextFormatting.RESET);
                    }
                }).setMaxLength(64));
    }

    @Override
    public Object matchItemStack(ItemStack itemStack) {
        return matchesItemStack(itemStack) ? "wtf is this system?? i can put any non null object here and it i will work??? $arch" : null;
    }

    public boolean matchesItemStack(ItemStack itemStack) {
        if (this.error) return false;
        Boolean cached = this.matchCache.get(itemStack);
        if (cached != null) {
            return cached;
        }
        boolean matches = this.glob.matches(itemStack);
        this.matchCache.put(itemStack, matches);
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
    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setString("OreDictionaryFilter", expression);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        this.expression = tagCompound.getString("OreDictionaryFilter");
        if (!this.expression.isEmpty()) {
            OreGlobCompileResult result = OreGlob.compile(this.expression);
            this.glob = result.getInstance();
            this.error = result.hasError();
        } else {
            this.glob = ImpossibleOreGlob.getInstance();
            this.error = true;
        }
    }
}
