package gtrmcore.common.metatileentities;

import gtrmcore.common.metatileentities.single.PrimitiveAssembler;
import gtrmcore.common.metatileentities.single.PrimitiveMixer;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static gtrmcore.api.util.GTRMUtility.gtrmId;

public class GTRMSingleMetaTileEntities {

    public static PrimitiveMixer PRIMITIVE_MIXER_BRONZE;
    public static PrimitiveMixer PRIMITIVE_MIXER_STEEL;
    public static PrimitiveAssembler PRIMITIVE_ASSEMBLER_BRONZE;
    public static PrimitiveAssembler PRIMITIVE_ASSEMBLER_STEEL;
    // public static PrimitiveCircuitAssembler PRIMITIVE_CIRCUIT_ASSEMBLER_BRONZE;
    // public static PrimitiveCircuitAssembler PRIMITIVE_CIRCUIT_ASSEMBLER_STEEL;

    public static void init() {
        // Primitive machine 11000~11005
        PRIMITIVE_MIXER_BRONZE = registerMetaTileEntity(11000,
                new PrimitiveMixer(gtrmId("primitive_mixer_bronze"), false));
        PRIMITIVE_MIXER_STEEL = registerMetaTileEntity(11001,
                new PrimitiveMixer(gtrmId("primitive_mixer_steel"), true));
        PRIMITIVE_ASSEMBLER_BRONZE = registerMetaTileEntity(11002,
                new PrimitiveAssembler(gtrmId("primitive_assembler_bronze"), false));
        PRIMITIVE_ASSEMBLER_STEEL = registerMetaTileEntity(11003,
                new PrimitiveAssembler(gtrmId("primitive_assembler_steel"), true));
        // PRIMITIVE_CIRCUIT_ASSEMBLER_BRONZE = registerMetaTileEntity(11004,
        // new PrimitiveCircuitAssembler(gtrmId("primitive_circuit_assembler_bronze"), false));
        // PRIMITIVE_CIRCUIT_ASSEMBLER_STEEL = registerMetaTileEntity(11005,
        // new PrimitiveCircuitAssembler(gtrmId("primitive_circuit_assembler_steel"), true));
    }
}
