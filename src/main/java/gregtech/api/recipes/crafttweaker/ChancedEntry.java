package gregtech.api.recipes.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

@ZenClass("mods.gregtech.recipe.ChancedEntry")
@ZenRegister
@SuppressWarnings("unused")
public class ChancedEntry {

    private final IItemStack output;
    private final int chance;
    private final int boostPerTier;

    public ChancedEntry(IItemStack output, int chance, int boostPerTier) {
        this.output = output;
        this.chance = chance;
        this.boostPerTier = boostPerTier;
    }

    @ZenGetter("output")
    public IItemStack getOutput() {
        return output;
    }

    @ZenGetter("chance")
    public int getChance() {
        return chance;
    }

    @ZenGetter("boostPerTier")
    public int getBoostPerTier() {
        return boostPerTier;
    }
}
