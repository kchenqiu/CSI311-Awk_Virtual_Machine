import java.util.LinkedList;
import java.util.Optional;


public class Parser {
	private TokenHandler tokenHandler;
	
	public Parser(Lexer lex) {
		this.tokenHandler = new TokenHandler(lex);
	}

	//accepts any amount of separators
	public boolean AcceptSeparators() {
		boolean separator = false;
		if(tokenHandler.Peek(0) != null && tokenHandler.Peek(0).equals(Optional.of(Token.TokenType.SEPARATOR))) {
			separator = true;
			while(tokenHandler.Peek(0).equals(Optional.of(Token.TokenType.SEPARATOR))) {
			tokenHandler.MatchAndRemove(Token.TokenType.SEPARATOR);
			}
		}
		return separator;
	}
	
	public ProgramNode Parse() {
		//initializes a program node
		LinkedList<FunctionDefinitionNode> functionDefinitionNode = new LinkedList<>();
		LinkedList<BlockNode> blocks = new LinkedList<>();
		LinkedList<BlockNode> blocksBegin = new LinkedList<>();
		LinkedList<BlockNode> blocksEnd = new LinkedList<>();
		ProgramNode program = new ProgramNode(functionDefinitionNode,blocksBegin, blocks, blocksEnd);
		
		//loops while there are still tokens
		while(tokenHandler.MoreTokens() != true) {
			if(ParseFunction(program)) {
				
			}
			else if(ParseAction(program)) {
				
			}
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.SEPARATOR)) {
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
	            LinkedList<String> parameterList = new LinkedList<>();
	            if (tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
	            	tokenHandler.MatchAndRemove(Token.TokenType.LEFTPARENTHESES);
	                // Parse parameter names
	                while (true) {
	                    Optional<Node> parameterToken = ParseOperation();
	                    if (parameterToken.isPresent()) {
	                        parameterList.add(parameterToken.get().toString());
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
	                    // Create a new FunctionDefinitionNode and add it to ProgramNode
	                    FunctionDefinitionNode functionNode = new FunctionDefinitionNode(functionName, parameterList, new LinkedList<>());
	                    programNode.getFunctionDefinitions().add(functionNode);

	                    // Parse the block and add statements to the function
	                    BlockNode blockNode = ParseBlock();
	                    functionNode.getStatements().addAll(blockNode.getStatements());

	                    return true; // Successfully parsed a function
	                }
	            }
	        }
	    }
	    
	    return false;
	}
	
	//Parsing Begin, other, conditional and End blocks
	public boolean ParseAction(ProgramNode programNode) {
		ProgramNode program = programNode;
		BlockNode blocksBegin = null;
		BlockNode blocksEnd = null;
		Optional<Node> condition = ParseOperation();
		        
		// Parsing begin blocks
        if (tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.BEGIN)) {
            blocksBegin.list.add(ParseBlock());
            program.list.add(blocksBegin);
            return true;	
        }

        // Parsing end blocks
        else if (tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.END)) {
            blocksEnd.list.add(ParseBlock());
            program.list.add(blocksEnd);
            return true;
            
        }
        
        // Parsing Conditional blocks
        else if (condition.isPresent()) {
            // Check if the next token is an opening curly brace for the action block
            if (tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
                // Parse the action block and add it to the ProgramNode
            	LinkedList<StatementNode> statements = ParseBlock().getStatements();
                BlockNode actionBlock = new BlockNode(statements, condition);
                program.list.add(actionBlock);
                return true;
            }
        }
        
        // Parsing any other blocks
        else
        {
        	ParseOperation();
        	return true;
        }
        
        return false;

}
	public Optional<Node> ParseBottomLevel(){
		String value;
		Optional<Node> nodeValue;
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
			nodeValue = ParseOperation();
			//checks for a right parentheses
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTPARENTHESES)) {
				//removes the right parentheses and returns the value of parse operation
				tokenHandler.MatchAndRemove(Token.TokenType.RIGHTPARENTHESES);
				return nodeValue;
			}
			//throws an exception if there isn't a right parentheses
			else {
				throw new RuntimeException("Error: Expected a Right Parentheses");
			}
		}
		//checks for a not token
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.NOT)) {
			tokenHandler.MatchAndRemove(Token.TokenType.NOT);
			nodeValue = ParseOperation();
			//returns a new operation node with result of ParseOperation, NOT
			return Optional.of(new OperationNode(nodeValue.get(), OperationNode.Operations.NOT));
		}
		//checks for a minus token
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.MINUS)) {
			tokenHandler.MatchAndRemove(Token.TokenType.MINUS);
			nodeValue = ParseOperation();
			//returns a new operation node with result of ParseOperation, UNARYNEG
			return Optional.of(new OperationNode(nodeValue.get(), OperationNode.Operations.UNARYNEG));
		}
		//checks for a plus token
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.PLUS)) {
			tokenHandler.MatchAndRemove(Token.TokenType.PLUS);
			nodeValue = ParseOperation();
			//returns a new operation node with result of ParseOperation, UNARYPOS
			return Optional.of(new OperationNode(nodeValue.get(), OperationNode.Operations.UNARYPOS));
		}
		//checks for an increment token
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.INCREMENT)) {
			tokenHandler.MatchAndRemove(Token.TokenType.INCREMENT);
			nodeValue = ParseOperation();
			//returns a new operation node with result of ParseOperation, PREINC
			return Optional.of(new OperationNode(nodeValue.get(), OperationNode.Operations.PREINC));
		}
		//checks for a decrement token
		else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.DECREMENT)) {
			tokenHandler.MatchAndRemove(Token.TokenType.DECREMENT);
			nodeValue = ParseOperation();
			//returns a new operation node with result of ParseOperation, PREDEC
			return Optional.of(new OperationNode(nodeValue.get(), OperationNode.Operations.PREDEC));
		}
		else {
			return ParseLValue();
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
					Optional<Node> index = ParseOperation();
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
	public Node ParseFactor() {
		Node value1 = ParseBottomLevel().get();
		if(tokenHandler.MoreTokens() != false) {
			//checks for parentheses
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTPARENTHESES)) {
				//recursively calls parse expression 
				Node value2 = ParseExpression();
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
	public Node ParsePostIncrementDecrement() {
		Node value = ParseFactor();
		if(tokenHandler.MoreTokens() != false) {
			//checks for increment token
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.INCREMENT)) {
				tokenHandler.MatchAndRemove(Token.TokenType.INCREMENT);
				return new OperationNode(value, OperationNode.Operations.POSTINC);
			}
			//checks for decrement token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.DECREMENT)) {
				tokenHandler.MatchAndRemove(Token.TokenType.DECREMENT);
				return new OperationNode(value, OperationNode.Operations.POSTDEC);
			}
		}
		//returns result of parse factor if there is not increment or decrement tokens
		return value;
	}
	
	//calls parse factor
	public Node ParseExponent() {
		Node base = ParsePostIncrementDecrement();
		if(tokenHandler.MoreTokens() != false) {
			//checks for exponent tokens recursively calls itself until there are no exponent tokens
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.EXPONENT)) {
				tokenHandler.MatchAndRemove(Token.TokenType.EXPONENT);
				Node exponent = ParseExponent();
				return new OperationNode(base, Optional.of(exponent), OperationNode.Operations.EXPONENT);
			}
		}
		//returns the result of parse post increment/decrement if there is no exponent token
		return base;
	}
	
	//calls parse exponent
	//checks for multiply, divide or modulus tokens
	public Node ParseTerm() {
		Node value1 = ParseExponent();
		if(tokenHandler.MoreTokens() != false) {
			//checks for multiply token
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.MULTIPLY)) {
				tokenHandler.MatchAndRemove(Token.TokenType.MULTIPLY);
				Node value2 = ParseExponent();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.MULTIPLY);
			}
			//checks for divide token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.DIVIDE)) {
				tokenHandler.MatchAndRemove(Token.TokenType.DIVIDE);
				Node value2 = ParseExponent();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.DIVIDE);
			}		
			//checks for modulus token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.MODULUS)) {
				tokenHandler.MatchAndRemove(Token.TokenType.MODULUS);
				Node value2 = ParseExponent();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.MODULAR);
			}
		}
		//returns the result of parse exponent if there are no multiply, divide, or modulas token
		return value1;
	}
	
	//calls parse term
	//checks for a plus or minus token
	public Node ParseExpression() {
		Node value1 = ParseTerm();
		if(tokenHandler.MoreTokens() != false) {
			//checks for a plus token
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.PLUS)) {
				tokenHandler.MatchAndRemove(Token.TokenType.PLUS);
				Node value2 = ParseTerm();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.ADD);
			}
			//checks for a minus token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.MINUS)) {
				tokenHandler.MatchAndRemove(Token.TokenType.MINUS);
				Node value2 = ParseTerm();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.SUBTRACT);
			}
		}
		//returns the result from parse term if there is no plus or minus token
		return value1;
	}
	
	//calls parse expression
	//checks for string literals
	public Node ParseConcatenation() {
		Node value1 = ParseExpression();
		if(tokenHandler.MoreTokens() != false) {
			//recursively calls itself if there is a string literal token
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.WORD)) {
				Node value2 = ParseConcatenation();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.CONCATENATION);
			}
		}
		//returns parse expression if there is no string literal
		return value1;
	}

	//calls parse concatenation
	//checks for the comparison tokens
	public Node ParseBooleanCompare() {
		Node value1 = ParseConcatenation();
		if(tokenHandler.MoreTokens() != false) {
			//checks for less than token
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LESSTHAN)) {
				tokenHandler.MatchAndRemove(Token.TokenType.LESSTHAN);
				Node value2 = ParseConcatenation();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.LT);
			}
			//checks for greater than token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.GREATERTHAN)) {
				tokenHandler.MatchAndRemove(Token.TokenType.GREATERTHAN);
				Node value2 = ParseConcatenation();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.GT);
			}
			//checks for less than or equal to token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LESSOREQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.LESSOREQUAL);
				Node value2 = ParseConcatenation();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.LE);
			}
			//checks for greater than or equal to token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.GREATEROREQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.GREATEROREQUAL);
				Node value2 = ParseConcatenation();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.GE);
			}
			//checks for not equal to token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.NOTEQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.NOTEQUAL);
				Node value2 = ParseConcatenation();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.NOTMATCH);
			}
			//checks for equal to token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.EQUALITY)) {
				tokenHandler.MatchAndRemove(Token.TokenType.EQUALITY);
				Node value2 = ParseConcatenation();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.EQ);
			}
		}
		//returns parse concatenation if there is no comparison tokens
		return value1;
	}
	
	//calls Parse Boolean Compare
	//creates an operation node if there is a tilde or does not match token
	public Node ParseMatch() {
		Node value1 = ParseBooleanCompare();
		if(tokenHandler.MoreTokens() != false) {
			//checks for a tilde
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.TILDE)) {
				tokenHandler.MatchAndRemove(Token.TokenType.TILDE);
				Node value2 = ParseBooleanCompare();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.MATCH);
			}
			//checks for a does not match token
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.DOESNOTMATCH)) {
				tokenHandler.MatchAndRemove(Token.TokenType.DOESNOTMATCH);
				Node value2 = ParseBooleanCompare();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.NOTMATCH);			
			}
		}
		//returns parse boolean compare if there is no tilde or does not match token
		return value1;
	}
	
	//calls ParseMatch
	//creates an operation node if there is left and right bracket
	public Node ParseArrayIndex() {
		Node expression;
		if(tokenHandler.MoreTokens() != false) {
			//checks for a left bracket
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.LEFTBRACKET)) {
				expression = ParseMatch();
				//checks for a right bracket
				if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.RIGHTBRACKET)) {
					return new OperationNode(expression, OperationNode.Operations.IN);
				}
				else {
					throw new RuntimeException("Expected Right Bracket");
				}
			}
			else {
				//returns parse match if there is no brackets
				expression = ParseMatch();
				return expression;
				}
		}
		else {
			//returns parse match if there is no brackets
			expression = ParseMatch();
			return expression;
			}
	}
	
	//calls parse array index 
	//creates an operation node if there is an and token
	public Node ParseAnd() {
		Node value1 = ParseArrayIndex();
		if(tokenHandler.MoreTokens() != false) {
			//checks for an and token
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.AND)) {
				tokenHandler.MatchAndRemove(Token.TokenType.AND);
				Node value2 = ParseArrayIndex();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.AND);
			}
		}
		//returns parse array index if there is no and token
		return value1;
	}
	
	//calls parse and
	//creates a operation node if there is an or token
	public Node ParseOr() {
		Node value1 = ParseAnd();
		if(tokenHandler.MoreTokens() != false) {
			//checks for an or token
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.OR)) {
				tokenHandler.MatchAndRemove(Token.TokenType.OR);
				Node value2 = ParseAnd();
				return new OperationNode(value1, Optional.of(value2), OperationNode.Operations.OR);
			}
		}
		//returns parse and if there is no or token
		return value1;
	}
	
	//calls ParseOr
	//creates a ternary node if there is a question mark and colon
	public Node ParseTernary() {
		Node condition = ParseOr();
		Node trueCase = null;
		Node falseCase = null;
		if(tokenHandler.MoreTokens() != false) {
			//checks for a question mark
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.QUESTION)) {
				tokenHandler.MatchAndRemove(Token.TokenType.QUESTION);
				trueCase = ParseOr();
				//checks for a colon
				if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.COLON)) {
					tokenHandler.MatchAndRemove(Token.TokenType.COLON);
					falseCase = ParseOr();		
					return new TernaryNode(condition, trueCase, falseCase);
				}
			}
		}
		//returns the result of parse or if there is no questionmark and colon
		return condition;

	}
	

	//calls parse ternary
	//creates an assignment node if there is any of the equals signs
	public Node ParseAssignment() {
		Node value1 = ParseTernary();
		if(tokenHandler.MoreTokens() != false) {
			//checks for an equal sign
			if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.EQUALS)) {
				tokenHandler.MatchAndRemove(Token.TokenType.EQUALS);
				Node value2 = ParseTernary();
				return new AssignmentNode(value1, new OperationNode(value1, Optional.of(value2), OperationNode.Operations.EQ));			
			}
			//checks for a plus equal sign
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.PLUSEQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.PLUSEQUAL);
				Node value2 = ParseTernary();
				return new AssignmentNode(value1, new OperationNode(value1, Optional.of(value2), OperationNode.Operations.ADD));			
			}	
			//checks for a minus equal sign
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.MINUSEQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.MINUSEQUAL);
				Node value2 = ParseTernary();
				return new AssignmentNode(value1, new OperationNode(value1, Optional.of(value2), OperationNode.Operations.SUBTRACT));			
			}
			//checks for a multiply equal sign
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.MULTIPLYEQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.MULTIPLYEQUAL);
				Node value2 = ParseTernary();
				return new AssignmentNode(value1, new OperationNode(value1, Optional.of(value2), OperationNode.Operations.MULTIPLY));			
			}
			//checks for a divide equal sign
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.DIVIDEEQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.DIVIDEEQUAL);
				Node value2 = ParseTernary();
				return new AssignmentNode(value1, new OperationNode(value1, Optional.of(value2), OperationNode.Operations.DIVIDE));			
			}	
			//checks for a modulus equal sign
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.MODULUSEQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.MODULUSEQUAL);
				Node value2 = ParseTernary();
				return new AssignmentNode(value1, new OperationNode(value1, Optional.of(value2), OperationNode.Operations.MODULAR));			
			}	
			//checks for a exponent equal sign
			else if(tokenHandler.Peek(0).get().getTokenType().equals(Token.TokenType.EXPONENTEQUAL)) {
				tokenHandler.MatchAndRemove(Token.TokenType.EXPONENTEQUAL);
				Node value2 = ParseTernary();
				return new AssignmentNode(value1, new OperationNode(value1, Optional.of(value2), OperationNode.Operations.EXPONENT));			
			}
		}
		//returns the result of parse ternary if there is no assignment
		return value1;
	}
	

	

	
	//empty holder for parsing blocks
	public BlockNode ParseBlock() {
		BlockNode block = new BlockNode(null, Optional.empty());
		return  block;
	}
	
	//empty holder for parsing operations 
	//calls parse bottom level in parser 2
	public Optional<Node> ParseOperation(){
		return Optional.of(ParseAssignment());
	}
}
