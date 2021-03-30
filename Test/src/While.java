import java.util.ArrayList;
import java.util.List;

public class While {

	public static void main(String[] args) {
		List<Integer> l = new ArrayList<>();
		
		while(l.iterator().hasNext()) {
			Integer i = l.iterator().next();
			System.out.println(i);
		}
		
		for(int j=0; j<l.size();j++) {
			Integer i = l.get(j);
			System.out.println(i);
		}
	}

}
