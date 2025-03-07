package gregtech.api.recipes.ingredients;

import gregtech.api.GTValues;
import gregtech.api.recipes.ingredients.nbt.NBTMatcher;
import gregtech.api.recipes.lookup.flag.FluidStackMatchingContext;
import gregtech.api.recipes.lookup.flag.ItemStackMatchingContext;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.EnumMap;

public final class IngredientCache {

    private static boolean released = false;

    private static @Nullable Int2ObjectOpenHashMap<OreItemIngredientCache> oreCache = new Int2ObjectOpenHashMap<>();

    private static @Nullable Object2ObjectMap<EnumMap<ItemStackMatchingContext, ObjectOpenCustomHashSet<ItemStack>>, StandardItemIngredientCache> itemCache = new Object2ObjectOpenCustomHashMap<>(
            new Hash.Strategy<>() {

                @Override
                public int hashCode(EnumMap<ItemStackMatchingContext, ObjectOpenCustomHashSet<ItemStack>> o) {
                    if (o == null) return 0;
                    int hash = 0;
                    for (ItemStackMatchingContext context : ItemStackMatchingContext.VALUES) {
                        hash = hash * 31;
                        ObjectOpenCustomHashSet<ItemStack> cO = o.get(context);
                        if (cO != null) {
                            hash += cO.hashCode();
                        }
                    }
                    return hash;
                }

                @Override
                public boolean equals(EnumMap<ItemStackMatchingContext, ObjectOpenCustomHashSet<ItemStack>> a,
                                      EnumMap<ItemStackMatchingContext, ObjectOpenCustomHashSet<ItemStack>> b) {
                    if (a == null && b == null) return true;
                    if (a == null ^ b == null) return false;
                    for (ItemStackMatchingContext context : ItemStackMatchingContext.VALUES) {
                        ObjectOpenCustomHashSet<ItemStack> cA = a.get(context);
                        ObjectOpenCustomHashSet<ItemStack> cB = b.get(context);
                        if (cA == null && cB == null) continue;
                        if (cA != null && cA.equals(cB)) continue;
                        return false;
                    }
                    return true;
                }
            });
    private static @Nullable Object2ObjectMap<EnumMap<FluidStackMatchingContext, ObjectOpenCustomHashSet<FluidStack>>, StandardFluidIngredientCache> fluidCache = new Object2ObjectOpenCustomHashMap<>(
            new Hash.Strategy<>() {

                @Override
                public int hashCode(EnumMap<FluidStackMatchingContext, ObjectOpenCustomHashSet<FluidStack>> o) {
                    if (o == null) return 0;
                    int hash = 0;
                    for (FluidStackMatchingContext context : FluidStackMatchingContext.VALUES) {
                        hash = hash * 31;
                        ObjectOpenCustomHashSet<FluidStack> cO = o.get(context);
                        if (cO != null) {
                            hash += cO.hashCode();
                        }
                    }
                    return hash;
                }

                @Override
                public boolean equals(EnumMap<FluidStackMatchingContext, ObjectOpenCustomHashSet<FluidStack>> a,
                                      EnumMap<FluidStackMatchingContext, ObjectOpenCustomHashSet<FluidStack>> b) {
                    if (a == null && b == null) return true;
                    if (a == null ^ b == null) return false;
                    for (FluidStackMatchingContext context : FluidStackMatchingContext.VALUES) {
                        ObjectOpenCustomHashSet<FluidStack> cA = a.get(context);
                        ObjectOpenCustomHashSet<FluidStack> cB = b.get(context);
                        if (cA == null && cB == null) continue;
                        if (cA != null && cA.equals(cB)) continue;
                        return false;
                    }
                    return true;
                }
            });

    public static OreItemIngredient getOreIngredient(String ore, long count) {
        return getOreIngredient(ore, count, null);
    }

    public static OreItemIngredient getOreIngredient(String ore, long count, @Nullable NBTMatcher matcher) {
        if (oreCache != null)
            return oreCache.computeIfAbsent(OreDictionary.getOreID(ore), OreItemIngredientCache::new).getWith(matcher,
                    count);
        else return new OreItemIngredient(new OreItemIngredient.OreItemIngredientBacker(ore), matcher, count);
    }

    public static StandardItemIngredient getItemIngredient(EnumMap<ItemStackMatchingContext, ObjectOpenCustomHashSet<ItemStack>> matching,
                                                           long count) {
        return getItemIngredient(matching, count, null);
    }

    public static StandardItemIngredient getItemIngredient(EnumMap<ItemStackMatchingContext, ObjectOpenCustomHashSet<ItemStack>> matching,
                                                           long count, @Nullable NBTMatcher matcher) {
        if (itemCache != null)
            return itemCache.computeIfAbsent(matching, StandardItemIngredientCache::new).getWith(matcher, count);
        else return new StandardItemIngredient(new StandardItemIngredient.ItemIngredientBacker(matching), matcher,
                count);
    }

    public static StandardFluidIngredient getFluidIngredient(EnumMap<FluidStackMatchingContext, ObjectOpenCustomHashSet<FluidStack>> matching,
                                                             long count) {
        return getFluidIngredient(matching, count, null);
    }

    public static StandardFluidIngredient getFluidIngredient(EnumMap<FluidStackMatchingContext, ObjectOpenCustomHashSet<FluidStack>> matching,
                                                             long count, @Nullable NBTMatcher matcher) {
        if (fluidCache != null)
            return fluidCache.computeIfAbsent(matching, StandardFluidIngredientCache::new).getWith(matcher, count);
        else return new StandardFluidIngredient(new StandardFluidIngredient.FluidIngredientBacker(matching), matcher,
                count);
    }

    /**
     * Removes all cache references and prevents caching for any further calls.
     */
    public static void releaseCaches() {
        if (released) return;
        released = true;
        assert oreCache != null;
        int count = 0;
        for (OreItemIngredientCache cache : oreCache.values()) {
            count += cache.cache.size();
            cache.cache = null;
        }
        assert itemCache != null;
        for (StandardItemIngredientCache cache : itemCache.values()) {
            count += cache.cache.size();
            cache.cache = null;
        }
        assert fluidCache != null;
        for (StandardFluidIngredientCache cache : fluidCache.values()) {
            count += cache.cache.size();
            cache.cache = null;
        }
        if (ConfigHolder.misc.debug || GTValues.isDeobfEnvironment()) {
            GTLog.logger.info("IngredientCache disabled; released {} unique instances", count);
        }
        oreCache = null;
        itemCache = null;
        fluidCache = null;
    }

    /**
     * Creates new cache references, enabling caching for future calls.
     */
    public static void reinstateCaches() {
        if (!released) return;
        released = false;
        oreCache = new Int2ObjectOpenHashMap<>();
        itemCache = new Object2ObjectOpenHashMap<>();
        fluidCache = new Object2ObjectOpenHashMap<>();
    }

    private static class OreItemIngredientCache extends OreItemIngredient.OreItemIngredientBacker {

        Object2ObjectOpenHashMap<MatcherAndCount, OreItemIngredient> cache = new Object2ObjectOpenHashMap<>();

        protected OreItemIngredientCache(int oreID) {
            super(oreID);
        }

        public OreItemIngredient getWith(@Nullable NBTMatcher matcher,
                                         @Range(from = 1, to = Long.MAX_VALUE) long count) {
            return cache.computeIfAbsent(MatcherAndCount.of(matcher, count),
                    c -> new OreItemIngredient(this, c.matcher(), c.count()));
        }
    }

    private static class StandardItemIngredientCache extends StandardItemIngredient.ItemIngredientBacker {

        Object2ObjectOpenHashMap<MatcherAndCount, StandardItemIngredient> cache = new Object2ObjectOpenHashMap<>();

        protected StandardItemIngredientCache(@NotNull EnumMap<ItemStackMatchingContext, ObjectOpenCustomHashSet<ItemStack>> matching) {
            super(matching);
        }

        public StandardItemIngredient getWith(@Nullable NBTMatcher matcher,
                                              @Range(from = 1, to = Long.MAX_VALUE) long count) {
            return cache.computeIfAbsent(MatcherAndCount.of(matcher, count),
                    c -> new StandardItemIngredient(this, c.matcher(), c.count()));
        }
    }

    private static class StandardFluidIngredientCache extends StandardFluidIngredient.FluidIngredientBacker {

        Object2ObjectOpenHashMap<MatcherAndCount, StandardFluidIngredient> cache = new Object2ObjectOpenHashMap<>();

        protected StandardFluidIngredientCache(@NotNull EnumMap<FluidStackMatchingContext, ObjectOpenCustomHashSet<FluidStack>> matching) {
            super(matching);
        }

        public StandardFluidIngredient getWith(@Nullable NBTMatcher matcher,
                                               @Range(from = 1, to = Long.MAX_VALUE) long count) {
            return cache.computeIfAbsent(MatcherAndCount.of(matcher, count),
                    c -> new StandardFluidIngredient(this, c.matcher(), c.count()));
        }
    }

    @Desugar
    private record MatcherAndCount(@Nullable NBTMatcher matcher, long count) {

        @Contract("_, _ -> new")
        public static @NotNull MatcherAndCount of(@Nullable NBTMatcher matcher, long count) {
            return new MatcherAndCount(matcher, count);
        }
    }
}
