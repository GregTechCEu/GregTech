package gtqt.common.metatileentities;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;

import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityMEPatternProviderProxy;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityThreadHatch;

import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityDualHatch;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityMEDualHatch;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityMEPatternProvider;

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

        registerMetaTileEntity(3000, ME_DUAL_IMPORT_HATCH);
        registerMetaTileEntity(3001, ME_DUAL_EXPORT_HATCH);
        registerMetaTileEntity(3002, ME_PATTERN_PROVIDER_PROXY);
    }
}
