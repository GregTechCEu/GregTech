package gtqt.common.metatileentities;

import gregtech.api.GTValues;

import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityDualHatch;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityMEDualHatch;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityMEPatternProvider;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityMEPatternProviderProxy;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntitySuperItemBus;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityThreadHatch;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityWirelessController;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityWirelessEnergyHatch;

import static gregtech.api.GTValues.VN;
import static gregtech.api.util.GTUtility.gregtechId;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class GTQTMetaTileEntities {

    public static final MetaTileEntityDualHatch[] DUAL_IMPORT_HATCH = new MetaTileEntityDualHatch[GTValues.V.length - 2]; // All tiers but MAX
    public static final MetaTileEntityDualHatch[] DUAL_EXPORT_HATCH = new MetaTileEntityDualHatch[GTValues.V.length - 2];
    public static final MetaTileEntityMEPatternProvider[] ME_PATTERN_PROVIDER = new MetaTileEntityMEPatternProvider[GTValues.V.length - 2];
    public static MetaTileEntityThreadHatch[] THREAD_HATCH = new MetaTileEntityThreadHatch[GTValues.V.length-1];
    public static MetaTileEntityMEDualHatch ME_DUAL_IMPORT_HATCH;
    public static MetaTileEntityMEDualHatch ME_DUAL_EXPORT_HATCH;
    public static MetaTileEntityMEPatternProviderProxy ME_PATTERN_PROVIDER_PROXY;

    public static final MetaTileEntitySuperItemBus[] SUPER_ITEM_IMPORT_BUS = new MetaTileEntitySuperItemBus[
            GTValues.V.length -
                    1]; // All tiers but MAX
    public static final MetaTileEntitySuperItemBus[] SUPER_ITEM_EXPORT_BUS = new MetaTileEntitySuperItemBus[
            GTValues.V.length -
                    1]; // All tiers but MAX

    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_4A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_4A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_16A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_16A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_64A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_64A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_256A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_256A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_1024A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_1024A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_4096A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_4096A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_16384A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_16384A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_65536A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_65536A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_262144A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_262144A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_INPUT_ENERGY_HATCH_1048576A = new MetaTileEntityWirelessEnergyHatch[15];
    public static final MetaTileEntityWirelessEnergyHatch[] WIRELESS_OUTPUT_ENERGY_HATCH_1048576A = new MetaTileEntityWirelessEnergyHatch[15];

    public static final MetaTileEntityWirelessController[] WIRELESS_CONTROLLERS = new MetaTileEntityWirelessController[15];
    //从2500开始写 与gtceu本体共用一个注册表
    //任务：GTQT内不方便写的内容转移到这里来写
    //例如 高等级的能源仓 激光仓等等
    public static void initialization() {
        for(int i=0;i<DUAL_IMPORT_HATCH.length;i++)
        {
            String voltageName = GTValues.VN[i+1].toLowerCase();
            DUAL_IMPORT_HATCH[i] = new MetaTileEntityDualHatch(gregtechId("dual_hatch.import." + voltageName), i+1, false);
            DUAL_EXPORT_HATCH[i] = new MetaTileEntityDualHatch(gregtechId("dual_hatch.export." + voltageName), i+1, true);
            ME_PATTERN_PROVIDER[i] = new MetaTileEntityMEPatternProvider(gregtechId("me_pattern_provider." + voltageName), i+1);

            registerMetaTileEntity(2500 + i, DUAL_IMPORT_HATCH[i]);
            registerMetaTileEntity(2515 + i, DUAL_EXPORT_HATCH[i]);
            registerMetaTileEntity(2530 + i, ME_PATTERN_PROVIDER[i]);


        }
        for (int i = 0; i < THREAD_HATCH.length; i++) {
            int tier = i+1;
            THREAD_HATCH[i] = registerMetaTileEntity(2600 + i, new MetaTileEntityThreadHatch(
                    gregtechId(String.format("thread_hatch.%s", GTValues.VN[tier])), tier));
        }

        ME_DUAL_IMPORT_HATCH = new MetaTileEntityMEDualHatch(gregtechId("me_dual_hatch.import"), false);
        ME_DUAL_EXPORT_HATCH = new MetaTileEntityMEDualHatch(gregtechId("me_dual_hatch.export"), true);
        ME_PATTERN_PROVIDER_PROXY= new MetaTileEntityMEPatternProviderProxy(gregtechId("me_pattern_provider_proxy"));

        registerMetaTileEntity(2700, ME_DUAL_IMPORT_HATCH);
        registerMetaTileEntity(2701, ME_DUAL_EXPORT_HATCH);
        registerMetaTileEntity(2702, ME_PATTERN_PROVIDER_PROXY);

        for (int i = 0; i < 14; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            SUPER_ITEM_IMPORT_BUS[i] = new MetaTileEntitySuperItemBus(
                    gregtechId("super_item_bus.import." + voltageName), i, false);
            SUPER_ITEM_EXPORT_BUS[i] = new MetaTileEntitySuperItemBus(
                    gregtechId("super_item_bus.export." + voltageName), i, true);

            registerMetaTileEntity(2800 + i, SUPER_ITEM_IMPORT_BUS[i]);
            registerMetaTileEntity(2815 + i, SUPER_ITEM_EXPORT_BUS[i]);
        }
        //无线能源仓注册 ID 3000+
        for (int i = 0; i < 15; i++) {
            String tier = VN[i].toLowerCase();

            WIRELESS_INPUT_ENERGY_HATCH[i] = registerMetaTileEntity(3000+i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.input." + tier), i, 2, false));
            WIRELESS_INPUT_ENERGY_HATCH_4A[i] = registerMetaTileEntity(3000+15 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.input_4a." + tier), i, 4, false));
            WIRELESS_INPUT_ENERGY_HATCH_16A[i] = registerMetaTileEntity(3000+30 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.input_16a." + tier), i, 16, false));
            WIRELESS_INPUT_ENERGY_HATCH_64A[i] = registerMetaTileEntity(3000+45 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.input_64a." + tier), i, 64, false));
            WIRELESS_INPUT_ENERGY_HATCH_256A[i] = registerMetaTileEntity(3000+60 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.input_256a." + tier), i, 256, false));
            WIRELESS_INPUT_ENERGY_HATCH_1024A[i] = registerMetaTileEntity(3000+75 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.input_1024a." + tier), i, 1024, false));
            WIRELESS_INPUT_ENERGY_HATCH_4096A[i] = registerMetaTileEntity(3000+90 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.input_4096a." + tier), i, 4096, false));
            WIRELESS_INPUT_ENERGY_HATCH_16384A[i] = registerMetaTileEntity(3000+105 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.input_16384a." + tier), i, 16384, false));
            WIRELESS_INPUT_ENERGY_HATCH_65536A[i] = registerMetaTileEntity(3000+120 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.input_65536a." + tier), i, 65536, false));
            WIRELESS_INPUT_ENERGY_HATCH_262144A[i] = registerMetaTileEntity(3000+135 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.input_262144a." + tier), i, 262144, false));
            WIRELESS_INPUT_ENERGY_HATCH_1048576A[i] = registerMetaTileEntity(3000+150 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.input_1048576a." + tier), i, 1048576, false));

            WIRELESS_OUTPUT_ENERGY_HATCH[i] = registerMetaTileEntity(3000+165 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.output." + tier), i, 2, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_4A[i] = registerMetaTileEntity(3000+180 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.output_4a." + tier), i, 4, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_16A[i] = registerMetaTileEntity(3000+195 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.output_16a." + tier), i, 16, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_64A[i] = registerMetaTileEntity(3000+210 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.output_64a." + tier), i, 64, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_256A[i] = registerMetaTileEntity(3000+225 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.output_256a." + tier), i, 256, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_1024A[i] = registerMetaTileEntity(3000+240 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.output_1024a." + tier), i, 1024, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_4096A[i] = registerMetaTileEntity(3000+255 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.output_4096a." + tier), i, 4096, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_16384A[i] = registerMetaTileEntity(3000+270 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.output_16384a." + tier), i, 16384, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_65536A[i] = registerMetaTileEntity(3000+285 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.output_65536a." + tier), i, 65536, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_262144A[i] = registerMetaTileEntity(3000+300 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.output_262144a." + tier), i, 262144, true));
            WIRELESS_OUTPUT_ENERGY_HATCH_1048576A[i] = registerMetaTileEntity(3000+315 + i, new MetaTileEntityWirelessEnergyHatch(gregtechId("wireless_energy_hatch.output_1048576a." + tier), i, 1048576, true));


            //管理单元
            WIRELESS_CONTROLLERS[i] = registerMetaTileEntity(3000+500 + i, new MetaTileEntityWirelessController(gregtechId("wireless_controller." + tier), i));
        }
    }
}
