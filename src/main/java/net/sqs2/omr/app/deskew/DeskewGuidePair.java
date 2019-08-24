package net.sqs2.omr.app.deskew;

public class DeskewGuidePair {
	
	DeskewGuide left;
	DeskewGuide right;
	
	DeskewGuidePair(DeskewGuide left, DeskewGuide right){
		this.left = left;
		this.right = right;
	}
	
	public DeskewGuide getLeft() {
		return left;
	}
	
	public DeskewGuide getRight() {
		return right;
	}
	
	@Override
	public String toString(){
		return getClass().getSimpleName()+"[left="+left+", right="+right+"]";
	}
}
