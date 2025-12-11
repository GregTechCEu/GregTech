package gregtech.common.covers.filter.readers;

import gregtech.api.util.oreglob.OreGlob;
import gregtech.api.util.oreglob.OreGlobCompileResult;
import gregtech.common.covers.filter.oreglob.impl.ImpossibleOreGlob;

import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

public class OreDictFilterReader extends SimpleItemFilterReader {

    private static final String EXPRESSION = "OreDictionaryFilter";
    private static final String CASE_SENSITIVE = "caseSensitive";
    private static final String MATCH_ALL = "matchAll";

    private OreGlob glob = ImpossibleOreGlob.getInstance();
    private OreGlobCompileResult result;

    public OreDictFilterReader() {
        super(0);
    }

    public void setExpression(String expression) {
        if (getStackTag().getString(EXPRESSION).equals(expression))
            return;

        getStackTag().setString(EXPRESSION, expression);
        recompile();
        markDirty();
    }

    public String getExpression() {
        return getStackTag().getString(EXPRESSION);
    }

    public void setCaseSensitive(boolean caseSensitive) {
        if (isCaseSensitive() == caseSensitive)
            return;

        if (!caseSensitive)
            getStackTag().setBoolean(CASE_SENSITIVE, false);
        else
            getStackTag().removeTag(CASE_SENSITIVE);
        recompile();
        markDirty();
    }

    public boolean isCaseSensitive() {
        return !getStackTag().hasKey(CASE_SENSITIVE);
    }

    public void setMatchAll(boolean matchAll) {
        if (shouldMatchAll() == matchAll)
            return;

        if (!matchAll)
            getStackTag().setBoolean(MATCH_ALL, false);
        else
            getStackTag().removeTag(MATCH_ALL);

        markDirty();
    }

    /**
     * {@code false} requires any of the entry to be match in order for the match to be success, {@code true}
     * requires
     * all entries to match
     */
    public boolean shouldMatchAll() {
        return !getStackTag().hasKey(MATCH_ALL);
    }

    @NotNull
    public OreGlob getGlob() {
        return this.glob;
    }

    public OreGlobCompileResult getResult() {
        return this.result;
    }

    public void recompile() {
        String expr = getExpression();
        if (!expr.isEmpty()) {
            result = OreGlob.compile(expr, !isCaseSensitive());
            this.glob = result.getInstance();
        } else {
            this.glob = ImpossibleOreGlob.getInstance();
            result = null;
        }
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);

        if (nbt.hasKey(EXPRESSION))
            this.setExpression(nbt.getString(EXPRESSION));

        if (nbt.hasKey(CASE_SENSITIVE))
            this.setCaseSensitive(nbt.getBoolean(CASE_SENSITIVE));

        if (nbt.hasKey(MATCH_ALL))
            this.setMatchAll(nbt.getBoolean(MATCH_ALL));
    }

    @Override
    public void handleLegacyNBT(NBTTagCompound tag) {
        super.handleLegacyNBT(tag);

        var legacyFilter = tag.getCompoundTag(KEY_LEGACY_FILTER);
        if (legacyFilter.hasKey(EXPRESSION))
            this.setExpression(legacyFilter.getString(EXPRESSION));

        if (legacyFilter.hasKey(CASE_SENSITIVE))
            this.setCaseSensitive(legacyFilter.getBoolean(CASE_SENSITIVE));

        if (legacyFilter.hasKey(MATCH_ALL))
            this.setMatchAll(legacyFilter.getBoolean(MATCH_ALL));
    }
}
