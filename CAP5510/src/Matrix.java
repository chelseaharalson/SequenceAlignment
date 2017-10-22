public class Matrix {
	Integer[][] val;
	
	public Matrix(int numRow, int numCol) {
		val = new Integer[numRow][numCol];
	}
	
	public int getRowCol(int row, int col) {
		return val[row][col];
	}
	
	public void setRowCol(int row, int col, int pval) {
		val[row][col] = pval;
	}
	
	public int getWidth() {
		if (val.length == 0) {
			return 0;
		}
		return val[0].length;
	}
	
	public int getHeight() {
		return val.length;
	}
	
	public int getCols() {
		if (val.length == 0) {
			return 0;
		}
		return val[0].length;
	}
	
	public int getRows() {
		return val.length;
	}
	
	public void printMatrix() {
		String rowCol = "";
		for (int row = 0; row < getRows(); row++) {
			for (int col = 0; col < getCols(); col++) {
				//System.out.print(getRowCol(row,col) + " ");
				rowCol += getRowCol(row,col) + " ";
				System.out.println("row: " + row + " col: " + col + " value: " + getRowCol(row,col));
			}
			//System.out.println();
			rowCol += "\n";
		}
		System.out.println(rowCol);
	}
}