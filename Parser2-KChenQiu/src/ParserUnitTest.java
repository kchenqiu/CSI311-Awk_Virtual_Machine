import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class ParserUnitTest {

	private TokenHandler tokenHandler;
	private Lexer lexer;
	private Parser parser;
	
	@Test
	 public void TokenHandlerTest() {
        // Initialize TokenManager with a LinkedList of tokens
        lexer = new Lexer("variable ;");
        tokenHandler = new TokenHandler(lexer);        

        
        Optional<Token> peekedToken = tokenHandler.Peek(1);
        assertTrue(peekedToken.isPresent());
        assertEquals(Token.TokenType.SEPARATOR, peekedToken.get().getTokenType());


        // Test MoreTokens method
        assertTrue(tokenHandler.MoreTokens());

        // Consume all tokens
        tokenHandler.MatchAndRemove(Token.TokenType.WORD);
        tokenHandler.MatchAndRemove(Token.TokenType.SEPARATOR);
        
        
        assertFalse(tokenHandler.MoreTokens());
        

        //reinitialize token handler
        lexer = new Lexer("variable ;");
        tokenHandler = new TokenHandler(lexer);        
        
        // Test MatchAndRemove method
        Optional<Token> matchedToken = tokenHandler.MatchAndRemove(Token.TokenType.WORD);
        assertTrue(matchedToken.isPresent());
        assertEquals(Token.TokenType.WORD, matchedToken.get().getTokenType());
        assertEquals("WORD(variable)", matchedToken.get().toString());
        
        matchedToken = tokenHandler.MatchAndRemove(Token.TokenType.SEPARATOR);
        assertTrue(matchedToken.isPresent());
        assertEquals(Token.TokenType.SEPARATOR, matchedToken.get().getTokenType());
        assertEquals("SEPARATOR", matchedToken.get().toString());
    }
	
	@Test
	public void ParseLValueTest() {
		//initializes the parser by putting in a value
		lexer = new Lexer("$variable");
		parser = new Parser(lexer);
		Optional<Node> result; 
		
		result = parser.ParseLValue();	
		
		assertTrue(result.isPresent());
		OperationNode operationNode = (OperationNode) result.get();
		//checks that the parse L value returns a dollar sign and creates a variable reference node
		assertEquals(operationNode.operation.DOLLAR.toString(), operationNode.getOperation());
		assertTrue(operationNode.getLeftNode() instanceof VariableReferenceNode);	
		
		//reinitialize the parser and puts in a different value
		lexer = new Lexer("array[5]");
		parser = new Parser(lexer);
		
		result = parser.ParseLValue();
		
		assertTrue(result.isPresent());
		VariableReferenceNode variableReferenceNode = (VariableReferenceNode) result.get();
		//checks that the parse L value creates a variable reference node and stores the value properly
		assertEquals("array", variableReferenceNode.getName());
	}
	
	@Test
	public void ParseBottomLevelTest() {
		//initializes the parser by putting in a value
		lexer = new Lexer("\"Hello, World!\"");
		parser = new Parser(lexer);
		Optional<Node> result; 
		
		result = parser.ParseBottomLevel();
		assertTrue(result.isPresent());
		
		//checks that parse bottom value works with a string literal
		ConstantNode constantNode = (ConstantNode) result.get();
		assertEquals("Hello, World!", constantNode.toString());
		
		//reinitialize the parser and puts in a different value
		lexer = new Lexer("123456");
		parser = new Parser(lexer);
		
		result = parser.ParseBottomLevel();
		assertTrue(result.isPresent());
		
		//checks that parse bottom value works with a number
		constantNode = (ConstantNode) result.get();
		assertEquals("123456", constantNode.toString());
	}
	
}
