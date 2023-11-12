
public class Token
{   
	//initializes the enum TokenTypes
	enum TokenType{WORD, NUMBER, SEPARATOR, WHILE, IF, DO, FOR, BREAK, CONTINUE, 
		ELSE, RETURN, BEGIN, END, PRINT, PRINTF, NEXT, IN, DELETE, GETLINE, EXIT, 
		NEXTFILE, FUNCTION, STRINGLITERAL, PATTERN, LESSOREQUAL, INCREMENT, DECREMENT, 
		GREATEROREQUAL, EQUALITY, NOTEQUAL, EXPONENTEQUAL, MODULUSEQUAL, MULTIPLYEQUAL, 
		DIVIDEEQUAL, PLUSEQUAL, MINUSEQUAL, DOESNOTMATCH, AND, APPEND, OR, LEFTCURLY, 
		RIGHTCURLY, LEFTBRACKET, RIGHTBRACKET, LEFTPARENTHESES, RIGHTPARENTHESES, 
		DOLLARSIGN, TILDE, EQUALS, LESSTHAN, GREATERTHAN, NOT, PLUS, EXPONENT, MINUS, 
		QUESTION, MULTIPLY, DIVIDE, MODULUS, COLON, VERTICALBAR, COMMA}
	
	
	//initializes private variables
	private TokenType type;
	private String string;
	private int lineNumber;
	private int charPosition;
   
	//constructor for tokens without a value
    public Token(TokenType type, int lineNumber, int charPosition) {
    	this.type = type;
    	this.lineNumber = lineNumber;
    	this.charPosition = charPosition;
	}

    //constructor for tokens with a value
    public Token(TokenType type,String string, int lineNumber, int charPosition) {
    	this.type = type;
        this.string = string;
    	this.lineNumber = lineNumber;
    	this.charPosition = charPosition;
    }

    //line number accessor method
    public int getLineNumber() {
    	return lineNumber;
    }

    //character position accessor method
    public int getCharPosition() {
    	return charPosition;
    }
    
    public Token.TokenType getTokenType(){
    	return type;
    }
    
    public String getValue() {
    	return string;
    }
    
    //Prints out in the format of [TokenType(value)], if there is no value only prints [TokenType]
    public String toString(){
        if(string != null) {
        	return type + "(" + string + ")";
        }
        else 
        {
        	return type.toString();
        }
    }
}