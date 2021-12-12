package gregtech.core.hooks;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.creativemd.littletiles.client.render.cache.LayeredRenderBoxCache;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import gregtech.client.utils.BloomEffectUtil;
import net.minecraft.util.BlockRenderLayer;

import java.util.Iterator;
import java.util.List;

public class LittleTilesHooks {

    public static LayeredRenderBoxCache initLayeredRenderBoxCache() {
        return new BloomLayeredRenderBoxCache();
    }


    public static class BloomLayeredRenderBoxCache extends LayeredRenderBoxCache {
        private List<LittleRenderBox> solid = null;
        private List<LittleRenderBox> cutout_mipped = null;
        private List<LittleRenderBox> cutout = null;
        private List<LittleRenderBox> bloom = null;
        private List<LittleRenderBox> translucent = null;

        public List<LittleRenderBox> get(BlockRenderLayer layer) {
            if (layer == BloomEffectUtil.BLOOM) {
                return bloom;
            }
            switch (layer) {
                case SOLID:
                    return solid;
                case CUTOUT_MIPPED:
                    return cutout_mipped;
                case CUTOUT:
                    return cutout;
                case TRANSLUCENT:
                    return translucent;
            }
            return null;
        }

        public void set(List<LittleRenderBox> cubes, BlockRenderLayer layer) {
            if (layer == BloomEffectUtil.BLOOM) {
                bloom = cubes;
            }
            switch (layer) {
                case SOLID:
                    solid = cubes;
                    break;
                case CUTOUT_MIPPED:
                    cutout_mipped = cubes;
                    break;
                case CUTOUT:
                    cutout = cubes;
                    break;
                case TRANSLUCENT:
                    translucent = cubes;
                    break;
            }
        }

        public boolean needUpdate() {
            return solid == null || cutout_mipped == null || cutout == null || translucent == null || bloom == null;
        }

        public void clear() {
            solid = null;
            cutout_mipped = null;
            cutout = null;
            translucent = null;
            bloom = null;
        }

        public void sort() {
            if (!OptifineHelper.isActive())
                return;

            for (Iterator<LittleRenderBox> iterator = solid.iterator(); iterator.hasNext();) {
                LittleRenderBox littleRenderingCube = iterator.next();
                if (littleRenderingCube.needsResorting) {
                    cutout_mipped.add(littleRenderingCube);
                    iterator.remove();
                }
            }
        }

    }
}
