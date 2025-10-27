package gregtech.api.block.coil;

import gregtech.api.util.GTControlledRegistry;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public class CoilRegistry extends GTControlledRegistry<ResourceLocation, CustomCoilBlock> implements BuilderFactory {

    private final String modid;
    private final int networkId;

    public CoilRegistry(String modId, int networkId) {
        super(Short.MAX_VALUE);
        this.modid = modId;
        this.networkId = networkId;
    }

    @Override
    public CoilBlockBuilder makeBuilder(int id, String name) {
        ResourceLocation loc = new ResourceLocation(this.modid, name);
        return new CoilBlockBuilder(this.modid, b -> register(id, loc, b));
    }

    @Override
    public void register(int id, @NotNull ResourceLocation key, @NotNull CustomCoilBlock value) {
        if (!canRegister(key.getNamespace())) {
            throw new IllegalArgumentException("Cannot register CoilBlock to another mod's registry");
        }
        value.setRegistryName(key);
        super.register(id, key, value);
    }

    /**
     * @param modid the modid to test
     * @return if the mod is allowed to be registered to this registry
     */
    private boolean canRegister(@NotNull String modid) {
        return this.modid.equals(modid);
    }
}
