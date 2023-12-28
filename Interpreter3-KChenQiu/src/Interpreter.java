import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//finish ternary and variable reference 11/4

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
		FunctionVariableHelper(programNode);
		
		
		//lambda expression to handle built in functions
		Function<HashMap<String, InterpreterDataType>, String> Execute = (FunctionVariables) -> {
		//print functions
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
	
	public InterpreterDataType GetIDT(Node node, HashMap<String, InterpreterDataType> localVariables) {
		//checks if the node is an assignment node
		if(node instanceof AssignmentNode) {
			//gets the target from the left side
			Node target = ((AssignmentNode) node).getTarget();				
			InterpreterDataType expression = null;
			//checks that the target is a variable or operation
			if(target instanceof VariableReferenceNode || target instanceof OperationNode) {
				//checks the right side
				expression = GetIDT(((AssignmentNode) node).getExpression(), localVariables);
			}
			//inserts the target with the result of the expression into the local variables
			localVariables.put(target.toString(), expression);
			//returns the result of the expression
			return expression;		
		}
		//checks if the node is a constant node and returns a new idt with the constant node as the value
		else if(node instanceof ConstantNode) {
			return new InterpreterDataType(((ConstantNode) node).toString());
		}
		//checks if the node is a function call node and calls RunFunctionCall
		else if(node instanceof FunctionCallNode) {
			String result = RunFunctionCall((FunctionCallNode)node, localVariables);
			return new InterpreterDataType(result);
		}	
		//checks if the node is a pattern node and throws an exception
		else if(node instanceof PatternNode) {
			throw new RuntimeException("Error: Does not handle PatternNode");
		}
		//checks if the node is a ternary node
		else if(node instanceof TernaryNode) {
			//gets the condition
			InterpreterDataType condition = GetIDT(((TernaryNode)node).getExpression(), localVariables);
			//checks if the condition returns true or false
			if(condition.toString().equals("true")) {
				return GetIDT(((TernaryNode)node).getTrueCase(), localVariables);
			}
			else if(condition.toString().equals("false")) {
				return GetIDT(((TernaryNode)node).getFalseCase(), localVariables);
			}
			
		}
		//checks if the node is a variable reference node
		else if(node instanceof VariableReferenceNode) {			
			//finds the name of the node in local or global variable
			InterpreterDataType localVariableStore = localVariables.get(((VariableReferenceNode)node).getName());
			InterpreterDataType globalVariableStore = globalVariables.get(((VariableReferenceNode)node).getName());
			//checks if the variable had an index
			if(((VariableReferenceNode)node).getIndex().equals(null)) {
				if(localVariableStore == null) {
					return globalVariableStore;
				}
				else if(globalVariableStore == null) {
					return localVariableStore;
				}
			}
			else {
				String index = ((VariableReferenceNode)node).getIndex().toString();
				if(globalVariableStore == null && localVariableStore == null) {
					throw new RuntimeException("Error: Array not found");
				}
				if(localVariableStore != null && localVariableStore instanceof InterpreterArrayDataType) {
					return ((InterpreterArrayDataType) localVariableStore).getIDT(index);
				}
				else if(globalVariableStore != null && globalVariableStore instanceof InterpreterArrayDataType) {
					return ((InterpreterArrayDataType) globalVariableStore).getIDT(index);
				}
			}
		}	
		//checks if the node is an operation node
		else if(node instanceof OperationNode) {
			InterpreterDataType leftNode = GetIDT(((OperationNode)node).getLeftNode(), localVariables);
			InterpreterDataType rightNode = null;
			String operationType = ((OperationNode)node).getOperation();
			//only gets the right node if it isn't a match or not match
			if(!operationType.equals("MATCH") && !operationType.equals("NOTMATCH") && !((OperationNode)node).getRightNode().equals(null)) {
				rightNode = GetIDT(((OperationNode)node).getRightNode(), localVariables);
			}					
			String leftString = leftNode.toString();
			String rightString = rightNode.toString();
			float left, right, result;
			//switch case for all the operation types
			switch(operationType) {				
				case "ADD":
					left = Float.valueOf(leftNode.toString());
					right = Float.valueOf(rightNode.toString());
					result = left + right;
					return new InterpreterDataType(String.valueOf(result));
					
				case "SUBTRACT":
					left = Float.valueOf(leftNode.toString());
					right = Float.valueOf(rightNode.toString());
					result = left - right;
					return new InterpreterDataType(String.valueOf(result));
					
				case "MULTIPLY":
					left = Float.valueOf(leftNode.toString());
					right = Float.valueOf(rightNode.toString());
					result = left * right;
					return new InterpreterDataType(String.valueOf(result));
					
				case "DIVIDE":
					left = Float.valueOf(leftNode.toString());
					right = Float.valueOf(rightNode.toString());
					result = left / right;
					return new InterpreterDataType(String.valueOf(result));
					
				case "EXPONENT":
					left = Float.valueOf(leftNode.toString());
					right = Float.valueOf(rightNode.toString());
					result = (float) Math.pow(left, right);
					return new InterpreterDataType(String.valueOf(result));
					
				case "MODULAR":
					left = Float.valueOf(leftNode.toString());
					right = Float.valueOf(rightNode.toString());
					result = left % right;
					return new InterpreterDataType(String.valueOf(result));
					
				case "EQ":
					left = Float.valueOf(leftNode.toString());
					right = Float.valueOf(rightNode.toString());
					if(left == 0 || right == 0) {
						if(leftString.equals(rightString)) {
							return new InterpreterDataType("true");
						}
						else {
							return new InterpreterDataType("false");
						}
					}
					else {
						if(left == right) {
							return new InterpreterDataType("true");
						}
						else {
							return new InterpreterDataType("false");
						}
					}
					
				case "NE":
					left = Float.valueOf(leftNode.toString());
					right = Float.valueOf(rightNode.toString());
					if(left == 0 || right == 0) {
						if(!leftString.equals(rightString)) {
							return new InterpreterDataType("true");
						}
						else {
							return new InterpreterDataType("false");
						}
					}
					else {
						if(left != right) {
							return new InterpreterDataType("true");
						}
						else {
							return new InterpreterDataType("false");
						}
					}
					
				case "LT":
					left = Float.valueOf(leftNode.toString());
					right = Float.valueOf(rightNode.toString());
					if(left == 0 || right == 0) {
						if(leftString.compareTo(rightString) < 0) {
							return new InterpreterDataType("true");
						}
						else {
							return new InterpreterDataType("false");
						}
					}
					else {
						if(left < right) {
							return new InterpreterDataType("true");
						}
						else {
							return new InterpreterDataType("false");
						}
					}
					
				case "LE":
					left = Float.valueOf(leftNode.toString());
					right = Float.valueOf(rightNode.toString());
					if(left == 0 || right == 0) {
						if(leftString.compareTo(rightString) <= 0) {
							return new InterpreterDataType("true");
						}
						else {
							return new InterpreterDataType("false");
						}
					}
					else {
						if(left <= right) {
							return new InterpreterDataType("true");
						}
						else {
							return new InterpreterDataType("false");
						}
					}
					
				case "GT":
					left = Float.valueOf(leftNode.toString());
					right = Float.valueOf(rightNode.toString());
					if(left == 0 || right == 0) {
						if(leftString.compareTo(rightString) > 0) {
							return new InterpreterDataType("true");
						}
						else {
							return new InterpreterDataType("false");
						}
					}
					else {
						if(left > right) {
							return new InterpreterDataType("true");
						}
						else {
							return new InterpreterDataType("false");
						}
					}
					
				case "GE":
					left = Float.valueOf(leftNode.toString());
					right = Float.valueOf(rightNode.toString());
					if(left == 0 || right == 0) {
						if(leftString.compareTo(rightString) >= 0) {
							return new InterpreterDataType("true");
						}
						else {
							return new InterpreterDataType("false");
						}
					}
					else {
						if(left >= right) {
							return new InterpreterDataType("true");
						}
						else {
							return new InterpreterDataType("false");
						}
					}
					
				case "CONCATENATION":
					return new InterpreterDataType(leftString + rightString);
					
				case "PREINC":
					left = Float.valueOf(leftNode.toString());
					++left;
					return new InterpreterDataType(String.valueOf(left));
					
				case "POSTINC":
					left = Float.valueOf(leftNode.toString());
					left++;
					return new InterpreterDataType(String.valueOf(left));
					
				case "PREDEC":
					left = Float.valueOf(leftNode.toString());
					--left;
					return new InterpreterDataType(String.valueOf(left));
					
				case "POSTDEC":
					left = Float.valueOf(leftNode.toString());
					left--;
					return new InterpreterDataType(String.valueOf(left));
					
				case "UNARYPOS":
					left = Float.valueOf(leftNode.toString());
					result = left * 1;
					return new InterpreterDataType(String.valueOf(result));
					
				case "UNARYNEG":
					left = Float.valueOf(leftNode.toString());
					result = left * -1;
					return new InterpreterDataType(String.valueOf(result));
					
				case "AND":
					left = Float.valueOf(leftNode.toString());
					right = Float.valueOf(rightNode.toString());
					if(left != 0 && right != 0) {
						return new InterpreterDataType("true");
					}
					else {
						return new InterpreterDataType("false");
					}
					
				case "OR":
					left = Float.valueOf(leftNode.toString());
					right = Float.valueOf(rightNode.toString());
					if(left != 0 || right != 0) {
						return new InterpreterDataType("true");
					}					
					else {
						return new InterpreterDataType("false");
					}
					
				case "NOT":
					return new InterpreterDataType("Not" + leftString);
					
				case "MATCH":
					Pattern pattern = Pattern.compile(leftString);
					Matcher matcher = pattern.matcher(rightString);
					boolean matchFound = matcher.find();
					if(matchFound) {
						return new InterpreterDataType("true");
					}
					else {
						return new InterpreterDataType("false");
					}
					
				case "NOTMATCH":
					pattern = Pattern.compile(leftString);
					matcher = pattern.matcher(rightString);
					matchFound = matcher.find();
					if(matchFound) {
						return new InterpreterDataType("false");
					}
					else {
						return new InterpreterDataType("true");
					}
					
				case "DOLLAR":
					InterpreterDataType idt = GetIDT(((OperationNode)node).getLeftNode(), localVariables);
					return new InterpreterDataType("$" + idt.toString());
					
				case "IN":
					Node arrayNode = ((OperationNode) node).getRightNode();
					if(arrayNode instanceof VariableReferenceNode) {
						if(((VariableReferenceNode)arrayNode).getIndex() != null) {
							if(localVariables.containsKey(((OperationNode) node).getLeftNode().toString())) {
								return localVariables.get(((OperationNode) node).getLeftNode().toString());
							}
							else if(globalVariables.containsKey(((OperationNode) node).getLeftNode().toString())) {
								return globalVariables.get(((OperationNode) node).getLeftNode().toString());
							}
						}
						else {
							throw new RuntimeException("Error: Array not found");
						}
					}
			}
			
		}
		return null;
	}
	
	public String RunFunctionCall(FunctionCallNode node, HashMap<String, InterpreterDataType> localVariables) {
		return "";
	}
	
	public ReturnType ProcessStatement(HashMap<String, InterpreterDataType> localVariables, StatementNode statement) {
		//returns a break return type
		if(statement instanceof BreakNode) {
			return new ReturnType(ReturnType.Type.BREAK);
		}
		//return a continue return type
		else if(statement instanceof ContinueNode) {
			return new ReturnType(ReturnType.Type.CONTINUE);
		}
		//deletes the parameter given by delete node and returns a none return type
		else if(statement instanceof DeleteNode) {
			Node parameter = ((DeleteNode)statement).getDelete();
			if(parameter instanceof VariableReferenceNode) {
				String arrayName = ((VariableReferenceNode)parameter).getName();					
				InterpreterDataType localVariableStore = localVariables.get(arrayName);
				InterpreterDataType globalVariableStore = globalVariables.get(arrayName);
				//checks if the array has an index
				if(((VariableReferenceNode)parameter).getIndex() != null) {
					//removes the array index from local or global variables
					if(localVariableStore != null && localVariableStore instanceof InterpreterArrayDataType) {
						((InterpreterArrayDataType)localVariableStore).removeIDT(((VariableReferenceNode)parameter).getIndex().toString());
						return new ReturnType(ReturnType.Type.NONE);
					}
					else if(globalVariableStore != null && globalVariableStore instanceof InterpreterArrayDataType) {
						((InterpreterArrayDataType)globalVariableStore).removeIDT(((VariableReferenceNode)parameter).getIndex().toString());
						return new ReturnType(ReturnType.Type.NONE);
					}
				}
				else {
					//the array didnt have an index stated, so it is deleting the whole array
					if(localVariableStore != null && localVariableStore instanceof InterpreterArrayDataType) {
						((InterpreterArrayDataType)localVariableStore).removeIDT(arrayName);
						return new ReturnType(ReturnType.Type.NONE);
					}
					else if(globalVariableStore != null && globalVariableStore instanceof InterpreterArrayDataType) {
						((InterpreterArrayDataType)globalVariableStore).removeIDT(arrayName);
						return new ReturnType(ReturnType.Type.NONE);
					}
				}
			}
			else {
				throw new RuntimeException("Error: Variable not found");
			}
		}
		//calls InterpretListOfStatements in a dowhile loop and checks the return statement
		else if(statement instanceof DoWhileNode) {
			DoWhileNode doWhileNode = (DoWhileNode)statement;
			InterpreterDataType conditionCheck;
			ReturnType statementCheck;
			do {
				statementCheck = InterpretListOfStatements(doWhileNode.getStatements(), localVariables);
				if(statementCheck.getType().equals(ReturnType.Type.BREAK)) {
					break;
				}
				if(statementCheck.getType().equals(ReturnType.Type.RETURN)) {
					return statementCheck;
				}
				conditionCheck = GetIDT(doWhileNode.getCondition(), localVariables);
			}while(conditionCheck.toString().equals("false"));
		}
		
		//creates a while loop using the condition and calls InterpretListOfStatements and checks the return type
		else if(statement instanceof ForNode) {
			ForNode forNode = (ForNode)statement;
			ProcessStatement(localVariables, ((StatementNode)forNode.getCondition().getFirst()));
			if(forNode.getCondition().get(1) != null) {
				GetIDT(forNode.getCondition().getFirst(), localVariables);
				InterpreterDataType condition = GetIDT(forNode.getCondition().get(1), localVariables);
				while(condition.toString().equals("false")) {
					InterpretListOfStatements(forNode.getStatements(), localVariables);
					ProcessStatement(localVariables, (StatementNode)forNode.getCondition().getLast());
				}
			}
			//looks for the array and calls InterpretListOfStatements on the statements for each array index
			else {
				InterpreterDataType array = GetIDT(forNode.getCondition().getFirst(), localVariables);
				if(array != null && array instanceof InterpreterArrayDataType) {
					int i = 0;
					InterpreterDataType arrayIndex = ((InterpreterArrayDataType)array).getIDT(String.valueOf(i));
					while(arrayIndex != null) {
						ReturnType statementCheck = InterpretListOfStatements(forNode.getStatements(), localVariables);
						if(statementCheck.getType().equals(ReturnType.Type.BREAK)) {
							break;
						}
						if(statementCheck.getType().equals(ReturnType.Type.RETURN)) {
							return statementCheck;
						}
						i++;
						arrayIndex = ((InterpreterArrayDataType)array).getIDT(String.valueOf(i));
					}
				}
			}
		}
		
		//looks through the entire Linked List of IfNodes and calls InterpretListOfStatements if the condition is empty or true
		else if(statement instanceof IfNode) {
			int i = 0;
			IfNode ifCheck = (IfNode) ((IfNode)statement).list.get(i);
			while(ifCheck != null) {
				if(ifCheck.getCondition() == null || GetIDT(ifCheck.getCondition(), localVariables).toString() == "true") {
					ReturnType statementCheck = InterpretListOfStatements(ifCheck.getStatements(), localVariables);
					if(!statementCheck.getType().equals(ReturnType.Type.NONE)) {
						return statementCheck;
					}
					i++;
					ifCheck = (IfNode) ((IfNode)statement).list.get(i);
				}
			}
		}
		
		//evaluates the value and returns a new ReturnType
		else if(statement instanceof ReturnNode) {
			if(((ReturnNode)statement).getParameter() != null) {
				return new ReturnType(((ReturnNode)statement).getParameter().toString(), ReturnType.Type.RETURN);
			}
		}
		
		//same as DoWhile but uses a While loop instead
		else if(statement instanceof WhileNode) {
			WhileNode whileNode = (WhileNode)statement;
			InterpreterDataType conditionCheck = GetIDT(((DoWhileNode)statement).getCondition(), localVariables);
			ReturnType statementCheck;
			while(conditionCheck.toString().equals("false")) {
				statementCheck = InterpretListOfStatements(whileNode.getStatements(), localVariables);
				if(statementCheck.getType().equals(ReturnType.Type.BREAK)) {
					break;
				}
				conditionCheck = GetIDT(whileNode.getCondition(), localVariables);
			}
		}
		else {
			InterpreterDataType idt = GetIDT(statement, localVariables);
			if(!idt.equals(null)) {
				return new ReturnType(ReturnType.Type.NONE);
			}
			else {
				throw new RuntimeException("Error: No Valid Value");
			}
		}
		return null;
	}
	
	//loops over all the statements and calls ProcessStatement on each
	//checks the Return type and returns the ReturnType if it is not NONE
	public ReturnType InterpretListOfStatements(LinkedList<StatementNode> statements, HashMap<String, InterpreterDataType> localVariables) {
		int i = 0;
		while(statements.get(i) != null) {
			ReturnType statementCheck = ProcessStatement(localVariables, statements.get(i));
			if(!statementCheck.getType().equals(ReturnType.Type.NONE)) {
				return statementCheck;
			}
			i++;
		}
		return null;
	}

	//initializes global variables
	public void GlobalVariableHelper() {
		globalVariables.put("FS", new InterpreterDataType(" "));
		globalVariables.put("OFMT", new InterpreterDataType("%.6g"));
		globalVariables.put("OFS", new InterpreterDataType(" "));
		globalVariables.put("ORS", new InterpreterDataType("\n"));
	}
	
	//initializes function variables
	public void FunctionVariableHelper(ProgramNode programNode) {
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
		for(int i = 0; i < programNode.getFunctionDefinitions().size(); i++) {
			functionVariables.put(programNode.getFunctionDefinitions().get(i).getName(), programNode.getFunctionDefinitions().get(i));
		}
		
	}

}
