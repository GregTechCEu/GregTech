package gregtech.api.recipes.crafttweaker;

import com.google.common.base.Preconditions;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.api.util.GTLog;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CTNBTMultiItemMatcher implements NBTMatcher {

    private final Map<ItemStack, List<NBTTagCompound>> map;

    /**
     * NBTMatcher for CraftTweaker recipes
     *
     * @param map the mappings of ItemStack to potential nbt tags to match. Key hashing should ignore nbt tags.
     */
    public CTNBTMultiItemMatcher(@Nonnull Map<ItemStack, List<NBTTagCompound>> map) {
        Preconditions.checkArgument(!map.isEmpty(), "Map must not be empty.");
        this.map = map;
    }

    @Override
    public boolean evaluate(@Nullable NBTTagCompound nbtTagCompound, @Nullable NBTCondition nbtCondition) {
        // should never get called
        GTLog.logger.warn("CTNBTMultiItemMatcher#evaluate(NBTTagCompound, NBTCondition) was called. This should not happen.");
        return false;
    }

    @Override
    public boolean evaluate(@Nonnull ItemStack stack, @Nullable NBTCondition nbtCondition) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        // stack has no nbt to match
        if (tagCompound == null) return false;

        List<NBTTagCompound> list = map.get(stack);
        // no nbt to match for this instance
        if (list == null) return true;

        final Set<Map.Entry<String, NBTBase>> toCheck = tagCompound.tagMap.entrySet();
        for (NBTTagCompound requirement : list) {
            // return if the tag to check has everything the recipe requires, ignoring extraneous tags on toCheck
            if (toCheck.containsAll(requirement.tagMap.entrySet())) return true;
        }
        return false;
    }
}
