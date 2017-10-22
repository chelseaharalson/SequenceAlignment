import java.util.ArrayList;
import java.util.Collections;

public class Matrix {
	ArrayList<ArrayList<Integer>> value;
	
	public Matrix(int numRow, int numCol) {
		value = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < numCol; i++) {
			value.add(new ArrayList<Integer>(Collections.nCopies(numRow, 0)));
			//System.out.println("Adding row: " + i + " col: " + numCol);
			//System.out.println("Value size: " + value.get(i).size());
		}
		//System.out.println("getWidth: " + getWidth());
		//System.out.println("getHeight: " + getHeight());
	}
	
	public int getRowCol(int row, int col) {
		return value.get(col).get(row);
	}
	
	public void setRowCol(int row, int col, int val) {
		value.get(col).set(row, val);
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
		for (int col = 0; col < getWidth(); col++) {
			for (int row = 0; row < getHeight(); row++) {
				System.out.print(getRowCol(row,col) + " ");
			}
			System.out.println();
		}
	}
}
