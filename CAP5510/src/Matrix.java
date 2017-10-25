public class Matrix {
	MatrixElement[][] matrix;
	
	public Matrix(int numRow, int numCol) {
		matrix = new MatrixElement[numRow][numCol];
		for (int row = 0; row < getRows(); row++) {
			for (int col = 0; col < getCols(); col++) {
				matrix[row][col] = new MatrixElement();
			}
		}
	}
	
	public MatrixElement getRowCol(int row, int col) {
		return matrix[row][col];
	}
	
	public void setRowCol(int row, int col, int pval) {
		matrix[row][col].val = pval;
	}
	
	public void setRowCol(int row, int col, Direction d) {
		matrix[row][col].direction = d;
	}
	
	public void setRowCol(int row, int col, int pval, Direction d) {
		matrix[row][col].val = pval;
		matrix[row][col].direction = d;
	}
	
	public int getCols() {
		if (matrix.length == 0) {
			return 0;
		}
		return matrix[0].length;
	}
	
	public int getRows() {
		return matrix.length;
	}
	
	public void printMatrix(boolean printNumbers) {
		for (int row = 0; row < getRows(); row++) {
			for (int col = 0; col < getCols(); col++) {
				if (printNumbers == false) {
					if (getRowCol(row, col).direction == Direction.DIAGONAL) {
						System.out.print("d ");
					}
					else if (getRowCol(row, col).direction == Direction.VERTICAL) {
						System.out.print("v ");
					}
					else if (getRowCol(row, col).direction == Direction.HORIZONTAL) {
						System.out.print("h ");
					}
					else if (getRowCol(row, col).direction == Direction.NONE) {
						System.out.print("x ");
					}
				}
				else {
					System.out.print(getRowCol(row,col).val + " ");
				}
			}
			System.out.println();
		}
	}
}