package gregtech.api.recipes.ui;

import org.jetbrains.annotations.Nullable;

public interface JEIDisplayControl {

    /**
     * Can provide a string that will be displayed beneath an item or fluid in JEI
     *
     * @param index the index of the output
     * @return the string to be displayed.
     */
    default @Nullable String addSmallDisplay(int index) {
        return null;
    }

    /**
     * Can provide a string that will be displayed in the tooltip for an item or fluid in JEI
     *
     * @param index the index of the rolled input/output
     * @return the string to be displayed.
     */
    default @Nullable String addTooltip(int index) {
        return null;
    }
}
