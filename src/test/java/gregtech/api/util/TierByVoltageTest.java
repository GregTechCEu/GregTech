package gregtech.api.util;


import org.junit.jupiter.api.Test;

import static gregtech.api.GTValues.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TierByVoltageTest {

    @Test
    public void testV() {
        assertThat(GTVoltageUtil.getTierByVoltage(V[ULV]), is((byte) ULV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[LV]), is((byte) LV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[MV]), is((byte) MV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[HV]), is((byte) HV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[EV]), is((byte) EV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[IV]), is((byte) IV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[LuV]), is((byte) LuV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[ZPM]), is((byte) ZPM));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UV]), is((byte) UV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UHV]), is((byte) UHV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UEV]), is((byte) UEV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UIV]), is((byte) UIV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UXV]), is((byte) UXV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[OpV]), is((byte) OpV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[MAX]), is((byte) MAX));
    }

    @Test
    public void testV_div_2() {
        assertThat(GTVoltageUtil.getTierByVoltage(V[ULV] / 2L), is((byte) ULV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[LV] / 2L), is((byte) LV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[MV] / 2L), is((byte) MV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[HV] / 2L), is((byte) HV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[EV] / 2L), is((byte) EV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[IV] / 2L), is((byte) IV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[LuV] / 2L), is((byte) LuV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[ZPM] / 2L), is((byte) ZPM));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UV] / 2L), is((byte) UV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UHV] / 2L), is((byte) UHV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UEV] / 2L), is((byte) UEV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UIV] / 2L), is((byte) UIV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UXV] / 2L), is((byte) UXV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[OpV] / 2L), is((byte) OpV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[MAX] / 2L), is((byte) MAX));
    }

    @Test
    public void testV_mult_2() {
        assertThat(GTVoltageUtil.getTierByVoltage(V[ULV] * 2L), is((byte) LV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[LV] * 2L), is((byte) MV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[MV] * 2L), is((byte) HV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[HV] * 2L), is((byte) EV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[EV] * 2L), is((byte) IV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[IV] * 2L), is((byte) LuV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[LuV] * 2L), is((byte) ZPM));
        assertThat(GTVoltageUtil.getTierByVoltage(V[ZPM] * 2L), is((byte) UV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UV] * 2L), is((byte) UHV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UHV] * 2L), is((byte) UEV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UEV] * 2L), is((byte) UIV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UIV] * 2L), is((byte) UXV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[UXV] * 2L), is((byte) OpV));
        assertThat(GTVoltageUtil.getTierByVoltage(V[OpV] * 2L), is((byte) MAX));
        assertThat(GTVoltageUtil.getTierByVoltage(V[MAX] * 2L), is((byte) MAX));
    }

    @Test
    public void testVA() {
        assertThat(GTVoltageUtil.getTierByVoltage(VA[ULV]), is((byte) ULV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[LV]), is((byte) LV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[MV]), is((byte) MV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[HV]), is((byte) HV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[EV]), is((byte) EV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[IV]), is((byte) IV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[LuV]), is((byte) LuV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[ZPM]), is((byte) ZPM));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UV]), is((byte) UV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UHV]), is((byte) UHV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UEV]), is((byte) UEV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UIV]), is((byte) UIV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UXV]), is((byte) UXV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[OpV]), is((byte) OpV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[MAX]), is((byte) MAX));
    }

    @Test
    public void testVA_div_2() {
        assertThat(GTVoltageUtil.getTierByVoltage(VA[ULV] / 2L), is((byte) ULV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[LV] / 2L), is((byte) LV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[MV] / 2L), is((byte) MV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[HV] / 2L), is((byte) HV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[EV] / 2L), is((byte) EV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[IV] / 2L), is((byte) IV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[LuV] / 2L), is((byte) LuV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[ZPM] / 2L), is((byte) ZPM));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UV] / 2L), is((byte) UV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UHV] / 2L), is((byte) UHV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UEV] / 2L), is((byte) UEV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UIV] / 2L), is((byte) UIV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UXV] / 2L), is((byte) UXV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[OpV] / 2L), is((byte) OpV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[MAX] / 2L), is((byte) MAX));
    }

    @Test
    public void testVA_mult_2() {
        assertThat(GTVoltageUtil.getTierByVoltage(VA[ULV] * 2L), is((byte) LV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[LV] * 2L), is((byte) MV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[MV] * 2L), is((byte) HV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[HV] * 2L), is((byte) EV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[EV] * 2L), is((byte) IV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[IV] * 2L), is((byte) LuV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[LuV] * 2L), is((byte) ZPM));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[ZPM] * 2L), is((byte) UV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UV] * 2L), is((byte) UHV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UHV] * 2L), is((byte) UEV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UEV] * 2L), is((byte) UIV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UIV] * 2L), is((byte) UXV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[UXV] * 2L), is((byte) OpV));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[OpV] * 2L), is((byte) MAX));
        assertThat(GTVoltageUtil.getTierByVoltage(VA[MAX] * 2L), is((byte) MAX));
    }

    @Test
    public void testSpecialCases() {
        assertThat(GTVoltageUtil.getTierByVoltage(0L), is((byte) ULV));
        assertThat(GTVoltageUtil.getTierByVoltage(2L), is((byte) ULV));
    }
}
