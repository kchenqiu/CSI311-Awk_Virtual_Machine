import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Lexer
{
	//initializes the private variables
    private StringHandler stringHandler;
    private int lineNumber;
    private int charPosition;
    private List<Token> tokens;
    private HashMap<String, Token.TokenType> keywords = new HashMap<>();
    private HashMap<String, Token.TokenType> symbols = new HashMap<>();
    private HashMap<String, Token.TokenType> symbol = new HashMap<>();
    
    //constructor to take in the document and initializes the string handler and position
    public Lexer(String input) {
    	this.stringHandler = new StringHandler(input);
    	this.lineNumber = 1;
    	this.charPosition = 1;
    	this.tokens = new LinkedList<>();
    }
    
    //main method that uses the string handler to break down and assign tokens to strings and numbers
    public List<Token> Lex(){
    	KeyWordHelper();
    	SymbolsHelper();
    	SymbolHelper();    		
    	while(stringHandler.IsDone() == false) {
    		char nextChar = stringHandler.peek(0);
    		String string = String.valueOf(nextChar); 		
    		//leads to ProcessWord method if the next character is a letter 
    		if(Character.isLetter(nextChar) == true ) {
    			tokens.add(ProcessWord());
    		}
    		
    		//leads to ProcessDigit method if the next character is a number
    		else if(Character.isDigit(nextChar) == true) {
    			tokens.add(ProcessDigit());
    		}
    		
    		//leads to HandleStringLiteral if the next character is "
    		else if(nextChar == '"') {
    			tokens.add(HandleStringLiteral());
    		}
    		
    		//leads to HandlePattern if the next character is `
    		else if(nextChar == '`') {
    			tokens.add(HandlePattern());
    		}
    		
    		//leads to ProcessSymbol if the next char is a symbol
    		else if(symbol.containsKey(string) == true || symbols.containsKey(stringHandler.PeekString(2)) == true) {
    			tokens.add(ProcessSymbol());
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
    		
    		//skips all the next characters until it reaches a line separator 
    		else if(nextChar == '#') {
    			while(stringHandler.peek(0) != '\n' && stringHandler.IsDone() == false) {
    				stringHandler.GetChar();
    			}
    		}
    		
    		//moves onto the next character if input is a tab or space
    		else if(nextChar == ' ' || nextChar == '\t') {
    			stringHandler.GetChar();
    			charPosition++;
    		}
    		
    		//creates a token for a line separator if there is a new line
    		else if(nextChar == '\n') {
    			tokens.add(new Token (Token.TokenType.SEPARATOR, lineNumber, charPosition));
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
    	
    	if(keywords.containsKey(word)) {
    		return new Token(keywords.get(word), lineNumber, charPosition - word.length());
    	}

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

    //creates a string and assigns a STRINGLITERAL token to it
    private Token HandleStringLiteral() {
    	StringBuilder builder = new StringBuilder();
    	boolean endQuotes = false;
    	stringHandler.GetChar();
    	charPosition++;
    	
    	//ends there is a " character
    	while(endQuotes == false) {
    		//checks of \ incase the stringliteral has a " characters in it
    		if(stringHandler.peek(0) == '\\') {    			
    			stringHandler.GetChar();
    			charPosition ++;
    			if(stringHandler.peek(0) == '"'){
    				builder.append(stringHandler.GetChar());
    				charPosition++;
    			}

    		}
    		else if(stringHandler.peek(0) != '"') {
    			builder.append(stringHandler.GetChar());
    			charPosition++;
    		}
    		else if(stringHandler.peek(0) == '"') {
    			stringHandler.GetChar();
    			endQuotes = true;
    			charPosition++;
    		}

    	}
    	String stringLiteral = builder.toString();
    	
    	return new Token(Token.TokenType.STRINGLITERAL, stringLiteral, lineNumber, charPosition - stringLiteral.length());
    	
    }
    
    //creates a string and assigns a PATTERN token to it
    private Token HandlePattern() {
    	StringBuilder builder = new StringBuilder();
    	boolean endPattern = false;
    	stringHandler.GetChar();
    	charPosition++;
    	
    	//loops until a ` character is input again
    	while(endPattern == false) {
    		if(stringHandler.peek(0) != '`') {
    			builder.append(stringHandler.GetChar());
    			charPosition++;
    		}
    		else {
    			stringHandler.GetChar();
    			endPattern = true;
    			charPosition++;
    		}
    	}
    	
    	String pattern = builder.toString();    	
    	
    	return new Token(Token.TokenType.PATTERN, pattern, lineNumber, charPosition - pattern.length());
    	
    }
    
    //returns a symbol token from the hashmap 
    private Token ProcessSymbol() {
    	String nextString = stringHandler.PeekString(2);
    	String nextChar = String.valueOf(stringHandler.peek(0));
    	if(symbols.containsKey(nextString) == true) {
    		stringHandler.Swallow(2);
    		charPosition +=2;
    		return new Token(symbols.get(nextString), lineNumber, charPosition - 2);
    	}
    	else if(symbol.containsKey(nextChar) == true) {
    		stringHandler.GetChar();
    		charPosition++;
    		return new Token(symbol.get(nextChar), lineNumber, charPosition -1);
    	}
    	else {
    		throw new RuntimeException("Unrecognized character at line " + lineNumber + ", position " + charPosition);
    	}
    	
    }
    
    //helper method to input all the keywords into a hashmap
    private void KeyWordHelper(){
    	keywords.put("while", Token.TokenType.WHILE);
    	keywords.put("if", Token.TokenType.IF);
    	keywords.put("do", Token.TokenType.DO);
    	keywords.put("for", Token.TokenType.FOR);
    	keywords.put("break", Token.TokenType.BREAK);
    	keywords.put("continue", Token.TokenType.CONTINUE);
    	keywords.put("else", Token.TokenType.ELSE);
    	keywords.put("return", Token.TokenType.RETURN);
    	keywords.put("BEGIN", Token.TokenType.BEGIN);
    	keywords.put("END", Token.TokenType.END);
    	keywords.put("print", Token.TokenType.PRINT);
    	keywords.put("printf", Token.TokenType.PRINTF);
    	keywords.put("next", Token.TokenType.NEXT);
    	keywords.put("in", Token.TokenType.IN);
    	keywords.put("delete", Token.TokenType.DELETE);
    	keywords.put("getline", Token.TokenType.GETLINE);
    	keywords.put("exit", Token.TokenType.EXIT);
    	keywords.put("nextfile", Token.TokenType.NEXTFILE);
    	keywords.put("function", Token.TokenType.FUNCTION);
    }
    
    //helper method to input all the two character symbols into a hashmap
    private void SymbolsHelper() {
    	symbols.put(">=", Token.TokenType.LESSOREQUAL);
    	symbols.put("++", Token.TokenType.INCREMENT);
    	symbols.put("--", Token.TokenType.DECREMENT);
    	symbols.put("<=", Token.TokenType.GREATEROREQUAL);
    	symbols.put("==", Token.TokenType.EQUALITY);
    	symbols.put("!=", Token.TokenType.NOTEQUAL);
    	symbols.put("^=", Token.TokenType.EXPONENTEQUAL);
    	symbols.put("%=", Token.TokenType.MODULUSEQUAL);
    	symbols.put("*=", Token.TokenType.MULTIPLYEQUAL);
    	symbols.put("/=", Token.TokenType.DIVIDEEQUAL);
    	symbols.put("+=", Token.TokenType.PLUSEQUAL);
    	symbols.put("-=", Token.TokenType.MINUSEQUAL);
    	symbols.put("!~", Token.TokenType.DOESNOTMATCH);
    	symbols.put("&&", Token.TokenType.AND);
    	symbols.put(">>", Token.TokenType.APPEND);
    	symbols.put("||", Token.TokenType.OR);
    }
    
    //helper method to input all the one character symbol into a hashmap
    private void SymbolHelper() {
    	symbol.put("{", Token.TokenType.LEFTCURLY);
    	symbol.put("}", Token.TokenType.RIGHTCURLY);
    	symbol.put("[", Token.TokenType.LEFTBRACKET);
    	symbol.put("]", Token.TokenType.RIGHTBRACKET);
    	symbol.put("(", Token.TokenType.LEFTPARENTHESES);
    	symbol.put(")", Token.TokenType.RIGHTPARENTHESES);
    	symbol.put("^", Token.TokenType.EXPONENT);
    	symbol.put("$", Token.TokenType.DOLLARSIGN);
    	symbol.put("~", Token.TokenType.TILDE);
    	symbol.put("=", Token.TokenType.EQUALS);
    	symbol.put("<", Token.TokenType.LESSTHAN);
    	symbol.put(">", Token.TokenType.GREATERTHAN);
    	symbol.put("!", Token.TokenType.NOT);
    	symbol.put("+", Token.TokenType.PLUS);
    	symbol.put("-", Token.TokenType.MINUS);
    	symbol.put("?", Token.TokenType.QUESTION);
    	symbol.put(":", Token.TokenType.COLON);
    	symbol.put("*", Token.TokenType.MULTIPLY);
    	symbol.put("/", Token.TokenType.DIVIDE);
    	symbol.put("%", Token.TokenType.MODULUS);
    	symbol.put(";", Token.TokenType.SEPARATOR);
    	symbol.put("|", Token.TokenType.VERTICALBAR);
    	symbol.put(",", Token.TokenType.COMMA);
    }
}