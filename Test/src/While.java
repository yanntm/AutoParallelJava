import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class While {
	static int  size =0;
	public static void main(String[] args) {
		List<Integer> l = new ArrayList<>();
		size = 7;
		++size;
		size++;
//		Iterator<Integer> it = l.iterator();
//		while(it.hasNext()) {
//			Integer i = it.next();
//			System.out.println(i);
//		}
//		size = l.size();
		for(int j=0, je=size; j<je; j++)
		for(int j=0; j<l.size();j++) {
			Integer integer = l.get(j);
			Integer i = integer;
			System.out.println(i);
		}
	}

}
