import java.util.ArrayList;
import java.util.List;

public class ReduceTest {

    public static void main(String[] args) {
        List<Integer> temp = new ArrayList<>();

        int somme = 0;
        int test1 = 1;
        for (Integer integer : temp) {
            somme+=integer;
        }

        for (Integer integer : temp) {
            somme+=Integer.parseInt("10");
        }

        for (Integer integer : temp) {
            somme+=Math.PI;

        }

        for (Integer integer : temp) {
            if(integer>18) {
                somme+=Math.PI;
            }
        }
        somme = 0;
        test1 = 1;
        for (Integer integer : temp) {
            somme+=integer;
        }

        for (Integer integer : temp) {
            somme+=Integer.parseInt("10");
        }
        
        for (Integer integer : temp) {
            somme+=Math.PI;
            
        }
        
        for (Integer integer : temp) {
            if(integer>18) {
                somme+=Math.PI;
            }
            
        }
//        
        List<Integer> res = new ArrayList<>();
        for(Integer i : temp) {
            if(i>18) {
                res.add(i);
            }

        }

        System.out.println(somme+"");
//        res = temp.stream().filter(i->i>18).map(i->i).collect(Collectors.toList());

//        System.out.println(somme+"");


        int test = temp.stream().mapToInt(i->i).sum();
    }
}