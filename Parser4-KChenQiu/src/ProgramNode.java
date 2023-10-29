import java.util.LinkedList;

public class ProgramNode extends Node{
	 private LinkedList<FunctionDefinitionNode> functionDefinitions = new LinkedList<>();
	 private LinkedList<BlockNode> blocksBegin = new LinkedList<>();
	 private LinkedList<BlockNode> blocksEnd = new LinkedList<>();
	 private LinkedList<BlockNode> blocks = new LinkedList<>();
	 
	 public ProgramNode(LinkedList<FunctionDefinitionNode> functionDefinitions,
			LinkedList<BlockNode> blocksBegin, LinkedList<BlockNode> blocks, LinkedList<BlockNode> blocksEnd) {
		 this.functionDefinitions = functionDefinitions;
		 this.blocksBegin = blocksBegin;
		 this.blocksEnd = blocksEnd;
		 this.blocks = blocks;
	 }
	 
	 public LinkedList<FunctionDefinitionNode> getFunctionDefinitions() {
		 return functionDefinitions;
	 }
	 
	 public LinkedList<BlockNode> getBlocksBegin(){
		 return blocksBegin;
	 }
	 
	 public LinkedList<BlockNode> getBlocks(){
		 return blocks;
	 }
	 
	 public LinkedList<BlockNode> getBlocksEnd(){
		 return blocksEnd;
	 }
	 
	 public String toString() {
		 return "Function: " + functionDefinitions.toString() + "\nBegin: " + blocksBegin.toString() + "\nBlocks: " + blocks.toString() + "\nEnd:" + blocksEnd.toString();
	 }
}
