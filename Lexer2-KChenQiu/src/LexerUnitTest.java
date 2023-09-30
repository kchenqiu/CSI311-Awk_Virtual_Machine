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
    	assertTrue("[SEPARATOR]".equals(string));
    	
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
    	
    	lexer = new Lexer("+");
    	string = lexer.Lex().toString();
    	assertTrue("[PLUS]".equals(string));
    	
    	lexer = new Lexer("+= ");
    	string = lexer.Lex().toString();
    	assertTrue("[PLUSEQUAL]".equals(string));
    	
    	lexer = new Lexer("{ } ");
    	string = lexer.Lex().toString();
    	assertTrue("[LEFTCURLY, RIGHTCURLY]".equals(string));
    	
    	lexer = new Lexer("while for ");
    	string = lexer.Lex().toString();
    	assertTrue("[WHILE, FOR]".equals(string));
    	
    	lexer = new Lexer("BEGIN, END");
    	string = lexer.Lex().toString();
    	assertTrue("[BEGIN, COMMA, END]".equals(string));
    	
    	lexer = new Lexer(" \" \" ");
    	string = lexer.Lex().toString();
    	assertTrue("[STRINGLITERAL( \" \" )]".equals(string));

    	lexer = new Lexer("#");
    	string = lexer.Lex().toString();
    	assertTrue("[]".equals(string));
    	
    	lexer = new Lexer("`test`");
    	string = lexer.Lex().toString();
    	assertTrue("[PATTERN(test)]".equals(string));
    	
    	lexer = new Lexer("\"TEST\"");
    	string = lexer.Lex().toString();
    	assertTrue("[STRINGLITERAL(\"TEST\")]".equals(string));
	}
	
	//tests various combinations of separators, numbers and words
    @Test
    public void testLexer() {
    	lexer = new Lexer("2.2 testing \n");
    	string = lexer.Lex().toString();
    	assertTrue(("[NUMBER(2.2), WORD(testing), SEPARATOR]").equals(string));
    	
    	lexer = new Lexer(" 12123.1231 .231 \n \n");
    	string = lexer.Lex().toString();
    	assertTrue("[NUMBER(12123.1231), NUMBER(.231), SEPARATOR, SEPARATOR]".equals(string));
    	
    	lexer = new Lexer(" word testing longstringofwords \n testing ");
    	string = lexer.Lex().toString();
    	assertTrue("[WORD(word), WORD(testing), WORD(longstringofwords), SEPARATOR, WORD(testing)]".equals(string));
    	
    	lexer = new Lexer(" word 2 testing 123 string");
    	string = lexer.Lex().toString();
    	assertTrue("[WORD(word), NUMBER(2), WORD(testing), NUMBER(123), WORD(string)]".equals(string));
    	

    	
    }
    
    //unit tests for lexer2 for various combinations
    @Test
    public void testLexer2() {    	
    	lexer = new Lexer("String quote = \"She said, \\\"hello there\\\" and then she left.\"; ");
    	string = lexer.Lex().toString();
    	assertTrue("[WORD(String), WORD(quote), EQUALS, STRINGLITERAL(\"She said, \"hello there\" and then she left.\"), SEMICOLON]".equals(string));
   
        lexer = new Lexer("for while hello do");
    	string = lexer.Lex().toString();
    	assertTrue("[FOR, WHILE, WORD(hello), DO]".equals(string));
    	
        lexer = new Lexer("#test \n testing");
    	string = lexer.Lex().toString();
    	assertTrue("[SEPARATOR, WORD(testing)]".equals(string));
    	
        lexer = new Lexer("1+2=3");
    	string = lexer.Lex().toString();
    	assertTrue("[NUMBER(1), PLUS, NUMBER(2), EQUALS, NUMBER(3)]".equals(string));
    	
        lexer = new Lexer("System.out.print(\"String Literal\" + \"test\" + 231); ");
    	string = lexer.Lex().toString();
    	assertTrue("[WORD(System), WORD(out), WORD(print, LEFTPARENTHESES, STRINGLITERAL(\"String Literal\"), PLUS, STRINGLITERAL(\"test\"), PLUS, NUMBER(231), RIGHTPARENTHESES, SEMICOLON)]".equals(string));
    }

    
    //testing if runtime exception messages printed properly
    @Test
    public void testError() {
    	lexer = new Lexer(" 12341.123.12");
    	thrown = assertThrows(RuntimeException.class, () -> lexer.Lex());
    	assertTrue("Unrecognized input at line 1, position 12".equals(thrown.getMessage()));
    	
    	lexer = new Lexer("&");
    	thrown = assertThrows(RuntimeException.class, () -> lexer.Lex());
    	assertTrue("Unrecognized character at line 1, position 1".equals(thrown.getMessage()));
    }

}
