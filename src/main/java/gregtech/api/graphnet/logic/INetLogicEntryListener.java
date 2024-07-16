package gregtech.api.graphnet.logic;

public interface INetLogicEntryListener {

    void markLogicEntryAsUpdated(INetLogicEntry<?, ?> entry, boolean fullChange);
}
