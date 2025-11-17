package gregtech.common.mui.widget;

import gregtech.api.util.GTLog;

import net.minecraft.util.math.MathHelper;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.utils.ParseResult;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import com.cleanroommc.modularui.widgets.textfield.BaseTextFieldWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldHandler;
import com.cleanroommc.modularui.widgets.textfield.TextFieldRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.text.ParsePosition;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.LongSupplier;
import java.util.function.LongUnaryOperator;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class GTTextFieldWidget extends BaseTextFieldWidget<GTTextFieldWidget> {

    private IStringValue<?> stringValue;
    private Function<String, String> validator = val -> val;
    private boolean numbers = false;
    private double defaultNumber = 0;
    private boolean tooltipOverride = false;
    private final GTTextFieldRenderer renderer;
    private Consumer<String> onTextAccept = null;

    public GTTextFieldWidget() {
        this.renderer = new GTTextFieldRenderer(this.handler);
        super.renderer = this.renderer;
    }

    public double parse(String num) {
        ParseResult result = MathUtils.parseExpression(num, this.defaultNumber, true);
        double value = result.getResult();
        if (result.isFailure()) {
            String mathFailMessage = result.getError();
            GTLog.logger.error("Math expression error in {}: {}", this, mathFailMessage);
        }
        return value;
    }

    @Override
    public void onInit() {
        super.onInit();
        if (this.stringValue == null) {
            this.stringValue = new StringValue("");
        }
        setText(this.stringValue.getStringValue());
        if (!hasTooltip() && !tooltipOverride) {
            tooltipBuilder(tooltip -> tooltip.addLine(IKey.str(getText())));
            tooltipOverride = false;
        }
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof IStringValue<?>iStringValue &&
                syncHandler instanceof ValueSyncHandler<?>valueSyncHandler) {
            this.stringValue = iStringValue;
            valueSyncHandler.setChangeListener(() -> {
                markTooltipDirty();
                setText(this.stringValue.getValue().toString());
            });
            return true;
        }
        return false;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isFocused()) {
            String s = this.stringValue.getStringValue();
            if (!getText().equals(s)) {
                setText(s);
            }
        }
    }

    @Override
    public @NotNull Result onKeyPressed(char character, int keyCode) {
        Result result = super.onKeyPressed(character, keyCode);
        if (result == Result.SUCCESS) switch (keyCode) {
            case Keyboard.KEY_RETURN, Keyboard.KEY_NUMPADENTER -> {
                if (this.onTextAccept != null) {
                    this.onTextAccept.accept(getText());
                }
            }
        }
        return result;
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        RichTooltip tooltip = getTooltip();
        if (tooltip != null &&
                (tooltipOverride || getScrollData().isScrollBarActive(getScrollArea())) &&
                isHoveringFor(tooltip.getShowUpTimer())) {
            tooltip.draw(getContext());
        }
    }

    public GTTextFieldWidget setPostFix(String postFix) {
        return setPostFix(() -> postFix);
    }

    public GTTextFieldWidget setPostFix(@Nullable Supplier<String> postFix) {
        if (postFix == null) return this;
        return setPostFix(IKey.dynamic(postFix));
    }

    public GTTextFieldWidget setPostFix(IKey postFix) {
        this.renderer.setPostFix(postFix);
        return getThis();
    }

    /**
     * @param onTextAccept Called when {@link Keyboard#KEY_RETURN} or {@link Keyboard#KEY_NUMPADENTER} is pressed.
     */
    public GTTextFieldWidget onTextAccept(Consumer<String> onTextAccept) {
        this.onTextAccept = onTextAccept;
        return this;
    }

    @NotNull
    public String getText() {
        if (this.handler.getText().isEmpty()) {
            return "";
        }
        if (this.handler.getText().size() > 1) {
            throw new IllegalStateException("GTTextFieldWidget can only have one line!");
        }
        return this.handler.getText().get(0);
    }

    public void setText(@NotNull String text) {
        if (this.handler.getText().isEmpty()) {
            this.handler.getText().add(text);
        } else {
            this.handler.getText().set(0, text);
        }
    }

    @Override
    public void onRemoveFocus(ModularGuiContext context) {
        super.onRemoveFocus(context);
        this.setText(this.validator.apply(getText()));
        this.stringValue
                .setStringValue(this.numbers ? format.parse(getText(), new ParsePosition(0)).toString() : getText());
    }

    @Override
    public boolean canHover() {
        return true;
    }

    public GTTextFieldWidget setMaxLength(int maxLength) {
        this.handler.setMaxCharacters(maxLength);
        return this;
    }

    public GTTextFieldWidget setPattern(Pattern pattern) {
        this.handler.setPattern(pattern);
        return this;
    }

    public GTTextFieldWidget setValidator(Function<String, String> validator) {
        this.validator = validator;
        return this;
    }

    public GTTextFieldWidget setNumbersLong(LongUnaryOperator validator) {
        this.numbers = true;
        setValidator(val -> {
            long num;
            if (val.isEmpty()) {
                num = (long) this.defaultNumber;
            } else {
                num = (long) parse(val);
            }
            return format.format(validator.applyAsLong(num));
        });
        return this;
    }

    public GTTextFieldWidget setNumbers(IntUnaryOperator validator) {
        this.numbers = true;
        return setValidator(val -> {
            int num;
            if (val.isEmpty()) {
                num = (int) this.defaultNumber;
            } else {
                num = (int) parse(val);
            }
            return format.format(validator.applyAsInt(num));
        });
    }

    public GTTextFieldWidget setNumbersDouble(DoubleUnaryOperator validator) {
        this.numbers = true;
        return setValidator(val -> {
            double num;
            if (val.isEmpty()) {
                num = this.defaultNumber;
            } else {
                num = parse(val);
            }
            return format.format(validator.applyAsDouble(num));
        });
    }

    public GTTextFieldWidget setNumbers(IntSupplier min, IntSupplier max) {
        return setNumbers(val -> Math.min(max.getAsInt(), Math.max(min.getAsInt(), val)));
    }

    public GTTextFieldWidget setNumbersLong(LongSupplier min, LongSupplier max) {
        return setNumbersLong(val -> Math.min(max.getAsLong(), Math.max(min.getAsLong(), val)));
    }

    public GTTextFieldWidget setNumbersLong(long min, long max) {
        return setNumbersLong(val -> Math.min(Math.max(val, min), max));
    }

    public GTTextFieldWidget setNumbers(int min, int max) {
        return setNumbers(val -> MathHelper.clamp(val, min, max));
    }

    public GTTextFieldWidget setNumbers() {
        return setNumbers(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public GTTextFieldWidget setDefaultNumber(double defaultNumber) {
        this.defaultNumber = defaultNumber;
        return this;
    }

    public GTTextFieldWidget value(IStringValue<?> stringValue) {
        this.stringValue = stringValue;
        setValue(stringValue);
        return this;
    }

    @Override
    public @NotNull RichTooltip tooltip() {
        tooltipOverride = true;
        return super.tooltip();
    }

    private static class GTTextFieldRenderer extends TextFieldRenderer {

        private @Nullable IKey postFix = null;

        public GTTextFieldRenderer(TextFieldHandler handler) {
            super(handler);
        }

        @Override
        protected void draw(String text, float x, float y) {
            if (postFix != null) {
                text += postFix.getFormatted();
            }
            super.draw(text, x, y);
        }

        public void setPostFix(@NotNull IKey postFix) {
            this.postFix = postFix;
        }
    }
}
