package gregtech.api.recipes.ingredients.NBTMatching;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

/**
 * This class is used to match NBT tags. Used to match a MapItemStackNBTIngredient NBT tag to a given NBT tag value.
 */
public class NBTMatcher {

    public NBTMatcher() {

    }

    /**
     * Return true without checking if the NBT actually tags match or exists.
     */
    public static NBTMatcher ANY = new NBTMatcher() {

        @Override
        public boolean evaluate(NBTTagCompound nbtTagCompound, NBTCondition NBTcondition) {
            return true;
        }

    };

    public static NBTMatcher LESS_THAN = new NBTMatcher() {
        @Override
        public boolean keyValueMatches(NBTTagCompound tagCompound, String nbtKey, Long value) {
            return getKeyValue(tagCompound, nbtKey) < value;
        }
    };

    public static NBTMatcher LESS_THAN_OR_EQUALS = new NBTMatcher() {
        @Override
        public boolean keyValueMatches(NBTTagCompound tagCompound, String nbtKey, Long value) {
            return getKeyValue(tagCompound, nbtKey) <= value;
        }
    };

    public static NBTMatcher EQUALS = new NBTMatcher() {
        @Override
        public boolean keyValueMatches(NBTTagCompound tagCompound, String nbtKey, Long value) {
            return getKeyValue(tagCompound, nbtKey) == value;
        }
    };

    public static NBTMatcher NO_TAG_OR_EQUALS_ZERO = new NBTMatcher() {

        @Override
        public boolean evaluate(NBTTagCompound tagCompound, NBTCondition NBTcondition) {
            if (tagCompound == null) {
                return true;
            }
            return tagCompound.hasKey(NBTcondition.nbtKey) && tagCompound.getLong(NBTcondition.nbtKey) == 0L;
        }
    };

    public static NBTMatcher GREATER_THAN_OR_EQUALS = new NBTMatcher() {
        @Override
        public boolean keyValueMatches(NBTTagCompound tagCompound, String nbtKey, Long value) {
            return getKeyValue(tagCompound, nbtKey) >= value;
        }
    };

    public static NBTMatcher GREATER_THAN = new NBTMatcher() {
        @Override
        public boolean keyValueMatches(NBTTagCompound tagCompound, String nbtKey, Long value) {
            return getKeyValue(tagCompound, nbtKey) > value;
        }
    };

    public boolean hasKey(NBTTagCompound tagCompound, String nbtKey) {
        if (tagCompound != null) {
            return tagCompound.hasKey(nbtKey);
        }
        return false;
    }

    public long getKeyValue(NBTTagCompound tagCompound, String nbtKey) {
        if (tagCompound != null) {
            return tagCompound.getLong(nbtKey);
        }
        return 0;
    }

    public boolean evaluate(ItemStack stack, NBTCondition NBTcondition) {
        return evaluate(stack.getTagCompound(), NBTcondition);
    }

    public boolean evaluate(FluidStack stack, NBTCondition NBTcondition) {
        return evaluate(stack.tag, NBTcondition);
    }

    public boolean evaluate(NBTTagCompound nbtTagCompound, NBTCondition NBTcondition) {
        return hasKey(nbtTagCompound, NBTcondition.nbtKey) && keyValueMatches(nbtTagCompound, NBTcondition.nbtKey, NBTcondition.value);
    }

    public boolean keyValueMatches(NBTTagCompound tagCompound, String nbtKey, Long value) {
        return false;
    }
}
