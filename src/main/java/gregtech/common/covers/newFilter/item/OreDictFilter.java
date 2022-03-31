package gregtech.common.covers.newFilter.item;

import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.common.internal.UIBuildContext;
import com.cleanroommc.modularui.common.widget.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.util.ItemStackKey;
import gregtech.api.util.OreDictExprFilter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

import java.util.*;
import java.util.regex.Pattern;

public class OreDictFilter extends ItemFilter {

    protected String oreDictFilterExpression = "";
    private String testMsg = "";
    private boolean testResult;
    private final ItemStackHandler testSlot = new ItemStackHandler() {
        @Override
        protected void onContentsChanged(int slot) {
            OreDictFilter.this.updateTestMsg();
        }
    };
    private static final Pattern pattern = Pattern.compile("[(!]* *[0-9a-zA-Z*]* *\\)*( *[&|^]? *[(!]* *[0-9a-zA-Z*]* *\\)*)*");

    private final List<OreDictExprFilter.MatchRule> matchRules = new ArrayList<>();
    private final Map<ItemStack, Boolean> recentlyChecked = new HashMap<>();

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
    public Widget createFilterUI(UIBuildContext buildContext) {
        return new MultiChildWidget()
                .addChild(GuiTextures.INFO_ICON.asWidget()
                        .addTooltip(new Text("cover.ore_dictionary_filter.info").localise())
                        .setSize(18, 18)
                        .setPos(39, 0))
                .addChild(createBlacklistButton(buildContext))
                .addChild(SlotWidget.phantom(testSlot, 0)
                        .setChangeListener(this::updateTestMsg)
                        .setPos(58, 0))
                .addChild(TextWidget.dynamicText(() -> {
                    if (testMsg.isEmpty()) {
                        return new Text("");
                    }
                    return new Text(testMsg)
                            .color(testResult ? 0x13610C : 0x801212)
                            .localise();
                }).setPos(78, 0).setSize(53, 18))
                .addChild(new TextFieldWidget()
                        .setSetter(val -> {
                            setOreDictFilterExpression(val);
                            updateTestMsg();
                        })
                        .setGetter(() -> oreDictFilterExpression)
                        .setPattern(pattern)
                        .setTextAlignment(Alignment.CenterLeft)
                        .setValidator(this::validateInput)
                        .setSynced(false, false)
                        .setBackground(GuiTextures.DISPLAY)
                        .setPos(0, 19)
                        .setSize(162, 18));
    }

    private void updateTestMsg() {
        ItemStack testStack = testSlot.getStackInSlot(0);
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
    public int getSlotTransferLimit(Object matchSlot, Set<ItemStackKey> matchedStacks, int globalTransferLimit) {
        return globalTransferLimit;
    }

    @Override
    public boolean showGlobalTransferLimitSlider() {
        return true;
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

    private String validateInput(String input) {
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
    }
}
