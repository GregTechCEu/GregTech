package gregtech.common.pipelike.block.cable;

import gregtech.api.graphnet.pipenet.physical.IInsulatable;
import gregtech.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.client.renderer.pipe.AbstractPipeModel;
import gregtech.client.renderer.pipe.CableModel;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Desugar
public record CableStructure(String name, int material, int costFactor, OrePrefix ore,
                             @Nullable CableStructure partialBurnStructure, @Nullable Integer partialBurnThreshold,
                             float renderThickness, AbstractPipeModel<?> model)
        implements IPipeMaterialStructure, IInsulatable {

    public static final int INSULATION_BURN_TEMP = 1000;

    public static final CableStructure WIRE_SINGLE = new CableStructure("wire_single", 1, 2, OrePrefix.wireGtSingle,
            null, null, 0.125f, CableModel.INSTANCE);
    public static final CableStructure WIRE_DOUBLE = new CableStructure("wire_double", 2, 2, OrePrefix.wireGtDouble,
            null, null, 0.25f, CableModel.INSTANCE);
    public static final CableStructure WIRE_QUADRUPLE = new CableStructure("wire_quadruple", 4, 3,
            OrePrefix.wireGtQuadruple, null, null, 0.375f, CableModel.INSTANCE);
    public static final CableStructure WIRE_OCTAL = new CableStructure("wire_octal", 8, 3, OrePrefix.wireGtOctal, null,
            null, 0.5f, CableModel.INSTANCE);
    public static final CableStructure WIRE_HEX = new CableStructure("wire_hex", 16, 3, OrePrefix.wireGtHex, null, null,
            0.75f, CableModel.INSTANCE);

    public static final CableStructure CABLE_SINGLE = new CableStructure("cable_single", 1, 1, OrePrefix.cableGtSingle,
            WIRE_SINGLE, INSULATION_BURN_TEMP, 0.25f, CableModel.INSULATED_INSTANCES[0]);
    public static final CableStructure CABLE_DOUBLE = new CableStructure("cable_double", 2, 1, OrePrefix.cableGtDouble,
            WIRE_DOUBLE, INSULATION_BURN_TEMP, 0.375f, CableModel.INSULATED_INSTANCES[1]);
    public static final CableStructure CABLE_QUADRUPLE = new CableStructure("cable_quadruple", 4, 1,
            OrePrefix.cableGtQuadruple, WIRE_QUADRUPLE, INSULATION_BURN_TEMP, 0.5f, CableModel.INSULATED_INSTANCES[2]);
    public static final CableStructure CABLE_OCTAL = new CableStructure("cable_octal", 8, 1, OrePrefix.cableGtOctal,
            WIRE_OCTAL, INSULATION_BURN_TEMP, 0.75f, CableModel.INSULATED_INSTANCES[3]);
    public static final CableStructure CABLE_HEX = new CableStructure("cable_hex", 16, 1, OrePrefix.cableGtHex,
            WIRE_HEX, INSULATION_BURN_TEMP, 1f, CableModel.INSULATED_INSTANCES[4]);

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public OrePrefix getOrePrefix() {
        return ore;
    }

    @Override
    public float getRenderThickness() {
        return renderThickness;
    }

    @Override
    public AbstractPipeModel<?> getModel() {
        return model;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public boolean isInsulated() {
        return partialBurnStructure != null;
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
