import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a witcher contract; this is what witchers complete to earn more gold
 * Has a zone, difficulty and travel time
 * Zone determines in which region the contract takes place
 *      for the 3. part it is essential that only one witcher can do contract per zone
 * Difficulty determines how much gold the witcher gets for completing the contract
 * Travel time determines how long the witcher has to travel to do the contract
 */
public class Contract {

    private static final String INFO_TEMPLATE = "Contract #%d: %s contract with %s travel time in %s";

    private final Zone zone;
    private final Difficulty difficulty;
    private final TravelTime travelTime;
    private final int contractId;

    // TODO (Part 1) isOwned flag
    private boolean isOwned;

    private int actualTravelTime = 0;

    private static final AtomicInteger GENERATOR = new AtomicInteger(1);

    private Contract(final Zone zone, final Difficulty difficulty, final TravelTime travelTime){
        this.zone = zone;
        this.difficulty = difficulty;
        this.travelTime = travelTime;
        this.contractId = GENERATOR.getAndIncrement(); // TODO (Part 1) generate a unique ID starting from 1
    }

    public static Contract generateContract(){
        return new Contract(Zone.randomZone(), Difficulty.randomZone(), TravelTime.randomZone());
    }

    /**
     * Used in the Simulation's startWitcher method
     * This marks that the contract has been picked up by a witcher
     * so no one else can pick it up
     */
    public synchronized void take(){
        // TODO (Part 1) set isOwned to true
        isOwned = true;
    }

    /**
     * Used in the Simulation's startWitcher method
     * @return If the contract is picked up by a witcher or not
     */
    public synchronized boolean isOwned(){
        return isOwned;
    }

    public Zone getZone(){
        return this.zone;
    }

    public Difficulty getDifficulty(){
        return this.difficulty;
    }

    public int getReward(){
        return difficulty.reward;
    }

    public int getTravelTime(){
        if(actualTravelTime == 0){
            actualTravelTime = getRandom(travelTime.min, travelTime.max);
        }
        return actualTravelTime;
    }

    public String getInfo(){
        return String.format(INFO_TEMPLATE, contractId, difficulty.name(), travelTime.name(), zone.name());
    }

    private int getRandom(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max + 1); // TODO (Part 1) random generation
    }
}