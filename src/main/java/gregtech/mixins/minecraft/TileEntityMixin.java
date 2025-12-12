package gregtech.mixins.minecraft;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.GTBaseTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTLog;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TileEntity.class)
public abstract class TileEntityMixin {

    @Shadow
    @Nullable
    public static ResourceLocation getKey(Class<? extends TileEntity> clazz) {
        return null;
    }

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
            if (mte == null) return original.call(instance);
            GTLog.logger.warn("creating {} from TileEntity#create", mte.metaTileEntityId, tagCompound);
            // noinspection unchecked
            return (T) mte.createMetaTileEntity(null);
        }
        return original.call(instance);
    }

    @SuppressWarnings({ "rawtypes" })
    @WrapOperation(method = "writeInternal",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/util/registry/RegistryNamespaced;getNameForObject(Ljava/lang/Object;)Ljava/lang/Object;"))
    public Object fixClass(RegistryNamespaced instance, Object value, Operation<Object> original) {
        if (GTBaseTileEntity.class.isAssignableFrom((Class<?>) value)) {
            return getKey(GTBaseTileEntity.class);
        }
        return original.call(instance, value);
    }
}
