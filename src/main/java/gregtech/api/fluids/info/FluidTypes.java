package gregtech.api.fluids.info;

/**
 * Stores the default fluid types, and creates all of them.
 */
public final class FluidTypes {

    public static FluidType LIQUID = new FluidType("liquid", FluidType.createDefaultFunction());
    public static FluidType GAS = new FluidType("gas", FluidType.createDefaultFunction());
    public static FluidType PLASMA = new FluidType("plasma", FluidType.createDefaultFunction("plasma"));

    private FluidTypes() {/**/}
}
