import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReduceTest {

    public static void main(String[] args) {
        List<Integer> temp = new ArrayList<>();

        int somme = 0;
        int test1 = 1;
        Integer integer123 = 0;
        
        somme += temp.stream().parallel().mapToInt((Integer integer) -> integer).sum();

        somme += temp.stream().parallel().mapToInt((Integer integer) -> Integer.parseInt("10")).sum();

        somme += temp.stream().parallel().mapToDouble((Integer integer) -> Math.PI).sum();

        somme += temp.stream().parallel().filter((Integer integer) -> integer > 18)
				.mapToDouble((Integer integer) -> Math.PI).sum();
        somme = 0;
        test1 = 1;
        somme += temp.stream().parallel().mapToInt((Integer integer) -> integer).sum();

        somme += temp.stream().parallel().mapToInt((Integer integer) -> Integer.parseInt("10")).sum();
        
        somme += temp.stream().parallel().mapToDouble((Integer integer) -> Math.PI).sum();
        
        somme += temp.stream().parallel().filter((Integer integer) -> integer > 18)
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


        int test = temp.stream().mapToInt(i->i).sum();
    }
}