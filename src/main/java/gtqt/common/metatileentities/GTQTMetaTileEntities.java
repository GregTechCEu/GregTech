package gtqt.common.metatileentities;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;

import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityDualHatch;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityMEDualHatch;

import static gregtech.api.util.GTUtility.gregtechId;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class GTQTMetaTileEntities {

    public static final MetaTileEntityDualHatch[] DUAL_IMPORT_HATCH = new MetaTileEntityDualHatch[GTValues.V.length - 1]; // All tiers but MAX
    public static final MetaTileEntityDualHatch[] DUAL_EXPORT_HATCH = new MetaTileEntityDualHatch[GTValues.V.length - 1];

    public static MetaTileEntityMEDualHatch ME_DUAL_IMPORT_HATCH;
    public static MetaTileEntityMEDualHatch ME_DUAL_EXPORT_HATCH;

    //从2500开始写 与gtceu本体共用一个注册表
    //任务：GTQT内不方便写的内容转移到这里来写
    //例如 高等级的能源仓 激光仓等等
    public static void initialization() {
        int endPos = GregTechAPI.isHighTier() ? GTValues.V.length - 1 : GTValues.UHV + 1;

        for(int i=0;i<endPos;i++)
        {
            String voltageName = GTValues.VN[i+1].toLowerCase();
            DUAL_IMPORT_HATCH[i] = new MetaTileEntityDualHatch(gregtechId("dual_hatch.import." + voltageName), i+1, false);
            DUAL_EXPORT_HATCH[i] = new MetaTileEntityDualHatch(gregtechId("dual_hatch.export." + voltageName), i+1, true);

            registerMetaTileEntity(2500 + i, DUAL_IMPORT_HATCH[i]);
            registerMetaTileEntity(2515 + i, DUAL_EXPORT_HATCH[i]);

        }
        ME_DUAL_IMPORT_HATCH = new MetaTileEntityMEDualHatch(gregtechId("me_dual_hatch.import"), false);
        ME_DUAL_EXPORT_HATCH = new MetaTileEntityMEDualHatch(gregtechId("me_dual_hatch.export"), true);

<<<<<<< Updated upstream

        registerMetaTileEntity(2500, ME_DUAL_IMPORT_HATCH);
        registerMetaTileEntity(2501, ME_DUAL_EXPORT_HATCH);

        registerMetaTileEntity(3000, ME_DUAL_IMPORT_HATCH);
        registerMetaTileEntity(3001, ME_DUAL_EXPORT_HATCH);

=======
        registerMetaTileEntity(3000, ME_DUAL_IMPORT_HATCH);
        registerMetaTileEntity(3001, ME_DUAL_EXPORT_HATCH);
>>>>>>> Stashed changes
    }
}
