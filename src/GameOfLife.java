import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * 10. Automato "Conway's Life Game" simuliacija.
 *
 * Programa parasyta skurdziai objektine prasme,
 * Remtasi pastaba, kad verta tureti kuo mazesni abstrakciju kieki
 * taip mazinant atminties suvartojima ir vengiant GC darbo itakos.
 *
 * Del sios priezasties viskas suprogramuota proceduriskai naudojant statinius kintamuosius.
 * Stiliaus prasme tai nera gerai, taciau minimalizuotas objektu turimu atmintyje kiekis
 *
 * Isivaizduojame kad pasaulis isdeliotas kaip matematine plokstuma.
 * Pradzia 1;1
 *
 * Skaidymo algoritmas:
 *  Game of Life vyksta nurodyto dydzio lentoje, paleidus programa nurodomas jos dydis, bei gyvavimo trukme.
 *  kiekviena iteracija sukuriamas nurodytu giju kiekis, kiekviena gija atlieka darba su 1 lentos stulpeliu.
 *
 *  Kai apdorojami visi stulpeliai prasideda kitas gyvenimo ciklas - darbas vel dinamiskai pasidalinamas tarp giju.
 */
public class GameOfLife {

    // Pasaulio dydis
    public static int worldSize;
    public static int yearsToLive;
    public static int threadCount;
    public static boolean fastMode = false;

    // Pasaulis - masyvas
    private static boolean[][] world;
    private static boolean[][] next_world;

    private static List<Worker> workerPool = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        if (!manageInput(args)) {
            showHelp();
            return;
        }

        generateInitialState();

        long startMillis = System.currentTimeMillis();

        runLife();

        long endMillis = System.currentTimeMillis();

        System.out.println("Pradiniai duomenys");
        System.out.println("Pasaulio dydis: " + args[0]);
        System.out.println("Gyvavimo trukme: " + args[1]);
        System.out.println("Giju skacius: " + args[2]);

        System.out.println("Uztruko: " + (endMillis - startMillis));
    }

    private static void runLife() throws InterruptedException {
        int yearsPassed = 0;

        while(yearsPassed < yearsToLive) {
            WorkCounter counter = new WorkCounter(worldSize - 2);

            createWorkers();

            for (Worker worker : workerPool) {
                worker.setEnvironment(world, next_world, counter);
                worker.start();
            }

            for (Worker thread : workerPool) {
                thread.join();
            }

            if (!fastMode) {
                System.out.println("Visos gijos baige darba. Metai: " + yearsPassed);
            }

            swapWorlds();
            yearsPassed++;
        }
    }

    private static void createWorkers() {
        workerPool.clear();

        for (int i = 0; i < threadCount; i++) {
            workerPool.add(new Worker());
        }
    }

    private static void generateInitialState() {
        Random random = new Random(System.currentTimeMillis());

        // Specialiai iteruojama ne per visa pasauli,
        // siekiant isvengti isimciu laikome, kad pasaulis turi papildoma krastine kurios busena nesikeicia
        // taciau tikriname jos kaimynu busenas.
        // Uzpildomas pradinis pasaulis
        for (int i = 1; i < worldSize - 1; i++) {
            for (int j = 1; j < worldSize - 1; j++) {
                world[i][j] = random.nextBoolean();
            }
        }
    }

    private static boolean manageInput(String[] args) {
        if (args.length != 4) {
            return false;
        }


        try {
            worldSize = Integer.parseInt(args[0]) + 2;
            yearsToLive = Integer.parseInt(args[1]);
            threadCount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return false;
        }

        world = new boolean[worldSize][worldSize]; // Laikome, kad pasaulis kvadratinis
        next_world = new boolean[worldSize][worldSize]; // Laikome, kad pasaulis kvadratinis

        if (args[3].toUpperCase().equals("VERBOSE")) {
            fastMode = false;
        } else if (args[3].toUpperCase().equals("FAST")) {
            fastMode = true;
        } else {
            return false;
        }

        return true;
    }

    private static void showHelp() {
        System.out.println("Netinkamas programos kvietimas. game 'pasaulio dydis' 'trukme' 'giju kiekis' 'rezimas'");
        System.out.println("Rezimai: FAST, VERBOSE ");
        System.out.println("pvz: game 10000 100 10 FAST");
    }

    // Nelabai grazus sprendimas tauciau taupome objektus.
    // remiantis normaliomis praktikomis - butu funkcija grazinanti reiksmes.
    private static void processLine(int lineNumber, boolean[] newDimension) {
        for (int i = 1; i < worldSize - 1; i++) {
            boolean cell = world[lineNumber][i];

            int neighbours = 0;

            if (world[lineNumber - 1][i + 1]) neighbours++;
            if (world[lineNumber][i + 1]) neighbours++;
            if (world[lineNumber + 1][i + 1]) neighbours++;

            if (world[lineNumber - 1][i]) neighbours++;
            if (world[lineNumber + 1][i]) neighbours++;

            if (world[lineNumber - 1][i - 1]) neighbours++;
            if (world[lineNumber][i - 1]) neighbours++;
            if (world[lineNumber + 1][i - 1]) neighbours++;

            newDimension[i] = willCellLive(cell, neighbours);
        }
    }

    /**
     * Taisykles gyvibingumui nustatyti pasiskoliname is wikipedia:
     *
     * Any live cell with fewer than two live neighbours dies, as if caused by under-population.
     * Any live cell with two or three live neighbours lives on to the next generation.
     * Any live cell with more than three live neighbours dies, as if by over-population.
     * Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
     *
     * @param cell
     * @param neighbours
     * @return
     */
    private static boolean willCellLive(boolean cell, int neighbours) {
        boolean alive = false;

        if (cell) {
            // Rules applied if cell is Alive
            if (neighbours < 2) {
                alive = false;
            } else if (neighbours == 2 || neighbours == 3) {
                alive = true;
            } else if (neighbours > 3) {
                alive = false;
            }

        } else {

            // Rules applied if cell is not Alive
            if (neighbours == 3) {
                alive = true;
            } else {
                alive = false;
            }
        }

        return alive;
    }

    /**
     * Atliekame savotiska triuka - kad nereiktu isskrineti atminties, sukeiciame rodykles.
     * Naujai uzpildyta pasauli naudosime kaip esama pasauli, o senaji pasauli naudosime vietoje sekancio.
     *
     */
    private static void swapWorlds() {
        Object tmp = world;
        world = next_world;
        next_world = (boolean[][]) tmp;
    }

    private static void printWorld(boolean[][] world) {
        System.out.println("----------");
        for(int i = 0; i < worldSize; i++) {
            for(int j = 0; j < worldSize; j++) {
                System.out.print(world[i][j] ? "+" : ".");
            }
            System.out.println();
        }
    }

    private static class Worker extends Thread {

        // Reference to world which he operates in.
        // This world is immutable from worker viewpoint
        private boolean[][] world;
        private boolean[][] nextWorld;

        private WorkCounter counter;

        public void setEnvironment(boolean[][] world, boolean[][] nextWorld, WorkCounter counter) {
            this.world = world;
            this.nextWorld = nextWorld;
            this.counter = counter;
        }


        @Override
        public void run(){
            Integer work = counter.getWork();

            while (work != null) {
                if (!fastMode) {
                    System.out.println("Gija: " + this.getId() + " baige apdoroti stulpeli: " + work);
                }

                // Perduodame objekta kuri uzpildys. Gauname ganetinai efektyvu darba
                // visos gijos naudosis vienu ReadOnly resursu world.
                // Ir kiekviena gija pildo savo stulpeli, todel nera kritiniu sekciju.
                processLine(work, nextWorld[work]);
                work = counter.getWork();
            }

            if (!fastMode) {
                System.out.println("Gija: " + this.getId() + " baige darba!");
            }
        }
    }

    private static class WorkCounter {

        private int workUnits;
        private int value = 1;

        public WorkCounter(int workUnits) {
            this.workUnits = workUnits;
        }

        synchronized public Integer getWork() {
            if (value > workUnits) {
                return null;
            }

            Integer result = new Integer(value);
            value++;
            return result;
        }
    }
}

