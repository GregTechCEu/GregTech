package gregtech.api.unification.material;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.properties.*;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public abstract class MaterialBuilder implements IMaterialBuilder {

    protected abstract <T extends IMaterialProperty<T>> void setProperty(PropertyKey<T> key, T property);

    protected abstract <T extends IMaterialProperty<T>> T getProperty(PropertyKey<T> key);

    protected <T extends IMaterialProperty<T>> T getOrDefaultProperty(PropertyKey<T> key) {
        T property = getProperty(key);
        return property != null ? property : key.constructDefault();
    }

    @Override
    public IMaterialBuilder fluid() {
        FluidProperty property = getOrDefaultProperty(PropertyKey.FLUID);
        setProperty(PropertyKey.FLUID, property);
        return this;
    }

    @Override
    public IMaterialBuilder fluid(FluidType type) {
        FluidProperty property = getOrDefaultProperty(PropertyKey.FLUID);
        property.setFluidType(type);
        setProperty(PropertyKey.FLUID, property);
        return this;
    }

    @Override
    public IMaterialBuilder fluid(FluidType type, boolean hasBlock) {
        FluidProperty property = getOrDefaultProperty(PropertyKey.FLUID);
        property.setFluidType(type);
        property.setHasBlock(hasBlock);
        setProperty(PropertyKey.FLUID, property);
        return this;
    }

    @Override
    public IMaterialBuilder plasma() {
        PlasmaProperty property = getOrDefaultProperty(PropertyKey.PLASMA);
        setProperty(PropertyKey.PLASMA, property);
        return this;
    }

    @Override
    public IMaterialBuilder dust() {
        DustProperty property = getOrDefaultProperty(PropertyKey.DUST);
        setProperty(PropertyKey.DUST, property);
        return this;
    }

    @Override
    public IMaterialBuilder dust(int harvestLevel) {
        DustProperty property = getOrDefaultProperty(PropertyKey.DUST);
        property.setHarvestLevel(harvestLevel);
        setProperty(PropertyKey.DUST, property);
        return this;
    }

    @Override
    public IMaterialBuilder dust(int harvestLevel, int burnTime) {
        DustProperty property = getOrDefaultProperty(PropertyKey.DUST);
        property.setHarvestLevel(harvestLevel);
        property.setBurnTime(burnTime);
        setProperty(PropertyKey.DUST, property);
        return this;
    }

    @Override
    public IMaterialBuilder ingot() {
        IngotProperty property = getOrDefaultProperty(PropertyKey.INGOT);
        setProperty(PropertyKey.INGOT, property);
        return this;
    }

    @Override
    public IMaterialBuilder ingot(int harvestLevel) {
        IngotProperty property = getOrDefaultProperty(PropertyKey.INGOT);
        setProperty(PropertyKey.INGOT, property);

        // Dust holds harvestLevel
        DustProperty dustProperty = getOrDefaultProperty(PropertyKey.DUST);
        dustProperty.setHarvestLevel(harvestLevel);
        setProperty(PropertyKey.DUST, dustProperty);
        return this;
    }

    @Override
    public IMaterialBuilder ingot(int harvestLevel, int burnTime) {
        IngotProperty property = getOrDefaultProperty(PropertyKey.INGOT);
        setProperty(PropertyKey.INGOT, property);

        // Dust holds harvestLevel and burnTime
        DustProperty dustProperty = getOrDefaultProperty(PropertyKey.DUST);
        dustProperty.setHarvestLevel(harvestLevel);
        dustProperty.setBurnTime(burnTime);
        setProperty(PropertyKey.DUST, dustProperty);
        return this;
    }

    @Override
    public IMaterialBuilder gem() {
        GemProperty property = getOrDefaultProperty(PropertyKey.GEM);
        setProperty(PropertyKey.GEM, property);
        return this;
    }

    @Override
    public IMaterialBuilder gem(int harvestLevel) {
        GemProperty property = getOrDefaultProperty(PropertyKey.GEM);
        setProperty(PropertyKey.GEM, property);

        // Dust holds harvestLevel
        DustProperty dustProperty = getOrDefaultProperty(PropertyKey.DUST);
        dustProperty.setHarvestLevel(harvestLevel);
        setProperty(PropertyKey.DUST, dustProperty);
        return this;
    }

    @Override
    public IMaterialBuilder gem(int harvestLevel, int burnTime) {
        GemProperty property = getOrDefaultProperty(PropertyKey.GEM);
        setProperty(PropertyKey.GEM, property);

        // Dust holds harvestLevel and burnTime
        DustProperty dustProperty = getOrDefaultProperty(PropertyKey.DUST);
        dustProperty.setHarvestLevel(harvestLevel);
        dustProperty.setBurnTime(burnTime);
        setProperty(PropertyKey.DUST, dustProperty);
        return this;
    }

    @Override
    public IMaterialBuilder burnTime(int burnTime) {
        DustProperty property = getOrDefaultProperty(PropertyKey.DUST);
        property.setBurnTime(burnTime);
        setProperty(PropertyKey.DUST, property);
        return this;
    }

    @Override
    public IMaterialBuilder color(int color) {
        return color(color, true);
    }

    @Override
    public IMaterialBuilder components(Object... components) {
        Preconditions.checkArgument(
                components.length % 2 == 0,
                "Material Components list malformed!"
        );
        ImmutableList.Builder<MaterialStack> builder = ImmutableList.builder();

        for (int i = 0; i < components.length; i += 2) {
            if (components[i] == null) {
                throw new IllegalArgumentException();
            }
            builder.add(new MaterialStack(
                    (Material) components[i],
                    (Integer) components[i + 1]
            ));
        }
        return components(builder.build());
    }

    @Override
    public IMaterialBuilder flags(Collection<MaterialFlag> f1, MaterialFlag... f2) {
        Collection<MaterialFlag> copy = new ArrayList<>(f1);
        Collections.addAll(copy, f2);
        return flags(copy.toArray(new MaterialFlag[0]));
    }

    @Override
    public IMaterialBuilder toolStats(float speed, float damage, int durability, int enchantability) {
        ToolProperty property = getOrDefaultProperty(PropertyKey.TOOL);
        property.setToolSpeed(speed);
        property.setToolAttackDamage(damage);
        property.setToolDurability(durability);
        property.setToolEnchantability(enchantability);
        setProperty(PropertyKey.TOOL, property);
        return this;
    }

    @Override
    public IMaterialBuilder toolStats(float speed, float damage, int durability, int enchantability, boolean ignoreCraftingTools) {
        ToolProperty property = getOrDefaultProperty(PropertyKey.TOOL);
        property.setToolSpeed(speed);
        property.setToolAttackDamage(damage);
        property.setToolDurability(durability);
        property.setToolEnchantability(enchantability);
        property.setShouldIgnoreCraftingTools(ignoreCraftingTools);
        setProperty(PropertyKey.TOOL, property);
        return this;
    }

    @Override
    public IMaterialBuilder blastTemp(int temp) {
        BlastProperty property = getOrDefaultProperty(PropertyKey.BLAST);
        property.setBlastTemperature(temp);
        setProperty(PropertyKey.BLAST, property);
        return this;
    }

    @Override
    public IMaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier) {
        BlastProperty property = getOrDefaultProperty(PropertyKey.BLAST);
        property.setBlastTemperature(temp);
        property.setGasTier(gasTier);
        setProperty(PropertyKey.BLAST, property);
        return this;
    }

    @Override
    public IMaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride) {
        BlastProperty property = getOrDefaultProperty(PropertyKey.BLAST);
        property.setBlastTemperature(temp);
        property.setGasTier(gasTier);
        property.setEUtOverride(eutOverride);
        setProperty(PropertyKey.BLAST, property);
        return this;
    }

    @Override
    public IMaterialBuilder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride, int durationOverride) {
        BlastProperty property = getOrDefaultProperty(PropertyKey.BLAST);
        property.setBlastTemperature(temp);
        property.setGasTier(gasTier);
        property.setEUtOverride(eutOverride);
        property.setDurationOverride(durationOverride);
        setProperty(PropertyKey.BLAST, property);
        return this;
    }

    @Override
    public IMaterialBuilder ore() {
        OreProperty property = getOrDefaultProperty(PropertyKey.ORE);
        setProperty(PropertyKey.ORE, property);
        return this;
    }

    @Override
    public IMaterialBuilder ore(boolean emissive) {
        OreProperty property = getOrDefaultProperty(PropertyKey.ORE);
        property.setEmissive(emissive);
        setProperty(PropertyKey.ORE, property);
        return this;
    }

    @Override
    public IMaterialBuilder ore(int oreMultiplier, int byproductMultiplier) {
        OreProperty property = getOrDefaultProperty(PropertyKey.ORE);
        property.setOreMultiplier(oreMultiplier);
        property.setByProductMultiplier(byproductMultiplier);
        setProperty(PropertyKey.ORE, property);
        return this;
    }

    @Override
    public IMaterialBuilder ore(int oreMultiplier, int byproductMultiplier, boolean emissive) {
        OreProperty property = getOrDefaultProperty(PropertyKey.ORE);
        property.setOreMultiplier(oreMultiplier);
        property.setByProductMultiplier(byproductMultiplier);
        property.setEmissive(emissive);
        setProperty(PropertyKey.ORE, property);
        return this;
    }

    @Override
    public IMaterialBuilder fluidTemp(int temp) {
        FluidProperty property = getOrDefaultProperty(PropertyKey.FLUID);
        property.setFluidTemperature(temp);
        setProperty(PropertyKey.FLUID, property);
        return this;
    }

    @Override
    public IMaterialBuilder washedIn(Material m) {
        OreProperty property = getOrDefaultProperty(PropertyKey.ORE);
        property.setWashedIn(m);
        setProperty(PropertyKey.ORE, property);
        return this;
    }

    @Override
    public IMaterialBuilder washedIn(Material m, int washedAmount) {
        OreProperty property = getOrDefaultProperty(PropertyKey.ORE);
        property.setWashedIn(m, washedAmount);
        setProperty(PropertyKey.ORE, property);
        return this;
    }

    @Override
    public IMaterialBuilder separatedInto(Material... m) {
        OreProperty property = getOrDefaultProperty(PropertyKey.ORE);
        property.setSeparatedInto(m);
        setProperty(PropertyKey.ORE, property);
        return this;
    }

    @Override
    public IMaterialBuilder oreSmeltInto(Material m) {
        OreProperty property = getOrDefaultProperty(PropertyKey.ORE);
        property.setDirectSmeltResult(m);
        setProperty(PropertyKey.ORE, property);
        return this;
    }

    @Override
    public IMaterialBuilder polarizesInto(Material m) {
        IngotProperty property = getOrDefaultProperty(PropertyKey.INGOT);
        property.setMagneticMaterial(m);
        setProperty(PropertyKey.INGOT, property);
        return this;
    }

    @Override
    public IMaterialBuilder arcSmeltInto(Material m) {
        IngotProperty property = getOrDefaultProperty(PropertyKey.INGOT);
        property.setArcSmeltingInto(m);
        setProperty(PropertyKey.INGOT, property);
        return this;
    }

    @Override
    public IMaterialBuilder macerateInto(Material m) {
        IngotProperty property = getOrDefaultProperty(PropertyKey.INGOT);
        property.setMacerateInto(m);
        setProperty(PropertyKey.INGOT, property);
        return this;
    }

    @Override
    public IMaterialBuilder ingotSmeltInto(Material m) {
        IngotProperty property = getOrDefaultProperty(PropertyKey.INGOT);
        property.setSmeltingInto(m);
        setProperty(PropertyKey.INGOT, property);
        return this;
    }

    @Override
    public IMaterialBuilder addOreByproducts(Material... byproducts) {
        OreProperty property = getOrDefaultProperty(PropertyKey.ORE);
        property.setOreByProducts(byproducts);
        setProperty(PropertyKey.ORE, property);
        return this;
    }

    @Override
    public IMaterialBuilder cableProperties(long voltage, int amperage, int loss) {
        WireProperties property = getOrDefaultProperty(PropertyKey.WIRE);
        property.setVoltage((int) voltage);
        property.setAmperage(amperage);
        property.setLossPerBlock(loss);
        setProperty(PropertyKey.WIRE, property);
        return this;
    }

    @Override
    public IMaterialBuilder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon) {
        WireProperties property = getOrDefaultProperty(PropertyKey.WIRE);
        property.setVoltage((int) voltage);
        property.setAmperage(amperage);
        property.setLossPerBlock(loss);
        property.setSuperconductor(isSuperCon);
        setProperty(PropertyKey.WIRE, property);
        return this;
    }

    @Override
    public IMaterialBuilder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon, int criticalTemperature) {
        WireProperties property = getOrDefaultProperty(PropertyKey.WIRE);
        property.setVoltage((int) voltage);
        property.setAmperage(amperage);
        property.setLossPerBlock(loss);
        property.setSuperconductor(isSuperCon);
        property.setSuperconductorCriticalTemperature(criticalTemperature);
        setProperty(PropertyKey.WIRE, property);
        return this;
    }

    @Override
    public IMaterialBuilder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof) {
        FluidPipeProperties property = getOrDefaultProperty(PropertyKey.FLUID_PIPE);
        property.setMaxFluidTemperature(maxTemp);
        property.setThroughput(throughput);
        property.setGasProof(gasProof);
        setProperty(PropertyKey.FLUID_PIPE, property);
        return this;
    }

    @Override
    public IMaterialBuilder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
        FluidPipeProperties property = getOrDefaultProperty(PropertyKey.FLUID_PIPE);
        property.setMaxFluidTemperature(maxTemp);
        property.setThroughput(throughput);
        property.setGasProof(gasProof);
        property.setAcidProof(acidProof);
        property.setCryoProof(cryoProof);
        property.setPlasmaProof(plasmaProof);
        setProperty(PropertyKey.FLUID_PIPE, property);
        return this;
    }

    @Override
    public IMaterialBuilder itemPipeProperties(int priority, float stacksPerSec) {
        ItemPipeProperties property = getOrDefaultProperty(PropertyKey.ITEM_PIPE);
        property.setPriority(priority);
        property.setTransferRate(stacksPerSec);
        setProperty(PropertyKey.ITEM_PIPE, property);
        return this;
    }

    @Override
    public IMaterialBuilder addDefaultEnchant(Enchantment enchant, int level) {
        ToolProperty property = getOrDefaultProperty(PropertyKey.TOOL);
        property.addEnchantmentForTools(enchant, level);
        setProperty(PropertyKey.TOOL, property);
        return this;
    }
}
