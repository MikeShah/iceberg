/*  =================== File Information =================
    File Name: ProjectController.java
    Description:
    Author: Michael Shah (Michael.Shah@tufts.edu)
    Original Author: Michael Shah
    Last Modified: 2/19/15

    Purpose: 

    	This file sets up the project.

    	Instances of this class can be launched in order to analyze the
    	critical sections of a program.
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

/*! \class ProjectController
	\brief ProjectController class

	Instances of the Project Controller represent an analysis of a program.
*/
public class ProjectController
{

     public static boolean optionPrintDebug = false;
	 public static boolean optionOpenNesting = false;

	 private String[] args;	///!< Stores arguments passed in through the console.
	 public String sootOutputDirectory; ///!< Where the output is stored
     public String testingProgram = "no-name-output";    ///!< Needs to change or be read in from command args
     public List<String> sootsClassPath;                 //!< Stores List of files soot will perform analysis on
     public String sootClassPathString;                  //!< Takes all entries of sootClassPath and concatenates them

    /**
    *	A constructor
    *
    */
	public ProjectController(String[] _args, String outputDirectory){
		args = _args;
        setSootOutputDirectory(outputDirectory);

		System.out.println("\n=========vRetrieving Program Argumentsv=========");
        sootsClassPath = new LinkedList<String>();
        sootClassPathString = "";
        // Command line arguments tell us
        if(args.length>0){
            boolean getNextSootClass = false;
            for(String s: args){
                System.out.println(s);
                if(s.startsWith("{project-name:")){
                    String[] tokens = s.split(":");
                        testingProgram = tokens[1].replace("}", "");
                        createDirectory(sootOutputDirectory+tokens[1].substring(0, tokens[1].length()-1));
                        G.v().out.println("Creating directory:"+sootOutputDirectory+tokens[1].substring(0, tokens[1].length()-1));
                        G.v().out.println("Program name="+tokens[1]);
                }
                else if(s.startsWith("-process-dir")){
                    getNextSootClass = true;
                }
                else if(getNextSootClass == true && !s.contains("{project-name:")){
                    sootsClassPath.add(s);
                    getNextSootClass = false;
                }
            }
        }
        System.out.println("\n=========^Retrieving Program Arguments^=========");

        System.out.println("\n=========vSoot Directories to processv=========");
        for(String s: sootsClassPath){
            System.out.println("Soot process-dir: "+s);
            sootClassPathString+=s+":";
        }
        System.out.println("Final soot-class-path:"+sootClassPathString);
        System.out.println("=========^Soot Directories to process^=========\n");
	}

    /**
    *	Sets the output directory
    *	@param path Sets the sootOutputDirectory path where files are dumped after analysis.
    */
    public void setSootOutputDirectory(String path){
    	sootOutputDirectory = path;
    }

    /** Create a new directory on the file system relative to sootOutputDirectory
    *
    *	Create a directory to store our critical sections in.
    * 	The directory is created in the root directory of the project.
    */
    private boolean createDirectory(String dirName){
         // Create a directory to hold all of our critical sections output
         String directoryName = dirName;
         try{
             boolean success = ( new File(sootOutputDirectory+directoryName).mkdir());
             if(!success){
                 return true;
             }
         }
         catch(Exception e){
             // Output error here
         }
         return false;
     }


    /** Create a new Transformation during jtp phase on critical sections.
    *
    *	Create a new Transformation during jtp phase on critical sections.
    */ 
    private Transform critSection(){
        // Create a new BodyTransformer for intraprocedural analysis.
    	Transform t = new Transform("jtp.criticalsections", new BodyTransformer() {

                // Class fields of new body transformer                
                FileWriter writerCriticalSections;
                CriticalSectionBlock block;
           
             
                     protected void internalTransform(Body body, String phase, Map options){
 
                         // Get the methods body
                            SootMethod m = body.getMethod();
                         // Find the declaring class
                            SootClass c = m.getDeclaringClass();
                            //System.out.println("!!Search for critical sections in!! " + c.getName() + "." + m.getName());
                            UnitGraph eug = new BriefUnitGraph(body);
                            ThreadLocalObjectsAnalysis tlo = null;
                            SynchronizedRegionFinder ta = new SynchronizedRegionFinder(eug, body, optionPrintDebug, optionOpenNesting, tlo);
                            NormalUnitPrinter nup = new NormalUnitPrinter(body);
                            nup.setIndent("  ");
                            Chain units = body.getUnits();
                            Unit lastUnit = (Unit) units.getLast();
                            FlowSet fs = (FlowSet) ta.getFlowBefore(lastUnit);
                            List fList = fs.toList();

                         // Create a directory for us to store our function in.
                            String directoryName = "CriticalSections";

                             for(int i = 0; i < fList.size(); i++) {
                                    // Try to create a file and find all of the
                                    // critical sections that appear within the code. 
                                   
                                            CriticalSection tn = ((SynchronizedRegionFlowPair) fList.get(i)).tn;
                                            //G.v().out.println("!!Found critical section in!! " + tn.method.getName());
                                            block = new CriticalSectionBlock(body, tn.method);
                                        
                                            Unit b = tn.beginning;
                                            for (Unit u : tn.units) {        
                                                // Write output to our file
                                                //writerCriticalSections.write("\\*|v==========================================v|*/\n");
                                                //writerCriticalSections.write("\t" + u.toString() + "\n");
                                                
                                                Stmt stmt = (Stmt)u;
                                                                                                
                                                //getMethodBody(u,2);
                                                
                                                if(u.branches()==true){
                                                    List<UnitBox> branchTargets = u.getUnitBoxes();
                                                    //writerCriticalSections.write("\t\t branches\n");
                                                    /*
                                                     for(UnitBox temp : branchTargets.getBeginUnitBox():){
                                                        writerCriticalSections.write("\t\t"+temp.toString()+"\n");
                                                     }
                                                     */
                                                }

                                                // Attempt to output any tags for the line.
                                                //writerCriticalSections.write("\\*======= Outputtin tags for line =======*/\n");
                                                // Try to grab the tags and output them
                                                String lineNumber="n/a";
                                                for(Iterator j = u.getTags().iterator(); j.hasNext();){
                                                    Tag tag = (Tag)j.next();
                                                    
                                                    //writerCriticalSections.write("\\*===== In iterator loop with tag ("+tag.getName()+") => "+tag.toString()+"*/\n");
                                                    
                                                    if(tag.getName().equals("LineNumberTag")){
                                                        //writerCriticalSections.write("\\*===== LineNumber => "+tag.toString()+" | tagValue => "+tag.getValue().toString()+" */\n");
                                                        lineNumber = tag.toString();
                                                    }
                                                    
                                                } //  for(Iterator j = u.getTags().iterator(); j.hasNext();)
                                                u.toString(nup);
                                                
                                                // Create a new CriticalSection Item.  If it is a method, this information is passed in.
                                                // If the item is indeed a method, then we can perform some analysis to figure out what
                                                // type of method it is, which is then passed into a block which is our major data structure
                                                // for analyzing individual parts of one critical section, and what all is in it.
                                                CriticalSectionItem newStatement = new CriticalSectionItem(stmt,lineNumber,stmt.containsInvokeExpr());
                                                block.addItem(newStatement);
                                                
                                                //writerCriticalSections.write("\\*|^==========================================^|*/\n");

                                            } // for (Unit u : tn.units)

                                            //writerCriticalSections.flush();
                                            //writerCriticalSections.close();
                                            G.v().out.println("Generating dotGraph for:  " + testingProgram+"/CriticalSections/");
                                            DotGraph newGraph = block.generateDOTGraph(sootOutputDirectory+testingProgram+"/CriticalSections");
                                            block.generateFile(testingProgram);



                                    
                             } // for(int i = 0; i < fList.size(); i++)  


                     }
				 }
             
			);	// ends Transform t

		return t;
    }


    /** wholeCallGraph Create a new Transformation during wjtp phase on critical sections.
    *
    *	Create a new Transformation during wjtp phase on critical sections.
    */ 
/*
    Transform wholeCallGraph(){
    	Transform t = new Transform("wjtp.myTrans", new SceneTransformer() {
                
                // Create a Dog Graph and attempt to add edges too it
                DotGraph callGraphOutput;
                CallGraph cg;
                List<edgeList> edges; // List that contains all possible edges in the graph. We want to eliminate duplicates
                                      // by making sure we only ever draw one source to one target here.


                // Print out the entire call graph
                // The output is "wholeProgram.dot"
                protected void walkGraph(SootMethod target, int levels){

                    // Get the methods body
                    if(levels<=0){
                        return;
                    }
                    if(target.hasActiveBody()){
                        Body methodBody = target.getActiveBody();
                        Chain sourceUnits = methodBody.getUnits();
                        Iterator stmtIt = sourceUnits.snapshotIterator();
                        int methodCount = 0;
                        if(sourceUnits.size()==0){
                            return;
                        }
                        while(stmtIt.hasNext()){
                            Stmt subStmt = (Stmt)stmtIt.next();
                            if(subStmt.containsInvokeExpr()){
                                SootMethod subMethod = subStmt.getInvokeExpr().getMethod();
                                if(!target.getName().equals(subMethod.getName())){
                                    
                                    // check every edge, and if it doesn't exist, then we draw it and add it to our edge list
                                    boolean foundEdge = false;
                                    for(int i =0 ; i < edges.size(); i++){
                                        if( edges.get(i).getTarget().equals(target.getName()) && edges.get(i).getSource().equals(subMethod.getName()) ) 
                                        {
                                            foundEdge = true;
                                            break;
                                        }
                                        else if(edges.get(i).getTarget().equals(subMethod.getName()) && edges.get(i).getSource().equals(target.getName())){
                                            foundEdge = true;
                                            break;
                                        }
                                    }
                                    if(foundEdge==false){
                                        callGraphOutput.drawEdge(target.getName(),subMethod.getName());
                                        // add an edge to our list so that we don't have duplicates
                                        edgeList temp = new edgeList(target,subMethod);
                                        
                                        if(target.isSynchronized()){
                                            DotGraphNode node1 = callGraphOutput.getNode(target.getName());
                                            node1.setStyle("filled,fillcolor=red");

                                            // BUILD CONTROL FLOW GRAPH HERE
                                            
                                        }  
                                        edges.add(temp);
                                    }

                                    walkGraph(subMethod,levels-1);
                                }
                            }
                        }
                    }
                }
                
                @Override
                protected void internalTransform(String phaseName, Map options) {
                G.v().out.println("wjtp.myTrans internalTransform");

                       
                       // Source based from: http://marc.info/?l=soot-list&m=128539641224765&w=2
                    //   cg = Scene.v().getCallGraph();
                    //   G.v().out.println("=================V=== Call Graph ===V================");
                    //   G.v().out.println("call graph size:"+cg.size());
                       //G.v().out.println(cg);
                    //   G.v().out.println("=================^=== Call Graph ===^================");
                    
                        String graphPath = sootOutputDirectory+testingProgram+"/wholeprogram.dot"; 
                        callGraphOutput = new DotGraph(graphPath); 
                        edges = new ArrayList<edgeList>();
                            G.v().out.println("WalkingGraph");

                        // Find all of the entry methods of the class.
                        List<SootMethod> entryMethods = EntryPoints.v().mainsOfApplicationClasses();
                        for(int i = 0; i < entryMethods.size(); i++){
                            SootMethod target = entryMethods.get(i);
                            callGraphOutput.drawEdge("main (entry point)",target.getName());
                            walkGraph(target,6);
                        }
                        callGraphOutput.plot(graphPath);
                }
			   
		   }
    	);
    	return t;
    }
*/

    /** wholeCallGraph2 Create a new Transformation during wjtp phase on critical sections.
    *
    *	Create a new Transformation during wjtp phase on critical sections.
    */ 
    Transform wholeCallGraph2(String transform_name){
        // Build a new SceneTransformer for a whole program analysis
    	Transform t = new Transform(transform_name, new SceneTransformer() {
                
                // Create a Dog Graph and attempt to add edges too it
                DotGraph callGraphOutput;
                CallGraph cg;
                List<edgeList> edges; // List that contains all possible edges in the graph. We want to eliminate duplicates
                                      // by making sure we only ever draw one source to one target here.

                // Print out the entire call graph
                // The output is "wholeProgram.dot"
                protected void walkGraph(SootMethod target){
					Queue<SootMethod> Q = new LinkedList<SootMethod>();
					int count = 0;

					// Add the first item onto our Q, generally this is
					// an entry point into the program.
					// 1  procedure BFS(G,v) is
					Q.add(target);

                   // While the Q is not empty, pop off the methods and continue to add them.
	                while(!Q.isEmpty()){
	                	// This is our source
	                    SootMethod _methodSource = Q.remove();
	                    // Extract all of the methods from the source of other methods and then enqueue them.
						if(_methodSource.hasActiveBody()){
	                        Body methodBody = _methodSource.getActiveBody();
	                        Chain sourceUnits = methodBody.getUnits();
	                        Iterator stmtIt = sourceUnits.snapshotIterator();
	                        		// Get all of the dges that are adjacent to our source
									while(stmtIt.hasNext()){
			                            Stmt subStmt = (Stmt)stmtIt.next();
			                            if(subStmt.containsInvokeExpr()){
			                                SootMethod _targetMethod = subStmt.getInvokeExpr().getMethod();

                                            // Capture anything that is not Java.
                                            if(!_targetMethod.isJavaLibraryMethod()){
            			                                // Handle special cases to avoid recursive calls
            			                                if(!_methodSource.getName().equals(_targetMethod.getName())){
            			                         			boolean foundEdge = false;   // Does the edge exist yet
                                                            boolean addtoList = true;

            			                         			 for(int i =0 ; i < edges.size(); i++){
                                                    			if(edges.get(i).edgeExist(_methodSource, _targetMethod)){
                                                    				foundEdge = true;
                                                    				break;
                                                    			}
                                                    		}

                                                    		// If the edge does not exist, then draw it, and add the method to our queue to continue
                                                    		// exploring our graph
                                                    		if(foundEdge==false){
                
                                                    			// Add to our edgelist, some targets.
                                                    			// This builds our adjacency list.
                                                    				for(int i =0 ; i < edges.size(); i++){
                                                    					if(edges.get(i).source.equals(_methodSource)){
                                                                            // Get a value by reference, and make a copy of it
                                                                            edgeList temp = edges.get(i);
                                                                            temp.targets = edges.get(i).getTargets();
                                                                            // Modify the targets
                                                                            temp.addTarget(_targetMethod);
                                                                            // Add our changes to the object in our list.
                                                                            edges.set(i,temp);
                                                                            addtoList = false;
                                                    						break;
                                                    					}
                                                                    }
                                                                // 
                                                                if (addtoList == true){
                                                    				edgeList temp = new edgeList(_methodSource,_targetMethod);
                                                    				edges.add(temp);
                                                                                                                        // Color nodes red.
                                                                    if(_targetMethod.isSynchronized()){
                                                                        DotGraphNode node1 = callGraphOutput.getNode(_targetMethod.getName());
                                                                        node1.setStyle("filled,fillcolor=red");
                                                                    }
                                                                }
                                                                else{
                                                                    // G.v().out.println("Not adding to list, we have found:"+_methodSource.getName()+"->"+_targetMethod.getName());
                                                                }
                                                    			
                                                                // Add any targets we have in our Q, and continue the breadth first search
            			                         				Q.add(_targetMethod);
                                                                // Add the edge that we have drawn to our graph, if we have not previously
                                                                // found it.
            			                         				callGraphOutput.drawEdge(_methodSource.getName(),_targetMethod.getName());

            			                         			}
            			                                }
                                            }
			                            }
			                        }
	                    }
	                }   


	                // Temp code to print out adjacency list
	                for(int i =0 ; i < edges.size();i++){
	                	G.v().out.println(edges.get(i).printTargets());
	                }


                }
                
                @Override
                protected void internalTransform(String phaseName, Map options) {

                        String graphPath = sootOutputDirectory+testingProgram+"/wholeprogram.dot"; 
                        callGraphOutput = new DotGraph(graphPath); 
                        edges = new ArrayList<edgeList>();

                        // Find all of the entry methods of the class.
                        List<SootMethod> entryMethods = EntryPoints.v().mainsOfApplicationClasses();
                        for(int i = 0; i < entryMethods.size(); i++){
                            SootMethod target = entryMethods.get(i);
                            callGraphOutput.drawEdge("main (entry point)",target.getName());
                            walkGraph(target);
                        }
                        callGraphOutput.plot(graphPath);
                }
			   
		   }
    	);
    	return t;
    }


    /**
    *	Runs a static analysis on a program.
    *
    *	The goal is to generate three types of graphs
    *	1.) The entireprogram, with all of the method calls
    *	2.) A Call Graph for each critical section, and the methods it calls.
    *	3.) A Control Flow Graph for the Body of each critical section
    *
    */
	public void run_static(){

        G.v().out.println("==== Building Critical Sections Analysis Transform ====");
         

        // Add a body transformation for individual methods
        PackManager.v().getPack("jtp").add(critSection()); 
				
         
		/*  added to support call graphs
			Based on tutorial/guide/examples/call_graph/src
		*/		
		// Add a transformation for the whole program.
		PackManager.v().getPack("wjtp").add(wholeCallGraph2("wjtp.myTrans2"));
				
        // Attempt to print line numbers of java source-code in jimple files
        // Source: http://www.massapi.com/source/sootsrc-2.4.0/src/soot/jbco/LineNumberGenerator.java.html
		// PackManager.v().getPack("jtp").add(new Transform("jtp.lnprinter",new BafLineNumberer()));

        // Add our Critical Sections transform
            
        // Attempt to get the simple Class Hierarhcy Analysis(CHA)
		// CHATransformer.v().transform();
         
            // Get the call graph transformation pack and add it.
            // PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans",new SceneTransformer());
//         CallGraph myCallGraph = Scene.v().getCallGraph();

            // Output some debugging information
		 	System.out.println("Soot class path " + Scene.v().getSootClassPath()+"\n\n");
            System.out.println("^(trying to use java 8) So instead Soot setting class path:");
            //Scene.v().setSootClassPath("/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/../Classes/classes.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/../Classes/ui.jar:/Library/Java/JavaVirtualMachines/1.6.0_65-b14-462.jdk/Contents/Home/jre/lib/rt.jar://Library/Java/JavaVirtualMachines/1.6.0_65-b14-462.jdk/Contents/Home/jre/lib/jce.jar:./benchmarks/Sunflow/sunflow2/sunflow.jar");

            // Scene.v().setSootClassPath("/Library/Java/JavaVirtualMachines/1.6.0_65-b14-462.jdk/Contents/Classes/*.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/jce.jar:/Users/michaelshah/Dropbox/school/GraduateSchool/Research/Soot/benchmarks/Sunflow/sunflow2/sunflow.jar:/Users/michaelshah/Dropbox/school/GraduateSchool/Research/Soot/benchmarks/Sunflow/sunflow2/janino.jar");
            //  ^ Working, uncomment
            System.out.println("Trying classpath with JDK 1.6 and 1.7");
            Scene.v().setSootClassPath("/Library/Java/JavaVirtualMachines/1.6.0_65-b14-462.jdk/Contents/Classes/*.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/jce.jar:"+sootClassPathString);


            System.out.println("New Soot class path " + Scene.v().getSootClassPath()+"\n\n");
            System.out.println("=====v Program Arguments v=====");
            if(args.length>0){
                for(String s: args){
                    System.out.println(s);
                }
            }
            System.out.println("=====^ Program Arguments ^=====\n\n");

        // Used for generating the individual method bodies
        Options.v().setPhaseOption("jtp","enabled:true");
        // Used for generating the whole program dot graph
        Options.v().setPhaseOption("wjtp","enabled:true");

        // Set up some of the debugging options
        Options.v().set_whole_program(true);
        // Prints out additoinal debugging information, (I think?)
        //Options.v().set_debug(true);
        // Prints out the time at the end.
        Options.v().set_time(true);
        //Options.v().set_verbose(true);
        Options.v().set_output_format(Options.output_format_jimple);
        // Prints out some information
        Options.v().set_print_tags_in_output(true);
        // Preserve the line numbers in our source
        Options.v().set_keep_line_number(true);         
        // Run soot with the arguments supplied
        soot.Main.main(args);
	}



}