package gregtech.api.recipes.tree.flag;

public interface IApplicatorMap<T> {

    void applyFlags(FlagMap flags, T context);
}
