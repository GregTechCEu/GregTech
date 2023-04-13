package gregtech.api.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.DataSerializerEntry;

public class GTNBTUtil {

    public static final DataSerializer<Vec3d> VECTOR = new DataSerializer<Vec3d>() {
        @Override
        public void write(PacketBuffer buf, Vec3d value) {
            buf.writeFloat((float) value.x);
            buf.writeFloat((float) value.y);
            buf.writeFloat((float) value.z);
        }

        @Override
        public Vec3d read(PacketBuffer buf) {
            return new Vec3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        @Override
        public DataParameter<Vec3d> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        @Override
        public Vec3d copyValue(Vec3d value) {
            return new Vec3d(value.x, value.y, value.z);
        }
    };

    public static void registerSerializers() {
        ForgeRegistries.DATA_SERIALIZERS.register(new DataSerializerEntry(VECTOR).setRegistryName("vector"));
    }

    public static Vec3d readVec3d(NBTTagCompound tag) {
        return new Vec3d(
                tag.getFloat("X"),
                tag.getFloat("Y"),
                tag.getFloat("Z")
        );
    }

    public static NBTTagCompound writeVec3d(Vec3d vector) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setFloat("X", (float) vector.x);
        tag.setFloat("Y", (float) vector.y);
        tag.setFloat("Z", (float) vector.z);
        return tag;
    }

    public static void writeItems(IItemHandler handler, String tagName, NBTTagCompound tag) {
        NBTTagList tagList = new NBTTagList();

        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                NBTTagCompound stackTag = new NBTTagCompound();
                stackTag.setInteger("Slot", i);
                handler.getStackInSlot(i).writeToNBT(stackTag);
                tagList.appendTag(stackTag);
            }
        }

        tag.setTag(tagName, tagList);
    }

    public static void readItems(IItemHandlerModifiable handler, String tagName, NBTTagCompound tag) {
        if (tag.hasKey(tagName)) {
            NBTTagList tagList = tag.getTagList(tagName, Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                int slot = tagList.getCompoundTagAt(i).getInteger("Slot");

                if (slot >= 0 && slot < handler.getSlots()) {
                    handler.setStackInSlot(slot, new ItemStack(tagList.getCompoundTagAt(i)));
                }
            }
        }
    }

    public static NBTTagCompound getOrCreateNbtCompound(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            compound = new NBTTagCompound();
            stack.setTagCompound(compound);
        }
        return compound;
    }
}
