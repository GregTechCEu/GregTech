package gregtech.api.graphnet.predicate;

import gregtech.api.graphnet.gather.GTGraphGatherables;

import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class EdgePredicateHandler implements INBTSerializable<NBTTagList>, Predicate<IPredicateTestObject> {

    private final Map<String, IEdgePredicate<?, ?>> predicateSet;

    public EdgePredicateHandler() {
        predicateSet = new Object2ObjectOpenHashMap<>();
    }

    private EdgePredicateHandler(Map<String, IEdgePredicate<?, ?>> predicateSet) {
        this.predicateSet = predicateSet;
    }

    public EdgePredicateHandler mergePredicate(IEdgePredicate<?, ?> predicate) {
        IEdgePredicate<?, ?> current = predicateSet.get(predicate.getName());
        if (predicate.getClass().isInstance(current)) {
            predicate = current.union(predicate);
        }
        return setPredicate(predicate);
    }

    public EdgePredicateHandler setPredicate(IEdgePredicate<?, ?> predicate) {
        predicateSet.put(predicate.getName(), predicate);
        return this;
    }

    public EdgePredicateHandler removePredicate(IEdgePredicate<?, ?> key) {
        predicateSet.remove(key.getName());
        return this;
    }

    @Override
    public boolean test(IPredicateTestObject iPredicateTestObject) {
        // TODO predicate 'categories' or 'affinities' that determine order of operations with and-y and or-y behavior?
        boolean result = false;
        for (IEdgePredicate<?, ?> predicate : predicateSet.values()) {
            boolean test = predicate.test(iPredicateTestObject);
            if (predicate.andy() && !test) return false;
            else result |= test;
        }
        return result;
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList list = new NBTTagList();
        for (IEdgePredicate<?, ?> entry : predicateSet.values()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("Tag", entry.serializeNBT());
            tag.setString("Name", entry.getName());
            list.appendTag(tag);
        }
        return list;
    }

    @Override
    public void deserializeNBT(NBTTagList nbt) {
        for (int i = 0; i < nbt.tagCount(); i++) {
            NBTTagCompound tag = nbt.getCompoundTagAt(i);
            String key = tag.getString("Name");
            IEdgePredicate<?, ?> entry = this.predicateSet.get(key);
            if (entry == null) entry = getSupplier(key).get();
            if (entry == null) continue;
            entry.deserializeNBTNaive(tag.getTag("Tag"));
        }
    }

    private static Supplier<IEdgePredicate<?, ?>> getSupplier(String identifier) {
        return GTGraphGatherables.getPredicatesRegistry().getOrDefault(identifier, () -> null);
    }
}
