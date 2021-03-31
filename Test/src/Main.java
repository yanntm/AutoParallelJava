import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


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
		
		Personne[] pers23 = new Personne[10];
		
		for(Personne p : pers23) {
			System.out.println(p);
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
		
		final StringBuilder buf = new StringBuilder();
		for(Personne p : pers) {
			buf.append(p.age+"");
			System.out.println(buf);
		}
		System.out.println(buf.toString());
		
		
		final int _somme = 1;
		for(Personne p : pers) {
			p.age = _somme;
		}
		
		
		//Pour les Map
		
		int testMap1=0;
		testMap1++;
		for (Personne personne : pers) {
			testMap1+=personne.age;
		}
		
		//Equivalent
		int testMap2 = pers.stream().mapToInt(p-> p.age).sum();
		
		
		int testMap3=0;
		//Test
		for (Personne personne : pers) {
			if(personne.age>=18)
			testMap1+=personne.age;
		}
		
		int testMap4 = pers.stream().filter(p->p.age>=18).mapToInt(p-> p.age).sum();
		
		List<Integer> testlistInteger = new ArrayList<>();
		for (Personne p : pers) {
			testlistInteger.add(p.age);
		}
		
		testlistInteger.addAll(pers.stream().map(p -> p.age).collect(Collectors.toList()));
		
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
	
	public static void mariage(Personne p, Personne a) {
		a.setNom(p.getNom());
	}
	
	
}
