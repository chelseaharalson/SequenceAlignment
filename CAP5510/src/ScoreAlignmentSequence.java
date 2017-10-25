
public class ScoreAlignmentSequence implements Comparable<ScoreAlignmentSequence>{
	int score;
	String sequence1;
	String sequence2;
	long queryTime;
	int startPosSeq1;
	int startPosSeq2;
	int queryID;
	int databaseID;
	int queryLength;

	public ScoreAlignmentSequence() {
		score = 0;
		sequence1 = "";
		sequence2 = "";
		queryTime = 0;
		startPosSeq1 = 0;
		startPosSeq2 = 0;
		queryID = 0;
		databaseID = 0;
		queryLength = 0;
	}

	public int compareTo(ScoreAlignmentSequence sac) {
		if (score > sac.score) {
			return -1;
		}
		else if (score < sac.score) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
