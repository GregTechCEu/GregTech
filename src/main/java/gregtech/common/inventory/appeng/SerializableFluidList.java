package gregtech.common.inventory.appeng;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.MeaningfulFluidIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * @Author GlodBlock
 * @Description A serializable {@link IItemList} from AE2, specially improved for fluid.
 * @Date 2023/4/18-23:52
 */
public class SerializableFluidList implements IItemList<IAEFluidStack>, INBTSerializable<NBTTagList> {

    private final Reference2ObjectMap<Fluid, IAEFluidStack> records = new Reference2ObjectOpenHashMap<>();

    public SerializableFluidList() {}

    @Override
    public void add(IAEFluidStack fluid) {
        if (fluid != null) {
            this.getOrCreateRecord(fluid).add(fluid);
        }
    }

    @Override
    public IAEFluidStack findPrecise(IAEFluidStack fluid) {
        if (fluid == null) {
            return null;
        } else {
            return this.records.get(fluid.getFluid());
        }
    }

    @Override
    public Collection<IAEFluidStack> findFuzzy(IAEFluidStack filter, FuzzyMode fuzzy) {
        IAEFluidStack stack = findPrecise(filter);
        return stack != null ? Collections.singleton(stack) : Collections.emptyList();
    }

    @Override
    public boolean isEmpty() {
        return !this.iterator().hasNext();
    }

    private IAEFluidStack getOrCreateRecord(@NotNull IAEFluidStack fluid) {
        return this.records.computeIfAbsent(fluid.getFluid(),
                key -> AEFluidStack.fromFluidStack(new FluidStack(key, 0)));
    }

    private IAEFluidStack getRecord(@NotNull IAEFluidStack fluid) {
        return this.records.get(fluid.getFluid());
    }

    @Override
    public void addStorage(IAEFluidStack fluid) {
        if (fluid != null) {
            this.getOrCreateRecord(fluid).incStackSize(fluid.getStackSize());
        }
    }

    @Override
    public void addCrafting(IAEFluidStack fluid) {
        if (fluid != null) {
            IAEFluidStack record = this.getRecord(fluid);
            if (record != null) {
                record.setCraftable(true);
            }
        }
    }

    @Override
    public void addRequestable(IAEFluidStack fluid) {
        if (fluid != null) {
            IAEFluidStack record = this.getRecord(fluid);
            if (record != null) {
                record.setCountRequestable(record.getCountRequestable() + fluid.getCountRequestable());
            }
        }
    }

    @Override
    public IAEFluidStack getFirstItem() {
        for (final IAEFluidStack fluid : this) {
            return fluid;
        }
        return null;
    }

    @Override
    public int size() {
        return this.records.size();
    }

    @Override
    public Iterator<IAEFluidStack> iterator() {
        return new MeaningfulFluidIterator<>(this.records.values().iterator());
    }

    @Override
    public void resetStatus() {
        for (final IAEFluidStack i : this) {
            i.reset();
        }
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList list = new NBTTagList();
        for (IAEFluidStack fluid : this) {
            if (fluid != null) {
                NBTTagCompound tag = new NBTTagCompound();
                fluid.writeToNBT(tag);
                list.appendTag(tag);
            }
        }
        return list;
    }

    @Override
    public void deserializeNBT(NBTTagList list) {
        for (NBTBase tag : list) {
            if (tag instanceof NBTTagCompound) {
                IAEFluidStack fluid = AEFluidStack.fromNBT((NBTTagCompound) tag);
                if (fluid != null) {
                    this.records.put(fluid.getFluid(), fluid);
                }
            }
        }
    }
}
