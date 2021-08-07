package gregtech.api.unification.material.properties;

//@ZenClass("mods.gregtech.material.DustMaterial")
//@ZenRegister
public class DustProperty implements IMaterialProperty<DustProperty> {

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

    public int getHarvestLevel() {
        return this.harvestLevel;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = burnTime;
    }

    public int getBurnTime() {
        return burnTime;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        FluidProperty prop = properties.getProperty(PropertyKey.FLUID);
        if (prop != null && prop.getFluidTemperature() == FluidProperty.BASE_TEMP) {
            prop.setFluidTemperature(1200);
        }
    }
}
