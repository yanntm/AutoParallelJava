import java.util.ArrayList;
import java.util.List;

public class ReduceTest {

	public static void main(String[] args) {
		List<Integer> temp = new ArrayList<>();
		
		int somme = 0;
		int test1 = 1;
		for (Integer integer : temp) {
			somme+=integer;
			test1*=integer;
			
		}
		
		System.out.println(somme+"");
		
		
		int test = temp.stream().mapToInt(i->i).sum();
	}

}
