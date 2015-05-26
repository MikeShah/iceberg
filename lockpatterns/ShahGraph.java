/*  =================== File Information =================
    File Name: ShahGraph.java
    Description:
    Author: Michael Shah (Michael.Shah@tufts.edu)
    Last Modified: 5/21/15

    Purpose: 
    ===================================================== */

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

// Be able to set some of the command line options from within our source.
import soot.options.Options;
// Be able to Tag objects
import soot.tagkit.*;

// Be able to output a DOT file (specifically for the callgraph
import soot.util.dot.*;
import soot.util.*;

/*!  ==============  ShahGraph==============
	Purpose: 	Build a Graph data structure
	Notes:
	Example usage: 
	1.)
	==================================== */ 
class ShahNode{
	String nodeName;           // Name of this node.
    List<String> neighbors;    // Name of all of the nodes we can point to (connected directed edges from this src pointing to dest)
    
}	
	
/*!
*/	
public class  ShahGraph{
    int order_Vertices; // Number of vertices in the graph
    int order_Edges;    // Number of edges in the graph
    
    /*!
        Empty constructor
    */
    public ShahGraph(){

    }
    
    void addVertex(){
        order_Vertices++;
    }
    
    void addEdge(){
        order_Edges++;
    }

}



