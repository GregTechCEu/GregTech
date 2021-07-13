package gregtech.common.blocks.models;

import java.util.ArrayList;
import net.minecraft.client.renderer.block.model.IBakedModel;

public class ModelCache {
    private ArrayList<ModelCachePackage> models = new ArrayList<>();

    private IBakedModel currentMatch = null;

    public void addToCache(IBakedModel model, String name) {
        ModelCachePackage pack = new ModelCachePackage(model, name);
        this.models.add(pack);
    }

    public boolean hasModel(String name) {
        boolean output = false;
        for (int i = 0; i < this.models.size(); i++) {
            ModelCachePackage pack = this.models.get(i);
            if (pack.getTextureName().contentEquals(name)) {
                output = true;
                this.currentMatch = pack.getModel();
                break;
            }
        }
        return output;
    }

    public IBakedModel getCurrentMatch() {
        return this.currentMatch;
    }

    public static class ModelCachePackage {
        private IBakedModel model;

        private String name;

        public ModelCachePackage(IBakedModel modelIn, String nameIn) {
            this.model = modelIn;
            this.name = nameIn;
        }

        public IBakedModel getModel() {
            return this.model;
        }

        public String getTextureName() {
            return this.name;
        }
    }

}
