package gregtech.mixins.minecraft;
import gtqt.api.util.alculateFacing;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class MixinEntity {


    @Shadow
    public float rotationYaw;
    /**
     * @author MeowmelMuku
     * @reason 尼玛东南西北分不清？？？？
     */
    @Overwrite
    public EnumFacing getHorizontalFacing()
    {
        // 获取标准化后的yaw角度（0~360）
        float normalizedYaw = alculateFacing.gregTech$normalizeYaw(this.rotationYaw);

        // 使用精确计算替代索引计算
        return alculateFacing.gregTech$calculateFacingFromYaw(normalizedYaw);
    }

}
