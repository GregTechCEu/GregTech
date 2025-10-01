package gregtech.api.block.coil;

import gregtech.api.unification.material.Material;

public class CoilStatBuilder {

    private final CustomCoilStats stats;

    CoilStatBuilder() {
        this.stats = new CustomCoilStats();
    }

    public CoilStatBuilder material(Material material) {
        stats.material = material;
        stats.name = material.getResourceLocation().getPath();
        return this;
    }

    public CoilStatBuilder coilTemp(int coilTemperature) {
        stats.coilTemperature = coilTemperature;
        return this;
    }

    public CoilStatBuilder tier(int tier) {
        stats.tier = Math.max(0, tier);
        return this;
    }

    public CoilStatBuilder multiSmelter(int level, int energyDiscount) {
        stats.level = level;
        stats.energyDiscount = energyDiscount;
        return this;
    }

    CustomCoilStats build() {
        return this.stats;
    }
}
