/*  =================== File Information =================
    File Name: buildCFG.java
    Description:
    Author: Michael Shah (Michael.Shah@tufts.edu)
    Last Modified: 1/21/15

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

/*  ============== buildCFG ==============
	Purpose: 	Useful for building a Control Flow Graph
	Notes:
	Example usage: 
	1.)
	==================================== */ 
public class buildCFG{

    Unit source;
    Unit target; // (deprecated, needs to be removed)
    List<Unit> targets;


    /*!
        Empty constructor
        Used so that I can write a copy constructor
    */
    public buildCFG(){

    }

	/*	Name: buildCFG
		Description: 
		Purpose:
	*/
    // Builds a Control Flow Graph (CFG) for a critical section.
	// It will start building from the method provided.	
    //
    public void buildCFG(SootMethod methodbody){
        UnitGraph g = new ExceptionalUnitGraph(methodbody.getActiveBody());
        G.v().out.println("BUILDING CONTROL FLOW GRAPH FOR METHOD"+methodbody.toString());
    }


    /*! Name: buildCFG
        Description: The buildCFG is the default constructor.
        Purpose:    The buildCFG is used for constructing a call Graph.
                    A source represents a node, and then the target is the 
                    node we will connect to.
    */
    public buildCFG(Unit source, Unit target){
        this.source = source;

        this.target = target;

        // If no targets have been filled in, create the list
        // and add the first element into it.
        if(targets==null){
            targets = new LinkedList<Unit>();
            targets.add(target);
        }
    }

    /*!
        Used to get the source edge (head)
    */
    public Unit getSource(){  return source;  }

    /*!
        Used to get a specific destination edge(tail)
    */
    public Unit getTargetAt(int i){   return targets.get(i);  }

    /*!
        (Deprecated) Used to get a first destination edge(tail)
    */
    public Unit getTarget(){  return target;  }


    /*!
        Pass in a source and a target and if it matches
        the buildCFGs source and target, then return true.
    */
    public boolean edgeExist(Unit _source, Unit _target){
        // Or the single (deprecated) target
        if(_source.equals(source) && _target.equals(target)){
            return true;
        }

        // If the source can get to any of the targets
        for(int i =0; i < targets.size(); i++){
            if(_source.equals(source) && _target.equals(targets.get(i))){
                return true;
            }
        }

        return false;
    }

    /*!
        Add an element to our adjacency list
    */
    public void addTarget(Unit _target){
        //if(!targets.contains(_target)){
            targets.add(_target);
        //}
    }

    /*!
        Returns all of the targets
    */
    public List<Unit> getTargets(){
        return targets;
    }

    public buildCFG getCopy(){
        buildCFG copy = new buildCFG();
        copy.source = this.source;
        copy.target = this.target;

        for(int i = 0; i < this.targets.size(); i++){
            copy.targets.add(this.targets.get(i));
        }

        return copy;
    }

    /*!
        Prints out all of the targets we can reach from our source.
    */
    public String printTargets(){
        String output = "["+source.toString()+"]-->";
        for(int i =0; i < targets.size(); i++){
            output += targets.get(i).toString();
            if(i<targets.size()-1){
                output += "->";
            }
        }
        return output;
    }

}



