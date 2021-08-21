package gregtech.loaders.recipe.component;

import gregtech.api.util.GTLog;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

public class AnnotatedComponentHandlerLoader {

    public static void discoverAndLoadAnnotatedComponentHandlers(ASMDataTable asmDataTable) {
        Set<ASMData> annotations = asmDataTable.getAll(IComponentHandler.RegisterComponentHandler.class.getName());
        int componentHandlersRegistered = 0;
        for (ASMData annotationData :annotations) {
            String componentHandlerClassName = annotationData.getClassName();
            Class<?> componentHandlerClass;
            try {
                componentHandlerClass = Class.forName(componentHandlerClassName);
            } catch (ClassNotFoundException e) {
                GTLog.logger.error("Failed to load annotated component handler class {}. FML annotation data is broken perhaps?", componentHandlerClassName);
                continue;
            }
            String denyReason = null;
            Throwable loadingError = null;

            Field instanceField = null;
            Constructor<?> constructor = null;

            if (!IComponentHandler.class.isAssignableFrom(componentHandlerClass)) {
                denyReason = "class does not implement IComponentHandler";
            } else if (Modifier.isAbstract(componentHandlerClass.getModifiers())) {
                denyReason = "class is abstract and cannot be initialized";
            }
            try {
                instanceField = componentHandlerClass.getField("INSTANCE");
                if (instanceField.getType() != componentHandlerClass) {
                    denyReason = "found INSTANCE field, but it's type is not the same as class type";
                } else if (!Modifier.isStatic(instanceField.getModifiers())) {
                    denyReason = "found INSTANCE field, but it's not static and cannot be used";
                }
            } catch (NoSuchFieldException noSuchField) {
                try {
                    constructor = componentHandlerClass.getConstructor();
                } catch (NoSuchMethodException noSuchMethod) {
                    denyReason = "didn't found public static final INSTANCE field or public no-arg constructor for initialization";
                }
            }

            IComponentHandler componentHandler = null;
            if (denyReason == null) {
                if (instanceField != null) {
                    try {
                        componentHandler = (IComponentHandler) instanceField.get(null);
                    } catch (ReflectiveOperationException e) {
                        denyReason = "failed to retrieve INSTANCE field value";
                        loadingError = e;
                    }
                } else {
                    try {
                        componentHandler = (IComponentHandler) constructor.newInstance();
                    } catch (ReflectiveOperationException e) {
                        denyReason = "failed to initialize component handler";
                        loadingError = e;
                    }
                }
            }
            if (denyReason == null) {
                GTLog.logger.info("Registered component handler {}", componentHandler.getClass().getName());
                IComponentHandler.registerComponentHandler(componentHandler);
                componentHandlersRegistered++;
            } else {
                GTLog.logger.error("Failed to load material handler class {} from {}: {}",
                        componentHandlerClassName, annotationData.getCandidate().getModContainer().getName(), denyReason, loadingError);
                GTLog.logger.error("Consult the mod author for a fix on their side.");
            }
        }
        GTLog.logger.info("{} annotated component handlers registered", componentHandlersRegistered);
    }
}
