/*  =================== File Information =================
    File Name: GotoInstrumentor.java
    Description:
    Author: Michael Shah (Michael.Shah@tufts.edu)
    Original Author: Michael Shah
    Last Modified: 6/01/15

    Purpose: 

    	Perform a dynamic analysis and count
		the number of times a method is called.
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

/*! \class GotoInstrumentor
	\brief GotoInstrumentor class

	Basic Dynamic analysis framework
*/
public class GotoInstrumentor extends BodyTransformer{
	// Create a private static instance of this class
	private static GotoInstrumentor instance = new GotoInstrumentor();
	private GotoInstrumentor(){}
	// Accessor to our instance of the Goto Instrumentor
	public static GotoInstrumentor v() { return instance; }
	
	private boolean addedFieldToMainClassAndLoadedPrintStream = false;
	private SootClass javaIoPrintStream;
	
	private Local addTmpRef(Body body){
		Local tmpRef = Jimple.v().newLocal("tmpRef",RefType.v("java.io.PrintStream"));
		body.getLocals().add(tmpRef);
		return tmpRef;
	}
	
	private Local addTmpLong(Body body){
		Local tmpLong = Jimple.v().newLocal("tmpLong",LongType.v());
		body.getLocals().add(tmpLong);
		return tmpLong;
	}
	
	/*!
		Adds statements
	*/
	private void addStmtsBefore(Chain units, Stmt s, SootField gotoCounter, Local tmpRef, Local tmpLong){
		// insert "tmpRef =java.lang.System.out"
		units.insertBefore(Jimple.v().newAssignStmt(tmpRef,Jimple.v().newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())),s);
		// insert "tmp Long = gotoCounter;"
		units.insertBefore(Jimple.v().newAssignStmt(tmpLong,Jimple.v().newStaticFieldRef(gotoCounter.makeRef())),s);
		// insert "tmpRef.println(tmpLong);"
		SootMethod toCall = javaIoPrintStream.getMethod("void println(long)");
		units.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tmpRef,toCall.makeRef(),tmpLong)),s);

	}
	
	protected void internalTransform(Body body, String phaseName, Map options){
		SootClass sClass = body.getMethod().getDeclaringClass();	// I believe we get the declaring class, because this
																	// gets us the correct class, even if it is an inner class.
		SootField gotoCounter = null;
		boolean addedLocals = false;
		Local tmpRef = null, tmpLong = null;
		Chain units = body.getUnits();
		
		synchronized(this){
			// Do a sanity check and make sure we have a main class.
			// We need a a main class in a Java program, because we are doing a dynamic analysis
			// and want an entry point to run benchmarks in.
			if(!Scene.v().getMainClass().declaresMethod("void main(java.lang.String[])")){
				throw new RuntimeException("couldn't find main() in mainClass (Dynamic analysis on libraries not supported)");
			}
			// Add a new field with our goto counter to our main class.
			if(addedFieldToMainClassAndLoadedPrintStream){
				gotoCounter = Scene.v().getMainClass().getFieldByName("gotoCount");
			}else{
				// Add gotoCounter field
				gotoCounter = new SootField("gotoCount", LongType.v(), Modifier.STATIC);
				Scene.v().getMainClass().addField(gotoCounter);
				
				Scene.v().loadClassAndSupport("java.io.PrintStream");
				javaIoPrintStream = Scene.v().getSootClass("java.io.PrintStream");
				
				addedFieldToMainClassAndLoadedPrintStream = true;
			}
		}
		

		
		// Check if it is the main method
		boolean isMainMethod = body.getMethod().getSubSignature().equals("void main(java.lang.String[])");
		// Add a local that will be our counter
		Local tmpLocal = Jimple.v().newLocal("tmp",LongType.v());
		body.getLocals().add(tmpLocal);
		
		Iterator stmtIt = body.getUnits().snapshotIterator();	// Note snapshot iterator allows us to make modifications to the chain
		while(stmtIt.hasNext()){
			Stmt s = (Stmt) stmtIt.next();
			if(s instanceof GotoStmt){
				/* Profile instructions here 
					This is where we find our counter and add 1 to it
				*/
				AssignStmt toAdd1 = Jimple.v().newAssignStmt(tmpLocal,Jimple.v().newStaticFieldRef(gotoCounter.makeRef()));
				AssignStmt toAdd2 = Jimple.v().newAssignStmt(tmpLocal,Jimple.v().newAddExpr(tmpLocal,LongConstant.v(1L)));				
				AssignStmt toAdd3 = Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(gotoCounter.makeRef()),tmpLocal);				
				
				// insert "tmpLocal = gotoCounter;"
				units.insertBefore(toAdd1, s);
				// insert "tmpLocal = tmpLocal + 1L;"
				units.insertBefore(toAdd2, s);
				// insert "gotoCounter = tmpLocal;"
				units.insertBefore(toAdd3, s);
			}
			else if(s instanceof InvokeStmt){
				/*
					If it is a System.exit(), then
					print-out statement before s
				*/
				
				InvokeExpr iexpr = (InvokeExpr)((InvokeStmt)s).getInvokeExpr();
				if(iexpr instanceof StaticInvokeExpr){
					SootMethod target = ((StaticInvokeExpr)iexpr).getMethod();
					if(target.getSignature().equals("<java.lang.System: void exit(int)>")){
						/* Insert printing statements here */
						if(!addedLocals){
							tmpRef = addTmpRef(body);
							tmpLong = addTmpLong(body);
							addedLocals = true;
						}
						addStmtsBefore(units, s, gotoCounter, tmpRef, tmpLong);
					}
				}
			}else if(isMainMethod && (s instanceof ReturnStmt || s instanceof ReturnVoidStmt)){
				/* In the main method, before the return statement,
					insert our final print outs
				*/
				if(!addedLocals){
					tmpRef = addTmpRef(body);
					tmpLong = addTmpLong(body);
					addedLocals = true;
				}
				addStmtsBefore(units, s, gotoCounter, tmpRef, tmpLong);
			}
		}
		
	}
}