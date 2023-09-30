import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class ParserUnitTest {

	private TokenHandler tokenHandler;
	private Lexer lexer;
	private Throwable thrown;
	
	@Test
	 public void TokenHandler() {
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
}
