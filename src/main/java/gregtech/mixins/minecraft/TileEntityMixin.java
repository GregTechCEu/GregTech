package gregtech.mixins.minecraft;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTLog;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

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
            var resloc = new ResourceLocation(tagCompound.getString("MetaId"));
            MetaTileEntity mte = GregTechAPI.mteManager.getRegistry(resloc.getNamespace())
                    .getObject(resloc);
            GTLog.logger.warn("creating {} from TileEntity#create", mte.metaTileEntityId, tagCompound);
        }
        return original.call(instance);
    }
}
