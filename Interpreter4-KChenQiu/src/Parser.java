import java.util.LinkedList;
import java.util.Optional;


public class Parser {
	private TokenHandler tokenHandler;		
	
	//initializes a program node
	private LinkedList<FunctionDefinitionNode> functionDefinitionNode = new LinkedList<>();
	private LinkedList<BlockNode> blocks = new LinkedList<>();
	private LinkedList<BlockNode> blocksBegin = new LinkedList<>();
	private LinkedList<BlockNode> blocksEnd = new LinkedList<>();
	private ProgramNode program = new ProgramNode(functionDefinitionNode,blocksBegin, blocks, blocksEnd);;
	
	public Parser(Lexer lex) {
		this.tokenHandler = new TokenHandler(lex);
	}

	//accepts any amount of separators
	public boolean AcceptSeparators() {
		boolean separator = false;
		if(tokenHandler.MoreTokens() == true && tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.SEPARATOR)) {
			separator = true;
			while(tokenHandler.MoreTokens() && tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.SEPARATOR)) {
				tokenHandler.MatchAndRemove(Token.TokenType.SEPARATOR);
			}
		}
		return separator;
	}
	
	public ProgramNode Parse() {
		//loops while there are still tokens
		while(tokenHandler.MoreTokens() == true) {
			if(ParseFunction(program)) {
				
			}
			else if(ParseAction(program)) {
				
			}
			else if(tokenHandler.MoreTokens() && tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.SEPARATOR)) {
				AcceptSeparators();
			}
			else {
				throw new RuntimeException("Unexpected Token:" + tokenHandler.Peek(0));
			}
		}
		return program;
	}
	
	// Parsing a function
	public boolean ParseFunction(ProgramNode programNode) {
		if (tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.FUNCTION)) {
			tokenHandler.MatchAndRemove(Token.TokenType.FUNCTION);
	        // Parse the function name
	        Optional<Token> functionNameToken = tokenHandler.MatchAndRemove(Token.TokenType.WORD);
	        if (functionNameToken.isPresent()) {
	            String functionName = functionNameToken.get().toString();

	            // Parse the parameter list
	            LinkedList<Node> parameterList = new LinkedList<>();
	            if (tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
	            	tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);
	                // Parse parameter names
	                while (true) {
	                    Optional<Node> parameterToken = ParseOperation();
	                    if (parameterToken.isPresent()) {
	                        parameterList.add(parameterToken.get());
	                        //ends the loop if there is no more parameters listed
	                        if (!tokenHandler.Peek(0).equals(Optional.of(Token.TokenType.COMMA))) {
	                            break;
	                        }
	                    } else {
	                        break;
	                    }
	                }

	                // Match the closing parenthesis
	                if (tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES).isPresent()) {
	                	BlockNode block = ParseBlock();
	                    // Create a new FunctionDefinitionNode and add it to ProgramNode
	                	functionDefinitionNode.add(new FunctionDefinitionNode(functionName, parameterList, block.getStatements()));
	                    return true; // Successfully parsed a function
	                }
	                else {
	                	throw new RuntimeException("Expected: Right Parentheses");
	                }
	            }
	        }
	    }
	    
	    return false;
	}
	
	//Parsing Begin, other, conditional and End blocks
	public boolean ParseAction(ProgramNode programNode) {	        
		// Parsing begin blocks
        if (tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.BEGIN)) {
        	tokenHandler.MatchAndRemove(Token.TokenType.BEGIN);
            blocksBegin.add(ParseBlock());
            return true;	
        }

        // Parsing end blocks
        else if (tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.END)) {
        	tokenHandler.MatchAndRemove(Token.TokenType.END);
            blocksEnd.add(ParseBlock());
            return true;
        }
        
        Optional<Node> condition = ParseOperation();
        // Parsing Conditional blocks
        if (!condition.equals(null) && condition.isPresent()) {
            // Check if the next token is an opening curly brace for the action block
            if (tokenHandler.MoreTokens() && tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTCURLY)) {
                // Parse the action block and add it to the ProgramNode
            	LinkedList<StatementNode> statements = ParseBlock().getStatements();
                BlockNode actionBlock = new BlockNode(statements, condition);
                blocks.add(actionBlock);
                return true;
            }
        }
        return false;

}
	public Optional<Node> ParseBottomLevel(){
		String value;
		Node nodeValue;
		//checks for a string literal token
		if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.STRINGLITERAL)) {
			//saves the string and removes from token list
			value = tokenHandler.MatchAndRemove(Token.TokenType.STRINGLITERAL).get().getValue();
			//creates a new node for string literal
			return Optional.of(new ConstantNode(value));
		}
		//checks for a number token
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.NUMBER)) {
			//saves the number and removes from token list
			value = tokenHandler.MatchAndRemove(Token.TokenType.NUMBER).get().getValue();
			//creates a new node for the number
			return Optional.of(new ConstantNode(value));
		}
		//checks for a pattern token
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.PATTERN)) {
			//saves the pattern and removes from token list
			value = tokenHandler.MatchAndRemove(Token.TokenType.PATTERN).get().getValue();
			//creates a new node for the pattern
			return Optional.of(new PatternNode(value));
		}
		//checks for a left parentheses token
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
			//removes the left parentheses
			tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);
			//saves the value of parse operation
			nodeValue = ParseAssignment().get();
			//checks for a right parentheses
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTPARENTHESES)) {
				//removes the right parentheses and returns the value of parse operation
				tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES);
				return Optional.of(nodeValue);
			}
			//throws an exception if there isn't a right parentheses
			else {
				throw new RuntimeException("Error: Expected a Right Parentheses");
			}
		}
		//checks for a not token
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.NOT)) {
			tokenHandler.MatchAndRemove(Token.TokenType.NOT);
			nodeValue = ParseAssignment().get();
			//returns a new operation node with result of ParseOperation, NOT
			return Optional.of(new OperationNode(nodeValue, OperationNode.Operations.NOT));
		}
		//checks for a minus token
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.MINUS)) {
			tokenHandler.MatchAndRemove(Token.TokenType.MINUS);
			nodeValue = ParseAssignment().get();
			//returns a new operation node with result of ParseOperation, UNARYNEG
			return Optional.of(new OperationNode(nodeValue, OperationNode.Operations.UNARYNEG));
		}
		//checks for a plus token
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.PLUS)) {
			tokenHandler.MatchAndRemove(Token.TokenType.PLUS);
			nodeValue = ParseAssignment().get();
			//returns a new operation node with result of ParseOperation, UNARYPOS
			return Optional.of(new OperationNode(nodeValue, OperationNode.Operations.UNARYPOS));
		}
		//checks for an increment token
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.INCREMENT)) {
			tokenHandler.MatchAndRemove(Token.TokenType.INCREMENT);
			nodeValue = ParseAssignment().get();
			//returns a new operation node with result of ParseOperation, PREINC
			return Optional.of(new AssignmentNode(nodeValue, new OperationNode(nodeValue, OperationNode.Operations.PREINC)));
		}
		//checks for a decrement token
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.DECREMENT)) {
			tokenHandler.MatchAndRemove(Token.TokenType.DECREMENT);
			nodeValue = ParseAssignment().get();
			//returns a new operation node with result of ParseOperation, PREDEC
			return Optional.of(new AssignmentNode(nodeValue, new OperationNode(nodeValue, OperationNode.Operations.PREDEC)));
		}
		else {
			return ParseFunctionCall();
		}
		
	}
	
	public Optional<Node> ParseLValue(){
		//checks for a dollar sign token
		if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.DOLLARSIGN)) {
			tokenHandler.MatchAndRemove(Token.TokenType.DOLLARSIGN);
			Optional<Node> value = ParseBottomLevel();
			//creates a new operation node with value, DOLLAR
			return Optional.of(new OperationNode(value.get(), OperationNode.Operations.DOLLAR));
		}
		//checks for a word token
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.WORD)) {
			//saves the content of the word token
			String name = tokenHandler.MatchAndRemove(Token.TokenType.WORD).get().getValue();
				if(tokenHandler.MoreTokens() == true && tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTBRACKET)) {
					tokenHandler.MatchAndRemove(Token.TokenType.LEFTBRACKET);
					Optional<Node> index;
					index = ParseOperation();
					if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTBRACKET)) {
						tokenHandler.MatchAndRemove(Token.TokenType.RIGHTBRACKET);
						//creates a new variable reference node with name, index
						return Optional.of(new VariableReferenceNode(name, index));
					}
					else {
						throw new RuntimeException("Error: Expected a Right Bracket");
					}
				}
				else {
					//creates a new variable reference node with just name
					return Optional.of(new VariableReferenceNode(name));
				}
		}
		else {
			//returns optional empty if there is no word or dollar sign token
			return Optional.empty();
		}
	}
	//calls parse bottom level
	//highest priority in the list
	public Optional<Node> ParseFactor() {
		Optional<Node> value1 = ParseBottomLevel();
		if(tokenHandler.MoreTokens() != false) {
			//checks for parentheses
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
				//recursively calls parse expression 
				Optional<Node> value2 = ParseExpression();
				if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTPARENTHESES)) {
					return value2;
				}
				else {
					throw new RuntimeException("Expected Right Parentheses");
				}
			}
		}
		//returns the result of parse bottom level if there is no parentheses
		return value1;

	}
	
	//calls parse factor
	//checks for increment or decrement tokens
	public Optional<Node> ParsePostIncrementDecrement() {
		Optional<Node> value = ParseFactor();
		if(tokenHandler.MoreTokens() != false) {
			//checks for increment token
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.INCREMENT)) {
				tokenHandler.MatchAndRemove(Token.TokenType.INCREMENT);
				return Optional.of(new AssignmentNode(value.get(), new OperationNode(value.get(), OperationNode.Operations.POSTINC)));
			}
			//checks for decrement token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.DECREMENT)) {
				tokenHandler.MatchAndRemove(Token.TokenType.DECREMENT);
				return Optional.of(new AssignmentNode(value.get(), new OperationNode(value.get(), OperationNode.Operations.POSTDEC)));
			}
		}
		//returns result of parse factor if there is not increment or decrement tokens
		return value;
	}
	
	//calls parse factor
	public Optional<Node> ParseExponent() {
		Optional<Node> base = ParsePostIncrementDecrement();
		if(tokenHandler.MoreTokens() != false) {
			//checks for exponent tokens recursively calls itself until there are no exponent tokens
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.EXPONENT)) {
				tokenHandler.MatchAndRemove(Token.TokenType.EXPONENT);
				Optional<Node> exponent = ParseExponent();
				return Optional.of(new OperationNode(base.get(), exponent, OperationNode.Operations.EXPONENT));
			}
		}
		//returns the result of parse post increment/decrement if there is no exponent token
		return base;
	}
	
	//calls parse exponent
	//checks for multiply, divide or modulus tokens
	public Optional<Node> ParseTerm() {
		Optional<Node> value1 = ParseExponent();
		if(tokenHandler.MoreTokens() != false) {
			//checks for multiply token
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.MULTIPLY)) {
				tokenHandler.MatchAndRemove(Token.TokenType.MULTIPLY);
				Optional<Node> value2 = ParseExponent();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.MULTIPLY));
			}
			//checks for divide token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.DIVIDE)) {
				tokenHandler.MatchAndRemove(Token.TokenType.DIVIDE);
				Optional<Node> value2 = ParseExponent();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.DIVIDE));
			}		
			//checks for modulus token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.MODULUS)) {
				tokenHandler.MatchAndRemove(Token.TokenType.MODULUS);
				Optional<Node> value2 = ParseExponent();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.MODULAR));
			}
		}
		//returns the result of parse exponent if there are no multiply, divide, or modulas token
		return value1;
	}
	
	//calls parse term
	//checks for a plus or minus token
	public Optional<Node> ParseExpression() {
		Optional<Node> value1 = ParseTerm();
		if(tokenHandler.MoreTokens() != false) {
			//checks for a plus token
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.PLUS)) {
				tokenHandler.MatchAndRemove(Token.TokenType.PLUS);
				Optional<Node> value2 = ParseTerm();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.ADD));
			}
			//checks for a minus token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.MINUS)) {
				tokenHandler.MatchAndRemove(Token.TokenType.MINUS);
				Optional<Node> value2 = ParseTerm();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.SUBTRACT));
			}
		}
		//returns the result from parse term if there is no plus or minus token
		return value1;
	}
	
	//calls parse expression
	//checks for string literals
	public Optional<Node> ParseConcatenation() {
		Optional<Node> value1 = ParseExpression();
		if(tokenHandler.MoreTokens() != false) {
			//recursively calls itself if there is a string literal token
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.WORD)) {
				Optional<Node> value2 = ParseConcatenation();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.CONCATENATION));
			}
		}
		//returns parse expression if there is no string literal
		return value1;
	}

	//calls parse concatenation
	//checks for the comparison tokens
	public Optional<Node> ParseBooleanCompare() {
		Optional<Node> value1 = ParseConcatenation();
		if(tokenHandler.MoreTokens() != false) {
			//checks for less than token
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LESSTHAN)) {
				tokenHandler.MatchAndRemove(Token.TokenType.LESSTHAN);
				Optional<Node> value2 = ParseConcatenation();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.LT));
			}
			//checks for greater than token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.GREATERTHAN)) {
				tokenHandler.MatchAndRemove(Token.TokenType.GREATERTHAN);
				Optional<Node> value2 = ParseConcatenation();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.GT));
			}
			//checks for less than or equal to token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LESSOREQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.LESSOREQUAL);
				Optional<Node> value2 = ParseConcatenation();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.LE));
			}
			//checks for greater than or equal to token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.GREATEROREQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.GREATEROREQUAL);
				Optional<Node> value2 = ParseConcatenation();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.GE));
			}
			//checks for not equal to token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.NOTEQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.NOTEQUAL);
				Optional<Node> value2 = ParseConcatenation();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.NOTMATCH));
			}
			//checks for equal to token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.EQUALITY)) {
				tokenHandler.MatchAndRemove(Token.TokenType.EQUALITY);
				Optional<Node> value2 = ParseConcatenation();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.EQ));
			}
		}
		//returns parse concatenation if there is no comparison tokens
		return value1;
	}
	
	//calls Parse Boolean Compare
	//creates an operation node if there is a tilde or does not match token
	public Optional<Node> ParseMatch() {
		Optional<Node> value1 = ParseBooleanCompare();
		if(tokenHandler.MoreTokens() != false) {
			//checks for a tilde
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.TILDE)) {
				tokenHandler.MatchAndRemove(Token.TokenType.TILDE);
				Optional<Node> value2 = ParseBooleanCompare();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.MATCH));
			}
			//checks for a does not match token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.DOESNOTMATCH)) {
				tokenHandler.MatchAndRemove(Token.TokenType.DOESNOTMATCH);
				Optional<Node> value2 = ParseBooleanCompare();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.NOTMATCH));			
			}
		}
		//returns parse boolean compare if there is no tilde or does not match token
		return value1;
	}
	
	//calls ParseMatch
	//creates an operation node if there is left and right bracket
	public Optional<Node> ParseArrayIndex() {
		Optional<Node> value1 = ParseMatch();
		Optional<Node> expression;
		if(tokenHandler.MoreTokens() != false) {
			//checks for a left bracket
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTBRACKET)) {
				tokenHandler.MatchAndRemove(Token.TokenType.LEFTBRACKET);
				expression = ParseMatch();
				//checks for a right bracket
				if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTBRACKET)) {
					tokenHandler.MatchAndRemove(Token.TokenType.RIGHTBRACKET);
					return Optional.of(new OperationNode(value1.get(), expression, OperationNode.Operations.IN));
				}
				else {
					throw new RuntimeException("Expected Right Bracket");
				}
			}
		}
		//returns parse match if there is no brackets
		return value1;
	}
	
	//calls parse array index 
	//creates an operation node if there is an and token
	public Optional<Node> ParseAnd() {
		Optional<Node> value1 = ParseArrayIndex();
		if(tokenHandler.MoreTokens() != false) {
			//checks for an and token
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.AND)) {
				tokenHandler.MatchAndRemove(Token.TokenType.AND);
				Optional<Node> value2 = ParseArrayIndex();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.AND));
			}
		}
		//returns parse array index if there is no and token
		return value1;
	}
	
	//calls parse and
	//creates a operation node if there is an or token
	public Optional<Node> ParseOr() {
		Optional<Node> value1 = ParseAnd();
		if(tokenHandler.MoreTokens() != false) {
			//checks for an or token
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.OR)) {
				tokenHandler.MatchAndRemove(Token.TokenType.OR);
				Optional<Node> value2 = ParseAnd();
				return Optional.of(new OperationNode(value1.get(), value2, OperationNode.Operations.OR));
			}
		}
		//returns parse and if there is no or token
		return value1;
	}
	
	//calls ParseOr
	//creates a ternary node if there is a question mark and colon
	public Optional<Node> ParseTernary() {
		Optional<Node> condition = ParseOr();
		Optional<Node> trueCase = null;
		Optional<Node> falseCase = null;
		if(tokenHandler.MoreTokens() != false) {
			//checks for a question mark
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.QUESTION)) {
				tokenHandler.MatchAndRemove(Token.TokenType.QUESTION);
				trueCase = ParseOr();
				//checks for a colon
				if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.COLON)) {
					tokenHandler.MatchAndRemove(Token.TokenType.COLON);
					falseCase = ParseOr();		
					return Optional.of(new TernaryNode(condition.get(), trueCase.get(), falseCase.get()));
				}
			}
		}
		//returns the result of parse or if there is no questionmark and colon
		return condition;

	}
	

	//calls parse ternary
	//creates an assignment node if there is any of the equals signs
	public Optional<Node> ParseAssignment() {
		Optional<Node> value1 = ParseTernary();
		if(tokenHandler.MoreTokens() != false) {
			//checks for an equal sign
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.EQUALS)) {
				tokenHandler.MatchAndRemove(Token.TokenType.EQUALS);
				Optional<Node> value2 = ParseTernary();
				return Optional.of(new AssignmentNode(null, new OperationNode(value1.get(), value2, OperationNode.Operations.EQ)));			
			}
			//checks for a plus equal sign
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.PLUSEQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.PLUSEQUAL);
				Optional<Node> value2 = ParseTernary();
				return Optional.of(new AssignmentNode(value1.get(), new OperationNode(value1.get(), value2, OperationNode.Operations.ADD)));			
			}	
			//checks for a minus equal sign
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.MINUSEQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.MINUSEQUAL);
				Optional<Node> value2 = ParseTernary();
				return Optional.of(new AssignmentNode(value1.get(), new OperationNode(value1.get(), value2, OperationNode.Operations.SUBTRACT)));			
			}
			//checks for a multiply equal sign
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.MULTIPLYEQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.MULTIPLYEQUAL);
				Optional<Node> value2 = ParseTernary();
				return Optional.of(new AssignmentNode(value1.get(), new OperationNode(value1.get(), value2, OperationNode.Operations.MULTIPLY)));			
			}
			//checks for a divide equal sign
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.DIVIDEEQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.DIVIDEEQUAL);
				Optional<Node> value2 = ParseTernary();
				return Optional.of(new AssignmentNode(value1.get(), new OperationNode(value1.get(), value2, OperationNode.Operations.DIVIDE)));			
			}	
			//checks for a modulus equal sign
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.MODULUSEQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.MODULUSEQUAL);
				Optional<Node> value2 = ParseTernary();
				return Optional.of(new AssignmentNode(value1.get(), new OperationNode(value1.get(), value2, OperationNode.Operations.MODULAR)));			
			}	
			//checks for a exponent equal sign
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.EXPONENTEQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.EXPONENTEQUAL);
				Optional<Node> value2 = ParseTernary();
				return Optional.of(new AssignmentNode(value1.get(), new OperationNode(value1.get(), value2, OperationNode.Operations.EXPONENT)));			
			}
		}
		//returns the result of parse ternary if there is no assignment
		return value1;
	}
	
	
	//empty holder for parsing blocks
	public BlockNode ParseBlock() {
		LinkedList<StatementNode> statements = new LinkedList<>();
		Optional<Node> statement;
		if(tokenHandler.MoreTokens() == true && tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTCURLY)) {
			tokenHandler.MatchAndRemove(Token.TokenType.LEFTCURLY);
			while(tokenHandler.MoreTokens() == true && !tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTCURLY)) {				
				if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.SEPARATOR)) {
					AcceptSeparators();
				}
				statement = ParseStatement();
				if(statement != null && statement.isPresent()) {
					statements.add((StatementNode) statement.get());
				}
			}
			tokenHandler.MatchAndRemove(Token.TokenType.RIGHTCURLY);
		}
		else{
			statement = ParseStatement();
			if(statement != null && statement.isPresent()) {
				statements.add((StatementNode) statement.get());
			}
		}
		tokenHandler.MatchAndRemove(Token.TokenType.RIGHTCURLY);		
		BlockNode block = new BlockNode(statements, Optional.empty());
		return  block;
	}
	
	//checks all the types of statements, leads to parse operation if none of them work
	public Optional<Node> ParseStatement() {
		if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.CONTINUE)) {
			return ParseContinue();
		}
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.BREAK)) {
			return ParseBreak();
		}
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.IF)) {
			return ParseIf();
		}
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.ELSE)){
			return ParseElse();
		}
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.FOR)) {
			return ParseFor();
		}
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.DELETE)) {
			return ParseDelete();
		}
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.WHILE)) {
			return ParseWhile();
		}
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.DO)) {
			return ParseDoWhile();
		}
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RETURN)) {
			return ParseReturn();
		}
		else {
			return ParseOperation();
		}
	}
	
	//parses a continue statement
	public Optional<Node> ParseContinue() {
		tokenHandler.MatchAndRemove(Token.TokenType.CONTINUE);
		return Optional.of(new ContinueNode());
	}
	
	//parses a break statement
	public Optional<Node> ParseBreak() {
		tokenHandler.MatchAndRemove(Token.TokenType.BREAK);
		return Optional.of(new BreakNode());
	}
	
	//parses an if block
	public Optional<Node> ParseIf() {
		BlockNode block;
		Optional<Node> condition;
		tokenHandler.MatchAndRemove(Token.TokenType.IF);
		if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
			tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);
			condition =  ParseOperation();
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTPARENTHESES)) {
				tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES);
				block = ParseBlock();
				return Optional.of(new IfNode(condition, block.getStatements()));
			}
			else {
				throw new RuntimeException("Error: Expected Right Parentheses");
			}
		}
		else {
			throw new RuntimeException("Error: Expected Condition");
		}
	}
	
	//parses an else block
	//checks if it is an if-else statement
	public Optional<Node> ParseElse() {
		BlockNode block;
		Optional<Node> condition;
		tokenHandler.MatchAndRemove(Token.TokenType.ELSE);
		if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.IF)) {
			tokenHandler.MatchAndRemove(Token.TokenType.IF);
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
				tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);
				condition =  ParseOperation();
				if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTPARENTHESES)) {
					tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES);
					block = ParseBlock();
					return Optional.of(new IfNode(condition, block.getStatements()));
				}
				else {
					throw new RuntimeException("Error: Expected Right Parentheses");
				}
			}
			else {
				throw new RuntimeException("Error: Expected Condition");
			}
		}
		else {
			block = ParseBlock();
			return Optional.of(new IfNode(Optional.empty(), block.getStatements()));
		}
	}
	
	//parses for by checking for amount of statements provided
	//can create a for node or a foreach node
	public Optional<Node> ParseFor() {
		LinkedList <Node> condition = new LinkedList<>();
		BlockNode block;
		boolean bool = false;
		tokenHandler.MatchAndRemove(Token.TokenType.FOR);
		if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
			tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);
				condition.add(ParseOperation().get());
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.SEPARATOR)) {
				AcceptSeparators();
				condition.add(ParseOperation().get());
				AcceptSeparators();
				condition.add(ParseOperation().get());
				bool = true;
			}
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTPARENTHESES)) {
				tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES);
				block = ParseBlock();
				if(bool) {
					return Optional.of(new ForNode(condition, block.getStatements()));
				}
				else {
					return Optional.of(new ForNode(condition, block.getStatements())) ;
				}
			}
			else {
				throw new RuntimeException("Error: Expected Right Parentheses");
			}
		}
		else {
			throw new RuntimeException("Error: Expected Condition");
		}
	}
	
	//parses delete and creates a delete node
	public Optional<Node> ParseDelete() {
		tokenHandler.MatchAndRemove(Token.TokenType.DELETE);
		Optional<Node> parameter;
		parameter = ParseOperation();
		return Optional.of(new DeleteNode(parameter));
	}
	
	//parses while by checking for a while, then parses the block
	public Optional<Node> ParseWhile() {
		tokenHandler.MatchAndRemove(Token.TokenType.WHILE);
		BlockNode block;
		Optional<Node> condition;
		if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
			tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);
			condition =  ParseOperation();
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTPARENTHESES)) {
				tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES);
				block = ParseBlock();
				return Optional.of(new WhileNode(condition, block.getStatements()));
			}
			else {
				throw new RuntimeException("Error: Expected Right Parentheses");
			}
		}
		else {
			throw new RuntimeException("Error: Expected Condition");
		}
	}
	
	//parses do while by checking for a do, then parseing the block and checking for the while condition
	public Optional<Node> ParseDoWhile() {
		tokenHandler.MatchAndRemove(Token.TokenType.DO);
		BlockNode block;
		Optional<Node> condition;
		block = ParseBlock();
		if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.WHILE)) {
			tokenHandler.MatchAndRemove(Token.TokenType.WHILE);
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
				tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);
				condition = ParseOperation();
				if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTPARENTHESES)) {
					tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES);
					return Optional.of(new DoWhileNode(condition, block.getStatements()));
				}
				else {
					throw new RuntimeException("Error: Expected Right Parentheses");
				}
			}
			else {
				throw new RuntimeException("Error: Expected Condition");
			}
		}
		else {
			throw new RuntimeException("Error: Expected While");
		}
	}
	
	//parses return and creates a return node
	public Optional<Node> ParseReturn() {
		tokenHandler.MatchAndRemove(Token.TokenType.RETURN);
		Optional<Node> parameter = ParseOperation();
		return Optional.of(new ReturnNode(parameter));
	}
	
	//leads to parse assignment
	public Optional<Node> ParseOperation(){
		Optional<Node> operation = ParseAssignment();
		return operation;
	}
	
	//parses new functions and built in functions
	public Optional<Node> ParseFunctionCall() {
		LinkedList<Node> parameters = new LinkedList<>();
		if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.WORD)) {
			Optional<Node> word = ParseLValue();
			if(tokenHandler.MoreTokens() == true && tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
				tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);
				while(!tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTPARENTHESES)) {
					parameters.add(ParseOperation().get());
					if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.SEPARATOR)) {
						break;
					}
				}
				if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTPARENTHESES)) {
					tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES);
					return Optional.of(new FunctionCallNode(word.get().toString(), parameters));
				}
				else {
					throw new RuntimeException("Error: Expected Right Parentheses");
				}
			}
			return word;
		}
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.PRINT)) {
			tokenHandler.MatchAndRemove(Token.TokenType.PRINT);
			if(tokenHandler.MoreTokens() && tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
				tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);
				while(!tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.SEPARATOR) && !tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTPARENTHESES)) {
					parameters.add(ParseBottomLevel().get());
				}
				if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTPARENTHESES)) {
					tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES);
				}
			}
			return Optional.of(new FunctionCallNode("print", parameters));
		}
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.PRINTF)) {
			tokenHandler.MatchAndRemove(Token.TokenType.PRINTF);
			if(tokenHandler.MoreTokens() && tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
				tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);
				while(!tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.SEPARATOR) && !tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTPARENTHESES)) {
					parameters.add(ParseBottomLevel().get());
				}
				if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTPARENTHESES)) {
					tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES);
				}
			}
			return Optional.of(new FunctionCallNode("printf", parameters));
		}
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.NEXT)) {
			tokenHandler.MatchAndRemove(Token.TokenType.NEXT);
			if(tokenHandler.MoreTokens() && tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
				tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);				
				tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES);
			}
			return Optional.of(new FunctionCallNode("next", null));
		}
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.GETLINE)) {
			tokenHandler.MatchAndRemove(Token.TokenType.GETLINE);
			if(tokenHandler.MoreTokens() && tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
				tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);				
				tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES);
			}
			return Optional.of(new FunctionCallNode("getline", null));
		}
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.EXIT)) {
			tokenHandler.MatchAndRemove(Token.TokenType.EXIT);
			if(tokenHandler.MoreTokens() && tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
				tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);				
				tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES);
			}
			return Optional.of(new FunctionCallNode("exit", null));
		}
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.NEXTFILE)) {
			tokenHandler.MatchAndRemove(Token.TokenType.NEXTFILE);
			if(tokenHandler.MoreTokens() && tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
				tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);				
				tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES);
			}
			return Optional.of(new FunctionCallNode("nextfile", null));
		}
		return ParseLValue();
	}
}
