/*  =================== File Information =================
    File Name: CriticalSectionBlock.java
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

// Be able to output a DOT file (specifically for the callgraph
import soot.util.dot.*;
import soot.util.*;
// CFG imports
import soot.util.cfgcmd.*;
import soot.coffi.*;

/*  ============== CriticalSectionBlock ==============
    Purpose: 
    Notes:
    Example usage: 
    1.)
    ==================================== */ 
// Our Main class that gets compiled.
public class CriticalSectionBlock
{
    String nameOfCriticalSection;   // Stores the name of the critical section
    public ArrayList<CriticalSectionItem> statementsList;  // Holds a list of all of the statements in the body of one piece of code
    int numberOfStatements; // Records how many statements there are total in the body of this critical section
    int numberOfMethods;    // Records how many of the statements are method calls (at the first level).  This is a conveinence function
        int allocations;
        int concreteMethodCount;    // Of the total 'numberOfMethods' how many are concrete
        int constructorsMethodCount;    // how many are constructors
        int entryMethodsCount;          // How many are entry methods
        int javaLibraryMethodsCount;    // How many are java library functions in the first level
        int totalJavaLibraryMethodsCount; // How many are java library functions outside of the first level
        int privateMethodsCount;        // How many are private
        int protectedMethodsCount;      // How many are protected
        int publicMethodsCount;         // How many are public
        int staticMethodsCount;         // How many are static
        int synchronizedMethodsCount;   // How many are synchronized methods within this synchronized methods body

    Body body;                          // The Body of the synchronized block. We use this for genreating the Control Flow Graph
    SootMethod m;                       // The code of the synchronized block.

    int[] statementTypes;   // Conveinent representation that tallies how many of each type of statement  exist within a critical section.
    
    DotGraph callGraphOutput;
    DotGraph criticalSectionGraphOutput;
    String graphPath2;
    //CallGraph cg;


    String criticalSectionSummary; // Stores a final string that summarizes the methods features.

    List<edgeList> edges; // List that contains all possible edges in the graph. We want to eliminate duplicates
                         // by making sure we only ever draw one source to one target here.
    
    int totalMethods;   // Total methods called from the synchronized section, plus the method calls of the branches below.
    int totalAllocations;  
    int totalSynchronizedSections;
    int totalStatementCount;

    // Start collecting statistics
    ArrayList<methodInfo> methodInfoNames; // Keeps track of all methods called.
    

    // Name:        CriticalSectionBlock
    // Description: Default constructor
    public CriticalSectionBlock(Body _body, SootMethod _m){
        m = _m;
        body = _body;
        this.nameOfCriticalSection = m.getName();
        statementsList = new ArrayList<CriticalSectionItem>();
        statementTypes = new int[17];
        criticalSectionSummary = "This needs to be implemented!";
        edges = new ArrayList<edgeList>();

        methodInfoNames = new ArrayList<methodInfo>();

        totalMethods = 0;
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

        totalAllocations = 0;
        totalSynchronizedSections = 0;
        totalStatementCount = 0;
    }
    
    public void setMethod(SootMethod _m){
        m = _m;
    }

    // Name:        addItem
    // Description: Adds a new statement to the statementsList.
    //              If it is a method than we increment that count.
    public void addItem(CriticalSectionItem item){
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

        statementsList.add(item);
        numberOfStatements++;
    }
    
    // Name:        addItem
    // Description: Adds a new statement to the statementsList.
    //              If it is a method than we increment that count.
    public void addItem(Stmt stmt, String lineNumber, boolean isAMethod){
        CriticalSectionItem newItem = new CriticalSectionItem(stmt, lineNumber, isAMethod);
        this.addItem(newItem);  // Call our own function so we only have to maintain one version
                                // with a consistent body of code for the addItem function.
    }

    // Name:        setCriticalSectionSummary
    // Description: Sets the critical section summary to its final output.
    //              Ideally this summary gets updated as needed, but for now we have
    //              a setter function.
    public void setCriticalSectionSummary(String _criticalSectionSummary){
        criticalSectionSummary = _criticalSectionSummary;
    }

    // Name:        getCriticalSectionSummary
    // Description: Returns the critical seciton summary
    //              This is a string that represents the final summary of methods
    public String getCriticalSectionSummary(){

        criticalSectionSummary =    "\n\nMethods called: " + numberOfMethods + "\n" + 
                                    "Constructors called: " + constructorsMethodCount + "\n" + 
                                    "Entry Methods called: " + entryMethodsCount + "\n" + 
                                    "Java Library Methods called: " + javaLibraryMethodsCount + "\n" + 
                                    "Private Methods called: " + privateMethodsCount + "\n" + 
                                    "Protected Methods called: " + protectedMethodsCount + "\n" + 
                                    "Public Methods called: " + publicMethodsCount + "\n" + 
                                    "Static Methods called: " + staticMethodsCount + "\n" + 
                                    "Synchronized Methods called: " + synchronizedMethodsCount + "\n";

        return criticalSectionSummary;
    }
    
    // Name:        getMethods
    // Description: Output only the methods that are contained within a critical section
    public String[] getMethods(){
        String[] statements = new String[numberOfMethods];
        int i = 0;
        int j = 0;
        while( i < numberOfStatements){
            if(statementsList.get(i).isAMethod==true){
                statements[j] = statementsList.get(i).outputItem();
                j++;
            }
            i++;
        }
        return statements;
    }
    
    // Name:        getStatements
    // Description: Outputs the entire list of statements
    public String[] getStatments(){
        String[] statements = new String[numberOfStatements];
        for(int i = 0; i < numberOfStatements; i++){
            statements[i] = statementsList.get(i).outputItem();
        }
        return statements;
    }

    // Name:        getStatementTypes
    // Description: Takes the body of statements and decides
    //              what type of statement it is.
    public String[] getStatementTypes(){
        String[] statements = new String[numberOfStatements];
        for(int i = 0; i < numberOfStatements; i++){
            Stmt item = statementsList.get(i).getStatement();

            // See if the statement is some structure
            if(item instanceof AssignStmt){
                statements[i] = "AssignStmt\t\t"+item.toString();
                statementTypes[0]++;
            }
            else if(item instanceof BreakpointStmt){
                statements[i] = "GotoStmt\t\t"+item.toString();
                statementTypes[1]++;
            }
            else if(item instanceof DefinitionStmt){
                statements[i] = "DefinitionStmt\t"+item.toString();
                statementTypes[2]++;
            }
            else if(item instanceof EnterMonitorStmt){
                statements[i] = "EnterMonitorStmt\t"+item.toString();
                statementTypes[3]++;
            }
            else if(item instanceof ExitMonitorStmt){
                statements[i] = "ExitMonitorStmt\t"+item.toString();
                statementTypes[4]++;
            }
            else if(item instanceof GotoStmt){
                statements[i] = "GotoStmt\t\t"+item.toString();
                statementTypes[5]++;
            }
            else if(item instanceof IdentityStmt){
                statements[i] = "IdentityStmt\t\t"+item.toString();
                statementTypes[6]++;
            }
            else if(item instanceof IfStmt){
                statements[i] = "IfStmt\t\t\t"+item.toString();
                statementTypes[7]++;
            }
            else if(item instanceof InvokeStmt){
                statements[i] = "InvokeStmt\t\t"+item.toString();
                statementTypes[8]++;
            }
            else if(item instanceof LookupSwitchStmt){
                statements[i] = "LookupSwitchStmt\t"+item.toString();
                statementTypes[9]++;
            }
            else if(item instanceof MonitorStmt){
                statements[i] = "MonitorStmt\t\t"+item.toString();
                statementTypes[10]++;
            }
            else if(item instanceof NopStmt){
                statements[i] = "NopStmt\t\t"+item.toString();
                statementTypes[11]++;
            }
            else if(item instanceof RetStmt){
                statements[i] = "RetStmt\t\t"+item.toString();
                statementTypes[12]++;
            }
            else if(item instanceof ReturnStmt){
                statements[i] = "ReturnStmt\t\t"+item.toString();
                statementTypes[13]++;
            }
            else if(item instanceof ReturnVoidStmt){
                statements[i] = "ReturnVoidStmt\t"+item.toString();
                statementTypes[14]++;
            }
            else if(item instanceof TableSwitchStmt){
                statements[i] = "TableSwitchStmt\t"+item.toString();
                statementTypes[15]++;
            }
            else if(item instanceof ThrowStmt){
                statements[i] = "ThrowStmt\t\t"+item.toString();
                statementTypes[16]++;
            }          
/*
            // See if the statement is a method call of some sort
            InvokeExpr methodCall = (InvokeExpr)item.getInvokeExpr();
            if(methodCall instanceof StaticInvokeExpr){
                statements[i] = "StaticInvokeExpr\t\t"+item.toString();
            }
            else if(methodCall instanceof SpecialInvokeExpr){
                statements[i] = "SpecialInvokeExpr\t\t"+item.toString();
            }
            else if(methodCall instanceof VirtualInvokeExpr){
                statements[i] = "VirtualInvokeExpr\t\t"+item.toString();
            }
            else if(methodCall instanceof InterfaceInvokeExpr){
                statements[i] = "InterfaceInvokeExpr\t\t"+item.toString();
            }
*/
        }
        return statements;
    }



    // Create a directory to store our critical sections in.
    // The directory is created in the root directory of the project.
    protected boolean createDirectory(String dirName){
             // Create a directory to hold all of our critical sections output
             String directoryName = dirName;
             try{
                 boolean success = ( new File("/Users/michaelshah/Documents/sootDump/"+directoryName).mkdir());
                 if(!success){
                     return true;
                 }
             }
             catch(Exception e){
                 // Output error here
             }
             return false;
     }

    /*!
        Makes a Control Flow Graph from the Jimple representation
    */
    public void makeControlFlowGraph(String directoryName){
        //CFG methodCFG= new CFG((method_info)m);
        CFGToDotGraph d = new CFGToDotGraph();
        UnitGraph eug = new BriefUnitGraph(body);
        DotGraph outputGraph;
        outputGraph = d.drawCFG(eug,body);
        outputGraph.plot(directoryName+"/"+nameOfCriticalSection+"/"+nameOfCriticalSection+"_cfg_generated_from_soot.dot");
        //d.CFGToDotGraph();
        

        try{
            FileWriter writerCFGTempInfo;
            File CFGTempInfoFile = new File(directoryName+"/"+nameOfCriticalSection+"/"+nameOfCriticalSection+"_cfgInfo.txt");
            CFGTempInfoFile.createNewFile();
            writerCFGTempInfo = new FileWriter(CFGTempInfoFile);

            // Build an actual data structure that we can manually inspect.
            // The above outputGraph is what we will check against to make sure ours is correct.
            List<Unit> heads = eug.getHeads();

            Queue<Unit> cfgUnitQueue = new LinkedList<Unit>();
            Unit firstHead = eug.getHeads().get(1);
            cfgUnitQueue.add( firstHead );  // Get the first head and put it in the queue
                                                        // This makes the assumption that there is only
                                                        // one head for now.
            
            List<buildCFG> edges = new ArrayList<buildCFG>();
            while(!cfgUnitQueue.isEmpty()){
                Unit nextUnit = cfgUnitQueue.remove();

                List<Unit> nodes = eug.getSuccsOf(nextUnit);
                
                // Perform Breadth First Search to build a CFG
                for(int i =0; i < nodes.size(); i++){
                    // Add nodes to the queue
                    cfgUnitQueue.add(nodes.get(i)); 
                    // Draw edges
                    buildCFG temp = new buildCFG(firstHead,nodes.get(i));

                    // Check if the edge exists in our graph, and then
                    // add it if it doesn't.
                    boolean addCFGEdge = true;
                    for(int j = 0; j < edges.size(); j++){
                        if(edges.get(j).edgeExist(firstHead,nodes.get(j))) {
                            addCFGEdge = false;
                            break;
                        }
                    }
                    if(addCFGEdge){
                        edges.add(temp);
                    }

                }

            }

            /*
            writerCFGTempInfo.write("Number of heads: "+heads.size());
            for(int i =0; i < heads.size(); i++){
                List<Unit> nodes = eug.getSuccsOf(heads.get(i));

                writerCFGTempInfo.write("Number of Succs "+nodes.size());
            }
            */

            writerCFGTempInfo.flush();
            writerCFGTempInfo.close(); 
        }
        catch(IOException e){
            // Output error here
            G.v().out.println(e.toString());
        }

        //DotGraph d = new DotGraph(eug,body);
/*


        // Get the unit graph
        UnitGraph eug = new BriefUnitGraph(body);

        // Get all of the units
        Chain units = body.getUnits();

        units

        UnitBox
        Unit lastUnit = (Unit) units.getLast();
        FlowSet fs = (FlowSet) ta.getFlowBefore(lastUnit);
        List fList = fs.toList();

        for(Object o: fList){
            o.getType().toString();
        }
*/

    }


    /*!
            Generate a dotgraph for the critical section.

            This is a control flow graph for an individual method, just showing the
            method calls, as opposed to having a 'control flow graph' that would demonstrate branching

    */
    public DotGraph generateDOTGraph(String directoryName){

        // Create a directory for the critical section
        String graphPath = directoryName+"/"+nameOfCriticalSection+"/"+nameOfCriticalSection+"-2.dot";
           createDirectory(directoryName+"/"+nameOfCriticalSection+"/");
       graphPath2 = directoryName+"/"+nameOfCriticalSection+"/"+nameOfCriticalSection+".dot";

        G.v().out.println("Attempting to create directory here: "+graphPath2); 
        // Create a new .dot file.
        callGraphOutput = new DotGraph(graphPath);

        criticalSectionGraphOutput = new DotGraph(graphPath2);
 
        //cg = Scene.v().getCallGraph();


        // Search through all of the statements in the critical section
        for(int i = 0; i < numberOfStatements; i++){
            if(statementsList.get(i).isAMethod==true){
                Stmt item = statementsList.get(i).getStatement();
                SootMethod current = item.getInvokeExpr().getMethod();

               // callGraphOutput.drawEdge(nameOfCriticalSection,current.getName());
                
                //G.v().out.println("About to enter recursion");
                //walkGraph(current,7);
                if(current.isConstructor()==true){
                    allocations++;
                }
                
                edgeList temp = new edgeList(m,current);
                edges.add(temp);
                criticalSectionGraphOutput.drawEdge(m.getName(),current.getName());
                
                G.v().out.println("Not using recursion");
                walkCriticalSectionBFS(current);
            }
        }




        // Modify the top node, because it is a critical section, and we want to output information about it.
        DotGraphNode node1 = callGraphOutput.getNode(nameOfCriticalSection);
        String node1Style = "<<TABLE><TR>";  
        node1Style += "<TD>Features of Critical Section:"+nameOfCriticalSection+"</TD></TR>";

        node1Style += "<TR><TD>Loops</TD>"; 
        node1Style += "<TD># ofNested Loops in first level</TD>";
        node1Style += "<TD>#Total # of netsed loops</TD>"; 
        node1Style += "<TD>has recrusion</TD>"; 
        node1Style += "<TD>-</TD></TR>";  

        node1Style += "<TR><TD>MethodCalls</TD>";
        node1Style += "<TD># of method calls in first level: "+numberOfMethods+ "</TD>";
        node1Style += "<TD>total # of method calls: "+(totalMethods+numberOfMethods)+"</TD>"; 
        node1Style += "<TD># of synchronized method calls in first level: "+ synchronizedMethodsCount + "</TD>"; 
        node1Style += "<TD>total # of sync method calls: "+ totalSynchronizedSections+"</TD></TR>";  

        float methodGrowth = 0;
        if(totalMethods+numberOfMethods != 0){ methodGrowth = (float)totalMethods / (float)(totalMethods+numberOfMethods); }
        
        node1Style += "<TR><TD>Method Analysis</TD>";
        node1Style += "<TD>Growth from first level: "+ methodGrowth + "</TD>";
        node1Style += "<TD>Level with most method calls</TD>"; 
        node1Style += "<TD>Deepest call path and path</TD>"; 
        node1Style += "<TD>-</TD></TR>"; 

        node1Style += "<TR><TD>Allocations(Constructors called)</TD>";
        node1Style += "<TD># of allocations in first level: "+constructorsMethodCount+"</TD>";
        node1Style += "<TD>Total # of allocations: "+totalAllocations+"</TD>"; 
        node1Style += "<TD>-</TD>"; 
        node1Style += "<TD>-</TD></TR>";


        float allocationGrowth = 0;
        if(totalAllocations !=0) { allocationGrowth = (float)(constructorsMethodCount / (float)totalAllocations); } 

        node1Style += "<TR><TD>Allocation Analysis</TD>";
        node1Style += "<TD>Growth from first level: "+allocationGrowth+"</TD>";
        node1Style += "<TD>Level with most allocations: </TD>"; 
        node1Style += "<TD>-</TD>"; 
        node1Style += "<TD>-</TD></TR>";  

        node1Style += "<TR><TD>LineCount</TD>";
        node1Style += "<TD>#of lines of IR in first level: </TD>";
        node1Style += "<TD>total # lines of IR: "+totalStatementCount+"</TD>"; 
        node1Style += "<TD>-</TD>"; 
        node1Style += "<TD>-</TD></TR>";  

        node1Style += "<TR><TD>Method Type</TD>";
        node1Style += "<TD># of Java library methods: "+javaLibraryMethodsCount+"</TD>";
                    int nonJavaMethodCount = (totalMethods+numberOfMethods-javaLibraryMethodsCount);
        node1Style += "<TD># of non-java methods"+nonJavaMethodCount+"</TD>"; 
        node1Style += "<TD>-</TD>";
        node1Style += "<TD>-</TD></TR>";  

        node1Style += "<TR><TD>Branching</TD>";
        node1Style += "<TD># of if statements"+statementTypes[7]+"</TD>";
        node1Style += "<TD># of return statements</TD>"; 
        node1Style += "<TD>-</TD>"; 
        node1Style += "<TD>-</TD></TR>";  

        node1Style += "</TABLE>>";;
        node1.setHTMLLabel(node1Style);  

        // Output the final dot graph
        callGraphOutput.plot(graphPath);

        // ========v CSV Output v===========
        
            
            try{
                File csvFeatures = new File(directoryName+"/"+nameOfCriticalSection+"/"+nameOfCriticalSection+"_csv.csv");
                FileOutputStream writerCSVSummary = new FileOutputStream(csvFeatures);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(writerCSVSummary));
                //writerCSVSummary = new FileOutputStream(csvFeatures);
                    out.write(nameOfCriticalSection+","); // "Features of Critical Section:"

                    out.write(numberOfMethods+",");            // "# of method calls in first level: "+
                    out.write(totalMethods+numberOfMethods+","); // "total # of method calls: " 
                    out.write(synchronizedMethodsCount+",");   // "# of synchronized method calls in first level: "
                    out.write(totalSynchronizedSections+",");  // "total # of sync method calls: "
                    out.write(methodGrowth+",");               // "Growth from first level: "

                    out.write(constructorsMethodCount+",");    // "Number of allocations in first level: "
                    out.write(totalAllocations+",");           // "Total # of allocations: "

                    out.write(allocationGrowth+",");           // "Growth from first level: "
                    out.write(totalStatementCount);        // "total # lines of IR: "
                out.close();
            }
            catch(IOException e){
                
            }
        // ========^ CSV Output ^===========

        // Make a control flow graph for the critical section
        makeControlFlowGraph(directoryName);


        return callGraphOutput;
    }



/*
    Name: WalkGraphBFS
    Description:    Performs a breadth-first search over a graph from a node.
                    Nodes that are 'synchronized' or otherswise marked as critical sections
                    are then put into an ArrayList and returned.
    parameters: 
    source - The first method in which to tart the Breadth-First Search
*/
/*
    public ArrayList<SootMethod> walkGraphBFS(SootMethod source){
        // The list that returns all of the critical sections from our search
        ArrayList<SootMethod> criticalSection = new ArrayList<SootMethod>();
        // Queue for breadth first search
        Queue<SootMethod> bfsQ;
        Queue<SootMethod> criticalSections;
        bfsQ.add(source);

        // If we have an actual body in our sooth method
        while(!bfsQ.isEmpty()){
                if(source.hasActiveBody()){
                    // Create an iterator to read through all of the statements in the methods body.
                    Body methodBody = source.getActiveBody();
                    Chain sourceUnits = methodBody.getUnits();
                    Iterator stmtIt = sourceUnits.snapshotIterator();
                    // Check if they are critical sections
                    while(stmtIt.hasNext()){    // If we have more statements in the methods body
                        Stmt nextStmt = (Stmt)stmtIt.next(); // Get the next statement in this iterator, and call it subStmt.
                        if(nextStmt.containsInvokeExpr()){
                            SootMethod subMethod = nextStmt.getInvokeExpr().getMethod();
                            bfsQ.add(subMethod);
                            // Add the subMethod to the bsfQ
                            // Just add the critical sections to a special critical setions Q
                        }
                    }
                }
                bfsQ.remove();
        }
    }
*/
    /*! Name:        walkCriticalSectionBFS
        Description: Performs Breadth first search 
    */
    public void walkCriticalSectionBFS(SootMethod source){
        // Create a queue
        Queue<SootMethod> methodQueue = new LinkedList<SootMethod>();
        // Enqueue the source method into the queue
        methodQueue.add(source);

        List<edgeList> edges = new ArrayList<edgeList>(); // List that contains all possible edges in the graph. We want to eliminate 
                              // any backedges so we do not get into an infinite recursion.

        while(!methodQueue.isEmpty()){
            SootMethod subMethod = methodQueue.remove();
            //G.v().out.println("Removing from Q:"+subMethod.getName());

                // Make sure the method is not empty
                if(subMethod.hasActiveBody()){                
                    // Get its statements
                    Body methodBody = subMethod.getActiveBody();
                    Chain sourceUnits = methodBody.getUnits();
                    Iterator stmtIt = sourceUnits.snapshotIterator();

                    // Get the next statement
                    while(stmtIt.hasNext()){
                        Stmt subStmt = (Stmt)stmtIt.next(); // Iterate
                        // Check if the next statement is a method

                        // Increment the number of statements we have found.
                        totalStatementCount++;
                        if(subStmt.containsInvokeExpr()){
                            SootMethod targets = subStmt.getInvokeExpr().getMethod();
                            // Each time we find a method, we have to increment the number of methods we have found.
                            totalMethods++;

                            // Increment how many synchronized and constructors we are finding
                            if(targets.isSynchronized()==true){
                                totalSynchronizedSections++;
                            }
                            if(targets.isConstructor()==true){
                                totalAllocations++;
                            }
                            if(targets.isJavaLibraryMethod()==true){
                                totalJavaLibraryMethodsCount++;
                            }

                            // Add the method to the queue, if it is not a recursive call
                            if(!subMethod.getName().equals(targets.getName())){
                                boolean foundEdge = false;
                                for(int i =0 ; i < edges.size(); i++){
                                    if(edges.get(i).edgeExist(subMethod,targets)){
                                        foundEdge = true;
                                        break;
                                    }
                                }
                                // If we do not find our edge, then add it.                            
                                if(foundEdge==false){
                                    edgeList temp = new edgeList(subMethod,targets);
                                    edges.add(temp);
                                    methodQueue.add(targets);
                                    //G.v().out.println("DRAWING edge:"+subMethod.getName()+"->"+targets.getName());
                                    // Draw edges from the source to each of the adjacent edges in the call graph.
                                    criticalSectionGraphOutput.drawEdge(subMethod.getName(),targets.getName());
                                }

                            }
                        }
                        else{

                        }
                    } // while(stmtIt.hasNext())
                }
        }   // end while(!methodQueue.isEmpty()){
            criticalSectionGraphOutput.plot(graphPath2);
    } // ends function body of     public void walkCriticalSectionBFS(SootMethod target


    // Name:        walkGraph
    // Description: Recursive walking of graph of a critical section
    //              Builds a call graph for a single critical section
    public void walkGraph(SootMethod target, int levels)
    {
        if(levels<=0){
            return;
        }
        else{
                // Get the methods body
                if(target.hasActiveBody()){
                    Body methodBody = target.getActiveBody();
                    Chain sourceUnits = methodBody.getUnits();
                    Iterator stmtIt = sourceUnits.snapshotIterator();
                    int statementCount = 0;
                    int methodCount = 0;
                    int synchronizedMethodCount = 0;
                    int allocations = 0;

                    if(sourceUnits.size()==0){
                        return;
                    }
                    while(stmtIt.hasNext()){
                        Stmt subStmt = (Stmt)stmtIt.next();
                        statementCount++;
                        totalStatementCount++;

                        if(subStmt.containsInvokeExpr()){
                            methodCount++;
                            
                                // Get the method and store it.
                                SootMethod subMethod = subStmt.getInvokeExpr().getMethod();
                                // Start retaining some information about methods
                                methodInfo mInfo = new methodInfo(subMethod.getName());
                                searchMethodInfo(mInfo);

                                if(target.getName().equals(subMethod.getName())) {
                                    // filter out self-edges
                                    //callGraphOutput.drawEdge(target.getName(),subMethod.getName());
                                }
                                else
                                {
                                    // * Filter out edges that
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

                                    // If we found an edge we can add to our list, actually do some
                                    // work here. 
                                    if(foundEdge==false){
                                        totalMethods++;
                                        // increment the synchronized methods called.
                                        if(target.isSynchronized()==true){
                                            synchronizedMethodCount++;
                                            totalSynchronizedSections++;
                                        }
                                        if(target.isConstructor()==true){
                                            allocations++;
                                            totalAllocations++;
                                        }

                                        // finally actually add our edge to the edge list
                                        callGraphOutput.drawEdge(target.getName(),subMethod.getName());
                                        // add an edge to our list so that we don't have duplicates
                                        edgeList temp = new edgeList(target,subMethod);
                                        edges.add(temp);
                                    
                                                    //Find loops
                                                    LoopNestTree targetsLoops = new LoopNestTree(target.getActiveBody());

                                                    String  node1SString = "<<TABLE><TR>";    
                                                    node1SString += "<TD>method Name:"+target.getName()+"</TD>";
                                                    node1SString += "<TD>Total Methods Called:"+methodCount+"</TD>";
                                                    node1SString += "<TD>Synchronized Methods Called:"+synchronizedMethodCount+"</TD>";
                                                    
                                                    // Is our method a constructor
                                                    if(target.isConstructor()==true) { 
                                                        node1SString += "<TD bgcolor=\"green\">isConstructor</TD></TR><TR>"; 
                                                    }
                                                    else { 
                                                        node1SString += "<TD bgcolor=\"red\">isConstructor</TD></TR><TR>"; 
                                                    }
                                                    criticalSectionSummary += target.isConstructor();
                                                    
                                                    // Is our method and Entry Method
                                                    if(target.isEntryMethod()==true) { 
                                                        node1SString += "<TD bgcolor=\"green\">isEntryMethod</TD>"; 
                                                    }
                                                    else { 
                                                        node1SString += "<TD bgcolor=\"red\">isEntryMethod</TD>"; 
                                                    }
                                                    criticalSectionSummary += "\t"+target.isEntryMethod();

                                                    if(targetsLoops.hasNestedLoops()==true) {
                                                        node1SString += "<TD bgcolor=\"green\">hasNestedLoops</TD>";
                                                    }
                                                    else{ 
                                                        node1SString += "<TD bgcolor=\"red\">hasNestedLoops</TD>"; 
                                                    }
                                                    criticalSectionSummary += "\t"+targetsLoops.hasNestedLoops();

                                                    if(target.isJavaLibraryMethod()==true) {
                                                        node1SString += "<TD bgcolor=\"green\">isJavaLibraryMethod</TD>"; 
                                                    }
                                                    else{
                                                        node1SString += "<TD bgcolor=\"red\">isJavaLibraryMethod</TD>"; 
                                                    }
                                                    criticalSectionSummary += "\t"+target.isJavaLibraryMethod();
                                                    
                                                    if(target.isPrivate()==true) { 
                                                        node1SString += "<TD bgcolor=\"green\">Scope=private</TD></TR><TR>"; 
                                                        criticalSectionSummary += "\t"+"private";
                                                    }
                                                    else if(target.isProtected()==true) { 
                                                        node1SString += "<TD bgcolor=\"green\">Scope=protected</TD></TR><TR>"; 
                                                        criticalSectionSummary += "\t"+"protected";
                                                    }
                                                    else { 
                                                        node1SString += "<TD bgcolor=\"green\">Scope=public</TD></TR><TR>"; 
                                                        criticalSectionSummary += "\t"+"public";
                                                    }

                                                    if(target.isStatic()==true) { 
                                                        node1SString += "<TD bgcolor=\"green\">isStatic</TD>"; }
                                                    else { 
                                                        node1SString += "<TD bgcolor=\"red\">isStatic</TD>";
                                                    }
                                                    criticalSectionSummary += "\t"+target.isStatic();

                                                    if(target.isSynchronized()==true) {
                                                        node1SString += "<TD bgcolor=\"green\">isSynchronized</TD>"; 
                                                    }
                                                    else { 
                                                        node1SString += "<TD bgcolor=\"red\">isSynchronized:</TD>"; 
                                                    }
                                                    criticalSectionSummary += "\t"+target.isSynchronized();

                                                    node1SString += "<TD bgcolor=\"green\">allocations:"+allocations+"</TD>";
                                                    node1SString += "<TD bgcolor=\"green\">statementCount:"+statementCount+"</TD>";
                                                    node1SString += "</TR></TABLE>>";

                                                    
                                                    if(target.isSynchronized()){
                                                        DotGraphNode node1 = callGraphOutput.getNode(target.getName());
                                                        node1.setHTMLLabel(node1SString);
                                                    }
                                                    if(targetsLoops.hasNestedLoops()){
                                                        DotGraphNode node1 = callGraphOutput.getNode(target.getName());
                                                        node1.setStyle("filled");
                                                    }
                                                    if(target.isConstructor()){
                                                        DotGraphNode node1 = callGraphOutput.getNode(target.getName());
                                                        node1.setShape("diamond");  
                                                    }

                                                    if(target.isJavaLibraryMethod()){
                                                        DotGraphNode node1 = callGraphOutput.getNode(target.getName());                                        
                                                        node1.setShape("house");  
                                                    }

                                                    if(subMethod.hasActiveBody()){
                                                        LoopNestTree subMethodLoops = new LoopNestTree(subMethod.getActiveBody());
                                                        if(subMethodLoops.hasNestedLoops()){
                                                            DotGraphNode node2 = callGraphOutput.getNode(subMethod.getName());
                                                            node2.setStyle("filled");
                                                        }
                                                    }
                                                    if(subMethod.isSynchronized()){
                                                        DotGraphNode node2 = callGraphOutput.getNode(subMethod.getName());
                                                        //node2.setHTMLLabel(node2SString);
                                                        node2.setShape("star");
                                                        node2.setStyle("filled,fillcolor=red");
                                                    }
                                                    if(subMethod.isConstructor()){
                                                        DotGraphNode node2 = callGraphOutput.getNode(subMethod.getName());
                                                        node2.setShape("diamond");
                                                    }
                                                    if(subMethod.isJavaLibraryMethod()){
                                                        DotGraphNode node2 = callGraphOutput.getNode(subMethod.getName());
                                                        node2.setShape("house");
                                                    }
                                    }
                                    walkGraph(subMethod,levels-1);
                            }
                        }
                    }
                }

        }
    } // end function

    // Find the name of a method, if it exists, then increment its count,
    // otherwise add it.
    public void searchMethodInfo(methodInfo m){
        boolean methodExists = false;
        for(int i =0 ; i < methodInfoNames.size(); i++){
            if(methodInfoNames.get(i).name.equals(m.name)){
                methodInfoNames.get(i).incrementCalls();
                methodExists = true;
                break;
            }
        }

        if(methodExists==false){
            methodInfoNames.add(m);
            m.incrementCalls();
        }
    }

    
    public void generateFile(String projectName){
        // Output files placed in the directory of the critical section.
        FileWriter writerCriticalSections;
        FileWriter writerCriticalSectionSummary;
        FileWriter writerJimpleStatistics;
        FileWriter writerAnnotatedJimple;
        FileWriter writerMethodCalls;
        FileWriter writerMethodInfo;
        
        // Directory where we generate information
        String directoryName = "CriticalSections";
        
        // Create a new directory if it doesn't exist for us to store our
        // files in
        try{
            boolean success = ( new File("/Users/michaelshah/Documents/sootDump/"+projectName+"/"+directoryName+"/"+nameOfCriticalSection).mkdirs());
            if(!success){
                    G.v().out.println("Unable to make directory");
            }
            
                        
            // Output Jimple Code
            File criticalSectionJimple = new File("/Users/michaelshah/Documents/sootDump/"+projectName+"/"+directoryName+"/"+nameOfCriticalSection+"/"+nameOfCriticalSection+"_Summary.txt");
            writerCriticalSections = new FileWriter(criticalSectionJimple);
                writerCriticalSections.write("//*==============v=Pure Jimple Code=v==============*/\n");
                // Output all of the statements in a critical section
                String []statements = this.getStatments();
                for(int i = 0; i < statements.length; i++ ){
                    writerCriticalSections.write(statements[i]+"\n");
                }
                writerCriticalSections.write("//*==============^=Pure Jimple Code=^==============*/\n");
            writerCriticalSections.flush();
            writerCriticalSections.close();


            // Output the summary of methods found in Critical section.
            File criticalSectionSummary = new File("/Users/michaelshah/Documents/sootDump/"+projectName+"/"+directoryName+"/"+nameOfCriticalSection+"/"+nameOfCriticalSection+"_Summary.txt");
            writerCriticalSectionSummary = new FileWriter(criticalSectionSummary);
                // Output a summary of all of the methods in a critical section
                writerCriticalSectionSummary.write("//*==============v=Summary=v==============*/\n");
                writerCriticalSectionSummary.write("ctor\tentry\tnest loop\tjavalib\tscope\tstatic\tsynch");
                writerCriticalSectionSummary.write(getCriticalSectionSummary());
                writerCriticalSectionSummary.write("\n//*==============v=Summary=v==============*/\n");
            writerCriticalSectionSummary.flush();
            writerCriticalSectionSummary.close();


            // Output the statistics of how many of each statements are found in critical section
            File jimpleStatisticsSectionSummary = new File("/Users/michaelshah/Documents/sootDump/"+projectName+"/"+directoryName+"/"+nameOfCriticalSection+"/"+nameOfCriticalSection+"_jimpleStatistics.txt");
            writerJimpleStatistics = new FileWriter(jimpleStatisticsSectionSummary);
                String []statementSummary = this.getStatementTypes();
                writerJimpleStatistics.write("//*==============v=Jimple Statement Statistics=v==============*/\n");
                writerJimpleStatistics.write("AssignStmt: "+statementTypes[0]+"\n");
                writerJimpleStatistics.write("GotoStmt: "+statementTypes[1]+"\n");
                writerJimpleStatistics.write("DefinitionStmt: "+statementTypes[2]+"\n");
                writerJimpleStatistics.write("EnterMonitorStmt: "+statementTypes[3]+"\n");
                writerJimpleStatistics.write("ExitMonitorStmt: "+statementTypes[4]+"\n");
                writerJimpleStatistics.write("GotoStmt: "+statementTypes[5]+"\n");
                writerJimpleStatistics.write("IdentityStmt: "+statementTypes[6]+"\n");
                writerJimpleStatistics.write("IfStmt: "+statementTypes[7]+"\n");
                writerJimpleStatistics.write("InvokeStmt: "+statementTypes[8]+"\n");
                writerJimpleStatistics.write("LookupSwitchStmt: "+statementTypes[9]+"\n");
                writerJimpleStatistics.write("MonitorStmt: "+statementTypes[10]+"\n");
                writerJimpleStatistics.write("NopStmt: "+statementTypes[11]+"\n");
                writerJimpleStatistics.write("RetStmt: "+statementTypes[12]+"\n");
                writerJimpleStatistics.write("ReturnStmt: "+statementTypes[13]+"\n");
                writerJimpleStatistics.write("ReturnVoidStmt: "+statementTypes[14]+"\n");
                writerJimpleStatistics.write("TableSwitchStmt: "+statementTypes[15]+"\n");
                writerJimpleStatistics.write("ThrowStmt: "+statementTypes[16]+"\n");
                writerJimpleStatistics.write("//*==============^=Jimple Statement Statistics=^==============*/\n");
            writerJimpleStatistics.flush();
            writerJimpleStatistics.close();


            // Output Jimple code with annotations.
            File fileAnnotatedJimple = new File("/Users/michaelshah/Documents/sootDump/"+projectName+"/"+directoryName+"/"+nameOfCriticalSection+"/"+nameOfCriticalSection+"_JimpleCode_Annotated.jimple");
            writerAnnotatedJimple = new FileWriter(fileAnnotatedJimple);
                writerAnnotatedJimple.write("//* (These are the statements within a single critical sections body)*/\n\n");
                
                writerAnnotatedJimple.write("//*==============v=Annotated Jimple Code Summary=v==============*/\n");
                for(int i = 0; i < statementSummary.length; i++ ){
                    writerAnnotatedJimple.write(statementSummary[i]+"\n");
                }
                writerAnnotatedJimple.write("//*==============^=Annotated Jimple Code Summary=^==============*/\n\n");
            writerAnnotatedJimple.flush();
            writerAnnotatedJimple.close(); 


            // Output all of the method calls in a list in the critical seciton
            File newFile = new File("/Users/michaelshah/Documents/sootDump/"+projectName+"/"+directoryName+"/" + nameOfCriticalSection +"/"+ nameOfCriticalSection + ".methodCalls");
            newFile.createNewFile();
            writerMethodCalls = new FileWriter(newFile);
                String []methodSummary = this.getMethods();
                writerMethodCalls.write("//*==============v=Methods Calls=v==============*/\n");
                writerMethodCalls.write("Method Calls: "+methodSummary.length+"\n");
                for(int i = 0; i < methodSummary.length; i++ ){
                    writerMethodCalls.write(methodSummary[i]+"\n");
                }
                writerMethodCalls.write("//*==============^=Methods Calls=^==============*/\n\n");
            writerMethodCalls.flush();
            writerMethodCalls.close();    

            // Output all of the method call information
            File methodInfoFile = new File("/Users/michaelshah/Documents/sootDump/"+projectName+"/"+directoryName+"/" + nameOfCriticalSection +"/"+ nameOfCriticalSection + ".methodInfo");
            methodInfoFile.createNewFile();
            writerMethodInfo = new FileWriter(methodInfoFile);
                writerMethodInfo.write("//*==============v=Methods Info and Statistics=v==============*/\n");
                for(int i = 0; i < methodInfoNames.size(); i++ ){
                    writerMethodInfo.write(methodInfoNames.get(i).output()+"\n");
                }
                writerMethodInfo.write("//*==============^=Methods Info and Statistics=^==============*/\n\n");
            writerMethodInfo.flush();
            writerMethodInfo.close(); 
            

        }
        catch(IOException e){
            // Output error here
            G.v().out.println(e.toString());
        }
        



    }
    
}
