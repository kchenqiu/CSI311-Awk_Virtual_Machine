import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main{
    public static void main(String args[]) throws IOException {
    	//puts the awk SampleText into the lexer
    	Path myPath = Paths.get("SampleText.awk");
    	String content = new String(Files.readAllBytes(myPath));
    	Lexer lex = new Lexer(content);
    	
    	//prints out the tokens and its value from the lexer
    	System.out.print(lex.Lex());
    }
}