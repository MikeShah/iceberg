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

/*! \class CriticalSectionFeature
	\brief CriticalSectionFeature class

*/
public class CriticalSectionFeature
{
    public int numberOfStatements; // Records how many statements there are total in the body of this critical section
    public int numberOfMethods;    // Records how many of the statements are method calls (at the first level).  
                            // This is a conveinence function
    public int allocations;
    public int concreteMethodCount;    // Of the total 'numberOfMethods' how many are concrete
    public int constructorsMethodCount;    // how many are constructors
    public int entryMethodsCount;          // How many are entry methods
    public int javaLibraryMethodsCount;    // How many are java library functions in the first level
    public int totalJavaLibraryMethodsCount; // How many are java library functions outside of the first level
    public int privateMethodsCount;        // How many are private
    public int protectedMethodsCount;      // How many are protected
    public int publicMethodsCount;         // How many are public
    public int staticMethodsCount;         // How many are static
    public int synchronizedMethodsCount;   // How many are synchronized methods within this synchronized methods body

    public int totalMethods;   // Total methods called from the synchronized section, plus the method calls of the branches below.
    public int totalAllocations;  
    public int totalSynchronizedSections;
    public int totalStatementCount;
    /*! Name: CriticalSectionFeature
        Description: Default Constructor
        Purpose:    Default Constructor
    */
    public CriticalSectionFeature(){
        numberOfStatements = 0;
        numberOfMethods = 0;
                  
        concreteMethodCount = 0;
        constructorsMethodCount = 0;     
        entryMethodsCount = 0;           
        javaLibraryMethodsCount = 0;
        totalJavaLibraryMethodsCount = 0;     
        privateMethodsCount = 0;         
        protectedMethodsCount = 0;       
        publicMethodsCount = 0;          
        staticMethodsCount = 0;          
        synchronizedMethodsCount = 0;
        
        totalMethods = 0;
        totalAllocations = 0;
        totalSynchronizedSections = 0;
        totalStatementCount = 0;
    }


    /*! Name:       incrementNumberOfMethods
    // Description: Increments the features of a critical section feature
                  Should be called every time we add a new method to a 
                  critical section block.
    */
    public void incrementNumberOfMethods(CriticalSectionItem item){

        if(item.isAMethod==true){
            numberOfMethods++;
                if(item.isConcrete)     { concreteMethodCount++;        }
                if(item.isConstructor)  { constructorsMethodCount++; allocations++;}
                if(item.isEntryMethod)  { entryMethodsCount++;          }
                if(item.isJavaLibraryMethod) { javaLibraryMethodsCount++; }
                if(item.isPrivate)      { privateMethodsCount++;        }
                if(item.isProtected)    { protectedMethodsCount++;      }
                if(item.isPublic)       { publicMethodsCount++;         }
                if(item.isStatic)       { staticMethodsCount++;         }
                if(item.isSynchronized) { synchronizedMethodsCount++;   }
        }

        numberOfStatements++;
    }

    /*! Name:       getCriticalSecitonFeatureSummary
    // Description: Returns a summary of the critical section features as a string
    */
    public String getCriticalSecitonFeatureSummary(){

        return  "\n\nMethods called: " + numberOfMethods + "\n" + 
                "Constructors called: " + constructorsMethodCount + "\n" + 
                "Entry Methods called: " + entryMethodsCount + "\n" + 
                "Java Library Methods called: " + javaLibraryMethodsCount + "\n" + 
                "Private Methods called: " + privateMethodsCount + "\n" + 
                "Protected Methods called: " + protectedMethodsCount + "\n" + 
                "Public Methods called: " + publicMethodsCount + "\n" + 
                "Static Methods called: " + staticMethodsCount + "\n" + 
                "Synchronized Methods called: " + synchronizedMethodsCount + "\n";

    }

}
