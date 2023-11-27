package gregtech.api.unification.stack;

import gregtech.api.unification.material.Material;
import gregtech.api.util.SmallDigits;

import crafttweaker.annotations.ZenRegister;
import org.jetbrains.annotations.NotNull;
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

    @ZenMethod("toString")
    @NotNull
    public String toFormatted() {
        final String chemicalFormula = material.getChemicalFormula();

        StringBuilder builder = new StringBuilder(chemicalFormula.length());
        if (chemicalFormula.isEmpty()) {
            builder.append('?');
        } else if (material.getMaterialComponents().size() > 1) {
            builder.append('(');
            builder.append(chemicalFormula);
            builder.append(')');
        } else {
            builder.append(chemicalFormula);
        }
        if (amount > 1) {
            builder.append(SmallDigits.toSmallDownNumbers(String.valueOf(amount)));
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return "MaterialStack{material=" + material + ", amount=" + amount + '}';
    }
}
