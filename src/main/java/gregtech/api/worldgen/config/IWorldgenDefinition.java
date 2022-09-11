package gregtech.api.worldgen.config;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

public interface IWorldgenDefinition {

    //This is the file name
    String getDepositName();

    boolean initializeFromConfig(@NotNull JsonObject configRoot);
}
