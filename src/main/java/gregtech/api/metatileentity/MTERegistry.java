package gregtech.api.metatileentity;

import gregtech.api.util.GTControlledRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

public class MTERegistry extends GTControlledRegistry<ResourceLocation, MetaTileEntity> {

    private Set<Class<? extends MetaTileEntity>> registeredTileEntities = new ObjectOpenHashSet<>();

    /**
     * {@code Map<ResourceLocation(modid, MTEClassName), ResourceLocation(modid, MTERegistryName)>}
     */
    private final Map<ResourceLocation, ResourceLocation> internalTileEntityNames = new Object2ObjectOpenHashMap<>();

    public MTERegistry() {
        super(Short.MAX_VALUE);
    }

    @Override
    public void register(int id, @Nonnull ResourceLocation key, @Nonnull MetaTileEntity value) {
        super.register(id, key, value);

        Class<? extends MetaTileEntity> clazz = value.getClass();
        ResourceLocation location = new ResourceLocation(key.getNamespace(), clazz.getName());
        internalTileEntityNames.put(location, key);

        if (registeredTileEntities.add(clazz)) {
            GameRegistry.registerTileEntity(clazz, location);
        }
    }

    public @NotNull MetaTileEntity createMetaTileEntity(int id) {
        MetaTileEntity metaTileEntity = getObjectById(id);
        if (metaTileEntity == null) throw new IllegalArgumentException("No MTE is registered for id " + id);
        return metaTileEntity.createMetaTileEntity();
    }

    public @NotNull MetaTileEntity createMetaTileEntity(@NotNull ResourceLocation id) {
        MetaTileEntity metaTileEntity = getObject(id);
        if (metaTileEntity == null) throw new IllegalArgumentException("No MTE is registered for id " + id);
        return metaTileEntity.createMetaTileEntity();
    }

    /**
     * Tests if a ResourceLocation is associated with a MetaTileEntity class for the world loading process
     *
     * @param id the ID to test
     * @return if there is an MTE associated with the ID
     */
    @ApiStatus.Internal
    public boolean isMetaTileEntity(@NotNull ResourceLocation id) {
        return internalTileEntityNames.containsKey(id);
    }

    /**
     * Loads a MetaTileEntity for the world loading process.
     *
     * @param id the TileEntity's registry ID
     * @return the MetaTileEntity to load
     */
    @ApiStatus.Internal
    public @NotNull MetaTileEntity loadMetaTileEntity(@NotNull ResourceLocation id) {
        ResourceLocation location = internalTileEntityNames.get(id);
        if (location == null) throw new IllegalArgumentException("No MTE is associated with class ResourceLocation " + id);
        return createMetaTileEntity(location);
    }

    @Override
    public void freeze() {
        super.freeze();
        registeredTileEntities.clear();
    }
}
