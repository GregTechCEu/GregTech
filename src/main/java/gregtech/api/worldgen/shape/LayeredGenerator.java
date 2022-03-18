package gregtech.api.worldgen.shape;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.math.Vec3i;

public class LayeredGenerator extends EllipsoidGenerator {

    private int yRadius;

    public LayeredGenerator() {
    }

    @Override
    public void loadFromConfig(JsonObject object) {
        super.loadFromConfig(object);
        JsonElement element = object.get("layers");
        if (element != null) {
            yRadius = element.getAsInt() / 2;
        } else {
            yRadius = 3; // default number of layers
        }
    }

    @Override
    public int getYRadius() {
        return yRadius;
    }

    @Override
    public Vec3i getMaxSize() {
        Vec3i result = super.getMaxSize();
        return new Vec3i(result.getX(), yRadius, result.getZ());
    }

    @Override
    public void generateBlock(int x, int y, int z, IBlockGeneratorAccess blockAccess) {
        blockAccess.generateBlock(x, y, z, false);
    }
}
