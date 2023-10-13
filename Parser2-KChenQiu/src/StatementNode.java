
public abstract class StatementNode extends Node{
    private String statement;

    //constructor for statement node
    public StatementNode(String statement) {
        this.statement = statement;
    }
    
    public String toString() {
    	return statement;
    }
}
