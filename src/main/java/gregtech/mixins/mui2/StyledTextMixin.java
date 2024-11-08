package gregtech.mixins.mui2;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.text.AnimatedText;
import com.cleanroommc.modularui.drawable.text.StyledText;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.TextWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = StyledText.class, remap = false)
public class StyledTextMixin {

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

    /**
     * @author GTCEu - Ghzdude
     * @reason Implement <a href="https://github.com/CleanroomMC/ModularUI/pull/86">MUI2 PR#86</a>
     */
    @Overwrite
    public TextWidget asWidget() {
        var text = new TextWidget(this.key)
                .alignment(this.alignment)
                .scale(this.scale);

        var theme = ITheme.getDefault().getTextFieldTheme();
        int color = this.color == null ? theme.getColor() : this.color;
        boolean shadow = this.shadow == null ? theme.getTextShadow() : this.shadow;

        return text.color(color)
                .shadow(shadow);
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

        var theme = ITheme.getDefault().getTextFieldTheme();
        int color = this.color == null ? theme.getColor() : this.color;
        boolean shadow = this.shadow == null ? theme.getTextShadow() : this.shadow;

        return text.color(color)
                .shadow(shadow);
    }
}
