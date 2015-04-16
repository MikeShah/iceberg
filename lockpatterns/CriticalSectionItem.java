/*  =================== File Information =================
    File Name: CriticalSectionItem.java
    Description:
    Author: Michael Shah (Michael.Shah@tufts.edu)
    Last Modified: 3/06/14

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


/*  ============== CriticalSectionItem ==============
    Purpose: 
    Notes:
    Example usage: 
    1.)
    ==================================== */ 
public class CriticalSectionItem{
    
    Stmt statement; // The actual jimple statement
    String lineNumber; // The line number the statement occurs on.
                    // This might not be 100% accurate, but only an estimate
    String methodName; // If this statement is a method, then set these values.
    boolean isAMethod; // Returns true if the statement is indeed a Method

    boolean isConcrete;
    boolean isConstructor;
    boolean isEntryMethod;
    boolean isJavaLibraryMethod;
    boolean isPrivate;
    boolean isProtected;
    boolean isPublic;
    boolean isStatic;
    boolean isSynchronized;


    
    // Constructor for a single Critical Section Item
    public CriticalSectionItem(Stmt stmt, String lineNumber, boolean isAMethod){
        this.statement = stmt;
        this.lineNumber = lineNumber;
        this.isAMethod = isAMethod;
        // Stores every method
        
        // Setup information about this Critical Section
        if(isAMethod==true){
            SootMethod current = statement.getInvokeExpr().getMethod();
            // Output information about Statement if it is a method
                methodName = current.getName();
                isConcrete = current.isConcrete();
                isConstructor = current.isConstructor();
                isEntryMethod = current.isEntryMethod();
                isJavaLibraryMethod = current.isJavaLibraryMethod();
                isPrivate = current.isPrivate();
                isProtected = current.isProtected();
                isPublic = current.isPublic();
                isStatic = current.isStatic();
                isSynchronized = current.isSynchronized();
        }
    }
    
    // Note you should only output Method attirbutes if you are sure the stmt is a method call.
    // Do some check 'if(isAMethod==true)'
    // This should be handled outside of the class
    String[] outputMethodAttributes(){
        String[] output = new String[12];

        SootMethod current = statement.getInvokeExpr().getMethod();

            // Output information about Statement if it is a method
                output[0]="\t |====================================|\n";
                output[1]="\t Name: "+methodName+"\n";
                output[2]="\t isConcrete: "+isConcrete+"\n";
                output[3]="\t isConstructor: "+isConstructor+"\n";
                output[4]="\t isEntryMethod: "+isEntryMethod+"\n";
                output[5]="\t isJavaLibraryMethod: "+isJavaLibraryMethod+"\n";
                output[6]="\t isPrivate: "+isPrivate+"\n";
                output[7]="\t isProtected: "+isProtected+"\n";
                output[8]="\t isPublic: "+isPublic+"\n";
                output[9]="\t isStatic: "+isStatic+"\n";
                output[10]="\t isSynchronized: "+isSynchronized+"\n";
                output[11]="\t |====================================|\n";
        return output;
    }
    
    // A critical section is made up of many statements.
    // outputItem will output one of the statements found within the body of
    // a critical section (i.e. synchronized) section of code.  If there is line
    // information, we will append it to the line so that we can trace the source code.
    public String outputItem(){

        if(lineNumber==null){
            return statement.toString()+" //* Line: n/a";
        }
        else
        {
            return statement.toString()+" //* Line: "+lineNumber+"*/";
        }
    }
    
    public Stmt getStatement(){
        return this.statement;
    }
    
    
}
