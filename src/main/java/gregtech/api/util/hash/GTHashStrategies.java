package gregtech.api.util.hash;

import net.minecraft.util.ResourceLocation;

import it.unimi.dsi.fastutil.Hash;

public final class GTHashStrategies {

    public static final Hash.Strategy<ResourceLocation> RESOURCE_LOCATION_STRATEGY = new Hash.Strategy<>() {

        @Override
        public int hashCode(ResourceLocation o) {
            if (o == null) return 0;
            int result = 31 + o.getNamespace().hashCode();
            return 31 * result + o.getPath().hashCode();
        }

        @Override
        public boolean equals(ResourceLocation a, ResourceLocation b) {
            if (a == b) return true;
            if (b == null) return false;
            return a.getNamespace().equals(b.getNamespace()) && a.getPath().equals(b.getPath());
        }
    };
}
