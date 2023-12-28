import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main{
    public static void main(String args[]) throws IOException {
    	//puts the awk SampleText into the lexer
    	Path myPath = Paths.get("SampleText.awk");
    	String content = new String(Files.readAllBytes(myPath));
    	//sets up the lexer
    	Lexer lex = new Lexer(content);
    	//puts the lexer through the parser
    	Parser parse = new Parser(lex);
    	//prints out the tokens and its value from the lexer 
    	System.out.println(lex.Lex());
    	//prints out the program(does nothing as of yet)
    	System.out.println(parse.Parse());
    	
    }
}