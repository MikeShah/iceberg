/*  =================== File Information =================
    File Name: CriticalSectionFeature.java
    Description:
    Author: Michael Shah (Michael.Shah@tufts.edu)
    Original Author: Michael Shah
    Last Modified: 4/21/15

    Purpose: 

    	Holds statistics about critical sections.
        Generally used within Critical Section Block
    ===================================================== 
*/

package lockpatterns;

// Import Java libraries
import java.util.*;
import java.io.*;

/*! \class IceBergApplication
	\brief IceBergApplication class
	
	Purpose is to be a static class where we can access
	data from anywhere. Usually this means configuration, or settings
	that effect the whole program.
	
	Generally this class is not instantiated by the user
	
	Note: Make sure lal additional member variables and mehods are
	static.

*/
public final class IceBergApplication
{
	public static String projectDirectory = "";
	
	/*!
		Constructor
	*/
	private IceBergApplication(){
		
	}
	
	/*!
	*/
	public static void setProjectOutputDirectory(String dir){
		projectDirectory = dir;
	}
}