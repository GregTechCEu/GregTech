package gregtech.api.mui.widget;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.util.ValueHelper;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IEnumValue;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class EnumButtonRow<T extends Enum<T>> {

    @NotNull
    private final IEnumValue<T> value;
    @NotNull
    private final Class<T> enumValue;
    private int margin = 2;
    @Nullable
    private IKey rowDescription;
    @Nullable
    private Function<@NotNull T, @Nullable IDrawable> background;
    @Nullable
    private Function<@NotNull T, @Nullable IDrawable> selectedBackground;
    @Nullable
    private Function<@NotNull T, @NotNull IDrawable> overlay;
    @Nullable
    private BiConsumer<T, ToggleButton> widgetExtras;

    public static <T extends Enum<T>> EnumButtonRow<T> builder(@NotNull IEnumValue<T> value) {
        return new EnumButtonRow<>(value);
    }

    private EnumButtonRow(IEnumValue<T> value) {
        this.value = value;
        this.enumValue = value.getEnumClass();
    }

    /**
     * Set the margin applied to the right side of each button. <br/>
     * The default is {@code 2}.
     */
    public EnumButtonRow<T> buttonMargin(int margin) {
        this.margin = margin;
        return this;
    }

    /**
     * Add an {@link IKey} to the row that will be right aligned at the end.
     */
    public EnumButtonRow<T> rowDescription(IKey lang) {
        this.rowDescription = lang;
        return this;
    }

    /**
     * Add a background to each {@link ToggleButton} when the button is not selected.
     */
    public EnumButtonRow<T> background(Function<T, IDrawable> background) {
        this.background = background;
        return this;
    }

    /**
     * Add a background to each {@link ToggleButton} when the button is selected.
     */
    public EnumButtonRow<T> selectedBackground(Function<T, IDrawable> selectedBackground) {
        this.selectedBackground = selectedBackground;
        return this;
    }

    /**
     * Add an overlay to each {@link ToggleButton}.
     */
    public EnumButtonRow<T> overlay(Function<T, IDrawable> overlay) {
        this.overlay = overlay;
        return this;
    }

    /**
     * Add an overlay to each {@link ToggleButton}.
     */
    public EnumButtonRow<T> overlay(IDrawable... overlay) {
        this.overlay = val -> overlay[val.ordinal()];
        return this;
    }

    /**
     * Add an overlay to each {@link ToggleButton}.
     */
    public EnumButtonRow<T> overlay(int size, IDrawable... overlay) {
        this.overlay = val -> overlay[val.ordinal()]
                .asIcon()
                .size(size);
        return this;
    }

    public EnumButtonRow<T> widgetExtras(BiConsumer<T, ToggleButton> widgetExtras) {
        this.widgetExtras = widgetExtras;
        return this;
    }

    public Flow build() {
        Flow row = Flow.row()
                .marginBottom(2)
                .widthRel(1f)
                .coverChildrenHeight();

        for (T enumVal : enumValue.getEnumConstants()) {
            ToggleButton button = new ToggleButton()
                    .marginRight(margin)
                    .size(18)
                    .value(ValueHelper.boolValueOf(value, enumVal));

            IDrawable background = GTGuiTextures.MC_BUTTON;
            if (this.background != null) {
                IDrawable backgroundReplacement = this.background.apply(enumVal);
                if (backgroundReplacement != null) {
                    background = backgroundReplacement;
                }
            }
            button.background(background);

            IDrawable selectedBackground = GTGuiTextures.MC_BUTTON_DISABLED;
            if (this.selectedBackground != null) {
                IDrawable selectedBackgroundReplacement = this.selectedBackground.apply(enumVal);
                if (selectedBackgroundReplacement != null) {
                    selectedBackground = selectedBackgroundReplacement;
                }
            }
            button.selectedBackground(selectedBackground);

            if (overlay != null) {
                button.overlay(overlay.apply(enumVal));
            }

            if (this.widgetExtras != null) {
                this.widgetExtras.accept(enumVal, button);
            }

            row.child(button);
        }

        if (this.rowDescription != null && !this.rowDescription.get().isEmpty()) {
            row.child(this.rowDescription.asWidget()
                    .align(Alignment.CenterRight)
                    .height(18));
        }

        return row;
    }
}
