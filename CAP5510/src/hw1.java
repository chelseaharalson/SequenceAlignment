import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.io.*;

public class hw1 {
	static InputParameters ip = new InputParameters();
	
	public static void main (String[] args) throws ParseException, FileNotFoundException, UnsupportedEncodingException {
		
		if (args.length > 0) {
		    ip.alignmentMethod = Integer.parseInt(args[0]);
		    ip.queryList = parseProtein(args[1]);
		    ip.databaseList = parseProtein(args[2]);
		    ip.fileAlphabet = readFile(args[3]);
		    ip.scoreList = parseMatrix(args[4]);
		    ip.numNearestNeighbors = Integer.parseInt(args[5]);
		    ip.gapPenalty = Integer.parseInt(args[6]);
		}
		boolean vi = validateInput();
		if (vi == false) {
			System.out.println("Input invalid.");
			System.exit(0);
		}
		/*for (int i = 0; i < ip.queryList.size(); i++) {
			System.out.println(ip.queryList.get(i).header);
			System.out.println(ip.queryList.get(i).sequence);
		}
		for (int i = 0; i < ip.scoreList.size(); i++) {
			for (int j = 0; j < ip.scoreList.get(i).size(); j++) {
				System.out.print(ip.scoreList.get(i).get(j) + " ");
			}
			System.out.println();
		}
		for (int i = 0; i < ip.databaseList.size(); i++) {
			System.out.println(ip.databaseList.get(i).header);
			System.out.println(ip.databaseList.get(i).sequence);
		}*/
		if (ip.alignmentMethod == 1) {
			globalAlignment();
		}
		else if (ip.alignmentMethod == 2) {
			localAlignment();
		}
		else if (ip.alignmentMethod == 3) {
			dovetailAlignment();
		}
		else {
			System.out.println("Please select a number from 1-3.");
			System.exit(0);
		}
    }
	
	public static String readFile(String fileName) {
        String line = null;
        String fileContent = "";
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                //System.out.println(line);
                fileContent += line;
            }   
            bufferedReader.close();         
        }
        catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");                  
        }
        return fileContent;
    }
	
	public static boolean validateInput() {
		boolean inputValid = false;
		if (!ip.queryList.isEmpty() && !ip.databaseList.isEmpty() && ip.fileAlphabet.length() != 0 && ip.scoreList.size() != 0) {
			inputValid = true;
		}
		return inputValid;
	}
	
	public static ArrayList<ProteinSequence> parseProtein(String fileName) {
		String line = null;
		ArrayList<ProteinSequence> proteinList = new ArrayList<ProteinSequence>();
		String header = "";
		String protein = "";
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
        			if (line.startsWith(">") && header.equals("")) {
        				header = line;
        			}
        			else if (line.startsWith(">")) {
        				ProteinSequence ps = new ProteinSequence(header, protein);
        				proteinList.add(ps);
        				header = line;
        				protein = "";
        			}
        			else {
        				protein += line;
        			}
            }
            if (!header.isEmpty() && !protein.isEmpty()) {	// last one
	    			ProteinSequence ps = new ProteinSequence(header, protein);
	    			proteinList.add(ps);
    			}
            bufferedReader.close();         
        }
        catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");                  
        }
		return proteinList;
	}
	
	public static ArrayList<ArrayList<Integer>> parseMatrix(String fileName) {
        ArrayList<ArrayList<Integer>> matrix = new ArrayList<ArrayList<Integer>>();
        try {
	    		Scanner input = new Scanner(new File(fileName));
	    		while (input.hasNextLine()) {
	    		    Scanner colScanner = new Scanner(input.nextLine());
	    		    ArrayList<Integer> col = new ArrayList<Integer>();
	    		    while (colScanner.hasNextInt()) {
	    		        col.add(colScanner.nextInt());
	    		    }
	    		    matrix.add(col);
	    		    colScanner.close();
	    		}      
	    		input.close();
        }
        catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");                
        }
        return matrix;
	}
	
	public static void globalAlignment() throws FileNotFoundException, UnsupportedEncodingException {
		ArrayList<ScoreAlignmentSequence> sacList = new ArrayList<ScoreAlignmentSequence>();
		for (int i = 0; i < ip.queryList.size(); i++) {
			for (int j = 0; j < ip.databaseList.size(); j++) {
				//System.out.println("Using querylist: "+ i + ", database list: " + j);
				Matrix m = new Matrix(ip.queryList.get(i).sequence.length()+1, ip.databaseList.get(j).sequence.length()+1);
				long startTime = System.currentTimeMillis();
				for (int col = 0; col < m.getCols(); col++) {
					for (int row = 0; row < m.getRows(); row++) {
						//System.out.println("Calculating row: " + row + " column: " + col);
						globalAlignmentHelper(m, row, col, ip.queryList.get(i).sequence, ip.databaseList.get(j).sequence);
					}
				}
				//System.out.println("Matrix generated");
				//m.printMatrix(true);
				ScoreAlignmentSequence sac = backtrackGlobal(m, ip.queryList.get(i).sequence, ip.databaseList.get(j).sequence);
				sac.score = m.getRowCol(m.getRows()-1, m.getCols()-1).val;
				long endTime = System.currentTimeMillis();
				sac.queryTime = endTime - startTime;
				sac.queryID = i;
				sac.databaseID = j;
				sac.queryLength = ip.queryList.get(i).sequence.length();
				//System.out.println("Completed matrix");
				sacList.add(sac);
			}
		}
		Collections.sort(sacList);
		printOutput(sacList, ip.numNearestNeighbors, 1);
	}
	
	public static int globalAlignmentHelper(Matrix matrix, int row, int col, String query, String database) {
		if (col == 0 && row == 0) {
			return 0;
		}
		if (col == 0) {
			matrix.setRowCol(row, col, row*ip.gapPenalty, Direction.VERTICAL);
			//System.out.println("row: " + row + " col: " + col + " val: " + row*ip.gapPenalty);
			return matrix.getRowCol(row, col).val;
		}
		if (row == 0) {
			matrix.setRowCol(row, col, col*ip.gapPenalty, Direction.HORIZONTAL);
			//System.out.println("row: " + row + " col: " + col + " val: " + col*ip.gapPenalty);
			return matrix.getRowCol(row, col).val;
		}
		int mod = computeSimilarityScore(query.charAt(row-1), database.charAt(col-1));
		int horizontal = matrix.getRowCol(row, col-1).val + ip.gapPenalty;
		int vertical = matrix.getRowCol(row-1, col).val + ip.gapPenalty;
		int diagonal = matrix.getRowCol(row-1, col-1).val + mod;
		int max = Math.max(diagonal, Math.max(horizontal, vertical));
		if (max == diagonal) {
			matrix.setRowCol(row, col, max, Direction.DIAGONAL);
		}
		else if (max == vertical) {
			matrix.setRowCol(row, col, max, Direction.VERTICAL);
		}
		else if (max == horizontal) {
			matrix.setRowCol(row, col, max, Direction.HORIZONTAL);
		}
		else {
			matrix.setRowCol(row, col, max);
		}
		return max;
	}
	
	public static int computeSimilarityScore(char char1, char char2) {
		int idx1 = ip.fileAlphabet.toLowerCase().indexOf(char1);
		int idx2 = ip.fileAlphabet.toLowerCase().indexOf(char2);
		//int score = ip.scoreList.get(idx1).get(idx2);
		//System.out.println("Comparing char1: " + char1 + " and char2: " + char2 + " with Score: " + score);
		return ip.scoreList.get(idx1).get(idx2);
	}
	
	public static ScoreAlignmentSequence backtrackGlobal(Matrix matrix, String sequence1, String sequence2) {
		ScoreAlignmentSequence sac = new ScoreAlignmentSequence();
		int row = matrix.getRows()-1;
		int col = matrix.getCols()-1;
		while (row > 0 || col > 0) {
			if (row == 0 && col == 0) {
				sac.startPosSeq1 = row;
				sac.startPosSeq2 = col;
				return sac;
			}
			if (matrix.getRowCol(row, col).direction == Direction.DIAGONAL) {
				//System.out.println("Diagonal");
				sac.sequence1 = sequence1.charAt(row-1)+sac.sequence1;
				sac.sequence2 = sequence2.charAt(col-1)+sac.sequence2;
				row--;
				col--;
			}
			else if (matrix.getRowCol(row, col).direction == Direction.VERTICAL) {
				//System.out.println("Vertical");
				sac.sequence1 = sequence1.charAt(row-1)+sac.sequence1;
				sac.sequence2 = "."+sac.sequence2;
				row--;
			}
			else if (matrix.getRowCol(row, col).direction == Direction.HORIZONTAL) {
				//System.out.println("Horizontal");
				sac.sequence1 = "."+sac.sequence1;
				sac.sequence2 = sequence2.charAt(col-1)+sac.sequence2;
				col--;
			}
		}
		return sac;
	}
	
	public static void printOutput(ArrayList<ScoreAlignmentSequence> sacList, int k, int alignMethod) throws FileNotFoundException, UnsupportedEncodingException {
		int idCount = 1;
		if (k > sacList.size()) {
			k = sacList.size();
		}
		String fileName = "";
		if (alignMethod == 1) {
			fileName = "global-";
		}
		else if (alignMethod == 2) {
			fileName = "local-";
		}
		else if (alignMethod == 3) {
			fileName = "dovetail-";
		}
		PrintWriter writer = new PrintWriter(fileName+"all.csv", "UTF-8");
		writer.println("Score,QueryLength,Time");
		PrintWriter writerTop = new PrintWriter(fileName+"top.csv", "UTF-8");
		writerTop.println("Rank,Score,QueryLength,Time,AlignmentMethod");
		for (int i = 0; i < sacList.size(); i++) {
			System.out.println("Query: " + sacList.get(i).queryID + ", Database: " + sacList.get(i).databaseID);
			System.out.println("Score = " + sacList.get(i).score);
			System.out.println("id" + idCount + " " + sacList.get(i).startPosSeq1 + " " + sacList.get(i).sequence1);
			idCount++;
			System.out.println("id" + idCount + " " + sacList.get(i).startPosSeq2 + " " + sacList.get(i).sequence2);
			idCount++;
			System.out.println("Query Length: " + sacList.get(i).queryLength);
			System.out.println("Total run time: " + sacList.get(i).queryTime + " ms");
			System.out.println();
			
			writer.println(sacList.get(i).score + "," + sacList.get(i).queryLength + "," + sacList.get(i).queryTime);
			
			if (i < k) {
				if (alignMethod == 1) {
					writerTop.println(i+1 + "," + sacList.get(i).score + "," + sacList.get(i).queryLength + "," + sacList.get(i).queryTime + ",global");
				}
				else if (alignMethod == 2) {
					writerTop.println(i+1 + "," + sacList.get(i).score + "," + sacList.get(i).queryLength + "," + sacList.get(i).queryTime + ",local");
				}
				else if (alignMethod == 3) {
					writerTop.println(i+1 + "," + sacList.get(i).score + "," + sacList.get(i).queryLength + "," + sacList.get(i).queryTime + ",dovetail");
				}
			}
		}
		writer.close();
		writerTop.close();
	}
	
	public static void localAlignment() throws FileNotFoundException, UnsupportedEncodingException {
		ArrayList<ScoreAlignmentSequence> sacList = new ArrayList<ScoreAlignmentSequence>();
		for (int i = 0; i < ip.queryList.size(); i++) {
			for (int j = 0; j < ip.databaseList.size(); j++) {
				//System.out.println("Using querylist: "+ i + " database list: " + j);
				int max = 0;
				int r = 0;
				int c = 0;
				Matrix m = new Matrix(ip.queryList.get(i).sequence.length()+1, ip.databaseList.get(j).sequence.length()+1);
				long startTime = System.currentTimeMillis();
				for (int col = 0; col < m.getCols(); col++) {
					for (int row = 0; row < m.getRows(); row++) {
						//System.out.println("Calculating row: " + row + " column: " + col);
						localAlignmentHelper(m, row, col, ip.queryList.get(i).sequence, ip.databaseList.get(j).sequence);
						if (m.getRowCol(row, col).val > max) {
							max = m.getRowCol(row, col).val;
							r = row;
							c = col;
						}
					}
				}
				//System.out.println("Matrix generated");
				//System.out.println("Max: " + max + " row: " + r + " col: " + c);
				//m.printMatrix(true);
				ScoreAlignmentSequence sac = backtrackLocal(m, ip.queryList.get(i).sequence, ip.databaseList.get(j).sequence, r, c);
				sac.score = max;
				long endTime = System.currentTimeMillis();
				sac.queryTime = endTime - startTime;
				sac.queryID = i;
				sac.databaseID = j;
				sac.queryLength = ip.queryList.get(i).sequence.length();
				//System.out.println("Completed matrix");
				sacList.add(sac);
			}
		}
		Collections.sort(sacList);
		printOutput(sacList, ip.numNearestNeighbors, 2);
	}
	
	public static int localAlignmentHelper(Matrix matrix, int row, int col, String query, String database) {
		if (col == 0) {
			matrix.setRowCol(row, 0, 0);
			return matrix.getRowCol(row, col).val;
		}
		if (row == 0) {
			matrix.setRowCol(0, col, 0);
			return matrix.getRowCol(row, col).val;
		}
		int mod = computeSimilarityScore(query.charAt(row-1), database.charAt(col-1));
		int horizontal = matrix.getRowCol(row, col-1).val + ip.gapPenalty;
		int vertical = matrix.getRowCol(row-1, col).val + ip.gapPenalty;
		int diagonal = matrix.getRowCol(row-1, col-1).val + mod;
		int max = Math.max(0, Math.max(diagonal, Math.max(horizontal, vertical)));
		if (max == diagonal) {
			matrix.setRowCol(row, col, max, Direction.DIAGONAL);
		}
		else if (max == vertical) {
			matrix.setRowCol(row, col, max, Direction.VERTICAL);
		}
		else if (max == horizontal) {
			matrix.setRowCol(row, col, max, Direction.HORIZONTAL);
		}
		else {
			matrix.setRowCol(row, col, max);
		}
		//System.out.println("Setting row: " + row + " col: " + col + " value: " + max);
		//System.out.println("Horizontal: " + horizontal + " Vertical: " + vertical + " Diagonal: " + diagonal);
		return max;
	}
	
	public static ScoreAlignmentSequence backtrackLocal(Matrix matrix, String sequence1, String sequence2, int startRow, int startCol) {
		ScoreAlignmentSequence sac = new ScoreAlignmentSequence();
		int row = startRow;
		int col = startCol;
		//System.out.println("numRows: " + matrix.getRows() + " numCol: " + matrix.getCols());
		//System.out.println("startRow: " + startRow + " startCol: " + startCol + " value: " + matrix.getRowCol(row, col).val);
		while (row > 0 || col > 0) {
			System.out.println("row: " + row + " col: " + col);
			if (matrix.getRowCol(row, col).val == 0 || matrix.getRowCol(row, col).direction == Direction.NONE) {
				sac.startPosSeq1 = row;
				sac.startPosSeq2 = col;
				return sac;
			}
			if (matrix.getRowCol(row, col).direction == Direction.DIAGONAL) {
				//System.out.println("Diagonal");
				sac.sequence1 = sequence1.charAt(row-1)+sac.sequence1;
				sac.sequence2 = sequence2.charAt(col-1)+sac.sequence2;
				row--;
				col--;
			}
			else if (matrix.getRowCol(row, col).direction == Direction.VERTICAL) {
				//System.out.println("Vertical");
				sac.sequence1 = sequence1.charAt(row-1)+sac.sequence1;
				sac.sequence2 = "."+sac.sequence2;
				row--;
			}
			else if (matrix.getRowCol(row, col).direction == Direction.HORIZONTAL) {
				//System.out.println("Horizontal");
				sac.sequence1 = "."+sac.sequence1;
				sac.sequence2 = sequence2.charAt(col-1)+sac.sequence2;
				col--;
			}
		}
		return sac;
	}
	
	public static void dovetailAlignment() throws FileNotFoundException, UnsupportedEncodingException {
		ArrayList<ScoreAlignmentSequence> sacList = new ArrayList<ScoreAlignmentSequence>();
		for (int i = 0; i < ip.queryList.size(); i++) {
			for (int j = 0; j < ip.databaseList.size(); j++) {
				//System.out.println("Using querylist: "+ i + " database list: " + j);
				int max = 0;
				int r = 0;
				int c = 0;
				Matrix m = new Matrix(ip.queryList.get(i).sequence.length()+1, ip.databaseList.get(j).sequence.length()+1);
				long startTime = System.currentTimeMillis();
				for (int col = 0; col < m.getCols(); col++) {
					for (int row = 0; row < m.getRows(); row++) {
						//System.out.println("Calculating row: " + row + " column: " + col);
						dovetailAlignmentHelper(m, row, col, ip.queryList.get(i).sequence, ip.databaseList.get(j).sequence);
						if ( (row == m.getRows()-1 || col == m.getCols()-1) && m.getRowCol(row, col).val > max ) {
							max = m.getRowCol(row, col).val;
							r = row;
							c = col;
						}
					}
				}
				//System.out.println("Matrix generated");
				//System.out.println("Max: " + max + " row: " + r + " col: " + c);
				//m.printMatrix(true);
				ScoreAlignmentSequence sac = backtrackDovetail(m, ip.queryList.get(i).sequence, ip.databaseList.get(j).sequence, r, c);
				sac.score = max;
				long endTime = System.currentTimeMillis();
				sac.queryTime = endTime - startTime;
				sac.queryID = i;
				sac.databaseID = j;
				sac.queryLength = ip.queryList.get(i).sequence.length();
				//System.out.println("Completed matrix");
				sacList.add(sac);
			}
		}
		Collections.sort(sacList);
		printOutput(sacList, ip.numNearestNeighbors, 3);
	}
	
	public static int dovetailAlignmentHelper(Matrix matrix, int row, int col, String query, String database) {
		if (col == 0) {
			matrix.setRowCol(row, 0, 0);
			return matrix.getRowCol(row, col).val;
		}
		if (row == 0) {
			matrix.setRowCol(0, col, 0);
			return matrix.getRowCol(row, col).val;
		}
		int mod = computeSimilarityScore(query.charAt(row-1), database.charAt(col-1));
		int horizontal = matrix.getRowCol(row, col-1).val + ip.gapPenalty;
		int vertical = matrix.getRowCol(row-1, col).val + ip.gapPenalty;
		int diagonal = matrix.getRowCol(row-1, col-1).val + mod;
		int max = Math.max(diagonal, Math.max(horizontal, vertical));
		if (max == diagonal) {
			matrix.setRowCol(row, col, max, Direction.DIAGONAL);
		}
		else if (max == vertical) {
			matrix.setRowCol(row, col, max, Direction.VERTICAL);
		}
		else if (max == horizontal) {
			matrix.setRowCol(row, col, max, Direction.HORIZONTAL);
		}
		else {
			matrix.setRowCol(row, col, max);
		}
		return max;
	}
	
	public static ScoreAlignmentSequence backtrackDovetail(Matrix matrix, String sequence1, String sequence2, int startRow, int startCol) {
		ScoreAlignmentSequence sac = new ScoreAlignmentSequence();
		int row = startRow;
		int col = startCol;
		//System.out.println("startRow: " + startRow + " startCol: " + startCol + " value: " + matrix.getRowCol(row, col));
		while (row > 0 || col > 0) {
			if (row == 0 || col == 0) {
				sac.startPosSeq1 = row;
				sac.startPosSeq2 = col;
				return sac;
			}
			if (matrix.getRowCol(row, col).direction == Direction.DIAGONAL) {
				//System.out.println("Diagonal");
				sac.sequence1 = sequence1.charAt(row-1)+sac.sequence1;
				sac.sequence2 = sequence2.charAt(col-1)+sac.sequence2;
				row--;
				col--;
			}
			else if (matrix.getRowCol(row, col).direction == Direction.VERTICAL) {
				//System.out.println("Vertical");
				sac.sequence1 = sequence1.charAt(row-1)+sac.sequence1;
				sac.sequence2 = "."+sac.sequence2;
				row--;
			}
			else if (matrix.getRowCol(row, col).direction == Direction.HORIZONTAL) {
				//System.out.println("Horizontal");
				sac.sequence1 = "."+sac.sequence1;
				sac.sequence2 = sequence2.charAt(col-1)+sac.sequence2;
				col--;
			}
		}
		return sac;
	}
}
