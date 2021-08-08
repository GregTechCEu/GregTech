package gregtech.api.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.DataSerializerEntry;

public class NBTUtil {

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
}
