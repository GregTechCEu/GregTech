package gregtech.mixins;

import gregtech.api.util.Mods;

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
        configs.add("mixins.gregtech.littletiles.json");
        configs.add("mixins.gregtech.vintagium.json");

        return configs;
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return switch (mixinConfig) {
            case "mixins.gregtech.theoneprobe.json" -> Mods.TheOneProbe.isModLoaded();
            case "mixins.gregtech.jei.json" -> Mods.JustEnoughItems.isModLoaded();
            case "mixin.gregtech.ctm.json" -> Mods.CTM.isModLoaded();
            case "mixins.gregtech.littletiles.json" -> Mods.LittleTiles.isModLoaded();
            case "mixins.gregtech.vintagium.json" -> Mods.Vintagium.isModLoaded();
            default -> true;
        };
    }
}
