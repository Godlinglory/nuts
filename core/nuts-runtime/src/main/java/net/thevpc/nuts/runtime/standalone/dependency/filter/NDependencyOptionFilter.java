package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;

public class NDependencyOptionFilter extends AbstractDependencyFilter{

    private final Boolean optional;

    public NDependencyOptionFilter(NSession session, Boolean optional) {
        super(session, NFilterOp.CUSTOM);
        this.optional = optional;
    }

    @Override
    public boolean acceptDependency(NId from, NDependency dependency, NSession session) {
        if (optional == null) {
            return false;
        }
//        String o = dependency.getOptional();
//        if (o == null) {
//            o = "";
//        }
//        o = o.trim().toLowerCase();
//        return optional == (o.isEmpty() || o.equals("true"));
        return optional == dependency.isOptional();
    }

    @Override
    public NDependencyFilter simplify() {
        if (optional == null) {
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        if(optional==null){
            return "any optional";
        }else{
            return optional?"optional":"not(optional)";
        }
    }
    
}
