import java.util.HashMap;

public class InterpreterArrayDataType extends InterpreterDataType{
	HashMap<String, InterpreterDataType> iadt;
	
	public InterpreterArrayDataType() {
		iadt = new HashMap<>();
	}
	
	public void addEntry(String string, InterpreterDataType idt) {
		iadt.put(string, idt);
	}
	
	public InterpreterDataType getIDT(String string) {
		return iadt.get(string);
	}
	
	public void removeIDT(String string) {
		iadt.remove(string);
	}
	
	public String toString() {
		return iadt.toString();
	}
}
