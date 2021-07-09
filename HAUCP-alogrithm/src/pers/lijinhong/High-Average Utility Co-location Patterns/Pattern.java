 package pers.lijinhong.colocation;

public class Pattern implements Comparable<Pattern>{
	
	public Pattern() {
	}
	
	public Pattern(String pattern, double utility) {
		this();
		this.pattern = new String(pattern);
		this.utility = utility;
	}
	
	public String pattern;
	public double utility;
	
	@Override
	public int compareTo(Pattern o) {
		if (o.utility > this.utility) {
			return 1;
		}else {
			return -1;
		}
	}
}
