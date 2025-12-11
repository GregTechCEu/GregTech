package gregtech.mixins.minecraft;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTLog;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TileEntity.class)
public class TileEntityMixin {

    @WrapOperation(method = "create",
                   at = @At(value = "INVOKE",
                            target = "Ljava/lang/Class;newInstance()Ljava/lang/Object;"))
    private static <T> T wrap(Class<T> instance, Operation<T> original,
                              @Local(argsOnly = true) NBTTagCompound tagCompound) {
        if (IGregTechTileEntity.class.isAssignableFrom(instance)) {
            // this is necessary to avoid the no args constructor call
            GTLog.logger.warn("creating mte with data {}", tagCompound);
        }
        return original.call(instance);
    }
}
