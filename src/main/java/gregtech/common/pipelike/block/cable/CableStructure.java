package gregtech.common.pipelike.block.cable;

import gregtech.api.graphnet.pipenet.physical.IInsulatable;
import gregtech.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import gregtech.api.graphnet.pipenet.physical.PipeStructureRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.client.renderer.pipe.PipeModelRedirector;
import gregtech.client.renderer.pipe.PipeModelRegistry;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@Desugar
public record CableStructure(String name, int material, int costFactor, OrePrefix ore,
                             @Nullable CableStructure partialBurnStructure, @Nullable Integer partialBurnThreshold,
                             float renderThickness, PipeModelRedirector model)
        implements IPipeMaterialStructure, IInsulatable {

    public static final int INSULATION_BURN_TEMP = 1000;

    public static final CableStructure WIRE_SINGLE = new CableStructure("wire_single", 1, 2, OrePrefix.wireGtSingle,
            null, null, 0.125f, PipeModelRegistry.getCableModel(0));
    public static final CableStructure WIRE_DOUBLE = new CableStructure("wire_double", 2, 2, OrePrefix.wireGtDouble,
            null, null, 0.25f, PipeModelRegistry.getCableModel(0));
    public static final CableStructure WIRE_QUADRUPLE = new CableStructure("wire_quadruple", 4, 3,
            OrePrefix.wireGtQuadruple, null, null, 0.375f, PipeModelRegistry.getCableModel(0));
    public static final CableStructure WIRE_OCTAL = new CableStructure("wire_octal", 8, 3, OrePrefix.wireGtOctal, null,
            null, 0.5f, PipeModelRegistry.getCableModel(0));
    public static final CableStructure WIRE_HEX = new CableStructure("wire_hex", 16, 3, OrePrefix.wireGtHex, null, null,
            0.75f, PipeModelRegistry.getCableModel(0));

    public static final CableStructure CABLE_SINGLE = new CableStructure("cable_single", 1, 1, OrePrefix.cableGtSingle,
            WIRE_SINGLE, INSULATION_BURN_TEMP, 0.25f, PipeModelRegistry.getCableModel(1));
    public static final CableStructure CABLE_DOUBLE = new CableStructure("cable_double", 2, 1, OrePrefix.cableGtDouble,
            WIRE_DOUBLE, INSULATION_BURN_TEMP, 0.375f, PipeModelRegistry.getCableModel(2));
    public static final CableStructure CABLE_QUADRUPLE = new CableStructure("cable_quadruple", 4, 1,
            OrePrefix.cableGtQuadruple, WIRE_QUADRUPLE, INSULATION_BURN_TEMP, 0.5f, PipeModelRegistry.getCableModel(3));
    public static final CableStructure CABLE_OCTAL = new CableStructure("cable_octal", 8, 1, OrePrefix.cableGtOctal,
            WIRE_OCTAL, INSULATION_BURN_TEMP, 0.75f, PipeModelRegistry.getCableModel(4));
    public static final CableStructure CABLE_HEX = new CableStructure("cable_hex", 16, 1, OrePrefix.cableGtHex,
            WIRE_HEX, INSULATION_BURN_TEMP, 1f, PipeModelRegistry.getCableModel(5));

    public CableStructure(String name, int material, int costFactor, OrePrefix ore,
                          @Nullable CableStructure partialBurnStructure, @Nullable Integer partialBurnThreshold,
                          float renderThickness, PipeModelRedirector model) {
        this.name = name;
        this.material = material;
        this.costFactor = costFactor;
        this.ore = ore;
        this.partialBurnStructure = partialBurnStructure;
        this.partialBurnThreshold = partialBurnThreshold;
        this.renderThickness = renderThickness;
        this.model = model;
        PipeStructureRegistry.register(this);
    }

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
    public boolean isPaintable() {
        return true;
    }

    @Override
    public boolean isInsulated() {
        return partialBurnStructure != null;
    }

    @Override
    public PipeModelRedirector getModel() {
        return model;
    }
}
