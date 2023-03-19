package gregtech.api.recipes.crafttweaker;

import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class CTNBTMatcher implements NBTMatcher {

    private final Set<Map.Entry<String, NBTBase>> requiredNBT;

    /**
     * NBTMatcher for CraftTweaker recipes
     *
     * @param tagCompound the required NBT tag to match.
     */
    public CTNBTMatcher(@Nonnull NBTTagCompound tagCompound) {
        this.requiredNBT = tagCompound.tagMap.entrySet();
    }

    @Override
    public boolean evaluate(@Nullable NBTTagCompound nbtTagCompound, @Nullable NBTCondition nbtCondition) {
        // this matcher requires a tag
        if (nbtTagCompound == null) return false;
        // return if the tag to check has everything the recipe requires
        return nbtTagCompound.tagMap.entrySet().containsAll(requiredNBT);
    }
}
