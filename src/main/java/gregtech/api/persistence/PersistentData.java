package gregtech.api.persistence;

import gregtech.api.GTValues;
import gregtech.api.util.GTLog;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PersistentData {

    private static final PersistentData INSTANCE = new PersistentData();

    private @Nullable Path path;
    private @Nullable NBTTagCompound tag;

    public static @NotNull PersistentData instance() {
        return INSTANCE;
    }

    private PersistentData() {}

    @ApiStatus.Internal
    public void init() {
        this.path = Loader.instance().getConfigDir().toPath()
                .resolve(GTValues.MODID)
                .resolve("persistent_data.dat");
    }

    /**
     * @return the stored persistent data
     */
    public synchronized @NotNull NBTTagCompound getTag() {
        if (this.tag == null) {
            this.tag = read();
        }
        return this.tag;
    }

    /**
     * @return the read NBTTagCompound from disk
     */
    private @NotNull NBTTagCompound read() {
        GTLog.logger.debug("Reading persistent data from path {}", path);
        if (this.path == null) {
            throw new IllegalStateException("Persistent data path cannot be null");
        }

        if (!Files.exists(path)) {
            return new NBTTagCompound();
        }

        try (InputStream inputStream = Files.newInputStream(this.path)) {
            return CompressedStreamTools.readCompressed(inputStream);
        } catch (IOException e) {
            GTLog.logger.error("Failed to read persistent data", e);
            return new NBTTagCompound();
        }
    }

    /**
     * Save the GT Persistent data to disk
     */
    public synchronized void save() {
        if (this.tag != null) {
            write(this.tag);
        }
    }

    /**
     * @param tagCompound the tag compound to save to disk
     */
    private void write(@NotNull NBTTagCompound tagCompound) {
        GTLog.logger.debug("Writing persistent data to path {}", path);
        if (tagCompound.isEmpty()) {
            return;
        }

        if (this.path == null) {
            throw new IllegalStateException("Persistent data path cannot be null");
        }

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                GTLog.logger.error("Could not create persistent data dir", e);
                return;
            }
        }

        try (OutputStream outputStream = Files.newOutputStream(path)) {
            CompressedStreamTools.writeCompressed(tagCompound, outputStream);
        } catch (IOException e) {
            GTLog.logger.error("Failed to write persistent data", e);
        }
    }
}
