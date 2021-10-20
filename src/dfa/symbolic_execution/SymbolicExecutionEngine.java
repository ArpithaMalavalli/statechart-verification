package symbolic_execution;

import ast.*;
import symbolic_execution.se_tree.DecisionNode;
import symbolic_execution.se_tree.InstructionNode;
import symbolic_execution.se_tree.SymbolicExpression;
import symbolic_execution.se_tree.SETNode;
import symbolic_execution.se_tree.StateEntryNode;
import symbolic_execution.se_tree.StateExitNode;
import symbolic_execution.se_tree.SymVars;
import symbolic_execution.SymbolicExecutionResult;

import java.util.*;
/*import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;*/
import java.io.IOException;

public class SymbolicExecutionEngine{
    
    private Statechart statechart;
    private static final Integer max_depth = 1;

    public SymbolicExecutionEngine(Statechart statechart){
            this.statechart = statechart;
            //this.execute(statechart,100);
    }
    public Integer get_depth()
    {
        return this.max_depth;
    }

    public void initialize_variables(State st, String scope, SETNode leaf)
    {
        for(Declaration d: st.declarations)
        {
            if(d.scope.name.equals(scope))
            {
                if(d.typeName.name.equals("int"))
                {
                    IntegerConstant i = new IntegerConstant(0);
                    SymbolicExpression exp = new SymbolicExpression(i," ");
                    InstructionNode n = new InstructionNode(d.vname, i, leaf );
                }
                else if(d.typeName.name.equals("string"))
                {
                    StringLiteral s = new StringLiteral(" ");
                    SymbolicExpression exp = new SymbolicExpression(s," ");
                    InstructionNode n = new InstructionNode(d.vname, s, leaf );
                }
                else if(d.typeName.name.equals("boolean"))
                {
                    BooleanConstant b = new BooleanConstant(false);
                    SymbolicExpression exp = new SymbolicExpression(b," ");
                    InstructionNode n = new InstructionNode(d.vname, b, leaf );
                }        
            }
        }
    }

    public List<State> get_all_states(State s)
    {
        List<State> all_states = new ArrayList<State>();
        all_states.add(s);
        if(s.states.isEmpty())
        {
            return all_states;
        }
        State temp =s;
            //all_states.addAll(temp.states);
            for(State st: temp.states)
            {
                all_states.addAll(get_all_states(st));
            }
        return all_states;
    }
   public  SymbolicExecutionResult enterSuperstate(State s, SETNode leaf)
    {
        //System.out.println("enterSuperstate");
        leaf = new StateEntryNode(s, leaf);
        initialize_variables(s, "local", leaf);
        //System.out.println(s.entry);
        return executeBlock(s.entry, leaf);
    }
    public  SymbolicExecutionResult enterSubstate(State s, SETNode leaf)
    {
        leaf = new StateEntryNode(s, leaf);
        initialize_variables(s, "local", leaf);
        initialize_variables(s, "parameter", leaf);
        return executeBlock(s.entry, leaf);
    }
    
    public List<SETNode> execute()
    {
        //initialise all static variables to v.type.init
        SETNode startnode = new SETNode(null);
        List<State> all_states = get_all_states(this.statechart);
        for(State st: all_states)
        {
            initialize_variables(st, "static", startnode);
        }
        //initialize_static(this.statechart);
        //System.out.println(this.statechart.declarations.get(0).vname);
        //System.out.println(this.statechart.declarations.get(0).input);
        List<SETNode> done = new ArrayList<SETNode>();
        List<SETNode> leaves = new ArrayList<SETNode>();
        List<SETNode> leaves_1 = new ArrayList<SETNode>();
        SymbolicExecutionResult res = new SymbolicExecutionResult();
        leaves.add(startnode);

        //conf = compute initial configuration
        List<State> conf = new ArrayList<State>();
        conf.add(this.statechart);
        State start = this.statechart.states.get(0);
        //System.out.println(start);
        conf.add(start);
        while(!start.states.isEmpty())
        {
            start = start.states.get(0);
           
            conf.add(start);
        }
        Collections.reverse(conf);
        for(State s: conf)
        { 
            for(SETNode leaf: leaves)
            { 
                //System.out.println(s.name);
                //if(s.getSuperstate() != null)
                //{
                    res = this.enterSuperstate(s, leaf);
                    done.addAll(res.getDoneNodes());
                
                    if(res.getLiveNodes().isEmpty())
                    {
                        break;
                    }
                    else
                    {
                        leaves_1 = res.getLiveNodes();
                    }
                //}
                //else
                //{
                  //  break;
                //}
            }
            //System.out.println(leaves_1);
            leaves = leaves_1;
        }
        List<Transition> ts = new ArrayList<Transition>();
        for(State st: conf)
        {
            ts.addAll(st.transitions);
        }
        List<Transition> out_ts = new ArrayList<Transition>();
        DeclarationList symvars = new DeclarationList();
        for(Declaration d: this.statechart.declarations)
        {
            if(d.input == true)
            {
                System.out.println(d.vname);
                boolean is_added = symvars.add(d);
            }
        }
        for(Transition tr :ts)
        {
            for(State st: conf)
            {
                if(tr.getSource().name.equals(st.name))
                {
                    out_ts.add(tr);
                }
            }
        }
        String s = " ";
        while(!leaves.isEmpty())
        {
            //out_ts = outgoing transitions from conf
            List<SETNode> leaves_2 = new ArrayList<SETNode>() ;
            for(SETNode leaf: leaves)
            {
                for(Transition t : out_ts)
                {
                    System.out.println(t.guard);
                    Solver solver = new Solver(sym_eval(t.guard, symvars), symvars);
                    try
                    {
                        s = solver.solve();
                        //System.out.println(s);
                    }
                    catch(IOException i)
                    {
                        System.out.println("Error");
                    }
                    if(s.equals("sat"))
                    {
                        System.out.println(t.name);
                        res = takeTransition(t, leaf);
                        done.addAll(res.getDoneNodes());
                        leaves_2.addAll(res.getLiveNodes());
                    }
                }
            }
            leaves = leaves_2;
        }

        res.setDoneNodes(done);
        res.setLiveNodes(leaves);
        return res.getDoneNodes();
    }

    public  SymbolicExecutionResult exitState(State s, SETNode leaf)
    {
        leaf = new StateExitNode(s, leaf);
        return executeBlock(s.exit, leaf);
    }
    public  SymbolicExecutionResult takeTransition(Transition t, SETNode leaf)
    {
        List<SETNode> leaves = new ArrayList<SETNode>();
        leaves.add(leaf);
        List<SETNode> done = new ArrayList<SETNode>();

        /*compute t . sources and t . destination as per StaBL semantics .
        This will include possibly taking into account history
        states in the t . destinations list . For example , if a
        composite state d in t . destinations has history mark , then
        subsequent destination substates should be identified as
        the last active substate at the time of the last exit
        from d .*/

        State s = t.getSource();
        List<SETNode> leaves_1 = new ArrayList<SETNode>();
        for(SETNode l: leaves)
        {
            SymbolicExecutionResult res = exitState(s, l);
            done.addAll(res.getDoneNodes());
            leaves_1.addAll(res.getLiveNodes());
        }
        leaves = leaves_1;

        leaves_1.clear();
        for(SETNode l : leaves)
        {
            System.out.println(t.action);
            SymbolicExecutionResult res = executeBlock(t.action, l);
            done.addAll(res.getDoneNodes());
            leaves_1.addAll(res.getLiveNodes());
        }
        leaves = leaves_1;

        State d = t.getDestination();

        if(d.name.equals(s.getSuperstate().name))
        {
            leaves_1.clear();
            for(SETNode l: leaves)
            {
                SymbolicExecutionResult res = enterSuperstate(d, l);
                done.addAll(res.getDoneNodes());
                leaves_1.addAll(res.getLiveNodes());
            }
            leaves = leaves_1;
        }

        else if(s.name.equals(d.getSuperstate().name))
        {
            leaves_1.clear();
            for(SETNode l: leaves)
            {
                SymbolicExecutionResult res = enterSubstate(d, l);
                done.addAll(res.getDoneNodes());
                leaves_1.addAll(res.getLiveNodes());
            }
            leaves = leaves_1;
        }
        SymbolicExecutionResult res = new SymbolicExecutionResult();

        res.setDoneNodes(done);
        res.setLiveNodes(leaves);
        return res;
    }
    public  SymbolicExecutionResult executeBlock(Statement block, SETNode leaf)
    {
        SymbolicExecutionResult res = new SymbolicExecutionResult();
        List<SETNode> leaves = new ArrayList<SETNode>();
        List<SETNode> leaves_1 = new ArrayList<SETNode>();
        leaves.add(leaf);
        List<SETNode> done = new ArrayList<SETNode>();
        //System.out.println("block");
        if(block instanceof StatementList)
        {
            StatementList statementlist = (StatementList)block;

            for(Statement s: statementlist.getStatements())
            {
                if(s instanceof InstructionStatement)
                {
                    for(SETNode l : leaves)
                    {
                        System.out.println(s);
                        res = executeInstruction(s, l);
                    }
                }
                else if(s instanceof IfStatement)
                {
                    for(SETNode l : leaves)
                    {
                        System.out.println("if");
                        res = executeIf(s, l);
                    }
                }
                else if(s instanceof StatementList)
                {
                    for(SETNode l : leaves)
                    {
                        System.out.println("list");
                        res = executeBlock(s, l);
                    }
                }
                done.addAll(res.getDoneNodes());
                leaves_1.addAll(res.getLiveNodes());
            }
        }
        else
        {
            if(block instanceof InstructionStatement)
                {
                    for(SETNode l : leaves)
                    {
                        System.out.println(block);
                        res = executeInstruction(block, l);
                    }
                }
                else if(block instanceof IfStatement)
                {
                    for(SETNode l : leaves)
                    {
                        System.out.println("if");
                        res = executeIf(block, l);
                    }
                }
                else if(block instanceof StatementList)
                {
                    for(SETNode l : leaves)
                    {
                        System.out.println("list");
                        res = executeBlock(block, l);
                    }
                }
                done.addAll(res.getDoneNodes());
                leaves_1.addAll(res.getLiveNodes());
        }
        res.setDoneNodes(done);
        res.setLiveNodes(leaves_1);
        return res;
    }
    public  SymbolicExecutionResult executeInstruction(Statement i, SETNode leaf)
    {
        //SETNode l = new DecisionNode();
        List<SETNode> done = new ArrayList<SETNode>();
        List<SETNode> leaves = new ArrayList<SETNode>();
        if(i instanceof AssignmentStatement)
        {
            System.out.println("Assignment Statement");
            SETNode l = new InstructionNode(((AssignmentStatement)i).lhs.getDeclaration().vname,((AssignmentStatement)i).rhs, leaf );
            System.out.println(InstructionNode.updates.get(((AssignmentStatement)i).lhs.getDeclaration().vname));
            if(leaf.depth +1 > max_depth)
            {
                done.add(l);
            }
            else{
                leaves.add(l);
            }
        }

        SymbolicExecutionResult res = new SymbolicExecutionResult();

        res.setDoneNodes(done);
        res.setLiveNodes(leaves);
        return res;
    }
    public Expression sym_eval(Expression e, DeclarationList sym_vars)
    {
        Expression expr;

        if(e instanceof BinaryExpression)
        {
            Expression left = sym_eval(((BinaryExpression)e).left, sym_vars);
            Expression right = sym_eval(((BinaryExpression)e).right, sym_vars);
            String operator = ((BinaryExpression)e).operator;

            expr = new BinaryExpression(left, right, operator);
            return expr;
        }
        else if (e instanceof SymbolicExpression)
        {
            if(((SymbolicExpression)e).right != null)
            {
                Expression left = sym_eval(((SymbolicExpression)e).left, sym_vars);
                Expression right = sym_eval(((SymbolicExpression)e).right, sym_vars);
                String operator = ((SymbolicExpression)e).operator;

                expr = new SymbolicExpression(left, right, operator);
                return expr;
            }
            else
            {
                Expression left = sym_eval(((SymbolicExpression)e).left, sym_vars);
                String operator = ((SymbolicExpression)e).operator;

                expr = new SymbolicExpression(left,operator);
                return expr;
            }
        }
        else if(e instanceof BooleanConstant || e instanceof IntegerConstant || e instanceof StringLiteral)
        {
            return e;
        }
        else if(e instanceof Name)
        {
            String s = ((Name)e).getDeclaration().vname;
            System.out.println(((SymbolicExpression)InstructionNode.updates.get(s)).right);
            //Declaration d = sym_vars.lookup(s);
            expr = sym_eval(InstructionNode.updates.get(s), sym_vars);
            return expr;
        }
        else if(e instanceof SymVars)
        {
            System.out.println(e);
            return e;
        }
        return null;
    }
    public SymbolicExecutionResult executeIf(Statement i, SETNode leaf)
    {
        List<SETNode> done = new ArrayList<SETNode>();
        List<SETNode> leaves = new ArrayList<SETNode>();
        DeclarationList symvars = new DeclarationList();
        for(Declaration d: this.statechart.declarations)
        {
            if(d.input == true)
            {
                System.out.println(d.vname);
                boolean is_added = symvars.add(d);
            }
        }
        Expression symeval = sym_eval(((IfStatement)i).condition, symvars);

        Solver solver = new Solver(symeval, symvars);
        String satisfiable = new String();
        try
        {
            satisfiable = solver.solve();
            //System.out.println(s);
        }
        catch(IOException exception)
        {
            System.out.println("Error");
        }

        if(satisfiable.equals("sat"))
        {
            SETNode leaf_1 = new DecisionNode(((IfStatement)i).condition, leaf);
            SymbolicExecutionResult res_1 = executeBlock(((IfStatement)i).then_body, leaf_1);
            done.addAll(res_1.getDoneNodes());
            leaves.addAll(res_1.getLiveNodes());
        }
        else
        {
            done.add(leaf);
        }

        if(satisfiable.equals("unsat"))
        {
            SETNode leaf_1 = new DecisionNode(((IfStatement)i).condition, leaf);
            SymbolicExecutionResult res_2 = executeBlock(((IfStatement)i).else_body, leaf_1);
            done.addAll(res_2.getDoneNodes());
            leaves.addAll(res_2.getLiveNodes());
        }
        else
        {
            done.add(leaf);
        }

        if(leaves.isEmpty())
        {
            SymbolicExecutionResult res = new SymbolicExecutionResult();
            done.add(leaf);
            res.setDoneNodes(done);
            System.out.println("if");
            System.out.println(res.getDoneNodes());
            return res;
        }
        else
        {
            SymbolicExecutionResult res = new SymbolicExecutionResult();
            res.setDoneNodes(done);
            res.setLiveNodes(leaves);
            System.out.println("else");
            System.out.println(res.getDoneNodes());

            return res;
        }
    }
}
