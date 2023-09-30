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
			else if(tokenHandler.Peek(0).equals(Optional.of(Token.TokenType.SEPARATOR))) {
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
		if (tokenHandler.Peek(0).equals(Optional.of(Token.TokenType.FUNCTION))) {
			tokenHandler.MatchAndRemove(Token.TokenType.FUNCTION);
	        // Parse the function name
	        Optional<Token> functionNameToken = tokenHandler.MatchAndRemove(Token.TokenType.WORD);
	        if (functionNameToken.isPresent()) {
	            String functionName = functionNameToken.get().toString();

	            // Parse the parameter list
	            LinkedList<String> parameterList = new LinkedList<>();
	            if (tokenHandler.Peek(0).equals(Optional.of(Token.TokenType.LEFTPARENTHESES))) {
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
        if (tokenHandler.Peek(0).equals(Optional.of(Token.TokenType.BEGIN))) {
            blocksBegin.list.add(ParseBlock());
            program.list.add(blocksBegin);
            return true;	
        }

        // Parsing end blocks
        else if (tokenHandler.Peek(0).equals(Optional.of(Token.TokenType.END))) {
            blocksEnd.list.add(ParseBlock());
            program.list.add(blocksEnd);
            return true;
            
        }
        
        // Parsing Conditional blocks
        else if (condition.isPresent()) {
            // Check if the next token is an opening curly brace for the action block
            if (tokenHandler.Peek(0).equals(Optional.of(Token.TokenType.LEFTPARENTHESES))) {
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
	
	//empty holder for parsing blocks
	public BlockNode ParseBlock() {
		BlockNode block = new BlockNode(null, Optional.empty());
		return  block;
	}
	
	//empty holder for parsing operations
	public Optional<Node> ParseOperation(){
		return Optional.empty();
	}
}
