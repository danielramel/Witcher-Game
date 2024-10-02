import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Part 1:
 * 5 witchers are doing contracts to get more money.
 * A witcher can only do one contract at a time and no one else can do that contract
 * A contract should be generated every WAIT_TIME_BETWEEN_CONTRACTS_MSEC and there can only be CONTRACT_MAX_NUMBER
 * at the same time
 * Let the simulation run for 30 seconds and shut down the simulation after that
 *
 * Part 2:
 * The simulation should run for as long as a witcher gets GOAL_WITCHER_GOLD_AMOUNT then shut it down
 * The simulation should run for a maximum of 1 minute
 *
 * Part 3:
 * Only one witcher can be in the same zone
 * If another witcher tries to take another contract in that zone, he should wait ZONE_LOCKOUT_WAIT_TIME_MSEC
 * and try again.
 * If the original witcher hasn't finished his contract by that time, look for a new contract
 */
public class Simulation {

    private final static String[] WITCHER_NAMES = "Geralt,Letho,Eskel,Lambert,Vesemir".split(",");
    private final static int CONTRACT_MAX_NUMBER = 5;
    private final static int GOAL_WITCHER_GOLD_AMOUNT = 1000;
    private final static int WAIT_TIME_BETWEEN_CONTRACTS_MSEC = 500;
    private final static int ZONE_LOCKOUT_WAIT_TIME_MSEC = 300;

    private final static List<Witcher> witchers = new ArrayList<>();

    // TODO (Part 1) create simulationOver flag
    private static final AtomicBoolean simulationOver = new AtomicBoolean(false);

    // TODO (Part 1) create collection for contracts with CONTRACT_MAX_NUMBER as limit
    private static final List<Contract> contracts = new CopyOnWriteArrayList<>();

    private static final ConcurrentHashMap<Zone, ReentrantLock> zoneLocks = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        // Creating the witchers
        for(String name : WITCHER_NAMES){
            witchers.add(new Witcher(name));
        }

        for (Zone zone : Zone.values()) {
            zoneLocks.put(zone, new ReentrantLock());
        }

        // Creating the initial contracts
        for(int i = 0; i < CONTRACT_MAX_NUMBER; ++i){
            Contract contract = Contract.generateContract();

            // TODO (Part 1) add to contracts collection
            contracts.add(contract);

            System.out.println("A new contract is up - " + contract.getInfo());
        }

        // Starting the work for all witchers
        for(Witcher witcher : witchers){
            // TODO (Part 1) invoke startWitcher on a new thread
            new Thread(() -> {
                try {
                    startWitcher(witcher);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // TODO (Part 1) start generateContract on a new thread
        new Thread(() -> {
            try {
                generateContract();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // TODO (Part 1) start checkSimulationOver on a new thread to run every 500 milliseconds
        new Thread(() -> {
            try {
                checkSimulationOver();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // TODO (Part 2) shut simulation down after 1 minute
        new Thread(() -> {
            try {
                Thread.sleep(60000); // Wait for 1 minute
                simulationOver.set(true);
                System.exit(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Generates a contract every WAIT_TIME_BETWEEN_CONTRACTS_MSEC
     * If there are already CONTRACT_MAX_NUMBER contracts waiting to be taken, do nothing with it
     * If the limit allows, add the new contract to the contracts collection
     *
     * Should stop generating contracts if simulation is over (simulationOver flag)
     */
    private static void generateContract() throws InterruptedException{
        while (!simulationOver.get()) { // TODO (Part 1) simulationOver instead of true
            if (contracts.size() < CONTRACT_MAX_NUMBER) {
                Contract contract = Contract.generateContract();
                contracts.add(contract);

                // TODO (Part 1) only print this if the contract could be added to the collection ( CONTRACT_MAX_NUMBER not reached)
                // TODO (Part 1) System.out.println("A new contract is up - " + contract.getInfo());
                System.out.println("A new contract is up - " + contract.getInfo());
            }

            // TODO (Part 1) wait WAIT_TIME_BETWEEN_CONTRACTS_MSEC
            Thread.sleep(WAIT_TIME_BETWEEN_CONTRACTS_MSEC);
        }
    }

    /**
     * Starts a witcher, who will look for contracts to take
     * If a witcher takes a contract, do it immediately (witcher.takeContract)
     * After a contract is done, wait WAIT_TIME_BETWEEN_CONTRACTS_MSEC before the new one
     *
     * If the simulation is over, the witcher should not try to pick up new contracts
     * but should finish his existing one, if there is one
     *
     * Part 3:
     * Two witcher can never be in the same zone.
     * If the witcher sees a new contract, but another witcher is doing another contract in that zone, wait
     * ZONE_LOCKOUT_WAIT_TIME_MSEC and try to pick it up again
     * If by that time the other witcher hasn't finished his contract in the zone, try to take on a new one
     * @param witcher The witcher
     */
    private static void startWitcher(final Witcher witcher) throws InterruptedException {
        new Thread(() ->  {
            while(!simulationOver.get()) { // TODO (Part 1) use simulationOver instead of true
                // TODO (Part 1) get a contract if there is one
                Contract contractToTake = null;
                for (Contract contract : contracts) {
                    if (!contract.isOwned()) {
                        ReentrantLock zoneLock = zoneLocks.get(contract.getZone());
                        long start = System.currentTimeMillis();
                        boolean free = zoneLock.tryLock();

                        while (!free) {
                            long elapsed = System.currentTimeMillis() - start;

                            try {
                                Thread.sleep(ZONE_LOCKOUT_WAIT_TIME_MSEC);
                                break;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            free = zoneLock.tryLock();
                        }

                        if (free){
                            try {
                                contractToTake = contract;
                                contract.take();
                                contracts.remove(contract);
                                break;
                            } finally {
                                zoneLock.unlock();
                            }
                        }
                    }
                }

                // TODO (Part 3) if someone is in the zone, wait ZONE_LOCKOUT_WAIT_TIME_MSEC
                // TODO (Part 3) if ZONE_LOCKOUT_WAIT_TIME_MSEC runs out before the zone is freed up, move on to next contract
                // TODO (Part 3) if zone is freed up during ZONE_LOCKOUT_WAIT_TIME_MSEC, take the contract
                // TODO (Part 3) System.out.println(witcher.getName() + " got tired of waiting for " + contract.getZone().name());
                // TODO (Part 3) print the above line if ZONE_LOCKOUT_WAIT_TIME_MSEC runs out before zone is free

                // TODO (Part 1) take the contract if it is not owned by someone else (witcher.takeContract method)
                if (contractToTake != null) {
                    try {
                        ReentrantLock zoneLock = zoneLocks.get(contractToTake.getZone());
                        zoneLock.lock();
                        try {
                            witcher.takeContract(contractToTake);
                            contracts.remove(contractToTake);
                        } finally {
                            zoneLock.unlock();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // TODO (Part 1) wait WAIT_TIME_BETWEEN_CONTRACTS_MSEC before trying to get a new contract
                try {
                    Thread.sleep(WAIT_TIME_BETWEEN_CONTRACTS_MSEC);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Part 1:
     * The simulation should end after 30 seconds, using simulationOver flag
     *
     * Part 2:
     * If a witcher has GOAL_WITCHER_GOLD_AMOUNT or more, shut down the simulation using simulationOver flag
     *
     * This check should happen every 500 milliseconds
     */
    private static void checkSimulationOver() throws InterruptedException {
        // TODO (Part 1) after 30 seconds shut down the simulation
        new Thread(() -> {
            long start = System.currentTimeMillis();
            while (!simulationOver.get()) {
                long elapsed = System.currentTimeMillis() - start;

                // TODO (Part 2) if the simulation is not over check if a witcher has GOAL_WITCHER_GOLD_AMOUNT or more gold
                for (Witcher witcher : witchers) {
                    if (witcher.getGold() >= GOAL_WITCHER_GOLD_AMOUNT) {
                        // TODO (Part 2) if someone does, simulation should be over and print this out:
                        // TODO (Part 2) System.out.println("***** " + witcher.getName() + " reached the set amount first *****");
                        simulationOver.set(true);
                        System.out.println("***** " + witcher.getName() + " reached the set amount first *****");
                        break;
                    }
                }

                if (elapsed >= 60000) {
                    simulationOver.set(true);
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.exit(0);
        }).start();
    }
}