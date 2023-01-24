package gregtech.api.unification.material.properties;

import gregtech.api.fluids.MaterialFluidDefinition;
import gregtech.api.fluids.info.FluidType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class FluidProperty implements IMaterialProperty<FluidProperty> {

    private final Map<FluidType, Fluid> fluids = new Object2ObjectOpenHashMap<>();
    private final Collection<MaterialFluidDefinition> definitions = new ObjectOpenHashSet<>();

    /**
     * Create a property with no fluids initially
     */
    public FluidProperty() {/**/}

    /**
     * Create a property with one fluid initially
     *
     * @param definition the definition to add
     */
    public FluidProperty(@Nonnull MaterialFluidDefinition definition) {
        this.definitions.add(definition);
    }

    /**
     * Create a property with many fluids initially
     *
     * @param definitions the definitions to add
     */
    public FluidProperty(@Nonnull MaterialFluidDefinition... definitions) {
        this(Arrays.asList(definitions));
    }

    /**
     * Create a property with many fluids initially
     *
     * @param definitions the definitions to add
     */
    public FluidProperty(@Nonnull Collection<MaterialFluidDefinition> definitions) {
        this.definitions.addAll(definitions);
    }

    /**
     * Add a definition to this property
     *
     * @param definition the definition to add
     */
    public void addDefinition(@Nonnull MaterialFluidDefinition definition) {
        this.definitions.add(definition);
    }

    /**
     * Add definitions to this property
     *
     * @param definitions the definitions to add
     */
    public void addDefinitions(@Nonnull Collection<MaterialFluidDefinition> definitions) {
        this.definitions.addAll(definitions);
    }

    /**
     * Forcibly sets and potentially overrides a fluid mapping. Use with caution.
     *
     * @param type  the type for the fluid
     * @param fluid the fluid to set
     */
    public final void setFluid(@Nonnull FluidType type, @Nonnull Fluid fluid) {
        this.fluids.put(type, fluid);
    }

    /**
     * @param type the type for the fluid
     * @return the fluid associated with the type, if it exists
     */
    @Nullable
    public Fluid getFluid(@Nonnull FluidType type) {
        return this.fluids.get(type);
    }

    /**
     * @param type the type to check
     * @return if the property has a fluid for the type
     */
    public boolean hasFluid(@Nonnull FluidType type) {
        return this.fluids.containsKey(type);
    }

    /**
     * @return all the definitions for this property
     */
    @Nonnull
    public Collection<MaterialFluidDefinition> getDefinitions() {
        return Collections.unmodifiableCollection(this.definitions);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {/**/}
}
