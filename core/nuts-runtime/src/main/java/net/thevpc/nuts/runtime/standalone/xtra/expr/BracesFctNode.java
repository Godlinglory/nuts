package net.thevpc.nuts.runtime.standalone.xtra.expr;

import net.thevpc.nuts.util.*;

import java.util.List;

public class BracesFctNode extends AbstractOp {
    public BracesFctNode() {
        super("{", NutsExprOpPrecedence.BRACES, NutsExprOpAssociativity.LEFT, NutsExprOpType.POSTFIX);
    }

    @Override
    public Object eval(String name, List<NutsExprNode> args, NutsExprDeclarations e) {
        throw new IllegalArgumentException("unable to evaluate");
    }
}
