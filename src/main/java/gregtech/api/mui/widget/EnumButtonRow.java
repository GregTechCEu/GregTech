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
    private final T[] enumValues;
    private int margin = 2;
    @Nullable
    private IKey rowDescription;
    @Nullable
    private Function<@NotNull T, @Nullable IDrawable> backgrounds;
    @Nullable
    private Function<@NotNull T, @Nullable IDrawable> selectedBackgrounds;
    @Nullable
    private Function<@NotNull T, @Nullable IDrawable> overlays;
    @Nullable
    private BiConsumer<@NotNull T, @NotNull ToggleButton> widgetExtras;

    public static <T extends Enum<T>> EnumButtonRow<T> builder(@NotNull IEnumValue<T> value) {
        return new EnumButtonRow<>(value);
    }

    private EnumButtonRow(@NotNull IEnumValue<T> value) {
        this.value = value;
        this.enumValues = value.getEnumClass().getEnumConstants();
    }

    /**
     * Set the margin applied to the right side of each button. <br/>
     * The default is {@code 2}.
     */
    public EnumButtonRow<T> buttonMargins(int margin) {
        this.margin = margin;
        return this;
    }

    /**
     * Add an {@link IKey} to the row that will be right aligned at the end.
     */
    public EnumButtonRow<T> rowDescription(@NotNull IKey lang) {
        this.rowDescription = lang;
        return this;
    }

    /**
     * Add a background to each {@link ToggleButton} when the button is not selected. <br/>
     * Return {@code null} from the function to skip setting a background on the button associated with the enum value.
     */
    public EnumButtonRow<T> backgrounds(Function<@NotNull T, @Nullable IDrawable> background) {
        this.backgrounds = background;
        return this;
    }

    /**
     * Add a background to each {@link ToggleButton} when the button is selected. <br/>
     * Return {@code null} from the function to skip setting a selected background on the button associated with the
     * enum value.
     */
    public EnumButtonRow<T> selectedBackgrounds(Function<@NotNull T, @Nullable IDrawable> selectedBackground) {
        this.selectedBackgrounds = selectedBackground;
        return this;
    }

    /**
     * Add an overlay to each {@link ToggleButton}. <br/>
     * Return {@code null} from the function to skip setting an overlay on the button associated with the enum value.
     */
    public EnumButtonRow<T> overlays(Function<@NotNull T, @Nullable IDrawable> overlay) {
        this.overlays = overlay;
        return this;
    }

    /**
     * Add an overlay to each {@link ToggleButton}. <br/>
     * The array can either have a length of 1 to apply the same overlay to every button, or it must have the same
     * number of elements as the enum does. <br/>
     * Use {@link #overlays(Function)} if you need more granular control over each button's overlay.
     *
     * @throws IllegalArgumentException if the two array length conditions aren't met
     */
    public EnumButtonRow<T> overlays(@NotNull IDrawable @NotNull... overlay) {
        int len = overlay.length;
        if (len == 1) {
            return overlays($ -> overlay[0]);
        } else if (len != enumValues.length) {
            throw new IllegalArgumentException(
                    "Number of elements in the overlay array must be 1 or the same as the enum!");
        }

        return overlays(val -> overlay[val.ordinal()]);
    }

    /**
     * Add an overlay with a certain size to each {@link ToggleButton}. <br/>
     * The array can either have a length of 1 to apply the same overlay to every button, or it must have the same
     * number of elements as the enum does. <br/>
     * Use {@link #overlays(Function)} if you need more granular control over each button's overlay.
     *
     * @throws IllegalArgumentException if the two array length conditions aren't met
     *
     */
    public EnumButtonRow<T> overlays(int size, @NotNull IDrawable @NotNull... overlay) {
        int len = overlay.length;
        if (len == 1) {
            IDrawable singleOverlay = overlay[0]
                    .asIcon()
                    .size(size);
            return overlays($ -> singleOverlay);
        } else if (len != enumValues.length) {
            throw new IllegalArgumentException(
                    "Number of elements in the overlay array must be 1 or the same as the enum!");
        }

        return overlays(val -> overlay[val.ordinal()]
                .asIcon()
                .size(size));
    }

    /**
     * Configure each toggle button directly.
     */
    public EnumButtonRow<T> widgetExtras(@NotNull BiConsumer<@NotNull T, @NotNull ToggleButton> widgetExtras) {
        this.widgetExtras = widgetExtras;
        return this;
    }

    public Flow build() {
        Flow row = Flow.row()
                .marginBottom(2)
                .widthRel(1f)
                .coverChildrenHeight();

        for (T enumVal : enumValues) {
            ToggleButton button = new ToggleButton()
                    .marginRight(margin)
                    .size(18)
                    .value(ValueHelper.boolValueOf(value, enumVal));

            IDrawable background = GTGuiTextures.MC_BUTTON;
            if (this.backgrounds != null) {
                IDrawable backgroundReplacement = this.backgrounds.apply(enumVal);
                if (backgroundReplacement != null) {
                    background = backgroundReplacement;
                }
            }
            button.background(background);

            IDrawable selectedBackground = GTGuiTextures.MC_BUTTON_DISABLED;
            if (this.selectedBackgrounds != null) {
                IDrawable selectedBackgroundReplacement = this.selectedBackgrounds.apply(enumVal);
                if (selectedBackgroundReplacement != null) {
                    selectedBackground = selectedBackgroundReplacement;
                }
            }
            button.selectedBackground(selectedBackground);

            if (overlays != null) {
                IDrawable overlay = this.overlays.apply(enumVal);
                if (overlay != null) {
                    button.overlay(overlay);
                }
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
