import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


class LexerUnitTest {
	//initializes private variables used
	private Lexer lexer;
	private String string;
	private Throwable thrown;
	
	
	//testing if the basic separator, number and word outputs correctly
	@Test
	public void initialTest() {
    	lexer = new Lexer("\n");
    	string = lexer.Lex().toString();
    	assertTrue("[LINESEPARATOR]".equals(string));
    	
    	lexer = new Lexer("word");
    	string = lexer.Lex().toString();
    	assertTrue("[WORD(word)]".equals(string));
    	
    	lexer = new Lexer("1");
    	string = lexer.Lex().toString();
    	assertTrue("[NUMBER(1)]".equals(string));
    	
    	lexer = new Lexer(" ");
    	string = lexer.Lex().toString();
    	assertTrue("[]".equals(string));
    	
    	lexer = new Lexer("\r");
    	string = lexer.Lex().toString();
    	assertTrue("[]".equals(string));
    	
    	lexer = new Lexer("\t");
    	string = lexer.Lex().toString();
    	assertTrue("[]".equals(string));
	}
	
	//tests various combinations of separators, numbers and words
    @Test
    public void testLexer() {
    	lexer = new Lexer("2.2 testing \n");
    	string = lexer.Lex().toString();
    	assertTrue(("[NUMBER(2.2), WORD(testing), LINESEPARATOR]").equals(string));
    	
    	lexer = new Lexer(" 12123.1231 .231 \n \n");
    	string = lexer.Lex().toString();
    	assertTrue("[NUMBER(12123.1231), NUMBER(.231), LINESEPARATOR, LINESEPARATOR]".equals(string));
    	
    	lexer = new Lexer(" word testing longstringofwords \n testing ");
    	string = lexer.Lex().toString();
    	assertTrue("[WORD(word), WORD(testing), WORD(longstringofwords), LINESEPARATOR, WORD(testing)]".equals(string));
    	
    	lexer = new Lexer(" word 2 testing 123 string");
    	string = lexer.Lex().toString();
    	assertTrue("[WORD(word), NUMBER(2), WORD(testing), NUMBER(123), WORD(string)]".equals(string));
    }
    
    //testing if runtime exception messages printed properly
    @Test
    public void testError() {
    	lexer = new Lexer(" asdfa ; \n");
    	thrown = assertThrows(RuntimeException.class, () -> lexer.Lex());
    	assertTrue("Unrecognized character at line 1, position 8".equals(thrown.getMessage()));
    	
    	lexer = new Lexer(" - ");
    	thrown = assertThrows(RuntimeException.class, () -> lexer.Lex());
    	assertTrue("Unrecognized character at line 1, position 2".equals(thrown.getMessage()));
    	
    	lexer = new Lexer(" as + ");
    	thrown = assertThrows(RuntimeException.class, () -> lexer.Lex());
    	assertTrue("Unrecognized character at line 1, position 5".equals(thrown.getMessage()));
    	
    	lexer = new Lexer("/");
    	thrown = assertThrows(RuntimeException.class, () -> lexer.Lex());
    	assertTrue("Unrecognized character at line 1, position 1".equals(thrown.getMessage()));
    	
    	lexer = new Lexer("\n*  ");
    	thrown = assertThrows(RuntimeException.class, () -> lexer.Lex());
    	assertTrue("Unrecognized character at line 2, position 1".equals(thrown.getMessage()));
    	
    	lexer = new Lexer(" 12341.123.12");
    	thrown = assertThrows(RuntimeException.class, () -> lexer.Lex());
    	assertTrue("Unrecognized input at line 1, position 12".equals(thrown.getMessage()));
    }

}
