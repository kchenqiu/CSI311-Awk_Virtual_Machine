import java.util.LinkedList;
import java.util.Optional;

public class BlockNode extends Node{
    private LinkedList<StatementNode> statements = new LinkedList<>();
    private Optional<Node> condition;

    //constructor for block node
    public BlockNode(LinkedList<StatementNode> statements, Optional<Node> condition) {
    	this.statements = statements;
    	this.condition = condition;
    }
    
    //accessor method for statements
    public LinkedList<StatementNode> getStatements() {
    	return statements;
    }
    
    //accessor method for condition
    public Optional<Node> getCondition(){
    	return condition;
    }
    
    public String toString() {
    	if(condition != null && condition.isPresent()) {
    		return "(" + condition.get().toString() + "): " + statements.toString() ;
    	}
    	else {
    		return statements.toString();
    	}
    }
}
