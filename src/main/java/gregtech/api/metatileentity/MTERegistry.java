package gregtech.api.metatileentity;

import gregtech.api.util.GTControlledRegistry;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Set;

public class MTERegistry extends GTControlledRegistry<ResourceLocation, MetaTileEntity> {

    private Set<Class<? extends MetaTileEntity>> registeredTileEntities = new ObjectOpenHashSet<>();

    public MTERegistry() {
        super(Short.MAX_VALUE);
    }

    @Override
    public void register(int id, @Nonnull ResourceLocation key, @Nonnull MetaTileEntity value) {
        super.register(id, key, value);
        if (registeredTileEntities.add(value.getClass())) {
            GameRegistry.registerTileEntity(value.getClass(), key);
        }
    }

    public @NotNull MetaTileEntity createMetaTileEntity(int id) {
        MetaTileEntity metaTileEntity = getObjectById(id);
        if (metaTileEntity == null) throw new IllegalArgumentException("No MTE is registered for id " + id);
        return metaTileEntity.createMetaTileEntity(null);
    }

    public @NotNull MetaTileEntity createMetaTileEntity(@NotNull ResourceLocation id) {
        MetaTileEntity metaTileEntity = getObject(id);
        if (metaTileEntity == null) throw new IllegalArgumentException("No MTE is registered for id " + id);
        return metaTileEntity.createMetaTileEntity(null);
    }

    @Override
    public void freeze() {
        super.freeze();
        registeredTileEntities.clear();
    }
}
