package gregtech.api.worldgen.config;

import com.google.gson.JsonObject;

import javax.annotation.Nonnull;

public interface IWorldgenDefinition {

    // This is the file name
    /**
     * Must be converted using {@link gregtech.api.util.FileUtility#slashToNativeSep(String)}
     * before it can be used as a file path
     */
    String getDepositName();

    boolean initializeFromConfig(@Nonnull JsonObject configRoot);
}
