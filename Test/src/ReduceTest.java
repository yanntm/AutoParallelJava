import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReduceTest {

	public static void main(String[] args) {
		List<Integer> temp = new ArrayList<>();
		
		int somme = 0;
		int test1 = 1;
		somme += temp.stream().mapToInt((Integer integer) -> integer).sum();
		
		somme += temp.stream().count();

		somme += temp.stream().mapToInt((Integer integer) -> Integer.parseInt("10")).sum();
		
		somme += temp.stream().mapToDouble((Integer integer) -> Math.PI).sum();
		
		somme += temp.stream().filter((Integer integer) -> integer > 18).mapToDouble((Integer integer) -> Math.PI)
				.sum();
		
		List<Integer> res = new ArrayList<>();
		res.addAll(temp.stream().filter((Integer i) -> i > 18).map((Integer i) -> i).collect(Collectors.toList()));
		
		int test = temp.stream().mapToInt(i->i).sum();
		
		for(Integer i : temp) {
			somme++ ;
		}
		
		System.out.println("test="+test+" somme="+somme);
	}

}
