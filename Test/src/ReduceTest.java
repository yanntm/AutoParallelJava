import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import Main.Personne;

public class ReduceTest {

    public static void main(String[] args) {
        List<Integer> temp = new ArrayList<>();

        int somme = 0;
        int test1 = 1;
        Integer integer123 = 0;
        
        somme += temp.stream().parallel().map(integer -> integer).sum();

        somme += temp.stream().parallel().mapToInt((Integer integer) -> Integer.parseInt("10")).sum();

        somme += temp.stream().parallel().mapToDouble((Integer integer) -> Math.PI).sum();

        somme += temp.stream().parallel().filter((Integer integer) -> integer > 18)
				.mapToDouble((Integer integer) -> Math.PI).sum();
        somme = 0;
        test1 = 1;
        somme += temp.stream().parallel().mapToInt((Integer integer) -> integer).sum();

        somme += temp.stream().parallel().mapToInt((Integer integer) -> Integer.parseInt("10")).sum();
        
        somme += temp.stream().parallel().mapToDouble((Integer integer) -> Math.PI).sum();
        
        somme += temp.parallelStream().parallel().filter((Integer integer) -> integer > 18)
				.mapToDouble((Integer integer) -> Math.PI).sum();
//        
        List<Integer> res = new ArrayList<>();
        res.addAll(temp.stream().parallel().filter((Integer i) -> i > 18).map((Integer i) -> i)
				.collect(Collectors.toList()));

        System.out.println(somme+"");
//        res = temp.stream().filter(i->i>18).map(i->i).collect(Collectors.toList());

//        System.out.println(somme+"");
        
        List<Main.Personne> pers = new ArrayList<>();
        
		somme = 0;
		for(Main.Personne p : pers) {
			somme++;
		}

		
		Integer[] testInteger = new Integer[5];
		Arrays.stream(testInteger).parallel().map(i->i).sequ.forEach(null);
		
        int test = temp.stream().mapToInt(i->i).sum();
        
        Personne p = new Personne();
        temp.stream().forEach(i-> i.machin(p));
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
		
		synchronized void anniversaire3() throws Exception{
			throw new Exception();
		}
		
		String getNom() {return nom;}
		
		void setNom(String s) {nom = s;}
	}
	
	public static void mariage(Personne p, Personne a) {
		a.setNom(p.getNom());
}