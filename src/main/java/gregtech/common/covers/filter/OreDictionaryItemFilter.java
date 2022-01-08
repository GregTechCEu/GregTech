package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.util.ItemStackKey;
import gregtech.api.util.OreDictExprFilter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class OreDictionaryItemFilter extends ItemFilter {

    private static final Pattern ORE_DICTIONARY_FILTER = Pattern.compile("\\*?[a-zA-Z0-9_]*\\*?");

    protected String oreDictFilterExpression = "";

    private final List<OreDictExprFilter.MatchRule> matchRules = new ArrayList<>();
    private final Map<ItemStack, Boolean> recentlyChecked = new HashMap<>();

    protected void setOreDictFilterExpression(String oreDictFilterExpression) {
        this.oreDictFilterExpression = oreDictFilterExpression;
        OreDictExprFilter.parseExpression(matchRules, oreDictFilterExpression);
        recentlyChecked.clear();
        markDirty();
    }

    public String getOreDictFilterExpression() {
        return oreDictFilterExpression;
    }

    @Override
    public void initUI(Consumer<Widget> widgetGroup) {
        widgetGroup.accept(new ImageWidget(12, 0, 20, 20, GuiTextures.INFO_ICON)
                .setTooltip("cover.ore_dictionary_filter.info")
        );
        widgetGroup.accept(new ImageWidget(10, 25, 156, 14, GuiTextures.DISPLAY));
        widgetGroup.accept(new TextFieldWidget2(14, 29, 152, 12, () -> oreDictFilterExpression, this::setOreDictFilterExpression)
                .setAllowedChars(Pattern.compile("[(!]* *[0-9a-zA-Z*]* *\\)*( *[&|^]? *[(!]* *[0-9a-zA-Z*]* *\\)*)*"))
                .setMaxLength(64)
                .setScale(0.75f)
                .setValidator(input -> {
                    input = input.replaceAll("\\*{2,}", "*");
                    input = input.replaceAll("&{2,}", "&");
                    input = input.replaceAll("\\|{2,}", "|");
                    input = input.replaceAll("!{2,}", "!");
                    input = input.replaceAll("\\^{2,}", "^");
                    input = input.replaceAll(" {2,}", " ");
                    StringBuilder builder = new StringBuilder();
                    int unclosed = 0;
                    char last = ' ';
                    for (int i = 0; i < input.length(); i++) {
                        char c = input.charAt(i);
                        if (c == ' ' && last != '(') {
                            builder.append(" ");
                            continue;
                        }
                        if (c == '(')
                            unclosed++;
                        else if (c == ')') {
                            unclosed--;
                            if (last == '&' || last == '|' || last == '^') {
                                int l = input.lastIndexOf(" " + last);
                                builder.insert(l == i - 1 ? i - 1 : i, ")");
                                continue;
                            }
                            if (i > 0 && builder.charAt(builder.length() - 1) == ' ') {
                                builder.deleteCharAt(builder.length() - 1);
                            }
                        } else if ((c == '&' || c == '|' || c == '^') && last == '(') {
                            builder.deleteCharAt(builder.lastIndexOf("("));
                            builder.append(c).append(" (");
                            continue;
                        }

                        builder.append(c);
                        last = c;
                    }
                    if (unclosed > 0) {
                        for (int i = 0; i < unclosed; i++) {
                            builder.append(")");
                        }
                    } else if (unclosed < 0) {
                        unclosed = -unclosed;
                        for (int i = 0; i < unclosed; i++) {
                            builder.insert(0, "(");
                        }
                    }
                    input = builder.toString();
                    input = input.replaceAll(" {2,}", " ");
                    return input;
                })
        );
    }

    @Override
    public Object matchItemStack(ItemStack itemStack) {
        return matchesItemStack(itemStack) ? "wtf is this system?? i can put any non null object here and it i will work??? $arch" : null;
    }

    public boolean matchesItemStack(ItemStack itemStack) {
        Boolean b = recentlyChecked.get(itemStack);
        if (b != null)
            return b;
        if (OreDictExprFilter.matchesOreDict(matchRules, itemStack)) {
            recentlyChecked.put(itemStack, true);
            return true;
        }
        recentlyChecked.put(itemStack, false);
        return false;
    }

    @Override
    public int getSlotTransferLimit(Object matchSlot, Set<ItemStackKey> matchedStacks, int globalTransferLimit) {
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
        tagCompound.setString("OreDictionaryFilter", oreDictFilterExpression);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        this.oreDictFilterExpression = tagCompound.getString("OreDictionaryFilter");
        OreDictExprFilter.parseExpression(this.matchRules, this.oreDictFilterExpression);
    }
}
