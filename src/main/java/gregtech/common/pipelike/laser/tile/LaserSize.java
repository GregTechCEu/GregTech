package gregtech.common.pipelike.laser.tile;
import gregtech.api.pipenet.block.material.IMaterialPipeType;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.ConfigHolder;
import gregtech.common.pipelike.cable.WireProperties;
import gregtech.common.pipelike.laser.tile.LaserProperties;

import static gregtech.api.GTValues.M;
import static gregtech.api.unification.ore.OrePrefix.Flags.ENABLE_UNIFICATION;

public enum LaserSize implements IMaterialPipeType<LaserProperties> {


    Laser_single("laser_single",10024,OrePrefix.laserGTSingle);


    public final String name;
    public final int amperage;
    public final OrePrefix orePrefix;

    LaserSize(String name, int amperage,  OrePrefix orePrefix) {
        this.name = name;
        this.amperage = amperage;
        this.orePrefix = orePrefix;
    }

    @Override
    public String getName() {
        return name;
    }



    @Override
    public OrePrefix getOrePrefix() {
        return orePrefix;
    }


    @Override
    public LaserProperties modifyProperties(LaserProperties baseProperties) {


        return new LaserProperties(baseProperties.laserVoltage, baseProperties.parallel);
    }

    @Override
    public float getThickness() {
        return .1f;
    }


    @Override
    public boolean isPaintable() {
        return true;
    }
}
