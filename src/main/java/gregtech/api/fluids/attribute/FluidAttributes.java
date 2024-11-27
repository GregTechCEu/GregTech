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

    /**
     * Attribute for basic fluids.
     */
    public static final FluidAttribute BASE = new FluidAttribute(gregtechId("base"),
            list -> list.add(I18n.format("gregtech.fluid.type_base.tooltip")),
            list -> list.add(I18n.format("gregtech.fluid_pipe.base_proof")));
    /**
     * Attribute for strong fluoride anion containing fluids.
     */
    public static final FluidAttribute FLUORIDE = new FluidAttribute(gregtechId("fluoride"),
            list -> list.add(I18n.format("gregtech.fluid.type_fluoride.tooltip")),
            list -> list.add(I18n.format("gregtech.fluid_pipe.fluoride_proof")));
    /**
     * Attribute for oxidizing fluids.
     */
    public static final FluidAttribute OXIDANT = new FluidAttribute(gregtechId("oxidant"),
            list -> list.add(I18n.format("gregtech.fluid.type_oxidant.tooltip")),
            list -> list.add(I18n.format("gregtech.fluid_pipe.oxidant_proof")));
    /**
     * Attribute for reducing fluids.
     */
    public static final FluidAttribute REDUCTANT = new FluidAttribute(gregtechId("reductant"),
            list -> list.add(I18n.format("gregtech.fluid.type_reductant.tooltip")),
            list -> list.add(I18n.format("gregtech.fluid_pipe.reductant_proof")));

    private FluidAttributes() {}
}
