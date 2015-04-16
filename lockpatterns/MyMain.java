/*  =================== File Information =================
    File Name: MyMain.java
    Description:
    Author: Michael Shah (Michael.Shah@tufts.edu)
    Original Author: Sam Guyer
    Last Modified: 3/06/14

    Purpose: 
    ===================================================== */

package lockpatterns;

// Import Java libraries
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

/*  ============== MyMain ==============
    Purpose: 
    Notes:
    Example usage: 
    1.)
    ==================================== */ 
/** Main Class
*
*   Entry point of our program. In order to keep this project clean
*   the main method will try to only have instances of ProjectController in it, where 
*   the bulk of the work is done for differnt analysis.
*   
*   It should be noted that the name of the Main class, called 'MyMain' has significance
*   when it is compiled using the soot library.
*/
public class MyMain
{
        
     public static ProjectController instance1;

	 public static void main(String[] args) {

        // Create a new project, that will be analyzed, and provide arguments and a directory to dump results.
        instance1 = new ProjectController(args,"/Users/michaelshah/Documents/sootDump/");
        instance1.run_static(); // Run a static analysis on our project.

    }
}
