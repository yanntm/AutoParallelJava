import java.util.ArrayList;
import java.util.List;

public class Main {

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
		
		for(Personne p:pers) {
			if(p.age==0) {
				p.anniversaire();
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
		
		for(Personne p:pers) {
			for(int i =0; i<10; i++) {
				p.anniversaire();
			}
			p.anniversaire();
		}
		
		for(Personne p:pers) {
			for(int i =0; i<10; i++) {
				p.anniversaire();
			}
		}
		
		for(Personne p:pers) {
			try {
				p.anniversaire2();
			}catch (Exception e) {
				// TODO: handle exception
			}
		}

		//Voulue
		//Pour le Test1, il ne doit proposer aucun changement doit 
		
		//Pour le TEST2 résultat voulue
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
		private int age=0;
		
		void anniversaire() {
			age++;
		}
		
		void anniversaire2() throws Exception{
			throw new Exception();
		}
	}
}
