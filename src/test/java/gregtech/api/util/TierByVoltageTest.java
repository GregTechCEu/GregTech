package gregtech.api.util;

import org.junit.jupiter.api.Test;

import static gregtech.api.GTValues.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TierByVoltageTest {

    @Test
    public void testV() {
        expectTier(V[ULV], ULV, ULV);
        expectTier(V[LV], LV, LV);
        expectTier(V[MV], MV, MV);
        expectTier(V[HV], HV, HV);
        expectTier(V[EV], EV, EV);
        expectTier(V[IV], IV, IV);
        expectTier(V[LuV], LuV, LuV);
        expectTier(V[ZPM], ZPM, ZPM);
        expectTier(V[UV], UV, UV);
        expectTier(V[UHV], UHV, UHV);
        expectTier(V[UEV], UEV, UEV);
        expectTier(V[UIV], UIV, UIV);
        expectTier(V[UXV], UXV, UXV);
        expectTier(V[OpV], OpV, OpV);
        expectTier(V[MAX], MAX, MAX);
    }

    @Test
    public void testV_div_2() {
        expectTier(V[ULV] / 2L, ULV, ULV);
        expectTier(V[LV] / 2L, LV, ULV);
        expectTier(V[MV] / 2L, MV, LV);
        expectTier(V[HV] / 2L, HV, MV);
        expectTier(V[EV] / 2L, EV, HV);
        expectTier(V[IV] / 2L, IV, EV);
        expectTier(V[LuV] / 2L, LuV, IV);
        expectTier(V[ZPM] / 2L, ZPM, LuV);
        expectTier(V[UV] / 2L, UV, ZPM);
        expectTier(V[UHV] / 2L, UHV, UV);
        expectTier(V[UEV] / 2L, UEV, UHV);
        expectTier(V[UIV] / 2L, UIV, UEV);
        expectTier(V[UXV] / 2L, UXV, UIV);
        expectTier(V[OpV] / 2L, OpV, UXV);
        expectTier(V[MAX] / 2L, MAX, OpV);
    }

    @Test
    public void testV_mult_2() {
        expectTier(V[ULV] * 2L, LV, ULV);
        expectTier(V[LV] * 2L, MV, LV);
        expectTier(V[MV] * 2L, HV, MV);
        expectTier(V[HV] * 2L, EV, HV);
        expectTier(V[EV] * 2L, IV, EV);
        expectTier(V[IV] * 2L, LuV, IV);
        expectTier(V[LuV] * 2L, ZPM, LuV);
        expectTier(V[ZPM] * 2L, UV, ZPM);
        expectTier(V[UV] * 2L, UHV, UV);
        expectTier(V[UHV] * 2L, UEV, UHV);
        expectTier(V[UEV] * 2L, UIV, UEV);
        expectTier(V[UIV] * 2L, UXV, UIV);
        expectTier(V[UXV] * 2L, OpV, UXV);
        expectTier(V[OpV] * 2L, MAX, OpV);
        expectTier(V[MAX] * 2L, MAX, MAX);
    }

    @Test
    public void testVA() {
        expectTier(VA[ULV], ULV, ULV);
        expectTier(VA[LV], LV, ULV);
        expectTier(VA[MV], MV, LV);
        expectTier(VA[HV], HV, MV);
        expectTier(VA[EV], EV, HV);
        expectTier(VA[IV], IV, EV);
        expectTier(VA[LuV], LuV, IV);
        expectTier(VA[ZPM], ZPM, LuV);
        expectTier(VA[UV], UV, ZPM);
        expectTier(VA[UHV], UHV, UV);
        expectTier(VA[UEV], UEV, UHV);
        expectTier(VA[UIV], UIV, UEV);
        expectTier(VA[UXV], UXV, UIV);
        expectTier(VA[OpV], OpV, UXV);
        expectTier(VA[MAX], MAX, OpV);
    }

    @Test
    public void testVA_div_2() {
        expectTier(VA[ULV] / 2L, ULV, ULV);
        expectTier(VA[LV] / 2L, LV, ULV);
        expectTier(VA[MV] / 2L, MV, LV);
        expectTier(VA[HV] / 2L, HV, MV);
        expectTier(VA[EV] / 2L, EV, HV);
        expectTier(VA[IV] / 2L, IV, EV);
        expectTier(VA[LuV] / 2L, LuV, IV);
        expectTier(VA[ZPM] / 2L, ZPM, LuV);
        expectTier(VA[UV] / 2L, UV, ZPM);
        expectTier(VA[UHV] / 2L, UHV, UV);
        expectTier(VA[UEV] / 2L, UEV, UHV);
        expectTier(VA[UIV] / 2L, UIV, UEV);
        expectTier(VA[UXV] / 2L, UXV, UIV);
        expectTier(VA[OpV] / 2L, OpV, UXV);
        expectTier(VA[MAX] / 2L, MAX, OpV);
    }

    @Test
    public void testVA_mult_2() {
        expectTier(VA[ULV] * 2L, LV, ULV);
        expectTier(VA[LV] * 2L, MV, LV);
        expectTier(VA[MV] * 2L, HV, MV);
        expectTier(VA[HV] * 2L, EV, HV);
        expectTier(VA[EV] * 2L, IV, EV);
        expectTier(VA[IV] * 2L, LuV, IV);
        expectTier(VA[LuV] * 2L, ZPM, LuV);
        expectTier(VA[ZPM] * 2L, UV, ZPM);
        expectTier(VA[UV] * 2L, UHV, UV);
        expectTier(VA[UHV] * 2L, UEV, UHV);
        expectTier(VA[UEV] * 2L, UIV, UEV);
        expectTier(VA[UIV] * 2L, UXV, UIV);
        expectTier(VA[UXV] * 2L, OpV, UXV);
        expectTier(VA[OpV] * 2L, MAX, OpV);
        expectTier(VA[MAX] * 2L, MAX, MAX);
    }

    @Test
    public void testVSpecialCases() {
        expectTier(0L, ULV, ULV);
        expectTier(2L, ULV, ULV);
        expectTier(Integer.MAX_VALUE + 1L, MAX, MAX);
        expectTier(Long.MAX_VALUE, MAX, MAX);
        expectTier(-1L, ULV, ULV);
    }

    private static void expectTier(long voltage, int expectedTier, int expectedFloorTier) {
        assertThat("Result of GTUtility#getTierByVoltage(" + voltage + ") differs from expected value",
                GTUtility.getTierByVoltage(voltage), is((byte) expectedTier));
        assertThat("Result of GTUtility#getFloorTierByVoltage(" + voltage + ") differs from expected value",
                GTUtility.getFloorTierByVoltage(voltage), is((byte) expectedFloorTier));
    }
}
