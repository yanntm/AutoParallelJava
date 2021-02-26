import java.util.ArrayList;
import java.util.List;


public class Main {

	public static void dodo() throws InterruptedException, IllegalAccessException {
		Thread.sleep(10);
		return;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<Integer> l =new ArrayList<>();
		List<Personne> pers = new ArrayList<Personne>();
		for(int i=0; i<10; i++) { 
			l.add(i);
			pers.add(new Personne());
		}
		
		
		//Test 1
		for(Integer i:l) {
			
		}
		
		//TEST 2 
		for(Personne p:pers) {
			p.anniversaire();
		}
		
		//Test 3
		for(Personne p:pers) {
			if(p.age==0) {
				p.anniversaire();
			}
			System.out.println("p");
		}
		
		//TEST4
		for(Personne p:pers) {
			if(p.age==0) {
				p.anniversaire();
			}
		}
		
		//Test 5
		
		for(Personne p:pers) {
			if(p.age==0) {
				if(p.sexe) {
					p.anniversaire();
					p.anniversaire();
				}
			}
		}
		
		for(Personne p:pers) {
			if(p.age==0) {
				if(p.sexe) {
					p.anniversaire();
					p.anniversaire();
				}
			}
			System.out.println("Yo");
		}
		
		for(Personne p:pers) {
			if(p.age==0) {
				if(p.sexe) {
					p.anniversaire();
					p.anniversaire();
				}
				System.out.println("ho");
			}
		}
		
		//TEST6
		for(Personne p:pers) {
			p.anniversaire();
			p.anniversaire();
		}
		
		for(Personne p1:pers) {
			for(int i =0; i<10; i++) {
				p1.anniversaire();
			}
			p1.anniversaire();
		}
		
		for(Personne p:pers) {
			Personne[] pers2 = new Personne[10];
			for(Personne p1: pers2) {
				p1.anniversaire();
			}
		}
		
		for(Personne p1:pers) {
			try {
				dodo();
				p1.anniversaire2();
				Personne pp1 = new Personne(120);
				throw new Exception();
			}catch (IllegalAccessException e) {
				// TODO: handle exception
			} catch (Exception e1) {
				
			}
		}
		
		try {
			for(Personne p1:pers) {
				try {
					dodo();
					p1.anniversaire2();
					Personne pp1 = new Personne(120);
					throw new Exception();
				}catch (IllegalAccessException e) {
					// TODO: handle exception
				} 
			}
		}catch(Exception e) {
		}
		
		
		int a = 0;
		for(Personne p:pers) {
			a+=p.age;
		}
		
		
		for(Personne p:pers) {
			int b = 0, c=1;
			b+=p.age;
		}
		
		// Break
		
		for(Personne p : pers) {
			if(p.age < 2 ) {
				break;
			}
			p.anniversaire();
		}
		
		for(Personne p1 :pers) {
			for(Personne p : pers) {
				if(p.age < 2 ) {
					break;
				}
				p.anniversaire();
			}
		}
		
		// Continue
		
		for(Personne p : pers) {
			if(p.age < 2 ) {
				continue;
			}
			p.anniversaire();
		}
		
		for(Personne p1 :pers) {
			for(Personne p : pers) {
				if(p.age < 2 ) {
					continue;
				}
				p.anniversaire();
			}
		}
		
		// Test Affectation 
		
		//non
		int somme = 0;
		for(Personne p : pers) {
			somme = p.age;
		}
		somme = 0;
		for(Personne p : pers) {
			somme += p.age;
		}
		somme = 0;
		for(Personne p : pers) {
			somme++;
		}
		for(Personne p : pers) {
			++somme;
		}
		//oui
		somme = 1;
		for(Personne p : pers) {
			p.age = somme;
		}
		
		for(Personne p : pers) {
			int age = p.age;
			while(age > 0) {
				System.out.println("a="+a);
				age--;
			}
		}
		
	}
	
		
		
	//Classe pour TEST
	public static class Personne{
		public int age=0;
		public boolean sexe = true;
		
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
	}
}
