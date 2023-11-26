package gregtech.common.inventory.appeng;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;

import java.util.Collection;
import java.util.Iterator;

/**
 * @Author GlodBlock
 * @Description A serializable {@link IItemList} from AE2
 * @Date 2023/4/18-23:52
 */
public class SerializableItemList implements IItemList<IAEItemStack>, INBTSerializable<NBTTagList> {

    private final ItemList parent = new ItemList();

    public SerializableItemList() {}

    @Override
    public void addStorage(IAEItemStack stack) {
        this.parent.addStorage(stack);
    }

    @Override
    public void addCrafting(IAEItemStack stack) {
        this.parent.addStorage(stack);
    }

    @Override
    public void addRequestable(IAEItemStack stack) {
        this.parent.addRequestable(stack);
    }

    @Override
    public IAEItemStack getFirstItem() {
        return this.parent.getFirstItem();
    }

    @Override
    public int size() {
        return this.parent.size();
    }

    @Override
    public Iterator<IAEItemStack> iterator() {
        return this.parent.iterator();
    }

    @Override
    public void resetStatus() {
        this.parent.resetStatus();
    }

    @Override
    public void add(IAEItemStack stack) {
        this.parent.add(stack);
    }

    @Override
    public IAEItemStack findPrecise(IAEItemStack stack) {
        return this.parent.findPrecise(stack);
    }

    @Override
    public Collection<IAEItemStack> findFuzzy(IAEItemStack stack, FuzzyMode fuzzyMode) {
        return this.parent.findFuzzy(stack, fuzzyMode);
    }

    @Override
    public boolean isEmpty() {
        return this.parent.isEmpty();
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList list = new NBTTagList();
        for (IAEItemStack item : this) {
            if (item != null) {
                NBTTagCompound tag = new NBTTagCompound();
                item.writeToNBT(tag);
                list.appendTag(tag);
            }
        }
        return list;
    }

    @Override
    public void deserializeNBT(NBTTagList list) {
        for (NBTBase tag : list) {
            if (tag instanceof NBTTagCompound) {
                IAEItemStack item = AEItemStack.fromNBT((NBTTagCompound) tag);
                if (item != null) {
                    this.parent.add(item);
                }
            }
        }
    }
}
