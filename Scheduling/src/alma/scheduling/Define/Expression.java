/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File Expression.java
 */
 
package alma.scheduling.Define;

import java.lang.reflect.*;
import java.util.*;

/**
 * The Expression class accepts a logical expression in the form of a 
 * string, parses and stores it.  There is also a method to evaluate
 * the expression, which yields a boolean.  The logical expression has 
 * the following definition.
 * Syntax:
 * <ul>
 * 	<li>	&lt;logical_expression&gt; :=
 *    <ul>
 * 		<li>	'(' &lt;logical_expression&gt; ')'
 * 		<li>	'!' '(' &lt;logical_expression&gt; ')'
 * 		<li>	&lt;logical_expression&gt; '&&' 	&lt;logical_expression&gt;
 * 		<li>	&lt;logical_expression&gt; '||' &lt;logical_expression&gt;
 * 		<li>	&lt;function_name&gt; &lt;comparison_operator&gt; &lt;numeric_value&gt;
 * 		<li>	&lt;numeric_value&gt; &lt;comparison_operator&gt; &lt;function_name&gt;
 *    </ul>
 *  <li>	&lt;comparison_operator&gt; := '==' | '!=' | '&lt;' | '&lt;=' | '&gt;' | '&gt;='
 * 	<li>	&lt;numeric_value&gt;  := a valid floating point number
 * </ul>
 * 
 * There are two things that must be configured prior to using this class.
 * The first is to supply the names of external fuctions that can appear in
 * expressions.  The second is to configure method names for evaluating those 
 * functions.  Failure to properly configure these names will result in a
 * null pointer exception, which is intentional.  This two step operation 
 * must be done in this order; setting the function names and then setting
 * the method names.
 * 
 * The function names that are part of the logical expression are static 
 * strings defined as part of the Expression class.  These must be defined 
 * before any expressions are parsed.  This is done via the static method 
 * setFunctionNames.  The Expression constructor parses the strings containing 
 * these expressions.
 * 
 * Supplying the method names that correspond to the function names is done via
 * the static setMethods method.  This must be done prior to any evaluation
 * of any function.  Java reflection is used to execute the functions
 * that correspond to the names.
 * 
 * @version $Id: Expression.java,v 1.5 2006/04/11 21:13:37 sslucero Exp $
 * @author Allen Farris
 */
public class Expression {
	
	// These following section defines the functions in the logical expressions.  
	private static String[] functionName;
	private static Method[] method;
	private static Object[] obj;

	
	/**
	 * A static method to define the function names that may be part of an expression.
	 */
	public static void setFunctionNames(String[] function) {
		functionName = new String [function.length];
		for (int i = 0; i < function.length; ++i)
			functionName[i] = function [i];
	}

	/**
	 * A static method to get the names of the functions.
	 */
	public static String[] getFunctionNames() {
		return  functionName;
	}
	/**
	 * A static method to get the name of the function given its index.
	 */
	public static String getFunctionName(int n) {
		return  functionName[n];
	}
	/**
	 * A static method to get the index of the named function.
	 */
	public static int getIndex(String name) {
		for (int i = 0; i < functionName.length; ++i) {
			if (name.equals(functionName[i]))
				return i;
		}
		return -1;	
	}
	
	/**
	 * A static method to supply the methods used to evaluate functions.
	 * The method names must be fully qualified names relative tothe Java 
	 * classpath.  They must also correspond to the same order as the names
	 * of the functions supplied in setFunctionNames.
	 * 
	 */
	public static void setMethods(Object[] o) {
		if (o.length != functionName.length)
			throw new IllegalArgumentException(
			"Expression initialization error!  The number of objects (" + 
			o.length + ") is not equal to the number of function names (" + 
			functionName.length + ").");
		method = new Method [functionName.length];
		obj = new Object [functionName.length];
		for (int i = 0; i < obj.length; ++i)
			obj[i] = o[i];
		Class classObj = null;
		for (int i = 0; i < functionName.length; ++i) {
			try {
				classObj = obj[i].getClass();
				method[i] = classObj.getMethod("compute");
				//method[i] = classObj.getMethod("compute",null);
			} catch (NoSuchMethodException err) {
				System.out.println("Invalid syntax defining functions! " +
					"There is no \"compute()\" method in " + classObj.getName());
				System.exit(0);
			}
		}
	}

	/**
	 * A static method to evaluate the function given its index.
	 */
	public static float execute(int n) {
		try {
            Double d =  (Double)(method[n].invoke(obj[n]));
            //Double d =  (Double)(method[n].invoke(obj[n],null));
			return (float)d.doubleValue();
		} catch (Exception err) {
			System.out.println("Oops! This isn't supposed to happen.");
			err.printStackTrace(System.out);
			System.exit(0);
		}
		return 0.0F;
	}


	/**
	 * Create an Expression object, given a string to be parsed.
	 */
	public Expression(String expression) {
		if (expression == null)
			throw new IllegalArgumentException("Expression cannot be null.");
		this.expression = expression;
		position = 0;
		parenCount = 0;
		// In the StringTokenizer, we want the delimiters returned.
 		list = parse(new StringTokenizer(expression,delimiters,true));
	}
	
	/**
	 * Evaluate the logical expression.
	 */
	public boolean evaluate() {
		if (list.isEmpty())
			return false;
		
		Stack stack = new Stack ();
		Pair p = null;
		float f1 = 0.0F;
		float f2 = 0.0F;
		boolean b1 = false;
		boolean b2 = false;
		String s1 = "";
		
		try {
			for (int i = 0; i < list.size(); ++i) {
				p = (Pair)list.get(i);
				switch (p.type) {
				case NAME:
					f1 = execute(((Integer)(p.value)).intValue());
					stack.push(new Pair(FLOAT,new Float(f1)));
					break;
				case FLOAT:
					stack.push(p);
					break;
				case COMPARISON_OPERATOR:
					f2 = ((Float)((Pair)stack.pop()).value).floatValue();
					f1 = ((Float)((Pair)stack.pop()).value).floatValue();
					s1 = (String)p.value;
					if (s1.equals("=="))       b1 = f1 == f2;
					else if (s1.equals("!="))  b1 = f1 != f2;
					else if (s1.equals("<"))   b1 = f1 < f2;
					else if (s1.equals("<="))  b1 = f1 <= f2;
					else if (s1.equals(">"))   b1 = f1 > f2;
					else if (s1.equals(">="))  b1 = f1 >= f2;
					stack.push(new Pair(BOOLEAN,new Boolean(b1)));
					break;
				case NOT_OPERATOR:
					b1 = ((Boolean)((Pair)stack.pop()).value).booleanValue();
					stack.push(new Pair(BOOLEAN,new Boolean(!b1)));
					break;
				case AND_OPERATOR:
					b2 = ((Boolean)((Pair)stack.pop()).value).booleanValue();
					b1 = ((Boolean)((Pair)stack.pop()).value).booleanValue();
					stack.push(new Pair(BOOLEAN,new Boolean(b1 && b2)));
					break;
				case OR_OPERATOR:
					b2 = ((Boolean)((Pair)stack.pop()).value).booleanValue();
					b1 = ((Boolean)((Pair)stack.pop()).value).booleanValue();
					stack.push(new Pair(BOOLEAN,new Boolean(b1 || b2)));
					break;
				default:
					throw new RuntimeException(
					"Internal runtime error!  Invalid type of element in list.");
				}
			}
			// check stack -- should be one element -- a boolean
			if (stack.size() != 1)
				throw new RuntimeException(
				"Internal runtime error!  Stack != 1. (size is " + 
				stack.size() + ")");
			return ((Boolean)((Pair)stack.pop()).value).booleanValue();
		} catch (ClassCastException err) {
			throw new RuntimeException("Internal runtime error!");
		} catch (EmptyStackException err) {
			throw new RuntimeException("Internal runtime error!");
		}
	}


	/**
	 * The toString method displays the translated expression in its 
	 * internal postfix form.  It is intended mainly for debugging.
	 */	
	public String toString() {
		if (list == null)
			return "";
		StringBuffer str = new StringBuffer ();
		Pair p = null;
		for (int i = 0; i < list.size(); ++i) {
			p = (Pair)list.get(i);
			if (p.type == NAME)
				str.append(getFunctionName(((Integer)p.value).intValue()));
			else
				str.append(p.value);
			if (i < (list.size() - 1))
				str.append(",");
		}
		return str.toString();
	}
		
	///////////////////////////////////////////////////////////////////////////////
	// The follwing code all relates to the parser.
	///////////////////////////////////////////////////////////////////////////////	
	
	/*
	 * Discussion
	 * 
	 * The method used to translate the logical expression into an ordered 
	 * sequence of values and operators in a postfix notaion is based on a 
	 * slightly different version of the grammar indicated at the beginning of 
	 * this class.  The actual grammar used is the following:
	 * 
	 * LogicalExpression :=
	 * 		OrTerm '||' OrTerm '||' ...
	 * OrTerm :=
	 * 		AndTerm '&&' AndTerm '&&' ...
	 * AndTerm :=
	 * 		ComparisonExpression
	 * 		'(' LogicalExpression ')'
	 * 		'!' '(' LogicalExpression ')'
	 * ComparisonExpression :=
	 * 		FunctionName ComparisonOperator NumericValue
	 * 		NumericValue ComparisonOperator FunctionName
 	 * ComparisonOperator := 
 	 * 		'==' | '!=' | '<' | '<=' | '>' | '>='
	 * NumericValue  := 
	 * 		a valid floating point number
	 * 
	 * A little thought will show that these two definitions are equivalent.  
	 * The actual parser/translator uses an or-list and an and-list to 
	 * accumulate logical terms in a postfix notation.  These lists then get 
	 * combined into the final list.  
	 * 
	 * The evaluator method uses the final list and a stack in order to carry 
	 * out the operations defined by the list.  The top of the stack, which 
	 * must be a boolean value, is returned.
	 */
	
	/**
	 * The array list is really the output of the constructor and is the 
	 * translation of the logical expression into a series of operands and 
	 * operations using postfix notation.
	 */
	private ArrayList list;
	// Expression and position are used to craft meaningful error messages.
	private String expression;
	private int position;
	// Parencount is used to check for unbalenced parentheses.
	private int parenCount;
	
	// The pair class defines the contents of the list.
	class Pair {
		int type;
		Object value;
		Pair(int type, Object value) {
			this.type = type;
			this.value = value;
		}
	};
	// These are the values for type.
	private static final int OPEN_PAREN_OPERATOR		= 0;	
	private static final int CLOSE_PAREN_OPERATOR	= 1;	
	private static final int NOT_PAREN_OPERATOR		= 2;
	private static final int NOT_OPERATOR			= 3;
	private static final int AND_OPERATOR			= 4;	
	private static final int OR_OPERATOR				= 5;	
	private static final int COMPARISON_OPERATOR		= 6;	
	private static final int NAME					= 7;
	private static final int FLOAT					= 8;
	private static final int BOOLEAN					= 9;
	
	// The delimiters are the grammar characters plus the whitespace chraacters.
	private static final String delimiters = "()!&|=<> \t\n\r\f";
	private static final String grammar = "()!&|=<>";
	private static final String whitespace = " \t\n\r\f";
	
	private void error(String message) {
		StringBuffer s = new StringBuffer();
		s.append("Invalid syntax in logical expression:\n");
		if (message != null && message.length() > 0) {
			s.append(message);
			s.append("\n");
		}
		s.append(expression);
		s.append('\n');
		for (int i = 0; i < position - 1; ++i)
			s.append(' ');
		s.append('^');
    	throw new IllegalArgumentException(s.toString());
 	}

	
	// The getElement method ignores whitespace, collects tokens into operators 
	// and returns the next lexical element as a Pair.  If there are no more 
	// tokens null is returned.
	private Pair getElement(StringTokenizer s) {
		// The next token.
     	String token = null;
     	// We want to collect tokens into operators.
     	String operator = null;
     	
     	while (s.hasMoreTokens()) { 
     		token = s.nextToken();
     		
     		// First, deal with the whitespace characters.  We discard them.
     		if (whitespace.indexOf(token) != -1) {
     			++position;
     			continue;
     		}
     		
     		// Second, deal with the grammar characters and return the operators.
     		if (grammar.indexOf(token) != -1) {
     		  	try { // Try handles unexpected end of expressions.
     				++position;
     				if (token.equals("&")) {
     					token = s.nextToken();
     					++position;
     					if (!token.equals("&"))
     						error("Invalid logical operator");
						return new Pair(AND_OPERATOR,"&&");
     				} else if (token.equals("|")) {
     					token = s.nextToken();
     					++position;
     					if (!token.equals("|"))
     						error("Invalid logical operator");
						return new Pair(OR_OPERATOR,"||");
     				} else if (token.equals("=")) {
     					token = s.nextToken();
     					++position;
     					if (!token.equals("="))
     						error("Invalid comparison operator");
     					return new Pair(COMPARISON_OPERATOR,"==");
     				} else if (token.equals("!")) {
     					token = s.nextToken();
      					++position;
     					if (token.equals("="))
     						return new Pair(COMPARISON_OPERATOR,"!=");
     					else {
     						while (whitespace.indexOf(token) != -1) {
     							token = s.nextToken();
    							++position;
     						}
     						if (!token.equals("("))
     							error("The not operator can only preceed an expression in parentheses.");
     						return new Pair(NOT_PAREN_OPERATOR,"!(");
     					}
       				} else if (token.equals("<")) {
     					token = s.nextToken();
     					++position;
     					if (token.equals("="))
     						return new Pair(COMPARISON_OPERATOR,"<=");
     					else
     						return new Pair(COMPARISON_OPERATOR,"<");
      				} else if (token.equals(">")) {
     					token = s.nextToken();
     					++position;
     					if (token.equals("="))
     						return new Pair(COMPARISON_OPERATOR,">=");
     					else
     						return new Pair(COMPARISON_OPERATOR,">");
      				} else if (token.equals("(")) {
      					return new Pair(OPEN_PAREN_OPERATOR,"(");
     				} else if (token.equals(")")) {
      					return new Pair(CLOSE_PAREN_OPERATOR,")");
     				} else
						error("Unrecognizable operator.");
     		  	} catch (NoSuchElementException err) {
     		  		error("Unexpected end of expression.");    		  		
     		  	}
    		}

    		// At this point, the next token must be either a name or numeric.
    		position += token.length();
    		int index = -1;
			for (int i = 0; i < functionName.length; ++i) {
				if (functionName[i].equals(token)) {
					index = i;
					break;
				}
			}
			if (index == -1) {
    			// The token is numeric.
    			try {
    				return new Pair(FLOAT,new Float(token));
    			} catch (NumberFormatException err) {
    				error("Either a function name or a numeric value was expected.  This is neither!");
    			}
			} else {
    			// The token is a name.
				return new Pair(NAME,new Integer(index));
 			}
    		
     	}
		return null;
	}
	
	// Massage the andList and add its items to the orList.
	private void addList(ArrayList orList, ArrayList andList) {
		if (andList.isEmpty())
			return;
		// We count the Ands and put them at the end, while adding 
		// elements to the or-list.
		Pair p = null;
		int numberAnds = 0;
		for (int i = 0; i < andList.size(); ++i) {
			p = (Pair)andList.get(i);
			if (p.type == AND_OPERATOR)
				++numberAnds;
			else
				orList.add(p);
		}
		for (int i = 0; i < numberAnds; ++i)
			orList.add(new Pair(AND_OPERATOR,"&&"));
	}
	
	// Massage the or-list.
	private ArrayList massageOrList(ArrayList orList) {
		if (orList.isEmpty())
			return orList;
		Pair p = null;
		int numberOrs = 0;
		ArrayList tmp = new ArrayList ();
		// We place the Ors at the end of the list.
		for (int i = 0; i < orList.size(); ++i) {
			p = (Pair)orList.get(i);
			if (p.type == OR_OPERATOR)
				++numberOrs;
			else
				tmp.add(p);
		}
		for (int i = 0; i < numberOrs; ++i)
			tmp.add(new Pair(OR_OPERATOR,"||"));
		return tmp;
	}

	// This is the parser that translates the string into a list.
	private ArrayList parse(StringTokenizer s) {

		ArrayList orList = new ArrayList ();
		ArrayList andList = new ArrayList ();
		
		ArrayList tmp = null;
		Pair p = getElement(s);
		Pair psave = null;
		
		while (p != null) {
			
			switch (p.type) {
			case OPEN_PAREN_OPERATOR:
				++parenCount;
				tmp = parse(s);
				orList.addAll(tmp);
				break;
			case CLOSE_PAREN_OPERATOR: 
				--parenCount;
				if (parenCount < 0)
					error("Unbalanced parentheses.");
				addList(orList,andList);
				orList = massageOrList(orList);
				return orList;
			case NOT_PAREN_OPERATOR:
				++parenCount;
				tmp = parse(s);
				tmp.add(new Pair(NOT_OPERATOR,"!"));
				orList.addAll(tmp);
				break;
			case AND_OPERATOR:
				// Put the and operator on the and-list.
				andList.add(new Pair(AND_OPERATOR,"&&"));
				break;
			case OR_OPERATOR:
				// Massage the and-list, place it on the or-list, 
				// and empty the and-list.
				addList(orList,andList);
				andList.clear();
				// put the or operator on the or-list.
				orList.add(new Pair(OR_OPERATOR,"||"));
				break;
			case NAME:
				// Get the commparison expression and put on and-list.
				andList.add(p);
				psave = getElement(s);
				if (psave.type != COMPARISON_OPERATOR)
					error("Expected a comparison operator.");
				p = getElement(s);
				if (p.type != FLOAT)
					error("Expected a floating point numeric value.");
				andList.add(p);
				andList.add(psave);
				break;
			case FLOAT:
				// Get the commparison expression and put on and-list.
				andList.add(p);
				psave = getElement(s);
				if (psave.type != COMPARISON_OPERATOR)
					error("Expected a comparison operator.");
				p = getElement(s);
				if (p.type != NAME)
					error("Expected a function name.");
				andList.add(p);
				andList.add(psave);
				break;
			default:
				error("Internal error!  Unidentified lexical type.");
			}
			
			p = getElement(s);
		}
		
		if (parenCount != 0)
			error("Unbalanced parentheses.");
		addList(orList,andList);
		orList = massageOrList(orList);
		return orList;
		
	}

	////////////////////////////////////////////////
	// The follwing methods relate to the unit test.
	////////////////////////////////////////////////

	public static void doTest(String expression) {
		// The follwing expressions in try blocks should give syntax errors.
		try {
			Expression p = new Expression(expression);
			System.out.println();
			System.out.println("Expression: " + expression);
			System.out.println(p.toString());
			System.out.println("expression is " + p.evaluate());	
		} catch (IllegalArgumentException err) {
			System.out.println(err.toString());
		}
	}

	/**
	 * Provide a unit test.
	 */
	public static void main(String[] args) {
		
		// First, set the function names.
		String[] names = {
			"temperature",
			"humidity",
			"pressure",
			"windVelocity",
			"windDirection"
		};
		Expression.setFunctionNames(names);
		System.out.println("OK, static functions are set.");
		// OK, done.
		
		// Now, we set the methods that correspond to the functions.
		// Have to modify this.
		// Simulator sim = new Simulator(null,null,null,
		//				   new Time(2003,2,26,10,0,0), 
		//				   new Time(2003,2,28,10,0,0));
		// EnvironmentalModel.setSimulator(sim);
		String[] methods = {
			"schedule.EnvironmentalModel.getTemperature",
			"schedule.EnvironmentalModel.getHumidity",
			"schedule.EnvironmentalModel.getPressure",
			"schedule.EnvironmentalModel.getWindVelocity",
			"schedule.EnvironmentalModel.getWindDirection"
		};
		Expression.setMethods(methods);
		System.out.println("OK, methods are set.");

		// Try and execute the functions.
		String[] s = Expression.getFunctionNames();
		for (int i = 0; i < s.length; ++i)
			System.out.println("function " + Expression.getIndex(s[i]) + 
			" is " + s[i]);	
		float x;
		for (int i = 0; i < s.length; ++i) {
			x = Expression.execute(i);
			System.out.println("i = " + i + " x = " + x);
		}

		System.out.println("");
		
		Expression p = null;
		
		// The following expressions should give syntax errors.
		doTest("! humidity <= 10.0");
		doTest("(humidity <= 10.0) && ((temperature <= 10.0) && (windVelocity  <= 3.5)");
		doTest("(humidity <= 10.0)) && (temperature <= 10.0) && (windVelocity  <= 3.5)");
		doTest("humidity => 10.0");
		doTest("humidety <= 10.0");
		doTest(null);
		
		// The follwoing expressions should be valid and should parse.
		doTest("");
		doTest("humidity <= 10.0");
		doTest("(humidity <= 10.0) && (temperature <= 10.0) && (windVelocity  <= 3.5)");
		doTest("humidity <= 10.0 || temperature <= 10.0 && windVelocity  <= 3.5");
		doTest("humidity <= 10.0 && temperature <= 10.0 || windVelocity  <= 3.5");
		doTest("humidity > 80.0 && temperature <= 10.0 || windVelocity  <= 5.5");
		
	}

	
}
