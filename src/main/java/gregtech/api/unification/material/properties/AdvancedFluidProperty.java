package gregtech.api.unification.material.properties;

import gregtech.api.fluids.MaterialFluidDefinition;
import gregtech.api.fluids.info.FluidTypeKey;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class AdvancedFluidProperty implements IMaterialProperty<AdvancedFluidProperty> {

    private final Map<FluidTypeKey, Fluid> fluids = new Object2ObjectOpenHashMap<>();
    private final Collection<MaterialFluidDefinition> definitions = new ObjectOpenHashSet<>();

    /**
     * Create a property with no fluids initially
     */
    public AdvancedFluidProperty() {/**/}

    /**
     * Create a property with one fluid initially
     * @param definition the definition to add
     */
    public AdvancedFluidProperty(@Nonnull MaterialFluidDefinition definition) {
        this.definitions.add(definition);
    }

    /**
     * Create a property with many fluids initially
     *
     * @param definitions the definitions to add
     */
    public AdvancedFluidProperty(@Nonnull MaterialFluidDefinition... definitions) {
        this(Arrays.asList(definitions));
    }

    /**
     * Create a property with many fluids initially
     *
     * @param definitions the definitions to add
     */
    public AdvancedFluidProperty(@Nonnull Collection<MaterialFluidDefinition> definitions) {
        this.definitions.addAll(definitions);
    }

    /**
     * Add a definition to this property
     * @param definition the definition to add
     */
    public void addDefinition(@Nonnull MaterialFluidDefinition definition) {
        this.definitions.add(definition);
    }

    /**
     * Add definitions to this property
     * @param definitions the definitions to add
     */
    public void addDefinitions(@Nonnull Collection<MaterialFluidDefinition> definitions) {
        this.definitions.addAll(definitions);
    }

    /**
     * Forcibly sets and potentially overrides a fluid mapping. Use with caution.
     *
     * @param key   the key for the fluid
     * @param fluid the fluid to set
     */
    public final void setFluid(@Nonnull FluidTypeKey key, @Nonnull Fluid fluid) {
        this.fluids.put(key, fluid);
    }

    /**
     * @param key the key for the fluid
     * @return the fluid associated with the key, if it exists
     */
    @Nullable
    public Fluid getFluid(@Nonnull FluidTypeKey key) {
        return this.fluids.get(key);
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
