package es;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestCd {
    private static  Map<String,Double> sortByValueDesc(){
        Map<String, Double> tm=new HashMap<>();

        tm.put("sda", 58.8);   tm.put("wq",32.4);
        tm.put("asda", 60.0);   tm.put("sd", 55.5);
        //这里将map.entrySet()转换成list
        /**
         * 这里不懂
         */
        List<Map.Entry<String,Double>> list = new ArrayList<>(tm.entrySet());
        //然后通过比较器来实现排序
        Collections.sort(list,new Comparator<Map.Entry<String,Double>>() {
            //升序排序
            @Override
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                return -(o2.getValue().compareTo(o1.getValue()));
            }
        });
        Map<String,Double> tm1=new LinkedHashMap<>();
        for(Map.Entry<String,Double> mapping:list){
            tm1.put(mapping.getKey(),mapping.getValue());
            //System.out.println(tm1.keySet());
            System.out.println(mapping.getKey()+":"+mapping.getValue());
        }
        return tm1;
    }
    public static void main(String[] args) throws ParseException {
//       Map<String,Double> sd= sortByValueDesc();
////      // List<Map.Entry<String,Double>> list=new ArrayList(sd.keySet());
////        //System.out.println(list.get(0).getKey());
////        List<Map.Entry<String,Double>> list = new ArrayList<>(sd.entrySet());
////        List<String> list1=new ArrayList<>();
////        for(Map.Entry<String,Double> mapping:list){
////            list1.add(mapping.getKey());
////        }
////        System.out.println(list1.get(0));
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        String time1="2019-11-25";
        //将字符串形式的时间转化为Date类型的时间
        Date a=sdf.parse(time1);
        System.out.println(a);
    }
}
