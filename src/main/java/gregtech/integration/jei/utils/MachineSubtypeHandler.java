package gregtech.integration.jei.utils;

import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.metatileentity.MetaTileEntity;
import mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class MachineSubtypeHandler implements ISubtypeInterpreter {
    @Nonnull
    @Override
    public String apply(@Nonnull ItemStack itemStack) {
        String additionalData = "";
        MetaTileEntity metaTileEntity = MachineItemBlock.getMetaTileEntity(itemStack);
        if (metaTileEntity != null) {
            additionalData = metaTileEntity.getItemSubTypeId(itemStack);
        }
        return String.format("%d;%s", itemStack.getMetadata(), additionalData);
    }
}
