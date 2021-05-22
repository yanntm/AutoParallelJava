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

		try {
			for (Point point : points) {
				point.here(points.get(0));
			}
		}catch(Exception e) {

		}

	}

}
