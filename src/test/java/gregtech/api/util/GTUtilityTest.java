package gregtech.api.util;

import org.junit.jupiter.api.Test;

public class GTUtilityTest {

    @Test
    public void binarySearchTest() {
        for (int i = 0; i < 1000; i++) {
            int finalI = i;

            long result = GTUtility.binarySearchLong(0, 10000, l -> l >= finalI, true);

            if (result != finalI) {
                throw new AssertionError("Got " + result + " when desiring " + finalI);
            }
        }
        for (int i = 0; i < 1000; i++) {
            int finalI = i;

            long result = GTUtility.binarySearchLong(0, 10000, l -> l <= finalI, false);

            if (result != finalI) {
                throw new AssertionError("Got " + result + " when desiring " + finalI);
            }
        }
    }
}
