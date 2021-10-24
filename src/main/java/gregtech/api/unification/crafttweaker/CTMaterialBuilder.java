package gregtech.api.unification.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.enchantments.IEnchantment;
import gregtech.api.GTValues;
import gregtech.api.unification.Element;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.enchantment.Enchantment;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenConstructor;
import stanhebben.zenscript.annotations.ZenMethod;

import static gregtech.api.unification.crafttweaker.CTMaterialHelpers.*;

@ZenClass("mods.gregtech.material.MaterialBuilder")
@ZenRegister
@SuppressWarnings("unused")
public class CTMaterialBuilder {

    private static int baseID = 32000;

    private final Material.Builder backingBuilder;

    @ZenConstructor
    public CTMaterialBuilder(int id, String name) {
        this.backingBuilder = new Material.Builder(id, name);
    }

    @ZenConstructor
    public CTMaterialBuilder(String name) {
        this(baseID++, name);
    }

    @ZenMethod
    public CTMaterialBuilder fluid(@Optional String type, @Optional boolean hasBlock) {
        backingBuilder.fluid(validateFluidType(type), hasBlock);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder plasma() {
        backingBuilder.plasma();
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder dust(@Optional int harvestLevel, @Optional int burnTime) {
        if (harvestLevel == 0) harvestLevel = 2;
        backingBuilder.dust(harvestLevel, burnTime);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder ingot(@Optional int harvestLevel, @Optional int burnTime) {
        if (harvestLevel == 0) harvestLevel = 2;
        backingBuilder.ingot(harvestLevel, burnTime);
        return this;
    }


    @ZenMethod
    public CTMaterialBuilder gem(@Optional int harvestLevel, @Optional int burnTime) {
        if (harvestLevel == 0) harvestLevel = 2;
        backingBuilder.gem(harvestLevel, burnTime);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder color(int color) {
        backingBuilder.color(color);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder colorAverage() {
        backingBuilder.colorAverage();
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder iconSet(String iconSet) {
        backingBuilder.iconSet(MaterialIconSet.getByName(iconSet));
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder components(MaterialStack[] components) {
        backingBuilder.components(validateComponentList(components));
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder flags(String... names) {
        for (String name : names) {
            MaterialFlag flag = MaterialFlag.getByName(name);
            if (flag != null) {
                backingBuilder.flags(flag);
            }
        }
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder element(String elementName) {
        Element element = Elements.get(elementName);
        if (element != null) {
            backingBuilder.element(element);
        }
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder toolStats(float speed, float damage, int durability, @Optional int enchantability) {
        if (enchantability == 0) {
            enchantability = 21; // Lowest enchantability by default
        }
        backingBuilder.toolStats(speed, damage, durability, enchantability);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder blastTemp(int temp) {
        backingBuilder.blastTemp(temp);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder ore(@Optional int oreMultiplier, @Optional int byproductMultiplier) {
        if (oreMultiplier == 0) oreMultiplier = 1;
        if (byproductMultiplier == 0) byproductMultiplier = 1;
        backingBuilder.ore(oreMultiplier, byproductMultiplier);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder washedIn(String name, @Optional int washedAmount) {
        return washedIn(validateMaterialName(name), washedAmount);
    }

    @ZenMethod
    public CTMaterialBuilder washedIn(Material m, @Optional int washedAmount) {
        if (washedAmount == 0) washedAmount = 100;
        if (m != null) backingBuilder.washedIn(m, washedAmount);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder separatedInto(String... names) {
        return separatedInto(validateMaterialNames("separatedInto", names));
    }

    @ZenMethod
    public CTMaterialBuilder separatedInto(Material... materials) {
        if (materials != null) backingBuilder.separatedInto(materials);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder addOreByproducts(String... names) {
        return addOreByproducts(validateMaterialNames("addOreByproducts", names));
    }

    @ZenMethod
    public CTMaterialBuilder addOreByproducts(Material... materials) {
        if (materials != null) backingBuilder.addOreByproducts(materials);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder oreSmeltInto(String name) {
        return oreSmeltInto(validateMaterialName(name));
    }

    @ZenMethod
    public CTMaterialBuilder oreSmeltInto(Material m) {
        if (m != null) backingBuilder.oreSmeltInto(m);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder polarizesInto(String name) {
        return polarizesInto(validateMaterialName(name));
    }

    @ZenMethod
    public CTMaterialBuilder polarizesInto(Material m) {
        if (m != null) backingBuilder.polarizesInto(m);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder arcSmeltInto(String name) {
        return arcSmeltInto(validateMaterialName(name));
    }

    @ZenMethod
    public CTMaterialBuilder arcSmeltInto(Material m) {
        if (m != null) backingBuilder.arcSmeltInto(m);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder macerateInto(String name) {
        return macerateInto(validateMaterialName(name));
    }

    @ZenMethod
    public CTMaterialBuilder macerateInto(Material m) {
        if (m != null) backingBuilder.macerateInto(m);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder ingotSmeltInto(String name) {
        return ingotSmeltInto(validateMaterialName(name));
    }

    @ZenMethod
    public CTMaterialBuilder ingotSmeltInto(Material m) {
        if (m != null) backingBuilder.ingotSmeltInto(m);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder fluidTemp(int temp) {
        backingBuilder.fluidTemp(temp);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder cableProperties(long voltage, int amperage, int loss, @Optional boolean isSuperCon) {
        backingBuilder.cableProperties(voltage, amperage, loss, isSuperCon);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof) {
        backingBuilder.fluidPipeProperties(maxTemp, throughput, gasProof);
        return this;
    }

    @ZenMethod
    public CTMaterialBuilder itemPipeProperties(int priority, float stacksPerSec) {
        backingBuilder.itemPipeProperties(priority, stacksPerSec);
        return this;
    }

    @ZenMethod
    @net.minecraftforge.fml.common.Optional.Method(modid = GTValues.MODID_CT)
    public CTMaterialBuilder addDefaultEnchant(IEnchantment enchantment) {
        Enchantment enchantmentType = (Enchantment) enchantment.getDefinition().getInternal();
        backingBuilder.addDefaultEnchant(enchantmentType, enchantment.getLevel());
        return this;
    }

    @ZenMethod
    public Material build() {
        return backingBuilder.build();
    }
}
