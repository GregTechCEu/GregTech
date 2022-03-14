package gregtech.api.items.toolitem;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.util.SoundEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class ToolBuilder<T extends IGTTool> {

    protected final String domain, id;

    protected final Set<String> toolClasses = new ObjectArraySet<>();
    protected final List<String> oreDicts = new ArrayList<>();

    protected int tier = -1;
    protected IGTToolDefinition toolStats;
    protected SoundEvent sound;
    protected Character symbol = null;

    public ToolBuilder(String domain, String id) {
        this.domain = domain;
        this.id = id;
    }

    public ToolBuilder<T> electric(int tier) {
        this.tier = tier;
        return this;
    }

    public ToolBuilder<T> toolStats(IGTToolDefinition toolStats) {
        this.toolStats = toolStats;
        return this;
    }

    public ToolBuilder<T> toolStats(UnaryOperator<ToolDefinitionBuilder> builder) {
        this.toolStats = builder.apply(new ToolDefinitionBuilder()).build();
        return this;
    }

    public ToolBuilder<T> sound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    public ToolBuilder<T> toolClasses(String... tools) {
        Collections.addAll(toolClasses, tools);
        return this;
    }

    public ToolBuilder<T> oreDicts(String... oreDicts) {
        Collections.addAll(this.oreDicts, oreDicts);
        return this;
    }

    public ToolBuilder<T> symbol(char symbol) {
        this.symbol = symbol;
        return this;
    }

    public abstract Supplier<T> supply();

    public T build() {
        IGTTool existing = ToolHelper.getToolFromSymbol(this.symbol);
        if (existing != null) {
            throw new IllegalArgumentException(String.format("Symbol %s has been taken by %s already!", symbol, existing));
        }
        T supplied = supply().get();
        ToolHelper.registerToolSymbol(this.symbol, supplied);
        return supplied;
    }

}
