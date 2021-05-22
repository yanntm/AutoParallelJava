package TestProjet.structure;

import java.util.Objects;

import TestProjet.utils.Math;

public class Point {
	private int x;
	private int y;

	public Point() {
		x=10;
		y=30;
	}

	public void move() {
		if(x>1) {
			x=Math.incr(x);
		}else {
			if(y<5) {
				y=Math.square(y);
			}else {
				if(x==0){
					x=Math.dec(x);
				}else {
					x=x+y;
				}
			}
		}
	}
	
	public void here(Point p) throws Exception{
		if (!p.equals(this))throw new Exception("Pas ici");
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		return x == other.x && y == other.y;
	}
	
	
}
