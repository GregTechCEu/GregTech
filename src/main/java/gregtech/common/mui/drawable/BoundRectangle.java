package gregtech.common.mui.drawable;

import com.cleanroommc.modularui.drawable.Circle;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.widget.AbstractScrollWidget;
import com.cleanroommc.modularui.widget.scroll.HorizontalScrollData;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

public class BoundRectangle extends Rectangle {

    private Circle circle;
    private Ring ring;
    private final List<Pair<IntConsumer, BooleanSupplier>> colorSetters = new ArrayList<>();

    @Override
    public BoundRectangle setColor(int color) {
        getCircle().setColor(color, color);
        getRing().setColor(color);
        super.setColor(color);
        if (colorSetters != null) {
            for (int i = 0; i < colorSetters.size(); i++) {
                var setterPair = colorSetters.get(i);
                if (setterPair.getRight().getAsBoolean()) {
                    colorSetters.remove(i);
                    i--;
                    continue;
                }
                setterPair.getLeft().accept(color);
            }
        }
        return this;
    }

    public Circle getCircle() {
        // circle needs to be initialized during the super constructor somehow, and this is the simplest way
        return circle == null ? circle = new Circle() : circle;
    }

    public Ring getRing() {
        return ring == null ? ring = new Ring() : ring;
    }

    public BoundRectangle bind(IntConsumer colorSetter, BooleanSupplier removeCondition) {
        colorSetters.add(Pair.of(colorSetter, removeCondition));
        colorSetter.accept(this.getColor());
        return this;
    }

    public BoundRectangle bindScrollFG(AbstractScrollWidget<?, ?> widget) {
        HorizontalScrollData scrollX = widget.getScrollArea().getScrollX();
        VerticalScrollData scrollY = widget.getScrollArea().getScrollY();
        if (scrollX != null) {
            scrollX.texture(this);
        }
        if (scrollY != null) {
            scrollY.texture(this);
        }
        return this;
    }

    public BoundRectangle bindScrollBG(AbstractScrollWidget<?, ?> widget) {
        return bind(widget.getScrollArea()::setScrollBarBackgroundColor, () -> !widget.isValid());
    }

    public void gc() {
        colorSetters.removeIf((e) -> e.getRight().getAsBoolean());
    }
}
