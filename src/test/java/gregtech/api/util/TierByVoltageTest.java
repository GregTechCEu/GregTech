package gregtech.api.util;


import org.junit.jupiter.api.Test;

import static gregtech.api.GTValues.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TierByVoltageTest {

    @Test
    public void testV() {
        assertThat(GTUtility.getTierByVoltage(V[ULV]), is((byte) ULV));
        assertThat(GTUtility.getTierByVoltage(V[LV]), is((byte) LV));
        assertThat(GTUtility.getTierByVoltage(V[MV]), is((byte) MV));
        assertThat(GTUtility.getTierByVoltage(V[HV]), is((byte) HV));
        assertThat(GTUtility.getTierByVoltage(V[EV]), is((byte) EV));
        assertThat(GTUtility.getTierByVoltage(V[IV]), is((byte) IV));
        assertThat(GTUtility.getTierByVoltage(V[LuV]), is((byte) LuV));
        assertThat(GTUtility.getTierByVoltage(V[ZPM]), is((byte) ZPM));
        assertThat(GTUtility.getTierByVoltage(V[UV]), is((byte) UV));
        assertThat(GTUtility.getTierByVoltage(V[UHV]), is((byte) UHV));
        assertThat(GTUtility.getTierByVoltage(V[UEV]), is((byte) UEV));
        assertThat(GTUtility.getTierByVoltage(V[UIV]), is((byte) UIV));
        assertThat(GTUtility.getTierByVoltage(V[UXV]), is((byte) UXV));
        assertThat(GTUtility.getTierByVoltage(V[OpV]), is((byte) OpV));
        assertThat(GTUtility.getTierByVoltage(V[MAX]), is((byte) MAX));
    }

    @Test
    public void testV_div_2() {
        assertThat(GTUtility.getTierByVoltage(V[ULV] / 2L), is((byte) ULV));
        assertThat(GTUtility.getTierByVoltage(V[LV] / 2L), is((byte) LV));
        assertThat(GTUtility.getTierByVoltage(V[MV] / 2L), is((byte) MV));
        assertThat(GTUtility.getTierByVoltage(V[HV] / 2L), is((byte) HV));
        assertThat(GTUtility.getTierByVoltage(V[EV] / 2L), is((byte) EV));
        assertThat(GTUtility.getTierByVoltage(V[IV] / 2L), is((byte) IV));
        assertThat(GTUtility.getTierByVoltage(V[LuV] / 2L), is((byte) LuV));
        assertThat(GTUtility.getTierByVoltage(V[ZPM] / 2L), is((byte) ZPM));
        assertThat(GTUtility.getTierByVoltage(V[UV] / 2L), is((byte) UV));
        assertThat(GTUtility.getTierByVoltage(V[UHV] / 2L), is((byte) UHV));
        assertThat(GTUtility.getTierByVoltage(V[UEV] / 2L), is((byte) UEV));
        assertThat(GTUtility.getTierByVoltage(V[UIV] / 2L), is((byte) UIV));
        assertThat(GTUtility.getTierByVoltage(V[UXV] / 2L), is((byte) UXV));
        assertThat(GTUtility.getTierByVoltage(V[OpV] / 2L), is((byte) OpV));
        assertThat(GTUtility.getTierByVoltage(V[MAX] / 2L), is((byte) MAX));
    }

    @Test
    public void testV_mult_2() {
        assertThat(GTUtility.getTierByVoltage(V[ULV] * 2L), is((byte) LV));
        assertThat(GTUtility.getTierByVoltage(V[LV] * 2L), is((byte) MV));
        assertThat(GTUtility.getTierByVoltage(V[MV] * 2L), is((byte) HV));
        assertThat(GTUtility.getTierByVoltage(V[HV] * 2L), is((byte) EV));
        assertThat(GTUtility.getTierByVoltage(V[EV] * 2L), is((byte) IV));
        assertThat(GTUtility.getTierByVoltage(V[IV] * 2L), is((byte) LuV));
        assertThat(GTUtility.getTierByVoltage(V[LuV] * 2L), is((byte) ZPM));
        assertThat(GTUtility.getTierByVoltage(V[ZPM] * 2L), is((byte) UV));
        assertThat(GTUtility.getTierByVoltage(V[UV] * 2L), is((byte) UHV));
        assertThat(GTUtility.getTierByVoltage(V[UHV] * 2L), is((byte) UEV));
        assertThat(GTUtility.getTierByVoltage(V[UEV] * 2L), is((byte) UIV));
        assertThat(GTUtility.getTierByVoltage(V[UIV] * 2L), is((byte) UXV));
        assertThat(GTUtility.getTierByVoltage(V[UXV] * 2L), is((byte) OpV));
        assertThat(GTUtility.getTierByVoltage(V[OpV] * 2L), is((byte) MAX));
        assertThat(GTUtility.getTierByVoltage(V[MAX] * 2L), is((byte) MAX));
    }

    @Test
    public void testVA() {
        assertThat(GTUtility.getTierByVoltage(VA[ULV]), is((byte) ULV));
        assertThat(GTUtility.getTierByVoltage(VA[LV]), is((byte) LV));
        assertThat(GTUtility.getTierByVoltage(VA[MV]), is((byte) MV));
        assertThat(GTUtility.getTierByVoltage(VA[HV]), is((byte) HV));
        assertThat(GTUtility.getTierByVoltage(VA[EV]), is((byte) EV));
        assertThat(GTUtility.getTierByVoltage(VA[IV]), is((byte) IV));
        assertThat(GTUtility.getTierByVoltage(VA[LuV]), is((byte) LuV));
        assertThat(GTUtility.getTierByVoltage(VA[ZPM]), is((byte) ZPM));
        assertThat(GTUtility.getTierByVoltage(VA[UV]), is((byte) UV));
        assertThat(GTUtility.getTierByVoltage(VA[UHV]), is((byte) UHV));
        assertThat(GTUtility.getTierByVoltage(VA[UEV]), is((byte) UEV));
        assertThat(GTUtility.getTierByVoltage(VA[UIV]), is((byte) UIV));
        assertThat(GTUtility.getTierByVoltage(VA[UXV]), is((byte) UXV));
        assertThat(GTUtility.getTierByVoltage(VA[OpV]), is((byte) OpV));
        assertThat(GTUtility.getTierByVoltage(VA[MAX]), is((byte) MAX));
    }

    @Test
    public void testVA_div_2() {
        assertThat(GTUtility.getTierByVoltage(VA[ULV] / 2L), is((byte) ULV));
        assertThat(GTUtility.getTierByVoltage(VA[LV] / 2L), is((byte) LV));
        assertThat(GTUtility.getTierByVoltage(VA[MV] / 2L), is((byte) MV));
        assertThat(GTUtility.getTierByVoltage(VA[HV] / 2L), is((byte) HV));
        assertThat(GTUtility.getTierByVoltage(VA[EV] / 2L), is((byte) EV));
        assertThat(GTUtility.getTierByVoltage(VA[IV] / 2L), is((byte) IV));
        assertThat(GTUtility.getTierByVoltage(VA[LuV] / 2L), is((byte) LuV));
        assertThat(GTUtility.getTierByVoltage(VA[ZPM] / 2L), is((byte) ZPM));
        assertThat(GTUtility.getTierByVoltage(VA[UV] / 2L), is((byte) UV));
        assertThat(GTUtility.getTierByVoltage(VA[UHV] / 2L), is((byte) UHV));
        assertThat(GTUtility.getTierByVoltage(VA[UEV] / 2L), is((byte) UEV));
        assertThat(GTUtility.getTierByVoltage(VA[UIV] / 2L), is((byte) UIV));
        assertThat(GTUtility.getTierByVoltage(VA[UXV] / 2L), is((byte) UXV));
        assertThat(GTUtility.getTierByVoltage(VA[OpV] / 2L), is((byte) OpV));
        assertThat(GTUtility.getTierByVoltage(VA[MAX] / 2L), is((byte) MAX));
    }

    @Test
    public void testVA_mult_2() {
        assertThat(GTUtility.getTierByVoltage(VA[ULV] * 2L), is((byte) LV));
        assertThat(GTUtility.getTierByVoltage(VA[LV] * 2L), is((byte) MV));
        assertThat(GTUtility.getTierByVoltage(VA[MV] * 2L), is((byte) HV));
        assertThat(GTUtility.getTierByVoltage(VA[HV] * 2L), is((byte) EV));
        assertThat(GTUtility.getTierByVoltage(VA[EV] * 2L), is((byte) IV));
        assertThat(GTUtility.getTierByVoltage(VA[IV] * 2L), is((byte) LuV));
        assertThat(GTUtility.getTierByVoltage(VA[LuV] * 2L), is((byte) ZPM));
        assertThat(GTUtility.getTierByVoltage(VA[ZPM] * 2L), is((byte) UV));
        assertThat(GTUtility.getTierByVoltage(VA[UV] * 2L), is((byte) UHV));
        assertThat(GTUtility.getTierByVoltage(VA[UHV] * 2L), is((byte) UEV));
        assertThat(GTUtility.getTierByVoltage(VA[UEV] * 2L), is((byte) UIV));
        assertThat(GTUtility.getTierByVoltage(VA[UIV] * 2L), is((byte) UXV));
        assertThat(GTUtility.getTierByVoltage(VA[UXV] * 2L), is((byte) OpV));
        assertThat(GTUtility.getTierByVoltage(VA[OpV] * 2L), is((byte) MAX));
        assertThat(GTUtility.getTierByVoltage(VA[MAX] * 2L), is((byte) MAX));
    }

    @Test
    public void testSpecialCases() {
        assertThat(GTUtility.getTierByVoltage(0L), is((byte) ULV));
        assertThat(GTUtility.getTierByVoltage(2L), is((byte) ULV));
    }
}
