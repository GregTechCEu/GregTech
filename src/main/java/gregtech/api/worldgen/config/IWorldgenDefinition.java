package gregtech.api.worldgen.config;

import com.google.gson.JsonObject;

import javax.annotation.Nonnull;

public interface IWorldgenDefinition {

    //This is the file name
    String getDepositName();

    boolean initializeFromConfig(@Nonnull JsonObject configRoot);
}
