import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InterpreterUnitTest {
	private Lexer lexer;
	private Parser parser;		
	Path myPath = Paths.get("SampleText.awk");
	private Interpreter interpreter;
	private Optional<Node>result;	
	private OperationNode operationNode;
	private AssignmentNode assignmentNode;
	private PatternNode patternNode;
	private BlockNode blockNode;
	private LinkedList <StatementNode> statements = new LinkedList<>();	
	private LinkedList <StatementNode> statements2 = new LinkedList<>();
	private LinkedList <StatementNode> statements3 = new LinkedList<>();
	private LinkedList <Node> conditions = new LinkedList<>();
	private LinkedList <Node> nodes = new LinkedList<>();
	private InterpreterDataType idt;
	private Throwable thrown;
	private HashMap <String, InterpreterDataType> localVariables = new HashMap<>();


	
	//tests the new added parse functions
	@Test
	void Parsertest() {
		FunctionCallNode functionCallNode;
		
		lexer = new Lexer("print");
		parser = new Parser(lexer);
		result = parser.ParseFunctionCall();
		assertTrue(result.isPresent());
		
		functionCallNode = (FunctionCallNode) result.get();		
		assertEquals("print: []", functionCallNode.toString());
		
		lexer = new Lexer("next");
		parser = new Parser(lexer);
		result = parser.ParseFunctionCall();
		assertTrue(result.isPresent());
		
		functionCallNode = (FunctionCallNode) result.get();		
		assertEquals("next", functionCallNode.toString());
		
		lexer = new Lexer("exit");
		parser = new Parser(lexer);
		result = parser.ParseFunctionCall();
		assertTrue(result.isPresent());
		
		functionCallNode = (FunctionCallNode) result.get();		
		assertEquals("exit", functionCallNode.toString());
	}



	@Test
	void Interpreter2Test() throws IOException {
		//initializes the interpreter
		lexer = new Lexer("");
		parser = new Parser(lexer);
		interpreter = new Interpreter(parser.Parse(), myPath);
		//creates a small abstract syntax tree assignment node a = 5+3
		assignmentNode = new AssignmentNode(new VariableReferenceNode("a"), new OperationNode(new ConstantNode("5"), Optional.of(new ConstantNode("3")), OperationNode.Operations.ADD));
		idt = interpreter.GetIDT(assignmentNode, localVariables);
		
		//checks if idt is filled
		assertTrue(idt.toString() != null);
		//checks that the assignment node is correct
		assertEquals("8.0", idt.toString());
		
		//resets the idt
		idt = new InterpreterDataType();
		
		//creates a small abstract syntax tree operation node 25/5
		operationNode = new OperationNode(new ConstantNode("25"), Optional.of(new ConstantNode("5")), OperationNode.Operations.DIVIDE);
		idt = interpreter.GetIDT(operationNode, localVariables);
		
		//checks if the idt is filled
		assertTrue(idt.toString() != null);
		//checks that the operation node is correct
		assertEquals("5.0", idt.toString());
		
		//resets the idt
		idt = new InterpreterDataType();
		
		//creates a small abstract syntax tree operation node 300 > 20
		operationNode = new OperationNode(new ConstantNode("300"), Optional.of(new ConstantNode("20")), OperationNode.Operations.GT);
		idt = interpreter.GetIDT(operationNode, localVariables);
		
		//checks if the idt is filled
		assertTrue(idt.toString() != null);
		//checks that the operation node is correct
		assertEquals("true", idt.toString());
		
		//resets the idt
		idt = new InterpreterDataType();
		
		//creates a small abstract syntax tree operation node 300 >= 300
		operationNode = new OperationNode(new ConstantNode("300"), Optional.of(new ConstantNode("300")), OperationNode.Operations.GE);
		idt = interpreter.GetIDT(operationNode, localVariables);
		
		//checks if the idt is filled
		assertTrue(idt.toString() != null);
		//checks that the operation node is correct
		assertEquals("true", idt.toString());
		
		//resets the idt
		idt = new InterpreterDataType();
		
		//creates a small abstract syntax tree operation node 20^2
		operationNode = new OperationNode(new ConstantNode("20"), Optional.of(new ConstantNode("2")), OperationNode.Operations.EXPONENT);
		idt = interpreter.GetIDT(operationNode, localVariables);
		
		//checks if the idt is filled
		assertTrue(idt.toString() != null);
		//checks that the operation node is correct
		assertEquals("400.0", idt.toString());
		
		//initializes a pattern node
		patternNode = new PatternNode("string");
		//stores the exception thrown
		thrown = assertThrows(RuntimeException.class, () -> interpreter.GetIDT(patternNode, localVariables)); 
		//checks that the right exception is thrown
		Assertions.assertEquals("Error: Does not handle PatternNode", thrown.getMessage());
	}
	
	@Test
	void InterpretBlocks() throws IOException {
		//initializing interpreter
		lexer = new Lexer("");
		parser = new Parser(lexer);
		interpreter = new Interpreter(parser.Parse(), myPath);
		//clearing the linked lists
		statements.clear();
		statements2.clear();
		
		//creates a linked list of statements
		//a = 5+3
		statements.add(new AssignmentNode(new VariableReferenceNode("a"), new OperationNode(new ConstantNode("5"), Optional.of(new ConstantNode("3")), OperationNode.Operations.ADD)));
		//a++
		statements2.add(new AssignmentNode(new VariableReferenceNode("a"), new OperationNode(new VariableReferenceNode("a"), OperationNode.Operations.POSTINC)));
		//if a <= 7 then a++
		statements.add(new IfNode(Optional.of(new OperationNode(new VariableReferenceNode("a"), Optional.of(new ConstantNode("7")), OperationNode.Operations.GE)), statements2));
		
		//puts the block node with a condition through the interpreter
		blockNode = new BlockNode(statements, Optional.of(new OperationNode(new VariableReferenceNode("200"), Optional.of(new ConstantNode("7")), OperationNode.Operations.GE)));
		interpreter.InterpretBlock(blockNode);
		assertEquals("9.0", interpreter.globalVariables.get("a").toString());
		
		//puts the block node without a condition through the interpreter
		blockNode = new BlockNode(statements, null);
		interpreter.InterpretBlock(blockNode);
		assertEquals("9.0", interpreter.globalVariables.get("a").toString());
	}
	
	@Test
	void InterpretFunctions() throws IOException {
		//initializing interpreter
		lexer = new Lexer("");
		parser = new Parser(lexer);
		interpreter = new Interpreter(parser.Parse(), myPath);
		//clearing the linked lists
		statements.clear();
		statements2.clear();
		statements3.clear();
		conditions.clear();
		nodes.clear();
			
		//a = 10
		statements.add(new AssignmentNode(new VariableReferenceNode("a"), new OperationNode(new ConstantNode("10"), OperationNode.Operations.EQ)));
		//a++
		statements3.add(new AssignmentNode(new VariableReferenceNode("a"), new OperationNode(new VariableReferenceNode("a"), OperationNode.Operations.POSTINC)));
		nodes.add(new VariableReferenceNode("a"));
		//prints out a
		statements3.add(new FunctionCallNode("print", conditions));
		

		//for loop "for(int i = 0; i < 10; i++)"
		conditions.add(new AssignmentNode(new VariableReferenceNode("i"),new OperationNode(new ConstantNode("0"), OperationNode.Operations.EQ)));
		conditions.add(new OperationNode(new VariableReferenceNode("i"), Optional.of(new ConstantNode("10")), OperationNode.Operations.LT));
		conditions.add(new AssignmentNode(new VariableReferenceNode("i"), new OperationNode(new VariableReferenceNode("i"), OperationNode.Operations.POSTINC)));
		//if a <= 5 then a++
		statements2.add(new IfNode(Optional.of(new OperationNode(new VariableReferenceNode("a"), Optional.of(new ConstantNode("5")), OperationNode.Operations.GE)), statements3));
		statements.add(new ForNode(conditions, statements2));
		nodes.add(new VariableReferenceNode("a"));
		nodes.add(new VariableReferenceNode("a"));
		
		interpreter.functionVariables.put("function", new FunctionDefinitionNode("function", nodes, statements));
		
		
		interpreter.RunFunctionCall(new FunctionCallNode("function", nodes), localVariables);
	}
	
	
	@Test
	void InterpretMath() throws IOException {
		//initializing interpreter
		lexer = new Lexer("");
		parser = new Parser(lexer);
		interpreter = new Interpreter(parser.Parse(), myPath);
		//clearing the linked lists
		statements.clear();
		statements2.clear();
		statements3.clear();
		
		//testing 153+32 using variables(normal cases)
		statements.add(new AssignmentNode(new VariableReferenceNode("a"), new OperationNode(new ConstantNode("153"), OperationNode.Operations.EQ)));
		statements.add(new AssignmentNode(new VariableReferenceNode("b"), new OperationNode(new ConstantNode("32"), OperationNode.Operations.EQ)));
		statements.add(new AssignmentNode(new VariableReferenceNode("c"), new OperationNode(new VariableReferenceNode("a"), Optional.of(new VariableReferenceNode("b")), OperationNode.Operations.ADD)));
		
		interpreter.InterpretListOfStatements(statements, localVariables);
		
		assertEquals(localVariables.get("a").toString(), "153.0");
		assertEquals(localVariables.get("b").toString(), "32.0");
		assertEquals(localVariables.get("c").toString(), "185.0");
		
		statements.clear();
		
		//testing 200/15 using variables(decimal cases)
		statements.add(new AssignmentNode(new VariableReferenceNode("a"), new OperationNode(new ConstantNode("200"), OperationNode.Operations.EQ)));
		statements.add(new AssignmentNode(new VariableReferenceNode("b"), new OperationNode(new ConstantNode("15"), OperationNode.Operations.EQ)));
		statements.add(new AssignmentNode(new VariableReferenceNode("c"), new OperationNode(new VariableReferenceNode("a"), Optional.of(new VariableReferenceNode("b")), OperationNode.Operations.DIVIDE)));
		
		interpreter.InterpretListOfStatements(statements, localVariables);
		
		assertEquals(localVariables.get("a").toString(), "200.0");
		assertEquals(localVariables.get("b").toString(), "15.0");
		assertEquals(localVariables.get("c").toString(), "13.333333");
		
		statements.clear();
		
		//testing 20-30 using variables(one negative case)
		statements.add(new AssignmentNode(new VariableReferenceNode("a"), new OperationNode(new ConstantNode("20"), OperationNode.Operations.EQ)));
		statements.add(new AssignmentNode(new VariableReferenceNode("b"), new OperationNode(new ConstantNode("30"), OperationNode.Operations.EQ)));
		statements.add(new AssignmentNode(new VariableReferenceNode("c"), new OperationNode(new VariableReferenceNode("a"), Optional.of(new VariableReferenceNode("b")), OperationNode.Operations.SUBTRACT)));
		
		interpreter.InterpretListOfStatements(statements, localVariables);
		
		assertEquals(localVariables.get("a").toString(), "20.0");
		assertEquals(localVariables.get("b").toString(), "30.0");
		assertEquals(localVariables.get("c").toString(), "-10.0");
		
		statements.clear();
		
		//testing -30*-6 using variables(two negative cases)
		statements.add(new AssignmentNode(new VariableReferenceNode("a"), new OperationNode(new ConstantNode("-30"), OperationNode.Operations.EQ)));
		statements.add(new AssignmentNode(new VariableReferenceNode("b"), new OperationNode(new ConstantNode("-6"), OperationNode.Operations.EQ)));
		statements.add(new AssignmentNode(new VariableReferenceNode("c"), new OperationNode(new VariableReferenceNode("a"), Optional.of(new VariableReferenceNode("b")), OperationNode.Operations.MULTIPLY)));
		
		interpreter.InterpretListOfStatements(statements, localVariables);
		
		assertEquals(localVariables.get("a").toString(), "-30.0");
		assertEquals(localVariables.get("b").toString(), "-6.0");
		assertEquals(localVariables.get("c").toString(), "180.0");
	}
	
	@Test
	void InterpretLoops() throws IOException {
		//initializing interpreter
		lexer = new Lexer("");
		parser = new Parser(lexer);
		interpreter = new Interpreter(parser.Parse(), myPath);
		//clearing the linked lists
		statements.clear();
		statements2.clear();
		statements3.clear();
		
		//for loop "for(int i = 0; i < 10; i++)"
		conditions.add(new AssignmentNode(new VariableReferenceNode("i"),new OperationNode(new ConstantNode("0"), OperationNode.Operations.EQ)));
		conditions.add(new OperationNode(new VariableReferenceNode("i"), Optional.of(new ConstantNode("10")), OperationNode.Operations.LT));
		conditions.add(new AssignmentNode(new VariableReferenceNode("i"), new OperationNode(new VariableReferenceNode("i"), OperationNode.Operations.POSTINC)));
		
		//a = 10
		statements.add(new AssignmentNode(new VariableReferenceNode("a"), new OperationNode(new ConstantNode("10"), OperationNode.Operations.EQ)));
		//a++
		statements3.add(new AssignmentNode(new VariableReferenceNode("a"), new OperationNode(new VariableReferenceNode("a"), OperationNode.Operations.POSTINC)));
		//if a <= 5 then a++
		statements2.add(new IfNode(Optional.of(new OperationNode(new VariableReferenceNode("a"), Optional.of(new ConstantNode("5")), OperationNode.Operations.GE)), statements3));
		
		statements.add(new ForNode(conditions, statements2));
		
		interpreter.InterpretListOfStatements(statements, localVariables);

		assertEquals(localVariables.get("a").toString(), "20.0");
		assertEquals(localVariables.get("i").toString(), "10.0");
		
		statements.clear();
		
		//checks for a<=30
		Optional<Node> node = Optional.of(new OperationNode(new VariableReferenceNode("a"), Optional.of(new ConstantNode("30")), OperationNode.Operations.LE));
		//makes a while loop 
		statements.add(new WhileNode(node, statements2));
		interpreter.InterpretListOfStatements(statements, localVariables);
		//checks that the while loop works the right amount of times
		assertEquals(localVariables.get("a").toString(), "31.0");
		
		statements.clear();
		
		//makes a do while loop
		statements.add(new DoWhileNode(node, statements2));
		interpreter.InterpretListOfStatements(statements, localVariables);
		//checks that the do while loop works the right amount of times
		assertEquals(localVariables.get("a").toString(), "32.0");
		
	}
}
