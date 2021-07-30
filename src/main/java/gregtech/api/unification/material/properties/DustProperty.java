package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.type.DustMaterial;
import gregtech.api.unification.material.type.FluidMaterial;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.type.SolidMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//@ZenExpansion("mods.gregtech.material.Material")
//@ZenRegister
// TODO extends FluidMaterial
public class DustProperty implements IMaterialProperty {

    /**
     * Tool level needed to harvest block of this material
     */
    //@ZenProperty
    public int harvestLevel;

    /**
     * Burn time of this material when used as fuel in furnace smelting
     * Zero or negative value indicates that this material cannot be used as fuel
     */
    //@ZenProperty
    public int burnTime = 0;

    //@ZenMethod
    public void setHarvestLevel(int harvestLevel) {
        this.harvestLevel = harvestLevel;
    }

    @Override
    protected void initializeMaterial() {
        super.initializeMaterial();
        if (shouldGenerateFluid()) {
            setFluidTemperature(1200); //default value for dusts
        }
    }

    public void addOreByProducts(FluidMaterial... byProducts) {
        this.oreByProducts.addAll(Arrays.asList(byProducts));
    }

    public void setDirectSmelting(SolidMaterial directSmelting) {
        this.directSmelting = directSmelting;
    }

    public void setOreMultiplier(int oreMultiplier) {
        this.oreMultiplier = oreMultiplier;
    }

    public void setByProductMultiplier(int byProductMultiplier) {
        this.byProductMultiplier = byProductMultiplier;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = burnTime;
    }
}
