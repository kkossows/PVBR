import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by rkossowski on 2017-04-24.
 */
public class Main {
    static Scanner scanner = new Scanner(System.in);

    /**
     * Główna pętla programu.
     * - uruchamia nową symulację;
     * - daje możliwość przerwania symulacji przed zakończeniem działania;
     * - daje możliwość wczytania czasów przybycia i obsługi z pliku w celach kontrolnych;
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("--------------------->  <-----------------------");
        System.out.println("---->    SYMULATOR SYSTEMÓW KOLEJKOWYCH   <-----");
        System.out.println("--------------------->  <-----------------------");


        symulationPVBR_loop();


        /* wybór symulatora */
        /*
        System.out.println("--->        Wybierz typ symulatora:              <---");
        System.out.println("-->             1 - M/M/1                       <--");
        System.out.println("-->             2 - PVBR                        <--");

        switch (scanner.nextInt()) {
            case 1:
                symulationMM1();
                break;
            case 2:
                symulationPVBR();
                break;
            default:
                System.out.println("Podano błędny typ symulatora!");
                break;
        }

        System.out.println("--------------------->  <-----------------------");
        System.out.println("---->           KONIEC PROGRAMU           <-----");
        System.out.println("--------------------->  <-----------------------");
        */
    }

    /**
     * Metoda odpowiedzalna za symulację modelu MM1
     */
    public static void symulationMM1() {
        /* utworzenie symulacji jednostkowej */
        MM1 simulation = new MM1();

        /* uruchomienie symulacji */
        if( !simulation.loadError )
            simulation.start();
    }

    /**
     * Metoda odpowiedzalna za symulację usługi PVBR
     * - utworzenie symulatora usługi PVRR;
     * - inicjalizacja statystyk programu;
     * - użytkownik podaje liczbę jednostkowych symulacji do przeprowadzenia;
     * - dopóki liczba wykonanych jednostkowych symulacji jest różna od liczby żądanej wykonuj:
     *      - uruchom algorytm inicjalizujący zmienne parametry symulacji jednostkowej;
     *      - uruchom program główny symulacji jednostkowej;
     *      - wypisz raport końcowy (finalny stan systemu);
     * - wypisz raport końcowy (finalny stan systemu);
     */
    public static void symulationPVBR() {
        /* utworzenie jednostkowej symulacji usługi PVRR */
        PVBR simulator = new PVBR();

        /* inicjalizacja statystyk programu */
        simulator.initializeSimulatorParameters();

        /* użytkownik podaje liczbę jednostkowych symulacji do przeprowadzenia */
        //System.out.println("---->    Podaj liczbę pojedyńczych symulacji do wykonania:");
        //simulator.setNumberOfRequestedSingleSimulations(scanner.nextInt());

        /* uruchom algorytm inicjalizujący stałe parametry symulacji jednostkowej */
        simulator.initializeStaticParameters();

        /* dopóki liczba wykonanych jednostkowych symulacji jest różna od liczby żądanej */
        while ( simulator.getNumberOfCompletedSingleSimulations() != simulator.getNumberOfRequestedSingleSimulations()) {
            /* uruchom algorytm inicjalizujący zmienne parametry symulacji jednostkowej */
            simulator.initializeVariableParameters();

            /* uruchom program główny symulacji jednostkowej */
            simulator.start();

            /* zwiększenie liczby ukończonych symulacji jednostkowych */
            simulator.increaseNumberOfCompletedSingleSimulations();

            /* wypisz raport pośredni (obecny stan systemu) */
            //simulator.simulatorReport(SimulatorTemplate.ReportType.CURRENT);
        }

        /* wypisz raport końcowy (finalny stan systemu) */
        simulator.simulatorReport(SimulatorTemplate.ReportType.FINAL);
    }

    public static void symulationPVBR_loop() {


        for(int k=70; k<101; k++) {
            System.out.println(k);

            /* liczba zmiennych parametrów */
            int numberOfPvbrSimulations = 11;


            for (int b = 0; b < numberOfPvbrSimulations; b++) {
                simulate(b,k);
            }

            /* liczba zmiennych parametrów */
            int numberOfPvbrSimulationsTwo = 66;

            for (int b = 10; b < numberOfPvbrSimulationsTwo; b += 5) {
                simulate(b,k);
            }

            if (k == 10)
                k = 49;
            else if (k == 69)
                k = 70;
        }
    }

    private static void simulate(int b, int k) {
        PVBR simulator = new PVBR();
        simulator.initializeSimulatorParameters();
        simulator.initializeStaticParameters(b, k);

        /* tutaj przekazujemy wybrane parametry dotyczące staticParameters */
        //simulator.setSpecialParameters(b,k);

        while (simulator.getNumberOfCompletedSingleSimulations() != simulator.getNumberOfRequestedSingleSimulations()) {
            simulator.initializeVariableParameters();
            simulator.start();
            simulator.increaseNumberOfCompletedSingleSimulations();
        }

            /* zapisujemy wynik do pliku o nazwie B_i.txt*/
        simulator.saveFinalReportToFile(b,k);
    }

}