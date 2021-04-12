import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestSynchronized {
	
	
	private Lock lock = new ReentrantLock();
	private boolean test = false;
	public static void main(String[] args) {
	}
	
	void test(){
		int tempInt = 0;
		test=true;
	}
}
