package gregtech.api.graphnet.logic;

public interface INetLogicEntryListener {

    void markLogicEntryAsUpdated(NetLogicEntry<?, ?> entry, boolean fullChange);
}
