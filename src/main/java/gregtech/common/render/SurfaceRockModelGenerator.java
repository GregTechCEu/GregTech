package gregtech.common.render;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.pipeline.attribute.ColourAttribute;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import gregtech.api.util.Position;
import gregtech.api.util.PositionedRect;
import gregtech.api.util.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SurfaceRockModelGenerator {

    public static CCModel generatePebblePileModel(Random random) {
        List<IndexedCuboid6> cuboid6s = generateCuboidList(random);
        CCModel ccModel = CCModel.quadModel(cuboid6s.size() * 24);
        for (int i = 0; i < cuboid6s.size(); i++) {
            IndexedCuboid6 cuboid6 = cuboid6s.get(i);
            ccModel.generateBlock(i * 24, cuboid6);
            int b = (int) cuboid6.data;
            int[] colours = ccModel.getOrAllocate(ColourAttribute.attributeKey);
            int color = (b & 0xFF) << 24 | (b & 0xFF) << 16 | (b & 0xFF) << 8 | (0xFF);
            Arrays.fill(colours, i * 24, i * 24 + 24, color);
        }
        return ccModel.computeNormals();
    }

    private static List<IndexedCuboid6> generateCuboidList(Random random) {
        ArrayList<IndexedCuboid6> result = new ArrayList<>();
        List<PositionedRect> occupiedAreas = new ArrayList<>();
        int stonePlaceAttempts = 64;
        int maxStones = 8;
        int stonesPlaced = 0;
        for (int i = 0; i < stonePlaceAttempts && stonesPlaced < maxStones; i++) {
            int sizeX = 2 + random.nextInt(3);
            int sizeZ = 2 + random.nextInt(3);
            int stoneHeight = 4 + random.nextInt(4);
            int posX = random.nextInt(16 - sizeX);
            int posZ = random.nextInt(16 - sizeZ);
            PositionedRect rect = new PositionedRect(new Position(posX, posZ), new Size(sizeX, sizeZ));
            if (occupiedAreas.stream().noneMatch(rect::intersects)) {
                Vector3 minVector = new Vector3(posX / 16.0, 0 / 16.0, posZ / 16.0);
                Cuboid6 bounds = new Cuboid6(minVector, minVector.copy());
                bounds.max.add(sizeX / 16.0, stoneHeight / 16.0, sizeZ / 16.0);
                int brightness = 100 + random.nextInt(130);
                result.add(new IndexedCuboid6(brightness, bounds));
                occupiedAreas.add(rect);
                stonesPlaced++;
            }
        }
        return result;
    }
}
