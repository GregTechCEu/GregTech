package gregtech.mixins.minecraft;

import gregtech.api.metatileentity.GTBaseTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TileEntity.class)
public abstract class TileEntityMixin {

    /**
     * Fixes the {@link Class#newInstance()} TE call by using nbt tag data to create the correct {@link MetaTileEntity}.
     *
     * @param instance    TileEntity class
     * @param original    {@link Class#newInstance()}
     * @param tagCompound additional data, used for {@link TileEntity#readFromNBT(NBTTagCompound)}
     * @return the correct MetaTileEntity, or the result of the original operation
     */
    @WrapOperation(method = "create",
                   at = @At(value = "INVOKE",
                            target = "Ljava/lang/Class;newInstance()Ljava/lang/Object;"))
    private static Object wrapNewInstance(Class<? extends TileEntity> instance,
                                          Operation<? extends TileEntity> original,
                                          @Local(argsOnly = true) NBTTagCompound tagCompound) {
        if (IGregTechTileEntity.class.isAssignableFrom(instance)) {
            // this is necessary to avoid the no args constructor call
            MetaTileEntity mte = GTUtility.getMetaTileEntity(tagCompound.getString("MetaId"));
            if (mte != null) return mte.copy();
        }
        return original.call(instance);
    }

    /**
     * Fixes an issue in {@link TileEntity#writeInternal(NBTTagCompound)} that expects the TE class to be registered.
     * However, MetaTileEntities are stored under the {@link GTBaseTileEntity} class.
     * 
     * @param value the TileEntity class
     * @return GTBaseTileEntity's class if the class extends from it, otherwise the original class
     */
    @ModifyArg(method = "writeInternal",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/util/registry/RegistryNamespaced;getNameForObject(Ljava/lang/Object;)Ljava/lang/Object;"))
    public Object fixClass(Object value) {
        if (GTBaseTileEntity.class.isAssignableFrom((Class<?>) value)) {
            return GTBaseTileEntity.class;
        }
        return value;
    }
}
