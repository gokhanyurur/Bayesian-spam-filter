package ai_labwork;

public class WordNode {
	int spamCount;
	int hamCount;
	double probability;
	
	public WordNode( int spamCount, int hamCount ) {
		this.spamCount = spamCount;
		this.hamCount = hamCount;
	}
	
	public String toString() {
		return String.valueOf(probability);
	}
}
