import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Determines the travel time for a contract, how long a witch is busy travelling to the contract's location
 */
public enum TravelTime {

    SHORT(1000,2500),
    MEDIUM(2500, 5000),
    LONG(5000, 10000);

    private static final List<TravelTime> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public final int min;
    public final int max;

    private TravelTime(int min, int max){
        this.min = min;
        this.max = max;
    }

    public static TravelTime randomZone()  {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}