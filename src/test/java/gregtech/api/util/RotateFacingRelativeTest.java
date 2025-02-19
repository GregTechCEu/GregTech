package gregtech.api.util;

import net.minecraft.util.EnumFacing;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

public class RotateFacingRelativeTest {

    @Test
    public void frontNorthAllOthers() {
        EnumFacing front = EnumFacing.NORTH;

        for (EnumFacing other : EnumFacing.values()) {
            checkFace(front, other);
        }
    }

    @Test
    public void frontSouthAllOthers() {
        EnumFacing front = EnumFacing.SOUTH;

        checkFace(front, EnumFacing.UP);
        checkFace(front, EnumFacing.DOWN);
        checkFace(front, EnumFacing.NORTH, EnumFacing.SOUTH);
        checkFace(front, EnumFacing.SOUTH, EnumFacing.NORTH);
        checkFace(front, EnumFacing.WEST, EnumFacing.EAST);
        checkFace(front, EnumFacing.EAST, EnumFacing.WEST);
    }

    @Test
    public void frontWestAllOthers() {
        EnumFacing front = EnumFacing.WEST;

        checkFace(front, EnumFacing.UP);
        checkFace(front, EnumFacing.DOWN);
        checkFace(front, EnumFacing.NORTH, EnumFacing.WEST);
        checkFace(front, EnumFacing.SOUTH, EnumFacing.EAST);
        checkFace(front, EnumFacing.WEST, EnumFacing.SOUTH);
        checkFace(front, EnumFacing.EAST, EnumFacing.NORTH);
    }

    @Test
    public void frontEastAllOthers() {
        EnumFacing front = EnumFacing.EAST;

        checkFace(front, EnumFacing.UP);
        checkFace(front, EnumFacing.DOWN);
        checkFace(front, EnumFacing.NORTH, EnumFacing.EAST);
        checkFace(front, EnumFacing.SOUTH, EnumFacing.WEST);
        checkFace(front, EnumFacing.WEST, EnumFacing.NORTH);
        checkFace(front, EnumFacing.EAST, EnumFacing.SOUTH);
    }

    @Test
    public void frontUpAllOthers() {
        EnumFacing front = EnumFacing.UP;

        checkFace(front, EnumFacing.UP, EnumFacing.NORTH);
        checkFace(front, EnumFacing.DOWN, EnumFacing.SOUTH);
        checkFace(front, EnumFacing.NORTH, EnumFacing.UP);
        checkFace(front, EnumFacing.SOUTH, EnumFacing.DOWN);
        checkFace(front, EnumFacing.WEST);
        checkFace(front, EnumFacing.EAST);
    }

    @Test
    public void frontDownAllOthers() {
        EnumFacing front = EnumFacing.DOWN;

        checkFace(front, EnumFacing.UP, EnumFacing.SOUTH);
        checkFace(front, EnumFacing.DOWN, EnumFacing.NORTH);
        checkFace(front, EnumFacing.NORTH, EnumFacing.UP);
        checkFace(front, EnumFacing.SOUTH, EnumFacing.DOWN);
        checkFace(front, EnumFacing.WEST);
        checkFace(front, EnumFacing.EAST);
    }

    private static void checkFace(EnumFacing front, EnumFacing other) {
        checkFace(front, other, other);
    }

    private static void checkFace(EnumFacing front, EnumFacing other, EnumFacing expected) {
        MatcherAssert.assertThat(GTUtility.getFaceRelativeToFace(front, other), is(expected));
    }
}
