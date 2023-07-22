package gregtech.mixins;

import gregtech.api.GTValues;
import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.List;

public class GregTechLateMixinLoadingPlugin implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        List<String> configs = new ArrayList<>();

        configs.add("mixins.gregtech.theoneprobe.json");

        return configs;
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {

        return switch (mixinConfig) {
            case "mixins.gregtech.theoneprobe.json" -> Loader.isModLoaded(GTValues.MODID_TOP);
            default -> true;
        };

    }
}
