package lockpatterns;


import java.util.*;
import java.io.*;

// Import all of the libraries from the soot library.
import soot.*;
import soot.util.Chain;
import soot.jimple.*;
import soot.jimple.toolkits.pointer.*;
import soot.jimple.toolkits.thread.ThreadLocalObjectsAnalysis;
import soot.jimple.toolkits.thread.synchronization.*;
import soot.jimple.toolkits.thread.mhp.MhpTester;
import soot.jimple.toolkits.thread.mhp.SynchObliviousMhpAnalysis;
import soot.jimple.toolkits.callgraph.*;
import soot.jimple.toolkits.infoflow.*;
import soot.jimple.spark.pag.*;
import soot.jimple.spark.sets.*;
import soot.toolkits.scalar.*;
import soot.toolkits.graph.*;

import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.BackwardFlowAnalysis;
// Be able to set some of the command line options from within our source.
import soot.options.Options;
// Be able to Tag objects
import soot.tagkit.*;

// Be able to output a DOT file (specifically for the callgraph
import soot.util.dot.*;
import soot.util.*;

// Change to extend ForwardFlowAnalysis or others as appropriate
public class IceBergFlowAnalysis extends BackwardFlowAnalysis {
    public IceBergFlowAnalysis(DirectedGraph g) {
        super(g);
        // some other initializations
        doAnalysis();
    }

    @Override
    protected void merge(Object in1, Object in2, Object out) {
        // must analysis => out <- in1 union in2
        // may analysis => out <- in1 intersection in2
    }

    @Override
    protected void copy(Object source, Object dest) {
        // copy from source to dest
    }

    @Override
    protected Object newInitialFlow() {
        // return e.g., the empty set
        return null;
    }

    @Override
    protected Object entryInitialFlow() {
        // return e.g., the empty set
        return null;
    }
    
    @Override
    protected void flowThrough(Object in, Object node, Object out) {
        // perform flow from in to out, through node
    }
}
