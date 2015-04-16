/*  =================== File Information =================
    File Name: methodInfo.java
    Description:
    Author: Michael Shah (Michael.Shah@tufts.edu)
    Last Modified: 1/20/15

    Purpose: 
    ===================================================== */
package lockpatterns;


/*  ============== methodInfo ==============
	Purpose: 	Used to store information about every method.
				Can be used as a metric for quickly finding critical section with
				lots of methods.
	Notes:
	Example usage: 
	1.)
	==================================== */ 
public class methodInfo{
	public String name;
	String time;
	int calls; // Number of times this method is called within a critical section.

	/*	Name: edgeList
		Description: The edgeList is the default constructor.
		Purpose:	The edgeList is used for constructing a call Graph.
				 	A source represents a node, and then the target is the 
				 	node we will connect to.
	*/
	public methodInfo(String _name){
		name = _name;
		calls = 0;
	}

	// If we find the method again, we increment the number of times it is called
	// within a critical section.
	public void incrementCalls(){
		calls = calls + 1;
	}

	// Outputs all information about the method to a string.
	public String output(){
		return "{\"methodInfo\":[" +			
					"{\"name\":\""+name+"\"}," 	+
					"{\"time\":\"n/a\"},"		+	
					"{\"calls\":"+calls+"}]"	+
				"}\"";
	}

}