package gregtech.api.persistence;

import gregtech.api.GTValues;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

public final class PersistentData {

    public static final String CATEGORY_NAME = "persistent data";

    private static final PersistentData INSTANCE = new PersistentData();
    private static final String FILE_NAME = "persistent_data.cfg";

    private @Nullable Configuration config;

    public static @NotNull PersistentData instance() {
        return INSTANCE;
    }

    private PersistentData() {}

    /**
     * @return the persistent data storage
     */
    public @NotNull Configuration getConfig() {
        if (config == null) {
            Path configFolderPath = Loader.instance().getConfigDir().toPath().resolve(GTValues.MODID);
            File file = configFolderPath.resolve(FILE_NAME).toFile();
            config = new Configuration(file);
        }
        return config;
    }

    @ApiStatus.Internal
    public void init() {
        Configuration configuration = getConfig();
        configuration.load();
        String comment = """
                GregTech Persistent Data. Items in this file will persist across game loads.
                Modifications to this file may be overwritten by GT.
                If you are a modpack author, you should ship this file in releases.""";
        configuration.addCustomCategoryComment(CATEGORY_NAME, comment);

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
