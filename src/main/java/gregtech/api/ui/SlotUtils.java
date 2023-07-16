package gregtech.api.ui;

import com.cleanroommc.modularui.widgets.FluidSlot;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public final class SlotUtils {

    private SlotUtils() {}

    public static @NotNull SlotGroupWidget.Builder itemGroup(int slots, int rowLength) {
        return group(slots, rowLength, 'I').key('I', i -> new ItemSlot());
    }

    public static @NotNull SlotGroupWidget.Builder fluidGroup(int slots, int rowLength) {
        return group(slots, rowLength, 'F').key('F', i -> new FluidSlot());
    }

    private static @NotNull SlotGroupWidget.Builder group(int slots, int rowLength, char c) {
        Preconditions.checkArgument(slots > 0, "Slots must be > 0");
        Preconditions.checkArgument(rowLength > 0, "Row Length must be > 0");

        SlotGroupWidget.Builder builder = SlotGroupWidget.builder();

        String row = buildString(rowLength, c);

        for (int i = 0; i < slots / rowLength; i++) {
            builder.row(row);
        }

        int extra = slots % rowLength;
        if (extra != 0) {
            builder.row(buildString(extra, c));
        }

        return builder;
    }

    private static @NotNull String buildString(int length, char c) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(c);
        }
        return builder.toString();
    }
}
