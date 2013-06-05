package com.example.booksync;

public class Scores implements Comparable<Scores>{
	public int page;
	public double score;
	
	@Override
	public int compareTo(Scores o1) {
		return (this.score>o1.score ? -1 : (this.score==o1.score ? 0 : 1));
	}
}