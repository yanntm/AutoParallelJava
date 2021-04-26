import java.util.ArrayList;
import java.util.List;

public class TestSimpleType {

	public static void main(String[] args) {
		List<Integer> ints = new ArrayList<>();
	
		for(int i : ints) {
			System.out.println(i);
		}
		
		
		int res = 0;
		for(int i : ints) {
			res+=i;
		}
	}
	

	
	
}
