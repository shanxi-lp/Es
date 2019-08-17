import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class APP {

    @Test
    public void test(){
        Map<String,people> map = new HashMap<>();

        for(int i =0 ;i<20;i++){
            map.put(""+i,new people());
        }

        Iterator iterator =  map.entrySet().iterator();
        while (iterator.hasNext())
            System.out.println(iterator.next());

    }


}
