import java.util.ArrayList;

public class InputParameters {
	int alignmentMethod;
	ArrayList<ProteinSequence> queryList;
	ArrayList<ProteinSequence> databaseList;
	String fileDatabaseSequences;
	String fileAlphabet;
	ArrayList<ArrayList<Integer>> scoreList;
	int numNearestNeighbors;
	int gapPenalty;
	
	InputParameters() {
		alignmentMethod = 0;
		queryList = new ArrayList<ProteinSequence>();
		databaseList = new ArrayList<ProteinSequence>();
		fileDatabaseSequences = "";
		fileAlphabet = "";
		scoreList = new ArrayList<ArrayList<Integer>>();
		numNearestNeighbors = 0;
		gapPenalty = 0;
	}
}
