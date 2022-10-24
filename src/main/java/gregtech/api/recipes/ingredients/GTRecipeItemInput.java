package gregtech.api.recipes.ingredients;

import gregtech.api.GTValues;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import java.util.List;

public class GTRecipeItemInput extends GTRecipeInput {
    ItemStack[] inputStacks;
    List<ItemToMetaList> itemList = new ObjectArrayList<>();

    protected GTRecipeItemInput(ItemStack stack, int amount) {
        this(new ItemStack[]{stack}, amount);
    }

    protected GTRecipeItemInput(ItemStack[] stack, int amount) {
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

    protected GTRecipeItemInput(ItemStack... stack) {
        this(stack, stack[0].getCount());
    }

    public static GTRecipeInput getOrCreate(ItemStack stack, int amount) {
        return getFromCache(new GTRecipeItemInput(stack, amount));
    }

    public static GTRecipeInput getOrCreate(GTRecipeInput ri, int i) {
        return getFromCache(new GTRecipeItemInput(ri.getInputStacks(), i));
    }

    public static GTRecipeInput getOrCreate(GTRecipeInput ri) {
        return getFromCache(new GTRecipeItemInput(ri.getInputStacks()));
    }

    public static GTRecipeInput getOrCreate(ItemStack ri) {
        return getFromCache(new GTRecipeItemInput(ri));
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
        for (int i = 0; i < itemList.size(); i++) {
            ItemToMetaList metaList = itemList.get(i);
            if (metaList.item == inputItem) {
                List<MetaToTAGList> tagLists = metaList.metaToTAGList;
                for (int j = 0; j < tagLists.size(); j++) {
                    MetaToTAGList tagList = tagLists.get(j);
                    if (tagList.meta == input.getMetadata()) {
                        final NBTTagCompound inputNBT = input.getTagCompound();
                        if (nbtMatcher != null) {
                            return nbtMatcher.evaluate(inputNBT, nbtCondition);
                        } else {
                            List<TagToStack> tagMaps = tagList.tagToStack;
                            for (int k = 0; k < tagMaps.size(); k++) {
                                TagToStack tagMapping = tagMaps.get(k);
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
    public int hashCode() {
        int hash = 1;
        for (ItemStack stack : inputStacks) {
            hash = 31 * hash + stack.getItem().hashCode();
            hash = 31 * hash + stack.getMetadata();
            if (stack.hasTagCompound() && this.nbtMatcher == null) {
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

        if (this.amount != other.amount) return false;
        if (this.isConsumable != other.isConsumable) return false;
        if (this.nbtMatcher != null && !this.nbtMatcher.equals(other.nbtMatcher)) return false;
        if (this.nbtCondition != null && !this.nbtCondition.equals(other.nbtCondition)) return false;

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

        if (this.nbtMatcher != null && !this.nbtMatcher.equals(other.nbtMatcher)) return false;
        if (this.nbtCondition != null && !this.nbtCondition.equals(other.nbtCondition)) return false;

        if (this.inputStacks.length != other.inputStacks.length) return false;
        for (int i = 0; i < this.inputStacks.length; i++) {
            if (!ItemStack.areItemsEqual(this.inputStacks[i], other.inputStacks[i]) || !ItemStack.areItemStackTagsEqual(this.inputStacks[i], other.inputStacks[i]))
                return false;
        }
        return true;
    }
}
