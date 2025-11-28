package gregtech.api.util;

import net.minecraft.util.IStringSerializable;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.ITooltip;
import com.cleanroommc.modularui.drawable.text.LangKey;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public interface ITranslatable extends IStringSerializable {

    /**
     * Get the I18n translation key of this object.
     */
    @NotNull
    @Override
    String getName();

    /**
     * Get a {@link LangKey} of this objects name.
     */
    @NotNull
    default IKey getKey() {
        return IKey.lang(getName());
    }

    /**
     * Get the I18n translation key of this object depending on a string key.
     */
    @NotNull
    default String getName(@NotNull String key) {
        return getName();
    }

    /**
     * Get a {@link LangKey} of this objects name depending on a string key.
     */
    @NotNull
    default IKey getKey(@NotNull String key) {
        return IKey.lang(getName(key));
    }

    /**
     * Handle adding tooltip line(s) to an {@link ITooltip}, typically {@link Widget}s
     */
    default void handleTooltip(@NotNull ITooltip<?> tooltip) {
        tooltip.addTooltipLine(getKey());
    }

    /**
     * Handle adding tooltip line(s) to an {@link ITooltip}, typically {@link Widget}s, depending on a string key.
     */
    default void handleTooltip(@NotNull ITooltip<?> tooltip, @NotNull String key) {
        tooltip.addTooltipLine(getKey(key));
    }
}
