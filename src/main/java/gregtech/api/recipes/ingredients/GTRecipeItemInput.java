package gregtech.api.recipes.ingredients;

import gregtech.api.GTValues;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GTRecipeItemInput extends GTRecipeInput {

    private final ItemStack[] inputStacks;
    private final List<ItemToMetaList> itemList = new ObjectArrayList<>();

    public GTRecipeItemInput(ItemStack stack) {
        this(new ItemStack[] { stack }, stack.getCount());
    }

    public GTRecipeItemInput(ItemStack stack, int amount) {
        this(new ItemStack[] { stack }, amount);
    }

    public GTRecipeItemInput(GTRecipeInput input) {
        this(input.getInputStacks());
    }

    public GTRecipeItemInput(GTRecipeInput input, int amount) {
        this(input.getInputStacks(), amount);
    }

    public GTRecipeItemInput(ItemStack... stacks) {
        this(stacks, stacks[0].getCount());
    }

    public GTRecipeItemInput(ItemStack[] stack, int amount) {
        this.amount = amount;

        NonNullList<ItemStack> lst = NonNullList.create();
        for (ItemStack is : stack) {
            if (is.getMetadata() == GTValues.W) {
                is.getItem().getSubItems(net.minecraft.creativetab.CreativeTabs.SEARCH, lst);
            } else {
                lst.add(is);
            }
        }

        for (ItemStack is : lst) {
            boolean addedStack = false;
            if (!is.isEmpty()) {
                for (ItemToMetaList item : this.itemList) {
                    if (item.getKey() == is.getItem()) {
                        List<MetaToTAGList> metaList = item.getValue();
                        for (MetaToTAGList meta : metaList) {
                            if (meta.getIntKey() == is.getMetadata()) {
                                meta.addStackToList(is);
                                addedStack = true;
                                break;
                            }
                        }
                        if (addedStack) break;
                        item.addStackToLists(is);
                        addedStack = true;
                        break;
                    }
                }
                if (addedStack) continue;
                this.itemList.add(new ItemToMetaList(is));
            }
        }
        this.inputStacks = lst.stream().map(is -> {
            is = is.copy();
            is.setCount(this.amount);
            return is;
        }).toArray(ItemStack[]::new);
    }

    /**
     * @deprecated Use constructors
     */
    @Deprecated
    public static GTRecipeInput getOrCreate(ItemStack stack, int amount) {
        return new GTRecipeItemInput(stack, amount);
    }

    /**
     * @deprecated Use constructors
     */
    @Deprecated
    public static GTRecipeInput getOrCreate(GTRecipeInput ri, int i) {
        return new GTRecipeItemInput(ri, i);
    }

    /**
     * @deprecated Use constructors
     */
    @Deprecated
    public static GTRecipeInput getOrCreate(GTRecipeInput input) {
        return new GTRecipeItemInput(input);
    }

    /**
     * @deprecated Use constructors
     */
    @Deprecated
    public static GTRecipeInput getOrCreate(ItemStack stack) {
        return new GTRecipeItemInput(stack);
    }

    /**
     * @deprecated Use constructors
     */
    @Deprecated
    public static GTRecipeInput getOrCreate(ItemStack[] stacks) {
        return new GTRecipeItemInput(stacks);
    }

    @Override
    protected GTRecipeItemInput copy() {
        GTRecipeItemInput copy = new GTRecipeItemInput(this.inputStacks, this.amount);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    @Override
    public GTRecipeInput copyWithAmount(int amount) {
        GTRecipeItemInput copy = new GTRecipeItemInput(this.inputStacks, amount);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    @Override
    public ItemStack[] getInputStacks() {
        return this.inputStacks;
    }

    @Override
    public boolean acceptsStack(ItemStack input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        List<ItemToMetaList> itemList = this.itemList;
        Item inputItem = input.getItem();
        for (ItemToMetaList metaList : itemList) {
            if (metaList.item == inputItem) {
                List<MetaToTAGList> tagLists = metaList.metaToTAGList;
                for (MetaToTAGList tagList : tagLists) {
                    if (tagList.meta == input.getMetadata()) {
                        final NBTTagCompound inputNBT = input.getTagCompound();
                        if (nbtMatcher != null) {
                            return nbtMatcher.evaluate(input, nbtCondition);
                        } else {
                            List<TagToStack> tagMaps = tagList.tagToStack;
                            for (TagToStack tagMapping : tagMaps) {
                                if ((inputNBT == null && tagMapping.tag == null) ||
                                        (inputNBT != null && inputNBT.equals(tagMapping.tag))) {
                                    return tagMapping.stack.areCapsCompatible(input);
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected int computeHash() {
        int hash = 1;
        for (ItemStack stack : inputStacks) {
            hash = 31 * hash + stack.getItem().hashCode();
            hash = 31 * hash + stack.getMetadata();
            if (stack.hasTagCompound() && stack.getTagCompound() != null && this.nbtMatcher == null) {
                hash = 31 * hash + stack.getTagCompound().hashCode();
            }
        }
        hash = 31 * hash + this.amount;
        hash = 31 * hash + (this.isConsumable ? 1 : 0);
        hash = 31 * hash + (this.nbtMatcher != null ? this.nbtMatcher.hashCode() : 0);
        hash = 31 * hash + (this.nbtCondition != null ? this.nbtCondition.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GTRecipeItemInput)) return false;
        GTRecipeItemInput other = (GTRecipeItemInput) obj;

        if (this.amount != other.amount || this.isConsumable != other.isConsumable) return false;
        if (!Objects.equals(this.nbtMatcher, other.nbtMatcher)) return false;
        if (!Objects.equals(this.nbtCondition, other.nbtCondition)) return false;

        if (this.inputStacks.length != other.inputStacks.length) return false;
        for (int i = 0; i < this.inputStacks.length; i++) {
            if (!ItemStack.areItemStacksEqual(this.inputStacks[i], other.inputStacks[i])) return false;
        }
        return true;
    }

    @Override
    public boolean equalIgnoreAmount(GTRecipeInput input) {
        if (this == input) return true;
        if (!(input instanceof GTRecipeItemInput)) return false;
        GTRecipeItemInput other = (GTRecipeItemInput) input;

        if (!Objects.equals(this.nbtMatcher, other.nbtMatcher)) return false;
        if (!Objects.equals(this.nbtCondition, other.nbtCondition)) return false;

        if (this.inputStacks.length != other.inputStacks.length) return false;
        for (int i = 0; i < this.inputStacks.length; i++) {
            if (!ItemStack.areItemsEqual(this.inputStacks[i], other.inputStacks[i]) ||
                    !ItemStack.areItemStackTagsEqual(this.inputStacks[i], other.inputStacks[i]))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        switch (this.inputStacks.length) {
            case 0:
                return amount + "x[]";
            case 1:
                return amount + "x" + toStringWithoutQuantity(this.inputStacks[0]);
            default:
                return amount + "x[" + Arrays.stream(this.inputStacks)
                        .map(GTRecipeItemInput::toStringWithoutQuantity)
                        .collect(Collectors.joining("|")) + "]";
        }
    }

    private static String toStringWithoutQuantity(ItemStack stack) {
        return stack.getItem().getTranslationKey(stack) + "@" + stack.getItemDamage();
    }
}
