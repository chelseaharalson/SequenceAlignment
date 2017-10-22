
public class ScoreAlignmentSequence implements Comparable<ScoreAlignmentSequence>{
	int score;
	String sequence1;
	String sequence2;
	long queryTime;

	public ScoreAlignmentSequence() {
		score = 0;
		sequence1 = "";
		sequence2 = "";
		queryTime = 0;
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
