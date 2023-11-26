package gregtech.api.fluids.attribute;

import net.minecraft.client.resources.I18n;

import static gregtech.api.util.GTUtility.gregtechId;

public final class FluidAttributes {

    /**
     * Attribute for acidic fluids.
     */
    public static final FluidAttribute ACID = new FluidAttribute(gregtechId("acid"),
            list -> list.add(I18n.format("gregtech.fluid.type_acid.tooltip")),
            list -> list.add(I18n.format("gregtech.fluid_pipe.acid_proof")));

    private FluidAttributes() {}
}
