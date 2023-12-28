import java.util.LinkedList;

public class FunctionCallNode extends StatementNode{
	String functionName;
	LinkedList<Node> parameters;

	public FunctionCallNode(String statement, LinkedList<Node> parameters) {
		functionName = statement;
		this.parameters = parameters;
	}

	public String getFunctionName() {
		return functionName;
	}
	
	public LinkedList<Node> getParameters(){
		return parameters;
	}
	
	public String toString() {
		if(parameters == null) {			
		return functionName;
		}
		else {
		return functionName + ": " + parameters.toString();
		}
	}
}
