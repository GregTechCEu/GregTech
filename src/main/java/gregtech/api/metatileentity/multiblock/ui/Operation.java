package gregtech.api.metatileentity.multiblock.ui;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@FunctionalInterface
public interface Operation extends Consumer<IRichTextBuilder<?>> {

    Operation NO_OP = richText -> {};

    static Operation addLine(IDrawable drawable) {
        return richText -> richText.addLine(drawable);
    }

    static Operation add(IDrawable drawable) {
        return richText -> richText.add(drawable);
    }

    static Operation addLineSpace(IDrawable drawable) {
        return addLine(drawable).spaceLine(1);
    }

    @Override
    void accept(IRichTextBuilder<?> richText);

    @NotNull
    default Operation spaceLine(int space) {
        return richText -> {
            this.accept(richText);
            richText.spaceLine(space);
        };
    }

    @NotNull
    default Operation newLine() {
        return richText -> {
            this.accept(richText);
            richText.newLine();
        };
    }
}
