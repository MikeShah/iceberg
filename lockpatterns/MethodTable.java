/*  =================== File Information =================
    File Name: MethodTable.java
    Description:
    Author: Michael Shah (Michael.Shah@tufts.edu)
    Original Author: Michael Shah
    Last Modified: 4/27/15

    Purpose: 

    	Holds information about every method in the program.
        Used like a symbol table
        Useful for computing longest path throughout a critical section
    ===================================================== 
*/

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

/*! \class MethodTable
	\brief MethodTable class, basically a hashtable for methods

*/

class MethodTable{

    // Stores stuff
    Hashtable functionNames;


    MethodTable(){
        // Create a hashtable of size 100
        // Note that we are using a hashtable, because it is synchronized.
        // Default load factor is 0.75.
        functionNames = new Hashtable(100);
    }

    /*! Adds a new method name
        Method name is fully qualified (package names need to be included, so it is unique)
    */
    synchronized void add(SootMethod fullFunctionName){
        if(!functionNames.containsKey(fullFunctionName.toString())){
            functionNames.put(fullFunctionName.toString(),new methodInfo(fullFunctionName));
        }
        else{
            // Update the values in the hash table by overwritting an old key
            methodInfo m = (methodInfo)functionNames.get(fullFunctionName.toString());
            m.incrementCalls();
            functionNames.put(fullFunctionName.toString(),m); 
        }
    }

    /*! 
        Pass in a SootMethod to see if it exists
    */
    synchronized boolean methodExists(SootMethod fullFunctionName){
        return functionNames.contains(fullFunctionName.toString());
    }


    /*! 
        Compute longest path lengths of all the methods
    */
    void computeLongestPaths(){
        Enumeration getAllNames = functionNames.keys();
        String str = "";
        int count = 1;
        G.v().out.println("====== Computing Longest Path of functions =====");
        G.v().out.println("Number of functions: "+functionNames.size());
        while(getAllNames.hasMoreElements()){
            String name = (String)getAllNames.nextElement();
            // G.v().out.println("Computing shortest path for: "+name);
        }
    }

    /*! 
        Compute longest path lengths of all the methods
    */
    void computeShortestPaths(){
        Enumeration getAllNames = functionNames.keys();
        String str = "";
        int count = 1;
        G.v().out.println("====== Computing Shortest Path of functions =====");
        G.v().out.println("Number of functions: "+functionNames.size());
        while(getAllNames.hasMoreElements()){
            String name = (String)getAllNames.nextElement();
            // G.v().out.println("Computing shortest path for: "+name);
        }
    }


    /*!
            Prints all methods out that exist in the project.

            @param: min is the minimum number of times a function needs
                    to be called in order to be printed out. (-1 prints out everything)
    */
    String print(int min){
        Enumeration getAllNames = functionNames.keys();
        String str = "";
        int count = 1;
        while(getAllNames.hasMoreElements()){
            // Get the next element and iterate
            String name = (String)getAllNames.nextElement();
            methodInfo m = (methodInfo)functionNames.get(name);
            // Print out the statement
            if(m.getCallCount()>min){
                str += count+".)\t Called: "+m.getCallCount()+" \t"+ name + "\n";
            }
            count++;
        }
        return str;
    }



}