package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.DrawableWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.OreDictFilterTestSlot;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.api.util.OreDictExprFilter;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenCustomHashMap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class OreDictionaryItemFilter extends ItemFilter {

    protected String oreDictFilterExpression = "";
    private String testMsg = "";
    private boolean testResult;
    private ItemStack testStack = ItemStack.EMPTY;

    private final List<OreDictExprFilter.MatchRule> matchRules = new ArrayList<>();
    private static final Hash.Strategy<ItemStack> strategy = ItemStackHashStrategy.builder().compareItem(true).compareDamage(true).build();
    private final Object2BooleanOpenCustomHashMap<ItemStack> recentlyChecked = new Object2BooleanOpenCustomHashMap<>(strategy);

    protected void setOreDictFilterExpression(String oreDictFilterExpression) {
        this.oreDictFilterExpression = oreDictFilterExpression;
        OreDictExprFilter.parseExpression(matchRules, oreDictFilterExpression);
        recentlyChecked.clear();
        markDirty();
        updateTestMsg();
    }

    public String getOreDictFilterExpression() {
        return oreDictFilterExpression;
    }

    @Override
    public void initUI(Consumer<Widget> widgetGroup) {
        widgetGroup.accept(new ImageWidget(12, 0, 20, 20, GuiTextures.INFO_ICON)
                .setTooltip("cover.ore_dictionary_filter.info"));
        widgetGroup.accept(new ImageWidget(10, 25, 156, 14, GuiTextures.DISPLAY));
        widgetGroup.accept(new TextFieldWidget2(14, 29, 152, 12, () -> oreDictFilterExpression, this::setOreDictFilterExpression)
                .setAllowedChars(Pattern.compile("[(!]* *[0-9a-zA-Z*]* *\\)*( *[&|^]? *[(!]* *[0-9a-zA-Z*]* *\\)*)*"))
                .setMaxLength(64)
                .setScale(0.75f)
                .setValidator(input -> {
                    // remove all operators that are double
                    input = input.replaceAll("\\*{2,}", "*");
                    input = input.replaceAll("&{2,}", "&");
                    input = input.replaceAll("\\|{2,}", "|");
                    input = input.replaceAll("!{2,}", "!");
                    input = input.replaceAll("\\^{2,}", "^");
                    input = input.replaceAll(" {2,}", " ");
                    // move ( and ) so it doesn't create invalid expressions f.e. xxx (& yyy) => xxx & (yyy)
                    // append or prepend ( and ) if the amount is not equal
                    StringBuilder builder = new StringBuilder();
                    int unclosed = 0;
                    char last = ' ';
                    for (int i = 0; i < input.length(); i++) {
                        char c = input.charAt(i);
                        if (c == ' ') {
                            if (last != '(')
                                builder.append(" ");
                            continue;
                        }
                        if (c == '(')
                            unclosed++;
                        else if (c == ')') {
                            unclosed--;
                            if (last == '&' || last == '|' || last == '^') {
                                int l = builder.lastIndexOf(" " + last);
                                int l2 = builder.lastIndexOf("" + last);
                                builder.insert(l == l2 - 1 ? l : l2, ")");
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

        widgetGroup.accept(new DrawableWidget(36, 1, 100, 18)
                .setBackgroundDrawer(((mouseX, mouseY, partialTicks, context, widget) -> {
                    if (testStack.isEmpty()) {
                        return;
                    }
                    int color = 0xD15858;
                    if (testResult) {
                        color = 0x66C261;
                    }
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(widget.getPosition().x, widget.getPosition().y, 0);
                    GlStateManager.colorMask(true, true, true, true);
                    Widget.drawText(I18n.format(testMsg), 22, 6.5f, 0.75f, color, false);
                    color |= (140 & 0xFF) << 24;
                    Widget.drawGradientRect(0, 0, 18, 18, color, color);
                    GlStateManager.popMatrix();

                }))
        );
        widgetGroup.accept(new OreDictFilterTestSlot(36, 1)
                .setListener(stack -> {
                    testStack = stack;
                    updateTestMsg();
                }));
    }

    private void updateTestMsg() {
        if (testStack.isEmpty()) {
            testMsg = "";
            return;
        }

        testResult = matchesItemStack(testStack);
        if (testResult) {
            testMsg = "cover.ore_dictionary_filter.matches";
        } else {
            testMsg = "cover.ore_dictionary_filter.matches_not";
        }
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
        tagCompound.setString("OreDictionaryFilter", oreDictFilterExpression);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        this.oreDictFilterExpression = tagCompound.getString("OreDictionaryFilter");
        OreDictExprFilter.parseExpression(this.matchRules, this.oreDictFilterExpression);
    }
}
