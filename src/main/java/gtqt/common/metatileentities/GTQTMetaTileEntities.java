package gtqt.common.metatileentities;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;

import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityBudgetHatch;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityDualHatch;

import static gregtech.api.util.GTUtility.gregtechId;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class GTQTMetaTileEntities {

    public static final MetaTileEntityDualHatch[] DUAL_IMPORT_HATCH = new MetaTileEntityDualHatch[GTValues.V.length - 1]; // All tiers but MAX
    public static final MetaTileEntityDualHatch[] DUAL_EXPORT_HATCH = new MetaTileEntityDualHatch[GTValues.V.length - 1];

    public static final MetaTileEntityBudgetHatch[] BUDGET_IMPORT_HATCH = new MetaTileEntityBudgetHatch[GTValues.V.length - 1];
    //从2000开始写 与gtceu本体共用一个注册表
    //任务：GTQT内不方便写的内容转移到这里来写
    //例如 高等级的能源仓 激光仓等等
    public static void initialization() {
        int endPos = GregTechAPI.isHighTier() ? GTValues.V.length - 1 : GTValues.UHV + 1;

        for(int i=0;i<endPos;i++)
        {
            String voltageName = GTValues.VN[i].toLowerCase();
            DUAL_IMPORT_HATCH[i] = new MetaTileEntityDualHatch(gregtechId("dual_hatch.import." + voltageName), i, false);
            DUAL_EXPORT_HATCH[i] = new MetaTileEntityDualHatch(gregtechId("dual_hatch.export." + voltageName), i, true);
            //BUDGET_IMPORT_HATCH[i] = new MetaTileEntityBudgetHatch(gregtechId("budget_hatch.import." + voltageName), i);

            registerMetaTileEntity(2000 + i, DUAL_IMPORT_HATCH[i]);
            registerMetaTileEntity(2015 + i, DUAL_EXPORT_HATCH[i]);
            //registerMetaTileEntity(2030 + i, BUDGET_IMPORT_HATCH[i]);
        }
    }
}
