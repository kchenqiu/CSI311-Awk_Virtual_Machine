import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InterpreterUnitTest {
	private Lexer lexer;
	private Parser parser;
	private Optional<Node>result;	
	Path myPath = Paths.get("SampleText.awk");
	private Interpreter interpreter;
	private OperationNode operationNode;
	private AssignmentNode assignmentNode;
	private PatternNode patternNode;
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
}
