package gregtech.mixins.mui2;

import gregtech.api.mui.IconAcessor;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.drawable.DelegateIcon;
import com.cleanroommc.modularui.drawable.Icon;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Icon.class, remap = false)
public class IconMixin implements IconAcessor {

    @Shadow
    @Final
    private IDrawable drawable;

    public IDrawable gregTech$getDrawable() {
        return this.drawable;
    }

    @Mixin(value = DelegateIcon.class, remap = false)
    public static abstract class DelegateMixin implements IconAcessor {

        @Shadow
        public abstract IIcon getDelegate();

        @Override
        public IDrawable gregTech$getDrawable() {
            if (getDelegate() instanceof IconAcessor acessor) {
                return acessor.gregTech$getDrawable();
            }
            return null;
        }
    }
}
