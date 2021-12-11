package gregtech.api.worldgen.config;

import stanhebben.zenscript.annotations.ZenGetter;

public interface IWorldgenDefinition {

    //This is the file name
    @ZenGetter("depositName")
    String getDepositName();
}
