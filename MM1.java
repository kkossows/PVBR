import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Klasa odpowiedzialna za symulator systemu M/M/1
 *
 * System typu 'X/Y/c/L/N':
 *      X - symbol rozkładu strumienia wejściowego
 *      Y - symbol rozkładu czasu obsługi zgłoszenia
 *      c - liczba kanałów obsługi
 *      L - liczba miejsc w poczekalni
 *      N - wymiar źródła zgłoszeń
 *
 *
 * System typu M/M/1 oznacza:
 *      M - wykładniczy rozkad strumienia wejściowego
 *      M - wykładniczy rozkład czasu obsługi zgłoszenia
 *      1 - jeden kanał obsługi
 *
 *      Dodatkowo:
 *          - zakładamy 1 strumień napływający;
 *          - zakłądamy nieskończoną liczbe miejsc w poczekalni (rozmiar kolejki nieskończony);
 *
 *      Parametry opisowe:
 *          lambda      - intensywność napływu zgłoszeń
 *          1/lambda    - średni czas między złgoszeniami
 *          mi          - intenstywność obsługi zgłoszeń
 *          1/mi        - średni czas obsługi pojedyńczego zgłoszenia
 *          n           - wartośc parametru kończącego symulację
 *
 *      Zmienne odnoszące się do parametrów:
 *          lambda  -> arrivalDistribution (klasa Stream)
 *          1/mi    -> departureDistribution (klasa Stream)
 *          n       -> numberOfDelays lub numberOfServedClients
 *
 */
public class MM1 extends SimulatorTemplate{
    /* średnia zajętośc serwera */
    private double theoreticalUtilizationFactor;
    /* średni czas klienta spędzony w kolejce */
    private double theoreticalAverageTimeInQueue;
    /* średnia liczba klientów w kolejce */
    private double theoreticalClientsInQueue;


    /**
     * Konstruktor klasy
     * - uruchamia konstruktor klasy bazowej;
     * - uruchamia metodę inicjalizującą parametry symulacji;
     * ( metoda uruchamiana jest tylko wtedy, gdy dotychczasowe dane wczytane zostały prawidłowo )
     */
    public MM1() {
        super();

        /* jeżeli nie wystapił błąd to kontynuuj inicjalizację */
        if(!loadError)
            initialize();
    }

    /* Metody algorytmu inicjalizującego */
    /**
     * Nadpisana metoda inicjalizująca system kolejkowy.
     * - wykonuje szczegółową dla danej klasy inicjalizaję;
     * - uruchamia bazową inicjalizację symulatora;
     * - generacja pierwszego zdarzenia z pierwszego strumienia;
     */
    @Override
    protected void initialize() {
        /* wspólne zmienne dla tego typu symulacji
            -   nieskończony limit kolejki;
        -   czas startu strumienia na 0;         */
        queueSizeLimit = Integer.MAX_VALUE;
        streams = new LinkedList<>();
        streamsStartTimes = new LinkedList<>();

        /* ustalamy liczbę napływów na 1 */
        numberOfIncomingStreams = 1;
        eachStreamsTheSame = true;
        streams.add(new Stream());
        streams.get(0).setStartTime(0.0);
        streamsStartTimes.add(0.0);

        /* wykonaj szczegółową dla danej klasy inicjalizację */
        switch (simulationType) {
            case FROM_FILE:
                fromFileInitialize();
                break;
            case MANUAL:
                manualInitialize();
                break;
            // zabezpieczenie przed wprowadzeniem innej zmiennej dokonane w konstruktorze SimulatorTemplate;
        }

        /* uruchom bazową inicjalizację */
        if (!loadError)
            super.initialize();

        /* obliczamy parametry charakterystyczne */
        calculateIndividualSimulationParameters();

        /* generacja pierwszego zdarzenia z pierwszego strumienia */
        generateNextArrivalEvent(streams.get(0));
    }
    /**
     * Manualna konfiguracja paramterów:
     * - użytkownik podaje wartość lambda;
     * - użytkownik podaje wartość mi;
     * - użytkownik podaje typ paramteru kończącego symulację;
     * - użytkownik podaje wartość parametru kończącego symulację;
     * - uruchomienie metody initializeSimulationEnd;
     * - użytkownik podaje wartośc interwału czasowego wypisywania raportów pośrednich liczoną w obsłużonych zdarzeniach;
     * @return Flaga oznaczająco powodzenie operacji, lub brak powodzenia (wystąpienie błedu, false);
     */
    @Override
    protected void manualInitialize() {
        System.out.println("---> Manualna inicjalizacja parametrów symulacji...");
        Scanner scanner = new Scanner(System.in);

        /* ARRIVAL
        - ustawiamy rozkład napływu na wykładniczy;
        - użytkownik podaje wartośc lambda
        */
        System.out.println("-->  Podaj intensywność napływu klientów:");
        streams.get(0).setArrivalProbabilityDistribution
                (Stream.DistributionTypes.EXPONENTIAL, scanner.nextDouble());

        /* DEPARTURE
        - ustawiamy rozkład czasu obsługi klienta przez serwer na wykładniczy,
        - użytkownik podaje wartośc mi (przeliczamy średni czas na intensywność 1/mi)
         */
        System.out.println("-->  Podaj średni czas obsługi klienta:");
        double parameter;
        parameter = ( 1 / scanner.nextDouble());
        streams.get(0).setDepartureProbabilityDistribution
                (Stream.DistributionTypes.EXPONENTIAL, parameter);

        /* wczytanie typu i wartości końca symulacji */
        boolean correct = super.initializeSimulationEnd();
        if (!correct) {
            loadError = true;
            return;
        }

        /* użytkownik podaje wartośc interwału czasowego wypisywania raportów pośrednich liczoną w obsłużonych zdarzeniach; */
        System.out.println("--> Podaj interwał czasowy raportów pośrednich liczony w liczbie obsłużonych zdarzeń:   <---");
        reportIntervalNumber = scanner.nextInt();

        /* ustawiamy odpowiednie flagi */
        streams.get(0).setInflowsDefineFlag(false);
        streams.get(0).setServiceTimeDefineFlag(false);
    }
    /**
     * Metoda służąca do wczytania parametrów wejściowych z pliku konfiguracyjnego:
     * - użytkownik podaje ściezke pliku konfiguracyjnego;
     * - na podstawie pliku wczytywane są odpowiednie parametry symulacji;
     */
    @Override
    protected void fromFileInitialize() {
        /*Scanner scanner = new Scanner(System.in);
        System.out.println("--> Podaj ścieżkę pliku konfiguracyjnego: <---");
        String configurationFilePath = scanner.nextLine();

        // DOKOnczyc

        */

        /* próba  testowa*/
        System.out.println("--> Parametry próby testowej: <---");
        simulationEndParameterType = EndType.NUMBER_OF_DELAYS;
        simulationEndParameterValue = 6;
        reportIntervalNumber = 0;
        streams.get(0).setInflowsDefineFlag(true);
        streams.get(0).setServiceTimeDefineFlag(true);

        streams.get(0).addInterval(0.4);
        streams.get(0).addInterval(1.2);
        streams.get(0).addInterval(0.5);
        streams.get(0).addInterval(1.7);
        streams.get(0).addInterval(0.2);
        streams.get(0).addInterval(1.6);
        streams.get(0).addInterval(0.2);
        streams.get(0).addInterval(1.4);
        streams.get(0).addInterval(1.9);

        streams.get(0).addServiceTime(2.0);
        streams.get(0).addServiceTime(0.7);
        streams.get(0).addServiceTime(0.2);
        streams.get(0).addServiceTime(1.1);
        streams.get(0).addServiceTime(3.7);
        streams.get(0).addServiceTime(0.6);
    }

    /**
     * Metoda służaca do wyliczania teoretycznych parametrów zwiazancyh z daną symulacją:
     * - średnia zajętosć serwera;
     * - średni czas klienta spędzony w kolejce;
     * ( obie wartości liczone są na podstawie intensywności napływu zgłoszeń oraz intensywności obsługi klientów )
     */
    @Override
    protected void calculateIndividualSimulationParameters() {
        /* średnie obciążenie systemu (ro = lambda / mi) */
        theoreticalUtilizationFactor = (
                streams.get(0).getArrivalDistributionParameter() / streams.get(0).getDepartureDistributionParameter()
        );

        /* średni czas w kolejce W = (ro / mi) / (1 - ro) */
        double left;
        double right;

        left = (
                theoreticalUtilizationFactor / streams.get(0).getDepartureDistributionParameter()
        );
        right = ( 1.0 - theoreticalUtilizationFactor );
        theoreticalAverageTimeInQueue = ( left / right );

        /* średnia liczba klientów w kolejce  (N = lambda * W ) */
        theoreticalClientsInQueue = (
                streams.get(0).getArrivalDistributionParameter() * theoreticalAverageTimeInQueue
        );

    }


    /* Metody algorytmu raportującego */
    /**
     * Metoda odpowiedzialna za wypisanie wszystkich parametrów wejściowych podanych manualnie przez użytkownika
     * podczas inicjalizacji systemu;
     * - wartośc lambda (intenywność napływu);
     * - wartośc mi (średni czas obsługi);
     * - typ parametru kończącego symulację;
     * - wartośc parametru kończącego sumulację;
     * - wartość interwału liczonego w zdarzeniach;
     */
    @Override
    protected void printManualConfiguration() {
        System.out.println("...........................................................");
        System.out.println("-----> Parametry wejściowe podane przez użytkownika:");

        System.out.print("-------> Wartośc lambda (intenywność napływu): ");
        System.out.println(streams.get(0).getArrivalDistributionParameter());

        System.out.print("-------> Wartośc 1/mi (średni czas obsługi): ");
        double tmp = 1.0;
        tmp = ( tmp / streams.get(0).getDepartureDistributionParameter() );
        System.out.println(tmp);

        System.out.print("-------> Typ parametru kończącego symulację: ");
        switch (simulationEndParameterType){
            case NUMBER_OF_DELAYS:
                System.out.println(EndType.NUMBER_OF_DELAYS.name());
                break;
            case NUMBER_OF_SERVED_CLIENTS:
                System.out.println(EndType.NUMBER_OF_SERVED_CLIENTS.name());
                break;
        }

        System.out.print("-------> Wartość parametru kończącego symulację: ");
        System.out.println(simulationEndParameterValue);

        System.out.print("-------> Wartość interwału raportu pośredniego liczonego w zdarzeniach: ");
        System.out.println(reportIntervalNumber);
    }
    @Override
    protected void printConfigurationFromFile() {

    }

    /**
     * Metoda służąca do wypisania teoretycznych wartości jakie powinno się uzyskać w systemi MM1
     * - podaje wejściowy parametr intensywności napływu klientów;
     * - podaje wejściowy parametr średni czas obłsługi klientów;
     * - podaje teoretyczną wartość średniego obciążenia systemu;
     * - podaje teoretyczną wartość średniego czasu oczekiwania w kolejce;
     */
    @Override
    protected void printIndividualSimulationParameters() {
        System.out.println("...........................................................");
        System.out.println("-----> Teoretyczne parametry wyjściowe dla systemu MM1:");

        System.out.print("-------> Intensywność napływu klientów dana rozkładem wykładniczym (lambda): ");
        System.out.println(streams.get(0).getArrivalDistributionParameter());

        System.out.print("-------> Sredni czas obsługi klientów dany rozkładem wykładniczym (1/mi) ");
        System.out.println(streams.get(0).getDepartureDistributionParameter());

        System.out.print("-------> Srednie obciążenie systemu (ro): ");
        System.out.println(theoreticalUtilizationFactor);

        System.out.print("-------> Sredni czas oczekiwania w kolejce (W): ");
        System.out.println(theoreticalAverageTimeInQueue);

        System.out.print("-------> Srednia liczba klientów w kolejce (N): ");
        System.out.println(theoreticalClientsInQueue);
    }

}
