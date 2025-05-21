package gregtech.datafix.impl;

import gregtech.datafix.migration.lib.MTEDataMigrator;

import org.jetbrains.annotations.NotNull;

import static gregtech.api.GTValues.*;
import static gregtech.api.util.GTUtility.gregtechId;
import static gregtech.common.metatileentities.MetaTileEntities.*;

public final class V2PostMTEReallocFixer {

    private V2PostMTEReallocFixer() {}

    public static void apply(@NotNull MTEDataMigrator dataMigrator) {
        // item/fluid input/output buses
        for (int i = 0; i <= UHV; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1150 + i, 11000 + i); // item in
            dataMigrator.migrateMTEMeta(MODID, 1165 + i, 11015 + i); // item out
            dataMigrator.migrateMTEMeta(MODID, 1180 + i, 11030 + i); // fluid in
            dataMigrator.migrateMTEMeta(MODID, 1195 + i, 11045 + i); // fluid out
        }
        // energy hatches
        for (int i = 0; i < ENERGY_INPUT_HATCH.length; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1210 + i, 11120 + i); // 2A IN
            dataMigrator.migrateMTEMeta(MODID, 1225 + i, 11135 + i); // 2A OUT
        }
        dataMigrator.migrateMTEMeta(MODID, 1399, 11150 + EV); // 4A EV IN
        dataMigrator.migrateMTEMeta(MODID, 1400, 11165 + EV); // 4A EV OUT
        for (int i = 0; i < IV; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1240 + i, 11150 + IV + i); // 4A IN
            dataMigrator.migrateMTEMeta(MODID, 1245 + i, 11180 + IV + i); // 16A IN
            dataMigrator.migrateMTEMeta(MODID, 1250 + i, 11165 + IV + i); // 4A OUT
            dataMigrator.migrateMTEMeta(MODID, 1255 + i, 11195 + IV + i); // 16A OUT
            dataMigrator.migrateMTEMeta(MODID, 1260 + i, 11210 + IV + i); // 64A IN
            dataMigrator.migrateMTEMeta(MODID, 1265 + i, 11225 + IV + i); // 64A OUT
        }
        // lasers
        for (int i = 0; i < UV; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1420 + i, 11240 + IV + i); // 256A IN
            dataMigrator.migrateMTEMeta(MODID, 1429 + i, 11255 + IV + i); // 256A OUT
            dataMigrator.migrateMTEMeta(MODID, 1438 + i, 11270 + IV + i); // 1024A IN
            dataMigrator.migrateMTEMeta(MODID, 1447 + i, 11285 + IV + i); // 1024A OUT
            dataMigrator.migrateMTEMeta(MODID, 1456 + i, 11300 + IV + i); // 4096A IN
            dataMigrator.migrateMTEMeta(MODID, 1465 + i, 11315 + IV + i); // 4096A OUT
        }
        // rotor holders
        for (int i = 0; i < 6; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1640 + i, 11450 + HV + i);
        }
        // quad/nonuple hatches
        for (int i = 0; i < (UHV - IV + 1); i++) {
            dataMigrator.migrateMTEMeta(MODID, 1780 + i, 11060 + IV + i); // 4x in
            dataMigrator.migrateMTEMeta(MODID, 1785 + i, 11075 + IV + i); // 9x in
            dataMigrator.migrateMTEMeta(MODID, 1790 + i, 11090 + IV + i); // 4x out
            dataMigrator.migrateMTEMeta(MODID, 1795 + i, 11105 + IV + i); // 9x out
        }
        // EV quad and nonuple import/export hatches
        dataMigrator.migrateMTEMeta(MODID, 1190, 11060 + EV);
        dataMigrator.migrateMTEMeta(MODID, 1191, 11075 + EV);
        dataMigrator.migrateMTEMeta(MODID, 1205, 11090 + EV);
        dataMigrator.migrateMTEMeta(MODID, 1206, 11105 + EV);
        dataMigrator.migrateMTEName(gregtechId("fluid_hatch.import_4x"),
                gregtechId("fluid_hatch.import_4x.ev"));
        dataMigrator.migrateMTEName(gregtechId("fluid_hatch.import_9x"),
                gregtechId("fluid_hatch.import_9x.ev"));
        dataMigrator.migrateMTEName(gregtechId("fluid_hatch.export_4x"),
                gregtechId("fluid_hatch.export_4x.ev"));
        dataMigrator.migrateMTEName(gregtechId("fluid_hatch.export_9x"),
                gregtechId("fluid_hatch.export_9x.ev"));
        // mufflers
        for (int i = 0; i < UHV; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1657 + i, 11465 + LV + i);
        }
        // maintenance
        for (int i = 0; i < 3; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1654 + i, 11480 + i);
        }
        dataMigrator.migrateMTEMeta(MODID, 1401, 11483);
        // PA hatch
        dataMigrator.migrateMTEMeta(MODID, 1398, 11513);
        // Passthrough hatches
        dataMigrator.migrateMTEMeta(MODID, 1402, 11496);
        dataMigrator.migrateMTEMeta(MODID, 1403, 11497);
        // data hatches
        dataMigrator.migrateMTEMeta(MODID, 1404, 11514);
        dataMigrator.migrateMTEMeta(MODID, 1405, 11515);
        dataMigrator.migrateMTEMeta(MODID, 1406, 11516);
        // computation hatches
        dataMigrator.migrateMTEMeta(MODID, 1407, 11517);
        dataMigrator.migrateMTEMeta(MODID, 1408, 11518);
        dataMigrator.migrateMTEMeta(MODID, 1409, 11519);
        dataMigrator.migrateMTEMeta(MODID, 1410, 11520);
        // reservoir hatch
        dataMigrator.migrateMTEMeta(MODID, 1418, 11521);
        // primitive pump hatch
        dataMigrator.migrateMTEMeta(MODID, 1649, 11522);
        // steam hatches
        dataMigrator.migrateMTEMeta(MODID, 1651, 11523);
        dataMigrator.migrateMTEMeta(MODID, 1652, 11524);
        dataMigrator.migrateMTEMeta(MODID, 1653, 11525);
        // object holder
        dataMigrator.migrateMTEMeta(MODID, 1411, 11526);
        // hpca components
        dataMigrator.migrateMTEMeta(MODID, 1412, 11527);
        dataMigrator.migrateMTEMeta(MODID, 1413, 11528);
        dataMigrator.migrateMTEMeta(MODID, 1414, 11529);
        dataMigrator.migrateMTEMeta(MODID, 1415, 11530);
        dataMigrator.migrateMTEMeta(MODID, 1416, 11531);
        dataMigrator.migrateMTEMeta(MODID, 1417, 11532);
        // tank valves
        dataMigrator.migrateMTEMeta(MODID, 1596, 11539);
        dataMigrator.migrateMTEMeta(MODID, 1598, 11540);
        // AE2 hatches
        dataMigrator.migrateMTEMeta(MODID, 1745, 11549);
        dataMigrator.migrateMTEMeta(MODID, 1746, 11548);
        dataMigrator.migrateMTEMeta(MODID, 1747, 11542);
        dataMigrator.migrateMTEMeta(MODID, 1748, 11542);
        dataMigrator.migrateMTEMeta(MODID, 1752, 11543);
        dataMigrator.migrateMTEMeta(MODID, 1753, 11544);
        // transformers
        for (int i = 0; i < TRANSFORMER.length; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1270 + i, 5000 + i);
            dataMigrator.migrateMTEMeta(MODID, 1730 + i, 5015 + i);
            dataMigrator.migrateMTEMeta(MODID, 1285 + i, 5030 + i);
        }
        // miners
        for (int i = 0; i < 3; i++) {
            dataMigrator.migrateMTEMeta(MODID, 920 + i, 5045 + i);
        }
        // item collectors
        for (int i = 0; i < 4; i++) {
            dataMigrator.migrateMTEMeta(MODID, 980 + i, 5060 + i);
        }
        // diodes
        for (int i = 0; i < DIODES.length; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1300 + i, 5300 + i);
        }
        // battery buffers
        for (int i = 0; i < BATTERY_BUFFER.length; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1315 + i, 5315 + i);
            dataMigrator.migrateMTEMeta(MODID, 1330 + i, 5330 + i);
            dataMigrator.migrateMTEMeta(MODID, 1345 + i, 5345 + i);
        }
        // turbo chargers
        for (int i = 0; i < CHARGER.length; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1375 + i, 5375 + i);
        }
        // world accelerators
        for (int i = 0; i < 8; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1390 + i, 5390 + i);
        }
        // buffers
        for (int i = 0; i < 3; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1510 + i, 5510 + i);
        }
        // fisher
        for (int i = 0; i < 4; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1515 + i, 5515 + i);
        }
        // pumps
        for (int i = 0; i < 8; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1530 + i, 5530 + i);
        }
        // super/quantum chests
        for (int i = 0; i < 10; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1560 + i, 5560 + i);
        }
        // super/quantum tanks
        for (int i = 0; i < 10; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1575 + i, 5575 + i);
        }
        // block breakers
        for (int i = 0; i < 4; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1590 + i, 5590 + i);
        }
        // drums
        for (int i = 0; i < 8; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1610 + i, 5610 + i);
        }
        // crates
        for (int i = 0; i < 7; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1625 + i, 5625 + i);
        }
        // clipboard mte
        dataMigrator.migrateMTEMeta(MODID, 1666, 5646);
        // crafting station
        dataMigrator.migrateMTEMeta(MODID, 1647, 5647);
        // monitor screen
        dataMigrator.migrateMTEMeta(MODID, 1667, 5648);
        // creative energy
        dataMigrator.migrateMTEMeta(MODID, 1650, 5640);
        // creative chest
        dataMigrator.migrateMTEMeta(MODID, 1668, 5651);
        // creative tank
        dataMigrator.migrateMTEMeta(MODID, 1669, 5652);
        // energy converters
        for (int i = 0; i < ENERGY_CONVERTER.length; i++) {
            for (int j = 0; j < ENERGY_CONVERTER[i].length; j++) {
                dataMigrator.migrateMTEMeta(MODID, 1670 + i + (j * 4), 5670 + (i * ENERGY_CONVERTER[i].length) + j);
            }
        }
        // long distance item endpoint
        dataMigrator.migrateMTEMeta(MODID, 1749, 5749);
        // long distance fluid endpoint
        dataMigrator.migrateMTEMeta(MODID, 1750, 5750);
        // alarm
        dataMigrator.migrateMTEMeta(MODID, 1751, 5760);
        // magic energy absorber
        dataMigrator.migrateMTEMeta(MODID, 984, 5761);
        // multiblocks
        for (int i = 0; i <= 42; i++) {
            dataMigrator.migrateMTEMeta(MODID, 1000 + i, 10000 + i);
        }
        // multiblock tanks
        dataMigrator.migrateMTEMeta(MODID, 1597, 10043);
        dataMigrator.migrateMTEMeta(MODID, 1599, 10044);
        // primitive water pump
        dataMigrator.migrateMTEMeta(MODID, 1648, 10045);
    }
}
