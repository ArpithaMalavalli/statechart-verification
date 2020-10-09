package visitors;

//import stablcfg.CFGNode;
//import program.IProgram;
import visitors.IExprVisitor;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;


import symbolic_execution.se_tree.*;
import ast.*;
//public class SETExpressionVisitor implements IExprVisitor<Expression> {
public class SETExpressionVisitor {

	private SETNode mNode;
	private Stack<Expression> mStack = new Stack<Expression>();
	private final String mContextType; 
	public static Set<String> symvars=new HashSet<String>();
	public SETExpressionVisitor(SETNode node, Type type) {
		this.mNode = node;
		this.mContextType = type.toString();		
	}

	//@Override
public void visit(Expression exp) {
//		System.out.println("Class of expression: "+exp.getClass());
		if(exp instanceof Name) {
			this.visit((Name)exp);
		}
		else if(exp instanceof BinaryExpression) {
			this.visit((BinaryExpression)exp);
		}
		else if(exp instanceof UnaryExpression) {
			this.visit((UnaryExpression)exp);
		}
		else if(exp == null) {
			
		}
		else {
			//Exception e = new Exception("SETExpressionVisitor : Type '" + exp.getClass().getCanonicalName() + "' of expression not handled.");
			//throw e;
			
		}
		
	}
	public static Expression visit(FunctionCall exp) {
		//this.mStack.push(new IntegerConstant(exp.getValue(), this.mNode.getSET()));
		//Map<Declaration,Expression> m=new HashMap<Declaration,Expression>();
		//m.put(exp.getDeclaration(),generateNewVariableName(symvars));
		System.out.println("Function Call : ");//+exp.getDeclaration());
		Name name = SETExpressionVisitor.generateNewVariableName(symvars);
		
		return name;
	}
	
	private static Name generateNewVariableName (Set<String> names) {
		
		while (true) {
			Random random = new Random ();
			int integer = random.nextInt();
			if (integer < 0) {
				integer = -1 * integer;
			}
			String name = "symvar" + Integer.toString(integer);
			if (!names.contains(name)) {
				return new Name(name);
			}
		}
	}
	public  Expression visit(UnaryExpression exp) {
		//exp.accept(this);
		//this.mStack.push(new UnaryExpression(this.mNode.getSET(), this.mStack.pop(),exp.getOperator()));
		return null;
	}
	public Expression visit(BinaryExpression exp){
		//exp.accept(this);
		System.out.println("Binary : ");
		//Expression lhs = this.visit(exp.left);
		//Expression rhs = this.visit(exp.right);
		//return new BinaryExpression(lhs, rhs, exp.operator);
		return new BinaryExpression(exp.left, exp.right, exp.operator);
	}
	//@Override
	public Expression getValue() {
		return this.mStack.peek();
	}

	public Stack<Expression> getStack() {
		return mStack;
	}
	/*@Override
	public void visit(Input exp) {
		IProgram p = this.mNode.getSET();
//        for(SETNode node:((SET) p).getNodeSet()){
//            System.out.println("p ka node set:" + node.getCFGNode());
//        }
		Set<Name> variables = p.getVariables();
        for(Name var:variables){
//            System.out.println("p ka vars:" + var);
        }
//        System.out.println("variables = " + variables);
		Set<String> names = new HashSet<String>();
		for(Name v : variables) {
			names.add(v.getName());
		}
//        System.out.println("Names:"+names);
		String name = SETExpressionVisitor.generateNewVariableName(names);
		try {
		    //here new variable is generated.
//            System.out.println("Before before adding variables:"+this.mNode.getSET().getVariables());
            Variable x = new Variable(name, this.mContextType, this.mNode.getSET());
			this.mStack.push(x);
//            System.out.println("Before adding variables:"+this.mNode.getSET().getVariables());
//            System.out.println("After adding variables:"+this.mNode.getSET().getVariables());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void visit(BooleanInput exp) {
		IProgram p = this.mNode.getSET();
		Set<Name> variables = p.getVariables();
		Set<String> names = new HashSet<String>();
		for(Name v : variables) {
			names.add(v.getName());
		}
		String name = SETExpressionVisitor.generateNewVariableName(names);
		try {
			this.mStack.push(new BooleanVariable(name, p));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override*/
	
        /*
	@Override
	public void visit(GreaterThanExpression exp) throws Exception {
		exp.accept(this);
		System.out.println("mStack: "+this.mStack);
		Expression lhs = this.mStack.pop();
		Expression rhs = this.mStack.pop();
		this.mStack.push(new GreaterThanExpression(this.mNode.getSET(), lhs, rhs));
	}

	@Override
	public void visit(AddExpression exp) throws Exception {
		exp.accept(this);
		Expression lhs = this.mStack.pop();
		Expression rhs = this.mStack.pop();
		this.mStack.push(new AddExpression(this.mNode.getSET(), lhs, rhs));
	}

	@Override
	public void visit(SubExpression exp) throws Exception {
		exp.accept(this);
		Expression lhs = this.mStack.pop();
		Expression rhs = this.mStack.pop();
		this.mStack.push(new SubExpression(this.mNode.getSET(), lhs, rhs));
	}

	@Override
	public void visit(MulExpression exp) throws Exception {
		exp.accept(this);
		Expression lhs = this.mStack.pop();
		Expression rhs = this.mStack.pop();
		this.mStack.push(new MulExpression(this.mNode.getSET(), lhs, rhs));
	}



	@Override
	public void visit(DivExpression exp) throws Exception {
		exp.accept(this);
		Expression lhs = this.mStack.pop();
		Expression rhs = this.mStack.pop();
		this.mStack.push(new DivExpression(this.mNode.getSET(), lhs, rhs));
	}
	
	@Override
	public void visit(GreaterThanEqualToExpression exp) throws Exception {
		exp.accept(this);
		Expression lhs = this.mStack.pop();
		Expression rhs = this.mStack.pop();
		this.mStack.push(new GreaterThanExpression(this.mNode.getSET(), lhs, rhs));
	}
	
	@Override
	public void visit(LesserThanExpression exp) throws Exception {
		exp.accept(this);
		Expression lhs = this.mStack.pop();
		Expression rhs = this.mStack.pop();
		System.out.println(new LesserThanExpression(this.mNode.getSET(), lhs, rhs));
		this.mStack.push(new LesserThanExpression(this.mNode.getSET(), lhs, rhs));
	}
	
	@Override
	public void visit(LesserThanEqualToExpression exp) throws Exception {
		exp.accept(this);
		Expression lhs = this.mStack.pop();
		Expression rhs = this.mStack.pop();
		this.mStack.push(new LesserThanEqualToExpression(this.mNode.getSET(), lhs, rhs));
	}
         */
//	@Override
	

	/*@Override
	public void visit(Variable exp) {
		System.out.println("Latest Value:"+this.mNode.getLatestValue(exp));
		this.mStack.push(this.mNode.getLatestValue(exp));
	}
	

	@Override
	public void visit(BooleanVariable exp) {
		this.mStack.push(this.mNode.getLatestValue(exp));
	}
*/
	
        /*
	@Override
	public void visit(AndExpression exp) throws Exception {
		exp.accept(this);
		Expression lhs = this.mStack.pop();
		Expression rhs = this.mStack.pop();
		this.mStack.push(new AndExpression(this.mNode.getSET(), lhs, rhs));
	}


	@Override
	public void visit(OrExpression exp) throws Exception {
		exp.accept(this);
		Expression lhs = this.mStack.pop();
		Expression rhs = this.mStack.pop();
		this.mStack.push(new OrExpression(this.mNode.getSET(), lhs, rhs));
	}
        */
//	@Override
	
        /*
	@Override
	public void visit(EqualsExpression exp) throws Exception {
		exp.accept(this);
		Expression lhs = this.mStack.pop();
		Expression rhs = this.mStack.pop();
		this.mStack.push(new EqualsExpression(this.mNode.getSET(), lhs, rhs));
	}
        */
	
}
