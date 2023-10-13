import java.util.Optional;

public class TokenHandler {
	private Lexer lexInfo;
	
	//constructor for token handler(takes in a list of tokens)
	public TokenHandler(Lexer info) {
		this.lexInfo = info;
	}
	
	//peeks j tokens ahead and returns a token
	public Optional<Token> Peek(int j) {
		if(lexInfo.Lex().isEmpty()) {
			return null;
		}
		else
		{
			return Optional.of(lexInfo.Lex().get(j));
		}
	}
	
	//returns true if there are more tokens in the list
	public boolean MoreTokens() {
		if(lexInfo.Lex().isEmpty()) {
			return false;
		}
		else {
			return true;
		}
	}
	
	//
	public Optional<Token> MatchAndRemove(Token.TokenType t){
		if(!lexInfo.Lex().isEmpty()) {
			Token token = lexInfo.Lex().get(0);
			if(token.getTokenType().equals(t)) {
				lexInfo.Lex().remove(0);
				return Optional.of(token);
			}	
		}
		return Optional.empty();
	}
}
