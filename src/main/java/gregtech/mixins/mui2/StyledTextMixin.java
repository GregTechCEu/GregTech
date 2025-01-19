package gregtech.mixins.mui2;

import gregtech.api.mui.UnboxFix;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.text.AnimatedText;
import com.cleanroommc.modularui.drawable.text.StyledText;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// todo remove this and interface once rc3 is released
@Mixin(value = StyledText.class, remap = false)
public abstract class StyledTextMixin implements UnboxFix {

    @Shadow
    @Final
    private IKey key;

    @Shadow
    private Alignment alignment;

    @Shadow
    private Integer color;

    @Shadow
    private float scale;

    @Shadow
    private Boolean shadow;

    @Unique
    private boolean gregTech$defaultTextColor = true;

    @Unique
    private boolean gregTech$defaultShadow = true;

    @Override
    public void gregTech$useDefaultTextColor(boolean b) {
        gregTech$defaultTextColor = b;
    }

    @Override
    public void gregTech$useDefaultShadow(boolean b) {
        gregTech$defaultShadow = b;
    }

    @Inject(method = "color", at = @At("HEAD"))
    private void setDefault(int color, CallbackInfoReturnable<StyledText> cir) {
        gregTech$useDefaultTextColor(false);
    }

    @Inject(method = "shadow", at = @At("HEAD"))
    private void setDefault(boolean shadow, CallbackInfoReturnable<StyledText> cir) {
        gregTech$useDefaultShadow(false);
    }

    /**
     * @author GTCEu - Ghzdude
     * @reason Implement <a href="https://github.com/CleanroomMC/ModularUI/pull/86">MUI2 PR#86</a>
     */
    @Overwrite
    public TextWidget asWidget() {
        var text = new TextWidget(this.key)
                .alignment(this.alignment)
                .scale(this.scale);

        ((UnboxFix) text).gregTech$useDefaultTextColor(this.gregTech$defaultTextColor);
        ((UnboxFix) text).gregTech$useDefaultShadow(this.gregTech$defaultShadow);

        return text.color(color == null ? 0 : color)
                .shadow(shadow != null && shadow);
    }

    /**
     * @author GTCEu - Ghzdude
     * @reason Implement <a href="https://github.com/CleanroomMC/ModularUI/pull/86">MUI2 PR#86</a>
     */
    @Overwrite
    public AnimatedText withAnimation() {
        var text = new AnimatedText(this.key)
                .alignment(this.alignment)
                .scale(this.scale);

        ((UnboxFix) text).gregTech$useDefaultTextColor(this.gregTech$defaultTextColor);
        ((UnboxFix) text).gregTech$useDefaultShadow(this.gregTech$defaultShadow);

        return text.color(color == null ? 0 : color)
                .shadow(shadow != null && shadow);
    }

    @SideOnly(Side.CLIENT)
    @ModifyArg(method = "draw",
               at = @At(value = "INVOKE",
                        target = "Lcom/cleanroommc/modularui/drawable/text/TextRenderer;setColor(I)V"))
    public int fixColor(int color, @Local(argsOnly = true) WidgetTheme theme) {
        return gregTech$defaultTextColor ? theme.getTextColor() : color;
    }

    @SideOnly(Side.CLIENT)
    @ModifyArg(method = "draw",
               at = @At(value = "INVOKE",
                        target = "Lcom/cleanroommc/modularui/drawable/text/TextRenderer;setShadow(Z)V"))
    public boolean fixShadow(boolean shadow, @Local(argsOnly = true) WidgetTheme theme) {
        return gregTech$defaultShadow ? theme.getTextShadow() : shadow;
    }
}
