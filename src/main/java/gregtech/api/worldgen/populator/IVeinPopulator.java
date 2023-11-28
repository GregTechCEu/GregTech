package gregtech.api.worldgen.populator;

import gregtech.api.worldgen.config.OreDepositDefinition;

import com.google.gson.JsonObject;

public interface IVeinPopulator {

    void loadFromConfig(JsonObject object);

    void initializeForVein(OreDepositDefinition definition);
}
