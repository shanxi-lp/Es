import java.util.Objects;
import java.util.Random;

public class people {
    private int age = new Random().nextInt(100);


    @Override
    public int hashCode() {
        return Objects.hash(age);
    }


    @Override
    public String toString() {
        return "people{" +
                "age=" + age +
                '}';
    }
}
