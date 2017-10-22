import java.text.ParseException;
import java.util.ArrayList;
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
		ArrayList<Matrix> matrix = new ArrayList<Matrix>();
		for (int i = 0; i < ip.queryList.size(); i++) {
			for (int j = 0; j < ip.databaseList.size(); j++) {
				Matrix m = new Matrix(ip.queryList.get(i).sequence.length()+1, ip.databaseList.get(j).sequence.length()+1);
				for (int col = 0; col < m.getWidth(); col++) {
					for (int row = 0; row < m.getHeight(); row++) {
						globalAlignmentHelper(m, row, col, ip.queryList.get(i).sequence, ip.databaseList.get(j).sequence);
					}
				}
				for (int q = 0; q < ip.queryList.get(i).sequence.length(); q++) {
					System.out.print(ip.queryList.get(i).sequence.charAt(q));
				}
				System.out.println();
				for (int d = 0; d < ip.databaseList.get(i).sequence.length(); d++) {
					System.out.print(ip.databaseList.get(i).sequence.charAt(d));
				}
				System.out.println();
				m.printMatrix();
				matrix.add(m);	// add to matrix
			}
		}
	}
	
	/*D(i,0) = Σ d(A(k),-), 0 <= k <= i
	D(0,j) = Σ d(-,B(k)), 0 <= k <= j
	D(i,j) = Min  {
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
		int horizontal = globalAlignmentHelper(matrix, row-1, col, query, database) + ip.gapPenalty;
		int vertical = globalAlignmentHelper(matrix, row, col-1, query, database) + ip.gapPenalty;
		int diagonal = globalAlignmentHelper(matrix, row-1, col-1, query, database) + mod;
		int max = Math.max(diagonal, Math.max(horizontal, vertical));
		/*System.out.println("Row: " + row + " Col: " + col);
		System.out.println("Horizontal: " + horizontal);
		System.out.println("Vertical: " + vertical);
		System.out.println("Diagonal: " + diagonal);
		System.out.println("Max: " + max);
		System.out.println();*/
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
	
	public static void localAlignment() {
		
	}
	
	public static void dovetailAlignment() {
		
	}
}
