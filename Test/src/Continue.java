import java.util.ArrayList;
import java.util.List;



public class Continue {
	public static void main(String[] args) {
		
		List<Integer> l =new ArrayList<>();
		List<Personne> pers = new ArrayList<Personne>();

		for(Personne p : pers) {
			if(p.age < 2 ) {
				int i;
				i=2;
				p.anniversaire();
				i = methode(i);
				if (i<2) {
					i=methode(i);
				}
			}
		}
/*
		for(Personne p1 :pers) {
			for(Personne p : pers) {
				if(p.age < 2 ) {
					p.anniversaire();
				}
				else {
					continue;
				}
			}
		}
		for(Personne p1 :pers) {
			for(Personne p : pers) {
				if(p.age < 2 ) {
					p.anniversaire();
				}
				else {
					p.anniversaire();
				}
			}
		}*/
	}
	
	
	public static int methode(int i) {
		return i+1;
	}
	
	//Classe pour TEST
	public static class Personne{
		public int age=0;
		public boolean sexe = true;
		private String nom ="Temp";

		Personne(int age) throws IllegalArgumentException {
			if (age> 100) {
				throw new IllegalArgumentException();
			}
			this.age = age;
		}

		public Personne() {
			// TODO Auto-generated constructor stub
		}

		void anniversaire() {
			age++;
		}

		void anniversaire2() throws Exception{
			throw new Exception();
		}

		String getNom() {return nom;}

		void setNom(String s) {nom = s;}
	}

}	

