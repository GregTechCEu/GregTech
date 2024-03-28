package gregtech.api.util;

import gregtech.api.GTValues;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VoltageScaleTest {

    @Test
    public void testVoltages() {
        // Some commonly used numbers for recipe voltages. Ensure none change at LV scaling tier
        assertEquals(2, GTUtility.scaleVoltage(2, GTValues.LV));
        assertEquals(4, GTUtility.scaleVoltage(4, GTValues.LV));
        assertEquals(7, GTUtility.scaleVoltage(7, GTValues.LV));
        assertEquals(8, GTUtility.scaleVoltage(8, GTValues.LV));
        assertEquals(16, GTUtility.scaleVoltage(16, GTValues.LV));
        assertEquals(24, GTUtility.scaleVoltage(24, GTValues.LV));
        assertEquals(30, GTUtility.scaleVoltage(30, GTValues.LV));
        // Also test that voltages above are not affected.
        assertEquals(120, GTUtility.scaleVoltage(120, GTValues.LV));

        // Test to make sure they scale to above LV, with the appropriate scaled voltage
        // The first few are capped at half voltage.
        assertEquals(GTValues.VHA[GTValues.MV], GTUtility.scaleVoltage(2, GTValues.MV));
        assertEquals(GTValues.VHA[GTValues.MV], GTUtility.scaleVoltage(4, GTValues.MV));
        assertEquals(GTValues.VHA[GTValues.MV], GTUtility.scaleVoltage(7, GTValues.MV));
        assertEquals(GTValues.VHA[GTValues.MV], GTUtility.scaleVoltage(8, GTValues.MV));
        assertEquals(GTValues.VHA[GTValues.MV], GTUtility.scaleVoltage(16, GTValues.MV));
        // The remaining should scale depending on their actual voltage
        assertEquals(96, GTUtility.scaleVoltage(24, GTValues.MV));
        assertEquals(120, GTUtility.scaleVoltage(30, GTValues.MV));
        // Ensure a recipe will not exceed VA, even if the provided value "should" scale higher
        assertEquals(120, GTUtility.scaleVoltage(32, GTValues.MV));
        // Test that voltages already at MV are unaffected.
        assertEquals(33, GTUtility.scaleVoltage(33, GTValues.MV));
        assertEquals(120, GTUtility.scaleVoltage(120, GTValues.MV));
        // Also test that voltages above are still unaffected.
        assertEquals(129, GTUtility.scaleVoltage(129, GTValues.MV));
        assertEquals(480, GTUtility.scaleVoltage(480, GTValues.MV));
    }
}
