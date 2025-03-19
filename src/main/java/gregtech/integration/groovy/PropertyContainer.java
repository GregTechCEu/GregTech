package gregtech.integration.groovy;

import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.PostMaterialEvent;

import net.minecraftforge.fml.common.eventhandler.EventPriority;

import com.cleanroommc.groovyscript.GroovyScript;
import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;
import com.cleanroommc.groovyscript.event.EventBusType;
import com.cleanroommc.groovyscript.event.GroovyEventManager;
import com.cleanroommc.groovyscript.sandbox.ClosureHelper;
import com.cleanroommc.groovyscript.sandbox.LoadStage;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

public class PropertyContainer extends GroovyPropertyContainer {

    public void materialEvent(EventPriority priority, @DelegatesTo(MaterialEvent.class) Closure<?> eventListener) {
        if (GroovyScriptModule.isCurrentlyRunning() &&
                GroovyScript.getSandbox().getCurrentLoader() != LoadStage.PRE_INIT) {
            GroovyLog.get().error("GregTech's material event can only be used in pre init!");
            return;
        }
        ClosureHelper.withEnvironment(eventListener, new MaterialEvent(), true);
        GroovyEventManager.INSTANCE.listen(priority, EventBusType.MAIN, MaterialEvent.class, eventListener);
    }

    public void materialEvent(Closure<?> eventListener) {
        materialEvent(EventPriority.NORMAL, eventListener);
    }

    public void lateMaterialEvent(EventPriority priority, Closure<?> eventListener) {
        if (GroovyScriptModule.isCurrentlyRunning() &&
                GroovyScript.getSandbox().getCurrentLoader() != LoadStage.PRE_INIT) {
            GroovyLog.get().error("GregTech's material event can only be used in pre init!");
            return;
        }
        GroovyEventManager.INSTANCE.listen(priority, EventBusType.MAIN, PostMaterialEvent.class,
                eventListener);
    }

    public void lateMaterialEvent(Closure<?> eventListener) {
        lateMaterialEvent(EventPriority.NORMAL, eventListener);
    }
}
