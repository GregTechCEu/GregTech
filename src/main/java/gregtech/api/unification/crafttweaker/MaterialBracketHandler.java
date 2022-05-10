package gregtech.api.unification.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.BracketHandler;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.zenscript.IBracketHandler;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionString;
import stanhebben.zenscript.parser.Token;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.type.natives.IJavaMethod;

import java.util.List;

@BracketHandler
@ZenRegister
@SuppressWarnings("unused")
public class MaterialBracketHandler implements IBracketHandler {

    private final IJavaMethod method;

    public MaterialBracketHandler() {
        this.method = CraftTweakerAPI.getJavaMethod(MaterialBracketHandler.class, "getMaterial", String.class);
    }

    public static Material getMaterial(String name) {
        Material material = name == null ? null : GregTechAPI.MaterialRegistry.get(name);
        if (material == null) {
            CraftTweakerAPI.logError("Could not find material with name " + name);
        }
        return material;
    }

    @Override
    public IZenSymbol resolve(IEnvironmentGlobal environment, List<Token> tokens) {
        if ((tokens.size() < 3)) return null;
        if (!tokens.get(0).getValue().equalsIgnoreCase("material")) return null;
        if (!tokens.get(1).getValue().equals(":")) return null;
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 2; i < tokens.size(); i++) {
            nameBuilder.append(tokens.get(i).getValue());
        }
        return position -> new ExpressionCallStatic(position, environment, method,
                new ExpressionString(position, nameBuilder.toString()));
    }

}
