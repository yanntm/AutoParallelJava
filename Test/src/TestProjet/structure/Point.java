package TestProjet.structure;

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
}
