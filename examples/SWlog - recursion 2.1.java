package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Demo {
	
    public static void main(String[] args) {
		int i = parseInput(args[0]);
		
		f(i);
		
		outputResults();
    }
	
	public static int parseInput(String arg) {
		return Integer.parseInt(arg);
	}
	
	public static void f(int i) {
		if (i <= 0) {
			base();
		} else {
			step1();
			g(i-1);
			step2();
		}
	}
	
	public static void g(int i) {
		if (i <= 0) {
			base();
		} else {
			step1();
			f(i-1);
			step2();
		}
	}
	
	public static void step1() {
	}
	
	public static void step2() {
	}
	
	public static void base() {
	}
	
	public static void outputResults() {
	}
}
