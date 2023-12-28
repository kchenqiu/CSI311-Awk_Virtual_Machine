import java.util.LinkedList;

public class FunctionDefinitionNode extends Node{
	private String name;
    private LinkedList<StatementNode> statements = new LinkedList<>();
    private LinkedList<Node> parameters = new LinkedList<>();

    //constructor for function definition node
    public FunctionDefinitionNode(String name, LinkedList<Node> parameters, LinkedList<StatementNode> statements) {
    	this.name = name;
    	this.parameters = parameters;
    	this.statements = statements;
    }
    
    //accessor method for name
    public String getName() {
    	return name;
    }
    
    //accessor method for parameters
    public LinkedList<Node> getParameters(){
    	return parameters;
    }
    
    //accessor method for statements
    public LinkedList<StatementNode> getStatements() {
    	return statements;
    }
    
    public String toString() {
    	return name + statements.toString() + parameters.toString();
    }
}


