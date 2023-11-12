import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interpreter {
	HashMap<String, InterpreterDataType> globalVariables = new HashMap<>();
	HashMap<String, FunctionDefinitionNode> functionVariables = new HashMap<>();
	
	public class LineManager{
		List<String> string;
		int lineIndex;
		String[] fieldSeperated;
		int nf;
		int nr;
		int fnr;
		
		//constructor takes in a list of strings and initializes all the variables
		public LineManager(List<String> string) {
			this.string = string;
			lineIndex = 0;
			nf = 0;
			nr = 0;
			fnr = 0;
		}
		
		public boolean SplitAndAssign() {
			//returns false if there is no line to split
			if(lineIndex >= string.size()) {
				return false;
			}
			
			String currentLine = string.get(lineIndex);
			fieldSeperated = currentLine.split(globalVariables.get("FS").toString());
			nf = fieldSeperated.length;
			nr++;
			fnr++;
			
			//fills the global variables with $0, $1, $2 and so on until it reaches the end 
			for(int i = 0; i < nf; i++) {
				String variableName = "$" + i;
				globalVariables.put(variableName, new InterpreterDataType(fieldSeperated[i]));
			}
			lineIndex++;
			return true;
		}
	}
	
	public Interpreter(ProgramNode programNode, Path filePath) throws IOException{
		//initialize line manager
		List<String> file = null;
		LineManager lineManager;
		if(!filePath.equals(null)) {
			file = Files.readAllLines(filePath);
		}
		lineManager = new LineManager(file);
		globalVariables.put("FILENAME", new InterpreterDataType(filePath.getFileName().toString()));
		
		//presets global and function variables
		GlobalVariableHelper();	
		FunctionVariableHelper();
		
		//lambda expression to handle built in functions
		Function<HashMap<String, InterpreterDataType>, String> Execute = (FunctionVariables) -> {
		//print funcitons
		if(functionVariables.containsKey("print")) {
			return functionVariables.get("print").getParameters().toString();
		}
		else if(functionVariables.containsKey("printf")) {
			return functionVariables.get("printf").getParameters().toString();
		}
		//getline and next calls split and assign
		else if(functionVariables.containsKey("getline") || functionVariables.containsKey("next")) {
			lineManager.SplitAndAssign();
			return null;
		}
		//gsub, match and sub use built in regular expressions
		else if(functionVariables.containsKey("gsub")) {
			Pattern pattern = Pattern.compile(functionVariables.get("gsub").getParameters().getLast().toString());
			Matcher matcher = pattern.matcher(functionVariables.get("gsub").getStatements().toString());
			if(matcher.find() == true) {
				return matcher.toString().replace(functionVariables.get("gsub").getParameters().getFirst().toString(), matcher.toString());
			}
			else {
				return null;
			}
		}
		else if(functionVariables.containsKey("match")) {
			Pattern pattern = Pattern.compile(functionVariables.get("match").getParameters().toString());
			Matcher matcher = pattern.matcher(functionVariables.get("match").getStatements().toString());
			if(matcher.find() == true) {
				return matcher.toString();
			}
			else {
				return null;
			}
		}
		else if(functionVariables.containsKey("sub")) {
			Pattern pattern = Pattern.compile(functionVariables.get("sub").getParameters().getLast().toString());
			Matcher matcher = pattern.matcher(functionVariables.get("sub").getParameters().getFirst().toString());
			if(matcher.find() == true) {
				return matcher.toString().replace(functionVariables.get("sub").getParameters().get(1).toString(), matcher.toString());
			}
			else {
				return null;
			}
		}
		//uses string functions to return a value
		else if(functionVariables.containsKey("index")) {
			return functionVariables.get("index").getParameters().toString();
		}
		else if(functionVariables.containsKey("length")) {
			int length = functionVariables.get("length").getParameters().getFirst().length();
			return String.valueOf(length);
		}
		else if(functionVariables.containsKey("split")) {
			String string = functionVariables.get("split").getParameters().getFirst();
			String arrayName = functionVariables.get("split").getParameters().get(1);
			String separator = functionVariables.get("split").getParameters().get(2);
			String separatorArray = functionVariables.get("split").getParameters().getLast();
			
			String[] array = string.split(separator);
			int count = 0;
			for(int i = 0; i < string.length();i++) {
				if(string.charAt(i) == separator.charAt(0)) {
					count++;
				}
			}
			return arrayName + array + separatorArray + count + separator;
		}
		else if(functionVariables.containsKey("substr")) {
			String string = functionVariables.get("substr").getParameters().getFirst();
			String start = functionVariables.get("substr").getParameters().get(1);
			String length = functionVariables.get("substr").getParameters().getLast();
			
			String substr = null;
			for(int i = Integer.parseInt(start); i < Integer.parseInt(length); i++) {
				substr = substr + string.charAt(i);
			}
			
			return substr;
		}
		else if(functionVariables.containsKey("tolower")) {
			String string = functionVariables.get("tolower").getParameters().getFirst();
			return string.toLowerCase();
		}
		else if(functionVariables.containsKey("toupper")) {
			String string = functionVariables.get("toupper").getParameters().getFirst();
			return string.toUpperCase();
		}
		return null;
	};
	}
	

	//initializes global variables
	public void GlobalVariableHelper() {
		globalVariables.put("FS", new InterpreterDataType(" "));
		globalVariables.put("OFMT", new InterpreterDataType("%.6g"));
		globalVariables.put("OFS", new InterpreterDataType(" "));
		globalVariables.put("ORS", new InterpreterDataType("\n"));
	}
	
	//initializes function variables
	public void FunctionVariableHelper() {
		functionVariables.put("print", new BuiltInFunctionDefinitionNode("print", true, null, null));
		functionVariables.put("printf", new BuiltInFunctionDefinitionNode("printf", true, null, null));
		functionVariables.put("getline", new BuiltInFunctionDefinitionNode("getline", false, null, null));
		functionVariables.put("next", new BuiltInFunctionDefinitionNode("next", false, null, null));
		functionVariables.put("gsub", new BuiltInFunctionDefinitionNode("gsub", false, null, null));
		functionVariables.put("match", new BuiltInFunctionDefinitionNode("match", false, null, null));
		functionVariables.put("sub", new BuiltInFunctionDefinitionNode("sub", false, null, null));
		functionVariables.put("index", new BuiltInFunctionDefinitionNode("index", false, null, null));
		functionVariables.put("length", new BuiltInFunctionDefinitionNode("length", false, null, null));
		functionVariables.put("split", new BuiltInFunctionDefinitionNode("split", false, null, null));
		functionVariables.put("substr", new BuiltInFunctionDefinitionNode("substr", false, null, null));
		functionVariables.put("tolower", new BuiltInFunctionDefinitionNode("tolower", false, null, null));
		functionVariables.put("toupper", new BuiltInFunctionDefinitionNode("toupper", false, null, null));
	}

}
