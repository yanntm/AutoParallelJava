package generation.graphe.methode.invocation.fr.lip6.move.gal.util;

public interface IBoolMatrixCol {
	boolean get (int col, int row);
	void set (int col, int row, boolean b);
	int[] getColumn (int col);
}
