package gregtech.mixins.mui2;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.TextWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(value = TextWidget.class, remap = false)
public abstract class TextWidgetMixin {

    @Shadow
    @Final
    private IKey key;

    @Shadow
    private String lastText;

    @Shadow
    protected abstract void onTextChanged(String newText);

    /**
     * @author Ghzdude
     * @reason Flawed equals checking. If {@code lastText} is null, then it will never call
     *         {@link #onTextChanged(String)}, meaning ScrollingTextWidget is never updated to the new String
     */
    @Overwrite
    protected String checkString() {
        String text = this.key.getFormatted();
        if (!Objects.equals(this.lastText, text)) {
            onTextChanged(text);
            this.lastText = text;
        }
        return text;
    }
}
