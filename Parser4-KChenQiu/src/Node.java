import java.util.LinkedList;
import java.util.List;

public abstract class Node {
	protected List<Node> list = new LinkedList<>();
	
	
	public String toString() {
		return list.toString();
	}
}
