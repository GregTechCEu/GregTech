package gregtech.api.modules;

public enum ModuleStage {
    C_SETUP,         // Initializing Module Containers
    M_SETUP,         // Initializing Modules
    PRE_INIT,        // MC PreInitialization stage
    INIT,            // MC Initialization stage
    POST_INIT,       // MC PostInitialization stage
    FINISHED,        // MC LoadComplete stage
    SERVER_STARTING, // MC ServerStarting stage
    SERVER_STARTED   // MC ServerStarted stage
}
