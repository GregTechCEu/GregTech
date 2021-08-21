package gregtech.loaders.recipe.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

public interface IComponentHandler {

    List<IComponentHandler> componentHandlers = new ArrayList<>();

    static void registerComponentHandler(IComponentHandler componentHandler) {
        componentHandlers.add(componentHandler);
    }

    static void runComponentHandlers() {
        componentHandlers.forEach(IComponentHandler::onComponentsInit);
    }

    void onComponentsInit();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface RegisterComponentHandler {
    }
}
