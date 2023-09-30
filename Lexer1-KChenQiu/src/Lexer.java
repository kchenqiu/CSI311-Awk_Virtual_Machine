import java.util.LinkedList;
import java.util.List;

public class Lexer
{
	//initializes the private variables
    private StringHandler stringHandler;
    private int lineNumber;
    private int charPosition;
    private List<Token> tokens;
    
    //constructor to take in the document and initializes the string handler and position
    public Lexer(String input) {
    	this.stringHandler = new StringHandler(input);
    	this.lineNumber = 1;
    	this.charPosition = 1;
    	this.tokens = new LinkedList<>();
    }
    
    //main method that uses the string handler to break down and assign tokens to strings and numbers
    public List<Token> Lex(){
    	while(stringHandler.IsDone() == false) {
    		char nextChar = stringHandler.peek(0);
    		
    		//leads to ProcessWord method if the next character is a letter 
    		if(Character.isLetter(nextChar) == true ) {
    			tokens.add(ProcessWord());
    		}
    		
    		//leads to ProcessDigit method if the next character is a number
    		else if(Character.isDigit(nextChar) == true) {
    			tokens.add(ProcessDigit());
    		}
    		
    		//checks if the period is a decimal or just alone 
    		//leads to the ProcessDigit method if it is apart of a number
    		else if(nextChar == '.') {
    			if(Character.isDigit(stringHandler.peek(1))) {
    				tokens.add(ProcessDigit());
    			}
    			else if(nextChar == ' ') {
    				stringHandler.GetChar();
    				charPosition++;
    			}
    		}
    		
    		//moves onto the next character if input is a tab or space
    		else if(nextChar == ' ' || nextChar == '\t') {
    			stringHandler.GetChar();
    			charPosition++;
    		}
    		
    		//creates a token for a line seperator if there is a new line
    		else if(nextChar == '\n') {
    			tokens.add(new Token (Token.TokenType.LINESEPARATOR, lineNumber, charPosition));
    			stringHandler.GetChar();
    			lineNumber++;
    			charPosition = 1;
    		}
    		
    		//does nothing if it is carriage return
    		else if(nextChar == '\r') {
    			stringHandler.GetChar();
    		}
    		
    		//throws an exception if there is a unrecognized character
    		else {
    			throw new RuntimeException("Unrecognized character at line " + lineNumber + ", position " + charPosition);
    		}
    		
    	}
    	
    	//returns and prints out the tokens in a format
    	return tokens;
    }
    
    //Creates the string and assigns a WORD token to it
    private Token ProcessWord() {
    	StringBuilder builder = new StringBuilder();

    	//continues until it reaches a character that is not a letter, number or _
    	while(Character.isLetterOrDigit(stringHandler.peek(0)) || stringHandler.peek(0) == '_') {
    		builder.append(stringHandler.GetChar());
    		charPosition++;
    	}
    	String word = builder.toString();

    	return new Token(Token.TokenType.WORD, word, lineNumber, charPosition - word.length());
    }
    
    //Creates a string of digits and assigns a NUMBER token to it
    private Token ProcessDigit() {
    	StringBuilder builder = new StringBuilder();
    	boolean decimal = false;
    	
    	//checks if it starts off with a decimal
    	if(stringHandler.peek(0) == '.'){
    		decimal = true;
    		builder.append(stringHandler.GetChar());
    	}
    	
    	//continues until it reaches a character that is not a decimal or number
    	while(Character.isDigit(stringHandler.peek(0)) || stringHandler.peek(0) == '.'  && stringHandler.peek(0)!=' ') {
    		char nextChar = stringHandler.GetChar();
    		charPosition++;
    		//throws an exception if two decimals were used
    		if(nextChar == '.' && decimal == true) {
    			throw new RuntimeException("Unrecognized input at line " + lineNumber + ", position " + charPosition);
    		}
    		else if(nextChar == '.' && decimal == false) {
    			decimal = true;
    		}
    		

    		
    		builder.append(nextChar);
    	}
    	String number = builder.toString();
    	
    	
    	return new Token(Token.TokenType.NUMBER, number, lineNumber, charPosition - number.length());
    }


}