import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * 10. Automato "Conway's Life Game" simuliacija.
 *
 * Programa rašyta skurdžiai objektine prasme,
 * remiantis jūsų pastaba, su tikslu turėti kuo mažesnį nereikalingų abstrakcijų kiekį
 * taip mažinant atminties suvartojimą ir vengiant GC darbo įtakos
 *
 * Įsivaizduojame kad pasaulis išdėliotas kaip matematinė plokštuma.
 * Pradžia 1;1
 */
public class Main {

    // Pasaulio dydis
    public static final int WORLD_SIZE = 1002;

    public static final int YEARS_TO_LIVE = 100;

    // Pasaulis - baitų masyvas
    private static boolean[][] world = new boolean[WORLD_SIZE][WORLD_SIZE]; // Laikome, kad pasaulis kvadratinis
    private static boolean[][] next_world = new boolean[WORLD_SIZE][WORLD_SIZE]; // Bufferis sekančio pasaulio konstravimui.

    public static void main(String[] args) {
        Random random = new Random(System.currentTimeMillis());

        // Specialiai iteruojama ne per visą pasaulį,
        // siekiant išvengti išmčių laikome, kad pasaulis turi papildomą kraštinę kurios būsena nesikeičia tačiau tikriname jos kaimynų būsenas.
        // Užpildomas pradinis pasaulis
        for (int i = 1; i < WORLD_SIZE - 1; i++) {
            for (int j = 1; j < WORLD_SIZE - 1; j++) {
                world[i][j] = random.nextBoolean();
            }
        }

        List<Thread> workerPool = new ArrayList<>();

        long startMillis = System.currentTimeMillis();
        int yearsPassed = 0;
        while(yearsPassed < YEARS_TO_LIVE) {

            for (int i = 1; i < WORLD_SIZE - 1; i ++) {
                // Perduodame objektą kurį užpildys. Gauname ganėtinai efektyvų darbą - visos gijos naudosis 1. ReadOnly resursu world.
                // Ir kiekviena gija pildo savo stulpelį, todėl nėra kritinių sekcijų.
                processLine(i, next_world[i]);
            }

            swapWorlds();
//            printWorld(world);
            yearsPassed++;
        }
        long endMillis = System.currentTimeMillis();
        System.out.println("Užtruko: " + (endMillis - startMillis));

        System.out.println("Hello World!");
    }

    // Nelabai gražus taučiau taupome objektus.
    // remiantis normaliomis praktikomis - būtų funkcija gražinanti.
    private static void processLine(int lineNumber, boolean[] newDimension) {
        for (int i = 1; i < WORLD_SIZE - 1; i++) {
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
     * Taisyklas gyvibingumui pasiskoliname iš wikipedia:
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
     * Atliekame savotišką triuką - kad nereiktų išsikrinėti atminties, sukeičiame rodykles.
     * Naujai užpildytą pasaulį naudosime kaip esamą pasaulį, o senąjį pasaulį naudosime vietoje sekančio.
     *
     */
    private static void swapWorlds() {
        Object tmp = world;
        world = next_world;
        next_world = (boolean[][]) tmp;
    }

    private static void printWorld(boolean[][] world) {
        System.out.println("----------");
        for(int i = 0; i < WORLD_SIZE; i++) {
            for(int j = 0; j < WORLD_SIZE; j++) {
                System.out.print(world[i][j] ? "+" : ".");
            }
            System.out.println();
        }
    }
}
