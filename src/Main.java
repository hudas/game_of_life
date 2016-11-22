import java.util.Random;

/**
 *
 * 10. Automato "Conway's Life Game" simuliacija.
 *
 * Programa rašyta skurdžiai objektine prasme,
 * remiantis jūsų pastaba, su tikslu turėti kuo mažesnį nereikalingų abstrakcijų kiekį
 * taip mažinant atminties suvartojimą ir vengiant GC darbo įtakos
 */
public class Main {

    // Pasaulio dydis
    public static final int WORLD_SIZE = 102;

    public static final int YEARS_TO_LIVE = 1000;

    // Pasaulis - baitų masyvas
    private static boolean[][] world = new boolean[WORLD_SIZE][WORLD_SIZE]; // Laikome, kad pasaulis kvadratinis
    private static boolean[][] next_world = new boolean[WORLD_SIZE][WORLD_SIZE]; // Bufferis sekančio pasaulio konstravimui.

    public static void main(String[] args) {
        Random random = new Random(System.currentTimeMillis());

        // Specialiai iteruojama ne per visą pasaulį,
        // siekiant išvengti išmčių laikome, kad pasaulis turi papildomą kraštinę kurios būsena nesikeičia tačiau tikriname jos kaimynų būsenas.
        // Užpildomas pradinis pasaulis
        for (int i = 1; i < WORLD_SIZE - 2; i ++) {
            for (int j = 1; j < WORLD_SIZE - 2; j++) {
                world[i][j] = random.nextBoolean();
            }
        }

        int yearsPassed = 0;
        while(yearsPassed < YEARS_TO_LIVE) {



            yearsPassed++;
        }

        System.out.println("Hello World!");
    }
}
