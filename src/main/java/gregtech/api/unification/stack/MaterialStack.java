package gregtech.api.unification.stack;

import crafttweaker.annotations.ZenRegister;
import gregtech.api.unification.material.Material;
import gregtech.api.util.SmallDigits;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

@ZenClass("mods.gregtech.material.MaterialStack")
@ZenRegister
public class MaterialStack {

    @ZenProperty
    public final Material material;
    @ZenProperty
    public final long amount;

    public MaterialStack(Material material, long amount) {
        this.material = material;
        this.amount = amount;
    }

    @ZenMethod
    public MaterialStack copy(long amount) {
        return new MaterialStack(material, amount);
    }

    @ZenMethod
    public MaterialStack copy() {
        return new MaterialStack(material, amount);
    }

    @Override
    @ZenMethod
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MaterialStack that = (MaterialStack) o;

        if (amount != that.amount) return false;
        return material.equals(that.material);
    }

    @Override
    public int hashCode() {
        return material.hashCode();
    }

    @Override
    @ZenMethod
    public String toString() {
        String string = "";
        if (material.getChemicalFormula().isEmpty()) {
            string += "?";
        } else if (material.getMaterialComponents().size() > 1) {
            string += '(' + material.getChemicalFormula() + ')';
        } else {
            string += material.getChemicalFormula();
        }
        if (amount > 1) {
            string += SmallDigits.toSmallDownNumbers(Long.toString(amount));
        }
        return string;
    }

}
