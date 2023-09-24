package gregtech.api.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * An optimised data structure backed by two arrays.
 * This is essentially equivalent to <code>List<Pair<Integer, byte[]>></code>, but more efficient.
 * {@link it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap} can not be used since it doesn't allow duplicate discriminators.
 */
public class PacketDataList {

    private int[] discriminators;
    private byte[][] data;
    private int size = 0;

    public PacketDataList() {
        this.discriminators = new int[4];
        this.data = new byte[4][];
    }

    private void ensureSize(int s) {
        if (this.discriminators.length < s) {
            int n = this.discriminators.length;
            int[] temp = new int[n + n];
            byte[][] temp2 = new byte[n + n][];
            System.arraycopy(this.discriminators, 0, temp, 0, n);
            System.arraycopy(this.data, 0, temp2, 0, n);
            this.discriminators = temp;
            this.data = temp2;
        }
    }

    public void add(int discriminator, byte[] data) {
        ensureSize(this.size + 1);
        this.discriminators[this.size] = discriminator;
        this.data[this.size] = data;
        this.size++;
    }

    public void addAll(PacketDataList dataList) {
        for (int i = 0; i < dataList.size; i++) {
            add(dataList.discriminators[i], dataList.data[i]);
        }
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        for (int i = 0; i < this.size; i++) {
            this.data[i] = null;
        }
        this.size = 0;
    }

    public NBTTagList dumpToNbt() {
        NBTTagList listTag = new NBTTagList();
        for (int i = 0; i < this.size; i++) {
            NBTTagCompound entryTag = new NBTTagCompound();
            entryTag.setByteArray(Integer.toString(this.discriminators[i]), this.data[i]);
            listTag.appendTag(entryTag);
            this.data[i] = null;
        }
        this.size = 0;
        return listTag;
    }
}
