package gregtech.api.recipes.lookup.flag;

public interface IApplicatorMap<T> {

    void applyFlags(FlagMap flags, T context);
}
