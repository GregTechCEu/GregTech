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
     * Default property values.
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
/*
    @Override
    protected void initializeMaterial() {
        super.initializeMaterial();
        if (shouldGenerateFluid()) {
            setFluidTemperature(1200); //default value for dusts
        }
    }
*/
    @Override
    public void verifyProperty(Properties properties) {
    }
}
