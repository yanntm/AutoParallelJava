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
		}
		
		//TEST4
		Personne pp;
		for(Personne p:pers) {
			if(p.age==0) {
				p.anniversaire();
				pp = new Personne();
				
			}else {
				
			}
		}
		
		//Test 5
		
		for(Personne p:pers) {
			if(p.age==0) {
				p.anniversaire();
				p.anniversaire();
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

		//Voulue
		//Pour le Test1, il ne doit proposer aucun changement doit 
		
		//Pour le TEST2 r�sultat voulue
		pers.stream()
		.forEach(p->p.anniversaire());
		
		//Pour le TEST3
		pers.stream()
		.filter(p->p.age==0)
		.forEach(p->p.anniversaire());
		
		//Pour le TEST4 ne rien faire
		
		//Pour le TEST5 
		pers.stream()
		.filter(p->p.age==0)
		.forEach(p->{
			p.anniversaire();
			p.anniversaire();
		});
		
		//Pour le test 6
		pers.stream()
		.forEach(p->{
			p.anniversaire();
			p.anniversaire();
		});
		
	}
		
		
	
	public static class Personne{
		public int age=0;
		
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
