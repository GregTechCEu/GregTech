package gregtech.api.unification.material.properties;

//@ZenClass("mods.gregtech.material.DustMaterial")
//@ZenRegister
public class DustProperty implements IMaterialProperty {

    /**
     * Tool level needed to harvest block of this Material.
     *
     * Default: 2 (Iron).
     */
    //@ZenProperty
    private int harvestLevel;

    /**
     * Burn time of this Material when used as fuel in Furnace smelting.
     * Zero or negative value indicates that this Material cannot be used as fuel.
     *
     * Default: 0.
     */
    //@ZenProperty
    private int burnTime;

    public DustProperty(int harvestLevel, int burnTime) {
        this.harvestLevel = harvestLevel;
        this.burnTime = burnTime;
    }

    /**
     * Default property constructor.
     */
    public DustProperty() {
        this(2, 0);
    }

    //@ZenMethod
    public void setHarvestLevel(int harvestLevel) {
        this.harvestLevel = harvestLevel;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = burnTime;
    }

    @Override
    public void verifyProperty(Properties properties) {
        FluidProperty prop = properties.getFluidProperty();
        if (prop != null && prop.getFluidTemperature() == FluidProperty.BASE_TEMP) {
            prop.setFluidTemperature(1200);
        }
    }

    @Override
    public boolean doesMatch(IMaterialProperty otherProp) {
        return otherProp instanceof DustProperty;
    }

    @Override
    public String getName() {
        return "dust_property";
    }

    @Override
    public String toString() {
        return getName();
    }
}
