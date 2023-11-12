import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class InterpreterUnitTest {
	private Lexer lexer;
	private Parser parser;
	private Optional<Node>result;
	
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

}
