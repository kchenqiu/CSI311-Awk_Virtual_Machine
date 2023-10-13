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
			//returns optional empty if there is no word or dollarsign token
			return Optional.empty();
		}
	}
	
	//empty holder for parsing blocks
	public BlockNode ParseBlock() {
		BlockNode block = new BlockNode(null, Optional.empty());
		return  block;
	}
	
	//empty holder for parsing operations 
	//calls parse bottom level in parser 2
	public Optional<Node> ParseOperation(){
		ParseBottomLevel();
		return Optional.empty();
	}
}
