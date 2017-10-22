import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.io.*;

public class hw1 {
	static InputParameters ip = new InputParameters();
	
	public static void main (String[] args) throws ParseException {
		
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
		for (int i = 0; i < ip.queryList.size(); i++) {
			System.out.println(ip.queryList.get(i).header);
			System.out.println(ip.queryList.get(i).sequence);
		}
		for (int i = 0; i < ip.scoreList.size(); i++) {
			for (int j = 0; j < ip.scoreList.get(i).size(); j++) {
				System.out.print(ip.scoreList.get(i).get(j) + " ");
			}
			System.out.println();
		}
		
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
	
	public static void globalAlignment() {
		ArrayList<ScoreAlignmentSequence> sacList = new ArrayList<ScoreAlignmentSequence>();
		for (int i = 0; i < ip.queryList.size(); i++) {
			for (int j = 0; j < ip.databaseList.size(); j++) {
				System.out.println("Using querylist: "+ i + " database list: " + j);
				long startTime = System.currentTimeMillis();
				Matrix m = new Matrix(ip.queryList.get(i).sequence.length()+1, ip.databaseList.get(j).sequence.length()+1);
				for (int col = 0; col < m.getWidth(); col++) {
					for (int row = 0; row < m.getHeight(); row++) {
						//System.out.println("Calculating row: " + row + " column: " + col);
						globalAlignmentHelper(m, row, col, ip.queryList.get(i).sequence, ip.databaseList.get(j).sequence);
					}
				}
				System.out.println("Matrix generated");
				ScoreAlignmentSequence sac = backtrackGlobal(m, ip.queryList.get(i).sequence, ip.databaseList.get(j).sequence);
				long endTime = System.currentTimeMillis();
				sac.queryTime = endTime - startTime;
				System.out.println("Completed matrix");
				sacList.add(sac);
			}
		}
		Collections.sort(sacList);
		printOutput(sacList, ip.numNearestNeighbors);
	}
	
	/*D(i,0) = Σ d(A(k),-), 0 <= k <= i
	D(0,j) = Σ d(-,B(k)), 0 <= k <= j
	D(i,j) = Max  {
	D(i-1,j) + d(A(i),-), 
	D(i,j-1) + d(-,B(j)),
	D(i-1,j-1) + d(A(i),B(j))}*/
	public static int globalAlignmentHelper(Matrix matrix, int row, int col, String query, String database) {
		if (col == 0) {
			matrix.setRowCol(row, col, row*ip.gapPenalty);
			//System.out.println("row: " + row + " col: " + col + " val: " + row*ip.gapPenalty);
			return matrix.getRowCol(row, col);
		}
		if (row == 0) {
			matrix.setRowCol(row, col, col*ip.gapPenalty);
			//System.out.println("row: " + row + " col: " + col + " val: " + col*ip.gapPenalty);
			return matrix.getRowCol(row, col);
		}
		int mod = computeSimilarityScore(query.charAt(row-1), database.charAt(col-1));
		int horizontal = matrix.getRowCol(row, col-1) + ip.gapPenalty;
		int vertical = matrix.getRowCol(row-1, col) + ip.gapPenalty;
		int diagonal = matrix.getRowCol(row-1, col-1) + mod;
		int max = Math.max(diagonal, Math.max(horizontal, vertical));
		matrix.setRowCol(row, col, max);
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
		int row = matrix.getHeight()-1;
		int col = matrix.getWidth()-1;
		while (row > 0 || col > 0) {
			sac.score += matrix.getRowCol(row, col);
			// Look at diagonal
			if (row > 0 && col > 0 && 
					matrix.getRowCol(row, col) == (matrix.getRowCol(row-1, col-1)+computeSimilarityScore(sequence1.charAt(row-1), sequence2.charAt(col-1))) ) {
				sac.sequence1 = sequence1.charAt(row-1)+sac.sequence1;
				sac.sequence2 = sequence2.charAt(col-1)+sac.sequence2;
				row--;
				col--;
			}
			// Look at vertical
			else if (row > 0 && matrix.getRowCol(row, col) == matrix.getRowCol(row-1, col)+ip.gapPenalty) {
				sac.sequence1 = sequence1.charAt(row-1)+sac.sequence1;
				sac.sequence2 = "-"+sac.sequence2;
				row--;
			}
			// Look at horizontal
			else {
				sac.sequence1 = "-"+sac.sequence1;
				sac.sequence2 = sequence2.charAt(col-1)+sac.sequence2;
				col--;
			}
		}
		return sac;
	}
	
	public static void printOutput(ArrayList<ScoreAlignmentSequence> sacList, int k) {
		int idCount = 1;
		if (k > sacList.size()) {
			k = sacList.size();
		}
		for (int i = 0; i < k; i++) {
			System.out.println("Score = " + sacList.get(i).score);
			System.out.println("id" + idCount+ " STARTPOS " + sacList.get(i).sequence1);
			idCount++;
			System.out.println("id" + idCount + " STARTPOS " + sacList.get(i).sequence2);
			idCount++;
			System.out.println("Total run time: " + sacList.get(i).queryTime + " ms");
		}
	}
	
	public static void localAlignment() {
		ArrayList<ScoreAlignmentSequence> sacList = new ArrayList<ScoreAlignmentSequence>();
		int max = 0;
		int r = 0;
		int c = 0;
		for (int i = 0; i < ip.queryList.size(); i++) {
			for (int j = 0; j < ip.databaseList.size(); j++) {
				System.out.println("Using querylist: "+ i + " database list: " + j);
				long startTime = System.currentTimeMillis();
				Matrix m = new Matrix(ip.queryList.get(i).sequence.length()+1, ip.databaseList.get(j).sequence.length()+1);
				for (int col = 0; col < m.getWidth(); col++) {
					for (int row = 0; row < m.getHeight(); row++) {
						//System.out.println("Calculating row: " + row + " column: " + col);
						localAlignmentHelper(m, row, col, ip.queryList.get(i).sequence, ip.databaseList.get(j).sequence);
						if (m.getRowCol(row, col) > max) {
							max = m.getRowCol(row, col);
							r = row;
							c = col;
						}
					}
				}
				System.out.println("Matrix generated");
				System.out.println("Max: " + max + " row: " + r + " col: " + c);
				m.printMatrix();
				ScoreAlignmentSequence sac = backtrackLocal(m, ip.queryList.get(i).sequence, ip.databaseList.get(j).sequence, r, c);
				sac.score = max;
				long endTime = System.currentTimeMillis();
				sac.queryTime = endTime - startTime;
				System.out.println("Completed matrix");
				sacList.add(sac);
			}
		}
		Collections.sort(sacList);
		printOutput(sacList, ip.numNearestNeighbors);
	}
	
	public static int localAlignmentHelper(Matrix matrix, int row, int col, String query, String database) {
		if (col == 0) {
			matrix.setRowCol(row, 0, 0);
			return matrix.getRowCol(row, col);
		}
		if (row == 0) {
			matrix.setRowCol(0, col, 0);
			return matrix.getRowCol(row, col);
		}
		int mod = computeSimilarityScore(query.charAt(row-1), database.charAt(col-1));
		int horizontal = matrix.getRowCol(row, col-1) + ip.gapPenalty;
		int vertical = matrix.getRowCol(row-1, col) + ip.gapPenalty;
		int diagonal = matrix.getRowCol(row-1, col-1) + mod;
		int max = Math.max(0, Math.max(diagonal, Math.max(horizontal, vertical)));
		matrix.setRowCol(row, col, max);
		//System.out.println("Setting row: " + row + " col: " + col + " value: " + max);
		//System.out.println("Horizontal: " + horizontal + " Vertical: " + vertical + " Diagonal: " + diagonal);
		return max;
	}
	
	public static ScoreAlignmentSequence backtrackLocal(Matrix matrix, String sequence1, String sequence2, int startRow, int startCol) {
		ScoreAlignmentSequence sac = new ScoreAlignmentSequence();
		int row = startRow;
		int col = startCol;
		//System.out.println("startRow: " + startRow + " startCol: " + startCol + " value: " + matrix.getRowCol(row, col));
		while (row > 0 || col > 0) {
			if (matrix.getRowCol(row, col) == 0) {
				return sac;
			}
			int diagonal = matrix.getRowCol(row-1, col-1);
			int vertical = matrix.getRowCol(row-1, col);
			int horizontal = matrix.getRowCol(row, col-1);
			if (diagonal == 0) {
				//System.out.println("Diagonal: " + diagonal);
				sac.sequence1 = sequence1.charAt(row-1)+sac.sequence1;
				sac.sequence2 = sequence2.charAt(col-1)+sac.sequence2;
				row--;
				col--;
			}
			else if (vertical == 0) {
				//System.out.println("Vertical: " + vertical);
				sac.sequence1 = sequence1.charAt(row-1)+sac.sequence1;
				sac.sequence2 = "-"+sac.sequence2;
				row--;
			}
			else if (horizontal == 0) {
				//System.out.println("Horizontal: " + horizontal);
				sac.sequence1 = "-"+sac.sequence1;
				sac.sequence2 = sequence2.charAt(col-1)+sac.sequence2;
				col--;
			}
			// Look at diagonal
			else if (diagonal >= horizontal && diagonal >= vertical) {
				//System.out.println("Diagonal: " + diagonal);
				sac.sequence1 = sequence1.charAt(row-1)+sac.sequence1;
				sac.sequence2 = sequence2.charAt(col-1)+sac.sequence2;
				row--;
				col--;
			}
			// Look at vertical
			else if (vertical >= diagonal && vertical >= horizontal) {
				//System.out.println("Vertical: " + vertical);
				sac.sequence1 = sequence1.charAt(row-1)+sac.sequence1;
				sac.sequence2 = "-"+sac.sequence2;
				row--;
			}
			// Look at horizontal
			else {
				//System.out.println("Horizontal: " + horizontal);
				sac.sequence1 = "-"+sac.sequence1;
				sac.sequence2 = sequence2.charAt(col-1)+sac.sequence2;
				col--;
			}
		}
		return sac;
	}
	
	public static void dovetailAlignment() {
		
	}
}
