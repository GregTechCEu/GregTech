package gregtech.api.pipenet.predicate;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class AbstractEdgePredicate<T extends AbstractEdgePredicate<T>> implements Predicate<IPredicateTestObject>, INBTSerializable<NBTTagCompound> {

    protected static final Map<String, Supplier<AbstractEdgePredicate<?>>> PREDICATE_SUPPLIERS = new Object2ObjectOpenHashMap<>();

    protected BlockPos sourcePos;
    protected BlockPos targetPos;

    public void setPosInfo(BlockPos sourcePos, BlockPos targetPos) {
        this.sourcePos = sourcePos;
        this.targetPos = targetPos;
    }

    protected abstract String predicateName();

    public BlockPos getSourcePos() {
        return sourcePos;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    private static Supplier<AbstractEdgePredicate<?>> getSupplier(String identifier) {
        return PREDICATE_SUPPLIERS.getOrDefault(identifier, () -> null);
    }

    @Nullable
    public static AbstractEdgePredicate<?> newPredicate(String identifier) {
        return getSupplier(identifier).get();
    }

    @Nullable
    public static AbstractEdgePredicate<?> nbtPredicate(@NotNull NBTTagCompound nbt) {
        AbstractEdgePredicate<?> predicate = getSupplier(nbt.getString("Type")).get();
        if (predicate != null) {
            predicate.deserializeNBT(nbt.getCompoundTag("Data"));
            return predicate;
        }
        return null;
    }

    public static @NotNull NBTTagCompound toNBT(@NotNull AbstractEdgePredicate<?> predicate) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Type", predicate.predicateName());
        tag.setTag("Data", predicate.serializeNBT());
        return tag;
    }
}
