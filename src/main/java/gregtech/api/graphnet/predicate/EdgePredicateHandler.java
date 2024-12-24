package gregtech.api.graphnet.predicate;

import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Predicate;

public final class EdgePredicateHandler implements INBTSerializable<NBTTagList>, Predicate<IPredicateTestObject> {

    private final Map<NetPredicateType<?>, EdgePredicate<?, ?>> predicateSet;

    public EdgePredicateHandler() {
        predicateSet = new Object2ObjectOpenHashMap<>();
    }

    /**
     * If the {@link EdgePredicate#union(EdgePredicate)} operation is not supported for this predicate,
     * nothing happens if a predicate is already present.
     */
    public EdgePredicateHandler mergePredicate(@NotNull EdgePredicate<?, ?> predicate) {
        EdgePredicate<?, ?> current = predicateSet.get(predicate.getType());
        if (current == null) return setPredicate(predicate);

        if (predicate.getClass().isInstance(current)) {
            predicate = current.union(predicate);
            if (predicate == null) return this;
        }
        return setPredicate(predicate);
    }

    /**
     * Do not modify the returned value
     */
    public Map<NetPredicateType<?>, EdgePredicate<?, ?>> getPredicateSet() {
        return predicateSet;
    }

    public EdgePredicateHandler setPredicate(@NotNull EdgePredicate<?, ?> predicate) {
        predicateSet.put(predicate.getType(), predicate);
        return this;
    }

    public EdgePredicateHandler removePredicate(@NotNull EdgePredicate<?, ?> predicate) {
        return removePredicate(predicate.getType());
    }

    public EdgePredicateHandler removePredicate(@NotNull NetPredicateType<?> type) {
        predicateSet.remove(type);
        return this;
    }

    public boolean hasPredicate(@NotNull EdgePredicate<?, ?> predicate) {
        return hasPredicate(predicate.getType());
    }

    public boolean hasPredicate(@NotNull NetPredicateType<?> type) {
        return predicateSet.containsKey(type);
    }

    public void clearPredicates() {
        this.predicateSet.clear();
    }

    public boolean shouldIgnore() {
        return predicateSet.isEmpty();
    }

    @Override
    public boolean test(IPredicateTestObject iPredicateTestObject) {
        if (shouldIgnore()) return true;
        boolean result = false;
        for (EdgePredicate<?, ?> predicate : predicateSet.values()) {
            // TODO predicate 'categories' or 'affinities' that determine order of operations with and-y and or-y
            // behavior?
            boolean test = predicate.test(iPredicateTestObject);
            if (predicate.andy() && !test) return false;
            else result |= test;
        }
        return result;
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList list = new NBTTagList();
        for (EdgePredicate<?, ?> entry : predicateSet.values()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("Tag", entry.serializeNBT());
            tag.setString("Type", entry.getType().getName());
            list.appendTag(tag);
        }
        return list;
    }

    @Override
    public void deserializeNBT(NBTTagList nbt) {
        for (int i = 0; i < nbt.tagCount(); i++) {
            NBTTagCompound tag = nbt.getCompoundTagAt(i);
            NetPredicateType<?> type = NetPredicateRegistry.getType(tag.getString("Type"));
            EdgePredicate<?, ?> entry = this.predicateSet.computeIfAbsent(type, NetPredicateType::getNew);
            entry.deserializeNBTNaive(tag.getTag("Tag"));
        }
    }
}
