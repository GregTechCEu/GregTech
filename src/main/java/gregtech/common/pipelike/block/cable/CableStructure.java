package gregtech.common.pipelike.block.cable;

import gregtech.api.graphnet.pipenet.physical.IPipeMaterialStructure;

import gregtech.api.unification.ore.OrePrefix;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record CableStructure(String name, int material, int costFactor, OrePrefix ore, @Nullable CableStructure partialBurnStructure, float renderThickness) implements IPipeMaterialStructure {

    public static final CableStructure WIRE_SINGLE =
            new CableStructure("wire_single", 1, 2, OrePrefix.wireGtSingle, null, 0.125f);
    public static final CableStructure WIRE_DOUBLE =
            new CableStructure("wire_double", 2, 2, OrePrefix.wireGtDouble, null, 0.25f);
    public static final CableStructure WIRE_QUADRUPLE =
            new CableStructure("wire_quadruple", 4, 3, OrePrefix.wireGtQuadruple, null, 0.375f);
    public static final CableStructure WIRE_OCTAL =
            new CableStructure("wire_octal", 8, 3, OrePrefix.wireGtOctal, null, 0.5f);
    public static final CableStructure WIRE_HEX =
            new CableStructure("wire_hex", 16, 3, OrePrefix.wireGtHex, null, 0.75f);

    public static final CableStructure CABLE_SINGLE =
            new CableStructure("cable_single", 1, 1, OrePrefix.cableGtSingle, WIRE_SINGLE, 0.25f);
    public static final CableStructure CABLE_DOUBLE =
            new CableStructure("cable_double", 2, 1, OrePrefix.cableGtDouble, WIRE_DOUBLE, 0.375f);
    public static final CableStructure CABLE_QUADRUPLE =
            new CableStructure("cable_quadruple", 4, 1, OrePrefix.cableGtQuadruple, WIRE_QUADRUPLE, 0.5f);
    public static final CableStructure CABLE_OCTAL =
            new CableStructure("cable_octal", 8, 1, OrePrefix.cableGtOctal, WIRE_OCTAL, 0.75f);
    public static final CableStructure CABLE_HEX =
            new CableStructure("cable_hex", 16, 1, OrePrefix.cableGtHex, WIRE_HEX, 1f);

    @Override
    public @NotNull String getName() {
        return this.name();
    }

    @Override
    public OrePrefix getOrePrefix() {
        return this.ore();
    }

    @Override
    public float getRenderThickness() {
        return renderThickness();
    }

    public static void registerDefaultStructures(Consumer<CableStructure> register) {
        register.accept(WIRE_SINGLE);
        register.accept(WIRE_DOUBLE);
        register.accept(WIRE_QUADRUPLE);
        register.accept(WIRE_OCTAL);
        register.accept(WIRE_HEX);
        register.accept(CABLE_SINGLE);
        register.accept(CABLE_DOUBLE);
        register.accept(CABLE_QUADRUPLE);
        register.accept(CABLE_OCTAL);
        register.accept(CABLE_HEX);
    }
}
