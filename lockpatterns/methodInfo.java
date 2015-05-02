/*  =================== File Information =================
    File Name: methodInfo.java
    Description:
    Author: Michael Shah (Michael.Shah@tufts.edu)
    Last Modified: 1/20/15

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
// CFG imports
import soot.util.cfgcmd.*;
import soot.coffi.*;


/*!  ============== methodInfo ==============
	Purpose: 	Used to store information about every method.
				Can be used as a metric for quickly finding critical section with
				lots of methods.
	Notes:
	Example usage: 
	1.)
	==================================== */ 
public class methodInfo{
	String time;
	int calls; // Number of times this method is called within a critical section.
	SootMethod method;

	/*!	Name: methodInfo
		Description: The methodInfo is the default constructor.
		Purpose:	The methodInfo is used for constructing a call Graph.
				 	A source represents a node, and then the target is the 
				 	node we will connect to.
	*/
	public methodInfo(SootMethod _method){
		method = _method;
		calls = 0;
		
		// Automatically generate a control flow graph
		G.v().out.println("Generating Callgraph: "+method.getName());
		
		this.buildControlFlowGraph("/Users/michaelshah/Documents/sootDump/sunflow/functions/"+method.toString());
	}

	/*! If we find the method again, we increment the number of times it is called
	 within a critical section.
	*/
	public synchronized void incrementCalls(){
		calls += 1;
	}

	/*!
		getCallCount
	*/
	public synchronized int getCallCount(){
		return calls;
	}


	/*!
		getCallCount
	*/
	void setLongestPath(int pathLength){

	}

	/*!
		getCallCount
	*/
	void setShortestPath(int pathLength){
		
	}

	/*!
		Build Control Flow Graph
	*/
    public void buildControlFlowGraph(String basePath){

		try{
		 boolean success = ( new File(basePath).mkdirs());
		}
		catch(Exception e){
		 // Output error here
		}

        CFGToDotGraph d = new CFGToDotGraph();
        UnitGraph eug = new BriefUnitGraph(method.getActiveBody());
        DotGraph outputGraph;
        DotGraph cfgOutput;
        outputGraph = d.drawCFG(eug,method.getActiveBody());
        outputGraph.plot		(basePath+"/_cfg_generated_from_soot.dot");
        cfgOutput = new DotGraph(basePath+"/_cfg_manual.dot");


            // Build an actual data structure that we can manually inspect.
            // The above outputGraph is what we will check against to make sure ours is correct.
            List<Unit> heads = eug.getHeads();



            Queue<Unit> cfgUnitQueue = new LinkedList<Unit>();
            Unit firstHead = eug.getHeads().get(0);
            // Create a default head node
            DotGraphNode nodeHead = cfgOutput.getNode(firstHead.toString());
            nodeHead.setShape("house");

            cfgUnitQueue.add( firstHead );  // Get the first head and put it in the queue
                                                        // This makes the assumption that there is only
                                                        // one head for now.
            List<buildCFG> edges = new ArrayList<buildCFG>();

            while(!cfgUnitQueue.isEmpty()){
                Unit nextUnit = cfgUnitQueue.remove();

                List<Unit> nodes = eug.getSuccsOf(nextUnit);                
                // Check if the edge exists in our graph, and then
                // add it if it doesn't.
                for(int i =0; i < nodes.size(); i++){


                    boolean addCFGEdge = true;      // Perform Breadth First Search to build a CFG

                    for(int j = 0; j < edges.size(); j++){
                        if(edges.get(j).edgeExist(nextUnit,nodes.get(i)) || edges.get(j).edgeExist(nextUnit,nextUnit) ) {
                            addCFGEdge = false;
                        }
                    }
        
                   if(addCFGEdge){
	                   // Add nodes to the queue
                    	cfgUnitQueue.add(nodes.get(i)); 
                    	// Draw edges
                    	buildCFG temp = new buildCFG(nextUnit,nodes.get(i));
					   // Add the edge because we have yet to discover it.
                        edges.add(temp);
                        DotGraphNode node1 = cfgOutput.getNode(nodes.get(i).toString());
                        DotGraphEdge myEdge = cfgOutput.drawEdge(nextUnit.toString(),nodes.get(i).toString());

						node1.setShape("box"); 
							String 	node1Style =  "<<TABLE>";
									node1Style += "<TR><TD>";
									node1Style += nodes.get(i).toString();
									node1Style += "</TD></TR><TR><TD>";
									node1Style += "Something here";
									node1Style += "</TD></TR></TABLE>>";
                        node1.setHTMLLabel(node1Style);

						// Edit the edge
						DotGraphNode srcNode = cfgOutput.getNode(nextUnit.toString());
                        myEdge.setLabel("-1-");

                    }
                }

            }

        cfgOutput.plot(basePath+"/_cfg_manual.dot");

    }   // End of function

	/*!
		 Outputs all information about the method to a string.
	*/
	public String output(){
		return "{\"methodInfo\":[" +			
					"{\"name\":\""+method.getName()+"\"}," 	+
					"{\"time\":\"n/a\"},"		+	
					"{\"calls\":"+calls+"}]"	+
				"}\"";
	}

}