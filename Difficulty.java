import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Determines the difficulty of a contract - reward is based on this
 */
public enum Difficulty {

    EASY(100),
    MEDIUM(150),
    HARD(200),
    VERY_HARD(250);

    private static final List<Difficulty> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public final int reward;

    private Difficulty(int reward){
        this.reward = reward;
    }

    public static Difficulty randomZone()  {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}