import java.util.ArrayList;
import java.util.Collections;

public class Matrix {
	ArrayList<ArrayList<Integer>> value;
	
	public Matrix(int numRow, int numCol) {
		value = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < numRow; i++) {
			value.add(new ArrayList<Integer>(Collections.nCopies(numCol, 0)));
			//System.out.println("Adding row: " + i + " col: " + numCol);
			//System.out.println("Value size: " + value.get(i).size());
		}
		//System.out.println("getWidth: " + getWidth());
		//System.out.println("getHeight: " + getHeight());
	}
	
	public int getRowCol(int row, int col) {
		return value.get(row).get(col);
	}
	
	public void setRowCol(int row, int col, int val) {
		value.get(row).set(col, val);
	}
	
	public int getWidth() {
		return value.size();
	}
	
	public int getHeight() {
		if (value.isEmpty()) {
			return 0;
		}
		return value.get(0).size();
	}
	
	public void printMatrix() {
		for (int i = 0; i < getWidth(); i++) {
			for (int j = 0; j < getHeight(); j++) {
				System.out.print(getRowCol(i,j));
			}
			System.out.println();
		}
	}
}
