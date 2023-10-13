import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ParserUnitTest {

	private TokenHandler tokenHandler;
	private Lexer lexer;
	private Parser parser;
	private Optional<Node>result;
	
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
		
		//reinitialize the parser and puts in a different value
		lexer = new Lexer("++a");
		parser = new Parser(lexer);
		
		result = parser.ParseBottomLevel();
		assertTrue(result.isPresent());
		
		//checks that parse bottom value works with pre inc
		OperationNode operationNode = (OperationNode) result.get();
		assertEquals("PREINC", operationNode.getOperation());	
	}
	
	@Test
	public void Parser3Test() {
		//initializing parser
		lexer = new Lexer("2+3");
		parser = new Parser(lexer);
		OperationNode operationNode;
		AssignmentNode assignmentNode;
		
		result = parser.ParseOperation();
		assertTrue(result.isPresent());
		
		//checks that the parser works with add
		operationNode = (OperationNode) result.get();
		assertEquals("ADD", operationNode.getOperation());
		assertEquals("2 ADD 3", operationNode.toString());
		
		//reinitializing parser
		lexer = new Lexer("2^2");
		parser = new Parser(lexer);
		
		result = parser.ParseOperation();
		assertTrue(result.isPresent());
		
		//checks that the parser works with exponent
		operationNode = (OperationNode) result.get();
		assertEquals("EXPONENT", operationNode.getOperation());
		assertEquals("2 EXPONENT 2", operationNode.toString());
		
		//reinitializing parser
		lexer = new Lexer("2/3");
		parser = new Parser(lexer);
		
		result = parser.ParseOperation();
		assertTrue(result.isPresent());
		
		//checks that the parser works with divide
		operationNode = (OperationNode) result.get();
		assertEquals("DIVIDE", operationNode.getOperation());
		assertEquals("2 DIVIDE 3", operationNode.toString());
		
		//reinitializing parser
		lexer = new Lexer("(2+3)/2");
		parser = new Parser(lexer);
		
		result = parser.ParseOperation();
		assertTrue(result.isPresent());
		
		//checks that the parser works with grouping 
		operationNode = (OperationNode) result.get();
		assertEquals("DIVIDE", operationNode.getOperation());
		assertEquals("2 ADD 3", operationNode.getLeftNode().toString());
		assertEquals("2 ADD 3 DIVIDE 2", operationNode.toString());
		
		//reinitializing parser
		lexer = new Lexer("3 == 3");
		parser = new Parser(lexer);
		
		result = parser.ParseOperation();
		assertTrue(result.isPresent());
		
		//checks that the parser works with assignments
		operationNode = (OperationNode) result.get();
		assertEquals("EQ", operationNode.getOperation());
		assertEquals("3 EQ 3", operationNode.toString());
		
		//reinitializing parser
		lexer = new Lexer("a+=3");
		parser = new Parser(lexer);
		
		result = parser.ParseOperation();
		assertTrue(result.isPresent());
		
		//checks that the parser works with assignments
		assignmentNode = (AssignmentNode) result.get();
		assertEquals("a ADD 3", assignmentNode.getExpression().toString());
		assertEquals("a = a ADD 3", assignmentNode.toString());
		
		//reinitializing parser
		lexer = new Lexer("( 3 * (4+2)) /2");
		parser = new Parser(lexer);
		
		result = parser.ParseOperation();
		assertTrue(result.isPresent());
		
		//checks that the parser gets the right order
		operationNode = (OperationNode) result.get();
		assertEquals("DIVIDE", operationNode.getOperation());
		assertEquals("3 MULTIPLY 4 ADD 2", operationNode.getLeftNode().toString());
		assertEquals("3 MULTIPLY 4 ADD 2 DIVIDE 2", operationNode.toString());
		
		//reinitializing parser
		lexer = new Lexer("2+2*3");
		parser = new Parser(lexer);
		
		result = parser.ParseOperation();
		assertTrue(result.isPresent());
		
		//checks that the parser gets the right order
		operationNode = (OperationNode) result.get();
		assertEquals("ADD", operationNode.getOperation());
		assertEquals("2 MULTIPLY 3", operationNode.getRightNode().toString());
		assertEquals("2 ADD 2 MULTIPLY 3", operationNode.toString());
		
		//reinitializing parser
		lexer = new Lexer("a <= 2");
		parser = new Parser(lexer);
		
		result = parser.ParseOperation();
		assertTrue(result.isPresent());
		
		//checks that the parser works with comparison
		operationNode = (OperationNode) result.get();
		assertEquals("GE", operationNode.getOperation());
		assertEquals("a GE 2", operationNode.toString());
		
		//reinitializing parser
		lexer = new Lexer("a b");
		parser = new Parser(lexer);
		
		result = parser.ParseOperation();
		assertTrue(result.isPresent());
		
		//checks that the parser works with concatenation
		operationNode = (OperationNode) result.get();
		assertEquals("CONCATENATION", operationNode.getOperation());
		assertEquals("a CONCATENATION b", operationNode.toString());
	}
	
}
