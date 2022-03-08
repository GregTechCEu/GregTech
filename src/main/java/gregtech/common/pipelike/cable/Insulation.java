package gregtech.common.pipelike.cable;

import gregtech.api.pipenet.block.material.IMaterialPipeType;
import gregtech.api.unification.material.properties.WireProperties;
import gregtech.api.unification.ore.OrePrefix;

import javax.annotation.Nonnull;

public enum Insulation implements IMaterialPipeType<WireProperties> {

    WIRE_SINGLE("wire_single", 0.125f, 1, 2, OrePrefix.wireGtSingle, -1),
    WIRE_DOUBLE("wire_double", 0.25f, 2, 2, OrePrefix.wireGtDouble, -1),
    WIRE_QUADRUPLE("wire_quadruple", 0.375f, 4, 3, OrePrefix.wireGtQuadruple, -1),
    WIRE_OCTAL("wire_octal", 0.5f, 8, 3, OrePrefix.wireGtOctal, -1),
    WIRE_HEX("wire_hex", 0.75f, 16, 3, OrePrefix.wireGtHex, -1),

    CABLE_SINGLE("cable_single", 0.25f, 1, 1, OrePrefix.cableGtSingle, 0),
    CABLE_DOUBLE("cable_double", 0.375f, 2, 1, OrePrefix.cableGtDouble, 1),
    CABLE_QUADRUPLE("cable_quadruple", 0.5f, 4, 1, OrePrefix.cableGtQuadruple, 2),
    CABLE_OCTAL("cable_octal", 0.75f, 8, 1, OrePrefix.cableGtOctal, 3),
    CABLE_HEX("cable_hex", 1.0f, 16, 1, OrePrefix.cableGtHex, 4);

    public final String name;
    public final float thickness;
    public final int amperage;
    public final int lossMultiplier;
    public final OrePrefix orePrefix;
    public final int insulationLevel;

    Insulation(String name, float thickness, int amperage, int lossMultiplier, OrePrefix orePrefix, int insulated) {
        this.name = name;
        this.thickness = thickness;
        this.amperage = amperage;
        this.orePrefix = orePrefix;
        this.insulationLevel = insulated;
        this.lossMultiplier = lossMultiplier;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getThickness() {
        return thickness;
    }

    @Override
    public OrePrefix getOrePrefix() {
        return orePrefix;
    }

    public boolean isCable() {
        return ordinal() > 4;
    }

    @Override
    public WireProperties modifyProperties(WireProperties baseProperties) {

        int lossPerBlock;
        if (!baseProperties.isSuperconductor() && baseProperties.getLossPerBlock() == 0)
            lossPerBlock = (int) (0.75 * lossMultiplier);
        else lossPerBlock = baseProperties.getLossPerBlock() * lossMultiplier;

        return new WireProperties(baseProperties.getVoltage(), baseProperties.getAmperage() * amperage, lossPerBlock, baseProperties.isSuperconductor());
    }

    @Override
    public boolean isPaintable() {
        return true;
    }
}
