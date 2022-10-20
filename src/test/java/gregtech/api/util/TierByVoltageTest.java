package gregtech.api.util;

import org.junit.Test;

import static gregtech.api.GTValues.*;
import static junit.framework.TestCase.assertEquals;

public class TierByVoltageTest {

    @Test
    public void testV() {
        assertEquals(ULV, GTUtility.getTierByVoltage(V[ULV]));
        assertEquals(LV, GTUtility.getTierByVoltage(V[LV]));
        assertEquals(MV, GTUtility.getTierByVoltage(V[MV]));
        assertEquals(HV, GTUtility.getTierByVoltage(V[HV]));
        assertEquals(EV, GTUtility.getTierByVoltage(V[EV]));
        assertEquals(IV, GTUtility.getTierByVoltage(V[IV]));
        assertEquals(LuV, GTUtility.getTierByVoltage(V[LuV]));
        assertEquals(ZPM, GTUtility.getTierByVoltage(V[ZPM]));
        assertEquals(UV, GTUtility.getTierByVoltage(V[UV]));
        assertEquals(UHV, GTUtility.getTierByVoltage(V[UHV]));
        assertEquals(UEV, GTUtility.getTierByVoltage(V[UEV]));
        assertEquals(UIV, GTUtility.getTierByVoltage(V[UIV]));
        assertEquals(UXV, GTUtility.getTierByVoltage(V[UXV]));
        assertEquals(OpV, GTUtility.getTierByVoltage(V[OpV]));
        assertEquals(MAX, GTUtility.getTierByVoltage(V[MAX]));
    }

    @Test
    public void testV_div_2() {
        assertEquals(ULV, GTUtility.getTierByVoltage(V[ULV] / 2L));
        assertEquals(LV, GTUtility.getTierByVoltage(V[LV] / 2L));
        assertEquals(MV, GTUtility.getTierByVoltage(V[MV] / 2L));
        assertEquals(HV, GTUtility.getTierByVoltage(V[HV] / 2L));
        assertEquals(EV, GTUtility.getTierByVoltage(V[EV] / 2L));
        assertEquals(IV, GTUtility.getTierByVoltage(V[IV] / 2L));
        assertEquals(LuV, GTUtility.getTierByVoltage(V[LuV] / 2L));
        assertEquals(ZPM, GTUtility.getTierByVoltage(V[ZPM] / 2L));
        assertEquals(UV, GTUtility.getTierByVoltage(V[UV] / 2L));
        assertEquals(UHV, GTUtility.getTierByVoltage(V[UHV] / 2L));
        assertEquals(UEV, GTUtility.getTierByVoltage(V[UEV] / 2L));
        assertEquals(UIV, GTUtility.getTierByVoltage(V[UIV] / 2L));
        assertEquals(UXV, GTUtility.getTierByVoltage(V[UXV] / 2L));
        assertEquals(OpV, GTUtility.getTierByVoltage(V[OpV] / 2L));
        assertEquals(MAX, GTUtility.getTierByVoltage(V[MAX] / 2L));
    }

    @Test
    public void testV_mult_2() {
        assertEquals(LV, GTUtility.getTierByVoltage(V[ULV] * 2L));
        assertEquals(MV, GTUtility.getTierByVoltage(V[LV] * 2L));
        assertEquals(HV, GTUtility.getTierByVoltage(V[MV] * 2L));
        assertEquals(EV, GTUtility.getTierByVoltage(V[HV] * 2L));
        assertEquals(IV, GTUtility.getTierByVoltage(V[EV] * 2L));
        assertEquals(LuV, GTUtility.getTierByVoltage(V[IV] * 2L));
        assertEquals(ZPM, GTUtility.getTierByVoltage(V[LuV] * 2L));
        assertEquals(UV, GTUtility.getTierByVoltage(V[ZPM] * 2L));
        assertEquals(UHV, GTUtility.getTierByVoltage(V[UV] * 2L));
        assertEquals(UEV, GTUtility.getTierByVoltage(V[UHV] * 2L));
        assertEquals(UIV, GTUtility.getTierByVoltage(V[UEV] * 2L));
        assertEquals(UXV, GTUtility.getTierByVoltage(V[UIV] * 2L));
        assertEquals(OpV, GTUtility.getTierByVoltage(V[UXV] * 2L));
        assertEquals(MAX, GTUtility.getTierByVoltage(V[OpV] * 2L));
        assertEquals(MAX, GTUtility.getTierByVoltage(V[MAX] * 2L));
    }

    @Test
    public void testVA() {
        assertEquals(ULV, GTUtility.getTierByVoltage(VA[ULV]));
        assertEquals(LV, GTUtility.getTierByVoltage(VA[LV]));
        assertEquals(MV, GTUtility.getTierByVoltage(VA[MV]));
        assertEquals(HV, GTUtility.getTierByVoltage(VA[HV]));
        assertEquals(EV, GTUtility.getTierByVoltage(VA[EV]));
        assertEquals(IV, GTUtility.getTierByVoltage(VA[IV]));
        assertEquals(LuV, GTUtility.getTierByVoltage(VA[LuV]));
        assertEquals(ZPM, GTUtility.getTierByVoltage(VA[ZPM]));
        assertEquals(UV, GTUtility.getTierByVoltage(VA[UV]));
        assertEquals(UHV, GTUtility.getTierByVoltage(VA[UHV]));
        assertEquals(UEV, GTUtility.getTierByVoltage(VA[UEV]));
        assertEquals(UIV, GTUtility.getTierByVoltage(VA[UIV]));
        assertEquals(UXV, GTUtility.getTierByVoltage(VA[UXV]));
        assertEquals(OpV, GTUtility.getTierByVoltage(VA[OpV]));
        assertEquals(MAX, GTUtility.getTierByVoltage(VA[MAX]));
    }

    @Test
    public void testVA_div_2() {
        assertEquals(ULV, GTUtility.getTierByVoltage(VA[ULV] / 2L));
        assertEquals(LV, GTUtility.getTierByVoltage(VA[LV] / 2L));
        assertEquals(MV, GTUtility.getTierByVoltage(VA[MV] / 2L));
        assertEquals(HV, GTUtility.getTierByVoltage(VA[HV] / 2L));
        assertEquals(EV, GTUtility.getTierByVoltage(VA[EV] / 2L));
        assertEquals(IV, GTUtility.getTierByVoltage(VA[IV] / 2L));
        assertEquals(LuV, GTUtility.getTierByVoltage(VA[LuV] / 2L));
        assertEquals(ZPM, GTUtility.getTierByVoltage(VA[ZPM] / 2L));
        assertEquals(UV, GTUtility.getTierByVoltage(VA[UV] / 2L));
        assertEquals(UHV, GTUtility.getTierByVoltage(VA[UHV] / 2L));
        assertEquals(UEV, GTUtility.getTierByVoltage(VA[UEV] / 2L));
        assertEquals(UIV, GTUtility.getTierByVoltage(VA[UIV] / 2L));
        assertEquals(UXV, GTUtility.getTierByVoltage(VA[UXV] / 2L));
        assertEquals(OpV, GTUtility.getTierByVoltage(VA[OpV] / 2L));
        assertEquals(MAX, GTUtility.getTierByVoltage(VA[MAX] / 2L));
    }

    @Test
    public void testVA_mult_2() {
        assertEquals(LV, GTUtility.getTierByVoltage(VA[ULV] * 2L));
        assertEquals(MV, GTUtility.getTierByVoltage(VA[LV] * 2L));
        assertEquals(HV, GTUtility.getTierByVoltage(VA[MV] * 2L));
        assertEquals(EV, GTUtility.getTierByVoltage(VA[HV] * 2L));
        assertEquals(IV, GTUtility.getTierByVoltage(VA[EV] * 2L));
        assertEquals(LuV, GTUtility.getTierByVoltage(VA[IV] * 2L));
        assertEquals(ZPM, GTUtility.getTierByVoltage(VA[LuV] * 2L));
        assertEquals(UV, GTUtility.getTierByVoltage(VA[ZPM] * 2L));
        assertEquals(UHV, GTUtility.getTierByVoltage(VA[UV] * 2L));
        assertEquals(UEV, GTUtility.getTierByVoltage(VA[UHV] * 2L));
        assertEquals(UIV, GTUtility.getTierByVoltage(VA[UEV] * 2L));
        assertEquals(UXV, GTUtility.getTierByVoltage(VA[UIV] * 2L));
        assertEquals(OpV, GTUtility.getTierByVoltage(VA[UXV] * 2L));
        assertEquals(MAX, GTUtility.getTierByVoltage(VA[OpV] * 2L));
        assertEquals(MAX, GTUtility.getTierByVoltage(VA[MAX] * 2L));
    }


    @Test
    public void testSpecialCases() {
        assertEquals(ULV, GTUtility.getTierByVoltage(0L));
        assertEquals(ULV, GTUtility.getTierByVoltage(2L));
    }
}
