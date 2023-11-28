package gregtech.api.worldgen.shape;

import gregtech.api.worldgen.config.OreConfigUtils;

import net.minecraft.util.math.Vec3i;

import com.google.gson.JsonObject;

import java.util.Random;

public class PlateGenerator extends ShapeGenerator {

    private int minLength;
    private int maxLength;
    private int minDepth;
    private int maxDepth;
    private int minHeight;
    private int maxHeight;
    private float floorSharpness;
    private float roofSharpness;

    public PlateGenerator() {}

    @Override
    public void loadFromConfig(JsonObject object) {
        int[] length = OreConfigUtils.getIntRange(object.get("length"));
        int[] depth = OreConfigUtils.getIntRange(object.get("depth"));
        int[] height = OreConfigUtils.getIntRange(object.get("height"));
        this.minLength = length[0];
        this.maxLength = length[1];
        this.minDepth = depth[0];
        this.maxDepth = depth[1];
        this.minHeight = height[0];
        this.maxHeight = height[1];
        if (object.has("floor_sharpness")) {
            this.floorSharpness = object.get("floor_sharpness").getAsFloat();
        } else this.floorSharpness = 0.3f;
        if (object.has("roof_sharpness")) {
            this.roofSharpness = object.get("roof_sharpness").getAsFloat();
        } else this.roofSharpness = 0.7f;
    }

    @Override
    public Vec3i getMaxSize() {
        int xzSize = Math.max(maxLength, maxDepth);
        return new Vec3i(xzSize * 2, maxDepth * 2, xzSize * 2);
    }

    @Override
    public void generate(Random gridRandom, IBlockGeneratorAccess relativeBlockAccess) {
        int length = (minLength == maxLength ? maxLength : minLength + gridRandom.nextInt(maxLength - minLength)) / 2;
        int depth = (minDepth == maxDepth ? maxDepth : minDepth + gridRandom.nextInt(maxDepth - minDepth)) / 2;
        int height = (minHeight == maxHeight ? maxHeight : minHeight + gridRandom.nextInt(maxHeight - minHeight)) / 2;
        boolean rotate = gridRandom.nextBoolean();
        for (int x = -length; x <= length; x++) {
            for (int z = -depth; z <= depth; z++) {
                boolean hasFloorSub = floorSharpness > gridRandom.nextFloat();
                boolean hasRoofSub = roofSharpness > gridRandom.nextFloat();
                for (int y = -height; y <= height; y++) {
                    if (hasRoofSub && (y == height || gridRandom.nextBoolean())) {
                        continue;
                    } else hasRoofSub = false;
                    if (hasFloorSub && y == -height)
                        continue;
                    relativeBlockAccess.generateBlock(rotate ? z : x, y, rotate ? x : z);
                }
            }
        }
    }
}
