package gregtech.api.pipenet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;

public abstract class AbstractEdgePredicate<T extends AbstractEdgePredicate<T>>
                                           implements Predicate<Object>, INBTSerializable<NBTTagCompound> {

    protected static final Map<String, AbstractEdgePredicate<?>> PREDICATES = new Object2ObjectOpenHashMap<>();

    protected BlockPos sourcePos;
    protected BlockPos targetPos;

    public void setPosInfo(BlockPos sourcePos, BlockPos targetPos) {
        this.sourcePos = sourcePos;
        this.targetPos = targetPos;
    }

    @NotNull
    protected abstract T createPredicate();

    protected abstract String predicateType();

    public static AbstractEdgePredicate<?> newPredicate(String identifier) {
        AbstractEdgePredicate<?> predicate = PREDICATES.get(identifier);
        if (predicate != null) {
            return predicate.createPredicate();
        }
        return null;
    }

    @Nullable
    public static AbstractEdgePredicate<?> nbtPredicate(NBTTagCompound nbt) {
        AbstractEdgePredicate<?> predicate = PREDICATES.get(nbt.getString("Type"));
        if (predicate != null) {
            predicate = predicate.createPredicate();
            predicate.deserializeNBT(nbt.getCompoundTag("Data"));
            return predicate;
        }
        return null;
    }

    public static NBTTagCompound toNBT(AbstractEdgePredicate<?> predicate) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Type", predicate.predicateType());
        tag.setTag("Data", predicate.serializeNBT());
        return tag;
    }
}
