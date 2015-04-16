/*  =================== File Information =================
    File Name: edgeList.java
    Description:
    Author: Michael Shah (Michael.Shah@tufts.edu)
    Last Modified: 3/06/14

    Purpose: 
    ===================================================== */
package lockpatterns;

// Import Java libraries
import java.util.*;
import java.io.*;

// Make use of SootMethod
import soot.*;

/*!  ============== edgeList ==============
	Purpose: 	Useful for building a callgraph that takes in strings
				as a parameter for the source and target (such as the graphviz format).

				Foundation structure for building an adjacency list.
	Notes:
	Example usage: 
	1.)
	==================================== */ 
public class edgeList{
	SootMethod source;
	SootMethod target; // (deprecated, needs to be removed)
	List<SootMethod> targets;

	
	/*!
		Empty constructor
		Used so that I can write a copy constructor
	*/
	public edgeList(){

	}

	/*!	Name: edgeList
		Description: The edgeList is the default constructor.
		Purpose:	The edgeList is used for constructing a call Graph.
				 	A source represents a node, and then the target is the 
				 	node we will connect to.
	*/
	public edgeList(SootMethod source, SootMethod target){
		this.source = source;

		this.target = target;

		// If no targets have been filled in, create the list
		// and add the first element into it.
		if(targets==null){
			targets = new LinkedList<SootMethod>();
			targets.add(target);
		}
	}

	/*!
		Used to get the source edge (head)
	*/
	public SootMethod getSource(){	return source;	}

	/*!
		Used to get a specific destination edge(tail)
	*/
	public SootMethod getTargetAt(int i){	return targets.get(i);	}

	/*!
		(Deprecated) Used to get a first destination edge(tail)
	*/
	public SootMethod getTarget(){	return target;	}


	/*!
		Pass in a source and a target and if it matches
		the edgeLists source and target, then return true.
	*/
	public boolean edgeExist(SootMethod _source, SootMethod _target){
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
	public void addTarget(SootMethod _target){
		//if(!targets.contains(_target)){
			targets.add(_target);
		//}
	}

	/*!
		Returns all of the targets
	*/
	public List<SootMethod> getTargets(){
		return targets;
	}

	public edgeList getCopy(){
		edgeList copy = new edgeList();
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
		String output = "["+source.getName()+"]-->";
		for(int i =0; i < targets.size(); i++){
			output += targets.get(i).getName();
			if(i<targets.size()-1){
				output += "->";
			}
		}
		return output;
	}

}