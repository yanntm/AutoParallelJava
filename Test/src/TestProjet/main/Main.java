package TestProjet.main;

import java.util.ArrayList;
import java.util.List;

import TestProjet.structure.Point;

public class Main {

	public static void main(String[] args) {
		List<Point> points = new ArrayList<>();
		
		for (Point point : points) {
			point.move();
		}
		
		for (Point point : points) {
			System.out.println("TEMPE");
		}
	}

}
