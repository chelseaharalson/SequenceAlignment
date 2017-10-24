
public class MatrixElement {
	int val;
	Direction direction;
	
	public MatrixElement() {
		val = 0;
		direction = Direction.NONE;
	}
	
	public MatrixElement(int pVal, Direction d) {
		val = pVal;
		direction = d;
	}
}
