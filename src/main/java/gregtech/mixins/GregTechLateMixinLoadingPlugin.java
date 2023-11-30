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
        configs.add("mixins.gregtech.jei.json");
        configs.add("mixins.gregtech.ctm.json");
        configs.add("mixins.gregtech.ccl.json");

        return configs;
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return switch (mixinConfig) {
            case "mixins.gregtech.theoneprobe.json" -> Loader.isModLoaded(GTValues.MODID_TOP);
            case "mixins.gregtech.jei.json" -> Loader.isModLoaded(GTValues.MODID_JEI);
            case "mixin.gregtech.ctm.json" -> Loader.isModLoaded(GTValues.MODID_CTM);
            default -> true;
        };
    }
}
