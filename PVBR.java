import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by rkossowski on 2017-04-29.
 */
public class PVBR extends SimulatorTemplate{

    /* parametry symulatora */
    private double total_meanWaitingTimeInQueue;
    private double total_meanNumberOfClientsInQueue;
    private double total_meanServerStatusBusy;
    private double total_probabilityOfClientRejection;

    private double simulator_meanWaitingTimeInQueue;
    private double simulator_meanNumberOfClientsInQueue;
    private double simulator_meanServerStatusBusy;
    private double simulator_probabilityOfClientRejection;

    private int simulator_numberOfRequestedSingleSimulations;
    private int simulator_numberOfCompletedSingleSimulations;

    private double simulator_serverCapacity;
    private int simulator_reportingPeriod;

    /* parametry pojedyńczego strumienia */
    private double stream_onStateDuration;
    private double stream_offStateDuration;
    private double stream_packetLength;
    private  double stream_meanIntensityOfIncomingPackets;

    private double stream_maxIntensityOfIncomingPackets;
    private double stream_serviceTime;
    private double stream_intervalBetweenPacketsInOneBurst;
    private double stream_intervalBetweenPacketsInDifferentBursts;
    private int stream_burstLength;

    /* parametry symylacji jednostkowej */
    private int simulation_Number;
    private int simulation_reportingPeriod;

    /* parametry potrzebne do prawidłowego wyświetlania raportów */
    private int loggedStateNumber;
    private int tmp;
    private int tmp2;
    protected java.util.List<Double> lossProbabilityScoresList;


    /* listy poboczne---------------------------------------------------------*/
    /*
    private double meanWaitingTimeInQueue_2;
    private double meanNumberOfClientsInQueue_2;
    private double meanServerStatusBusy_2;
    private double probabilityOfClientRejection_2;
    private double total_probabilityOfClientRejection_2;
    private double total_meanWaitingTimeInQueue_2;
    private double total_meanNumberOfClientsInQueue_2;
    private double total_meanServerStatusBusy_2;
    protected java.util.List<Double> lossProbabilityScoresList_2;


    protected double meanWaitingTimeInQueue_10;
    protected double meanNumberOfClientsInQueue_10;
    protected double meanServerStatusBusy_10;
    protected double probabilityOfClientRejection_10;
    private double total_meanWaitingTimeInQueue_10;
    private double total_meanNumberOfClientsInQueue_10;
    private double total_meanServerStatusBusy_10;
    private double total_probabilityOfClientRejection_10;
    protected java.util.List<Double> lossProbabilityScoresList_10;

    protected double meanWaitingTimeInQueue_100;
    protected double meanNumberOfClientsInQueue_100;
    protected double meanServerStatusBusy_100;
    protected double probabilityOfClientRejection_100;
    private double total_meanWaitingTimeInQueue_100;
    private double total_meanNumberOfClientsInQueue_100;
    private double total_meanServerStatusBusy_100;
    private double total_probabilityOfClientRejection_100;
    protected java.util.List<Double> lossProbabilityScoresList_100;


    protected double meanWaitingTimeInQueue_250;
    protected double meanNumberOfClientsInQueue_250;
    protected double meanServerStatusBusy_250;
    protected double probabilityOfClientRejection_250;
    private double total_meanWaitingTimeInQueue_250;
    private double total_meanNumberOfClientsInQueue_250;
    private double total_meanServerStatusBusy_250;
    private double total_probabilityOfClientRejection_250;
    protected java.util.List<Double> lossProbabilityScoresList_250;


    protected double meanWaitingTimeInQueue_500;
    protected double meanNumberOfClientsInQeue_500;
    protected double meanServerStatusBusy_500;
    protected double probabilityOfClientRejection_500;
    private double total_meanWaitingTimeInQueue_500;
    private double total_meanNumberOfClientsInQueue_500;
    private double total_meanServerStatusBusy_500;
    private double total_probabilityOfClientRejection_500;
    protected java.util.List<Double> lossProbabilityScoresList_500;



    protected double simulator_meanWaitingTimeInQueue_30;
    protected double simulator_meanNumberOfClientsInQueue_30;
    protected double simulator_meanServerStatusBusy_30;
    protected double simulator_probabilityOfClientRejection_30;

    protected double simulator_meanWaitingTimeInQueue_100;
    protected double simulator_meanNumberOfClientsInQueue_100;
    protected double simulator_meanServerStatusBusy_100;
    protected double simulator_probabilityOfClientRejection_100;

    protected double simulator_meanWaitingTimeInQueue_250;
    protected double simulator_meanNumberOfClientsInQueue_250;
    protected double simulator_meanServerStatusBusy_250;
    protected double simulator_probabilityOfClientRejection_250;

    protected double simulator_meanWaitingTimeInQueue_500;
    protected double simulator_meanNumberOfClientsInQueue_500;
    protected double simulator_meanServerStatusBusy_500;
    protected double simulator_probabilityOfClientRejection_500;

    protected double simulator_meanWaitingTimeInQueue_1000;
    protected double simulator_meanNumberOfClientsInQueue_1000;
    protected double simulator_meanServerStatusBusy_1000;
    protected double simulator_probabilityOfClientRejection_1000;
    */
    /*--------------------------------------------------------------------*/

    /**
     * Kontruktor
     * - użytkownik wprowadza sposób wczytania danych statycznych
     * ( ręcznie z poziomu konsoli lub automatycznie z pliku)
     */
    public PVBR() {
        super();
    }

    @Override
    protected void manualInitialize() {
        initializeStaticParametersManually();
    }
    @Override
    protected void fromFileInitialize() {
        initializeStaticParametersFromFile();
    }
    /**
     * Metoda odpowiedzialna za wyliczenie pozostałych parametrów:
     1) czas t_on liczony w sekundach;
     2) czas toff liczony w sekundach;
     3) czas obsługi pakietu przez serwer;
     4) odstęp pomiędzy pakietami w paczce;
     5) odstęp pomiędzy ostatnim a pierwszym pakietem w różnych paczkach;

     1. t_on_sek=długość_paczki*długość_pakietu/PR;
     2. ze wzoru na SR=t_on/(t_on+t_off)*PR -> toff = (PR/SR * t_on_sek - t_on_sek)
     3. czas_obsługi = dł_pakietu / C = M/C;
     4. delta1 = dł_pakietu / PR = M/PR;
     5. delta2 = delta1 + t_off;
     */
    @Override
    protected void calculateIndividualSimulationParameters() {
        /* czas t_off liczony w sekundach */
        stream_onStateDuration = (stream_burstLength * (stream_packetLength / stream_maxIntensityOfIncomingPackets));
        stream_offStateDuration =
                (stream_onStateDuration * ((stream_maxIntensityOfIncomingPackets / stream_meanIntensityOfIncomingPackets) - 1));

        /* czas obsługi pakietu przez serwer */
        stream_serviceTime = (stream_packetLength / simulator_serverCapacity);

        /* odstęp pomiędzy pakietami w paczce */
        stream_intervalBetweenPacketsInOneBurst = (stream_packetLength / stream_maxIntensityOfIncomingPackets );

        /* odstęp pomiędzy ostatnim a pierwszym pakietem w różnych paczkach */
        stream_intervalBetweenPacketsInDifferentBursts = (
                stream_intervalBetweenPacketsInOneBurst + stream_offStateDuration);
    }


    @Override
    protected void printManualConfiguration() {

    }
    @Override
    protected void printConfigurationFromFile() {

    }
    @Override
    protected void printIndividualSimulationParameters() {

    }


    /* --------------------------METODY SYMULACJI------------------------------ */
    /**
     * Metoda odpowiedzialna za wczytanie stałych parametrów symulacji;
     *
     * Stałe parametry symulacji jednostkowej:
     *   1) liczbę stanów ON/OFF kończących symulację jednostkową;
     *   2) rozmiar kolejki;
     *   3) liczba strumieni napływających;
     *   4) przepustowość serwera obsługi (łącza);
     *   5) t_on;
     *   6) t_off;
     *   7) długość pakietu;
     *   8) średnia szybkość napływu pakietów (SR);
     */
    public void initializeStaticParameters() {
        switch (this.simulationType) {
            case FROM_FILE:
                fromFileInitialize();
                break;
            case MANUAL:
                manualInitialize();
                break;
        }

        /* inicjalizacja listy wyników jednostkowych */
        lossProbabilityScoresList = new ArrayList<>();

        // ------------------------------------
        //lossProbabilityScoresList_2 = new ArrayList<>();
        //lossProbabilityScoresList_10 = new ArrayList<>();
        //lossProbabilityScoresList_100 = new ArrayList<>();
        //lossProbabilityScoresList_250 = new ArrayList<>();
        //lossProbabilityScoresList_500 = new ArrayList<>();
        //
        //--------------------------------------
    }
    /**
     * Metoda odpowiedzialna za wczytanie stałych parametrów symulacji;
     * - użytkownik podaje parametry symulatora;
     * - użytkownik podaje parametry opisujące pojedyńczy strumień;
     * - wylicza resztę danych opisujących pojedyńczy strumien;
     * - użytkownik podaje wartości dotyczące raportów pośrednich;
     */
    private void initializeStaticParametersManually() {
        Scanner scanner = new Scanner(System.in);

        /* pobierz parametry symulatora
            użytkownik podaje wartości parametrów:
                1) liczba stanów ON/OFF kończących symulację jednostkową;
                2) rozmiar kolejki;
                3) liczba strumieni napływających;
                4) przepustowość serwera obsługi (łącza C);
         */
        System.out.println("---->    Parametry symulatora:");
        System.out.println("---->    Podaj liczbę stanów ON/OFF kończących symulację jednostkową (ni):");
        simulationEndParameterValue = scanner.nextInt();
        simulationEndParameterType = EndType.NUMBER_OF_ON_OFF_STATES;
        System.out.println("---->    Podaj rozmiar kolejki liczoną w pakietach (B):");
        queueSizeLimit = scanner.nextInt();
        System.out.println("---->    Podaj liczbę strumieni napływających (N):");
        numberOfIncomingStreams = scanner.nextInt();
        System.out.println("---->    Podaj przepustowość serwera (bajt/sekunde) (C):");
        simulator_serverCapacity = scanner.nextDouble();


        /* parametry opisujące pojedyńczy strumień
            użytkownik podaje wartości parametrów:
                1) długość pakietu;
                2) maksymalna intensywność napływu pakietów (PR);
                3) średnia intensywność napływu pakietów (SR);
                4) długość trwania stanu ON liczona w pakietach;

         */
        System.out.println("---->    Parametry opisujące pojedyńczy strumień:");
        System.out.println("---->    Podaj długość pakietu w bajtach (M):");
        stream_packetLength = scanner.nextDouble();
        System.out.println("---->    Podaj maksymalną intensywność napływu pakietów (bajt/jednostka czasu) (PR):");
        stream_maxIntensityOfIncomingPackets = scanner.nextDouble();
        System.out.println("---->    Podaj średnią intensywność napływu pakietów (bajt/jednostka czasu) (SR):");
        stream_meanIntensityOfIncomingPackets = scanner.nextDouble();
        System.out.println("---->    Podaj długośc trwania stanu ON liczona w pakietach (K):");
        stream_burstLength = scanner.nextInt();

         /* wylicza resztę danych opisujących pojedyńczy strumien
            1) czas t_on liczony w sekundach;
            2) czas toff liczony w sekundach;
            3) czas obsługi pakietu przez serwer;
            4) odstęp pomiędzy pakietami w paczce;
            5) odstęp pomiędzy ostatnim a pierwszym pakietem w różnych paczkach;

            1. t_on_sek=długość_paczki*długość_pakietu/PR;
            2. ze wzoru na SR=t_on/(t_on+t_off)*PR -> toff = (PR/SR * t_on_sek - t_on_sek)
            3. czas_obsługi = dł_pakietu / C = M/C;
            4. delta1 = dł_pakietu / PR = M/PR;
            5. delta2 = delta1 + t_off;
         */
        calculateIndividualSimulationParameters();

        /* pobierz wartości dotyczące raportów pośrednich
            Użytkownik podaje wartość parametrów:
            1) okres raportowania pośredniego symulacji jednostkowej liczony w liczbie stanów ON/OFF ;
            2) okres raportowania pośredniego całej symulacji liczony w liczbie wykonanych symulacji jednostkowych;
         */
        System.out.println("---->    Parametry opisujące pośrednie raportowanie:");
        System.out.println("---->    Podaj okres raportowania pośredniego symulacji jednostkowej liczony w liczbie stanów ON/OFF: ");
        simulation_reportingPeriod = scanner.nextInt();
        System.out.println("---->    Podaj okres raportowania pośredniego całej symulacji liczony w liczbie wykonanych symulacji jednostkowych:");
        simulator_reportingPeriod = scanner.nextInt();
    }
    /**
     * Metoda odpowiedzialna za wczytywanie stałych parametrów symulacji z pliku
     */
    private void initializeStaticParametersFromFile() {

        simulator_numberOfRequestedSingleSimulations = 500;

        simulationEndParameterValue = 500;
        simulationEndParameterType = EndType.NUMBER_OF_ON_OFF_STATES;
        //queueSizeLimit = 0;     //B
        numberOfIncomingStreams = 43;   //N
        simulator_serverCapacity = 100000000.0;     //C

        stream_packetLength = 100.0;    //M
        stream_maxIntensityOfIncomingPackets = 11000000.0;  //PR
        stream_meanIntensityOfIncomingPackets = 1000000.0;  //SR
        stream_burstLength = 10;    //K

        calculateIndividualSimulationParameters();

        simulation_reportingPeriod = 10000;
        simulator_reportingPeriod = 10000;

    }
    /**
     * Metoda odpowiedzialna za inicjalizajcę zmiennych parametrów symulacji;
     * - zainicjalizuj numer porządkowy symulacji jednostkowej;
     * - zainicjalizuj zegar symulacji;
     * - zainicjalizuj liczniki statystyk
     * - zainicjalizuj pozostałe parametry;
     * - zainicjalizuj listy zdarzeń;
     * - zainicjalizuj pierwszy strumień;
     * - uruchom algorytm losowania czasów startu napływu strumieni;
     * - stwórz tyle strumieni ile zostało podane przez użytkownika;
     * - każdemu ze strumieni przypisz  wylosowany czas startu napływu;
     * - dla każdego strumienia,  stwórz i dodaj do listy zdarzeń zdarzen;
     * - wyzeruj liczbę wykonanych stanów ON/OFF;
     *
     * Zmienne parametry symulacji jednostkowej:
     *   0) numer porządkowy symulacji jednostkowej
     *   1) zegar symulacji;
     *   2) status serwera;
     *   3) liczba pakietów w kolejce;
     *   4) czasy przybycia pakietów w kolejce;
     *   5) czas ostatniego zdarzenia;
     *   6) lista zdarzeń;
     *   7) liczniki statystyk;
     */
    public void initializeVariableParameters() {
        /* Zainicjalizuj numer porządkowy symulacji jednostkowejZainicjalizuj numer porządkowy symulacji jednostkowej */
        this.simulation_Number = simulator_numberOfCompletedSingleSimulations + 1;

        /* Zainicjalizuj liczniki statystyk
            Zerowanie liczników statystyk:
            1) Prawdopodobieństwo odrzucenia pakietu;
            2) Średni czas oczekiwania pakietu w kolejce;
            3) Średnia liczba pakietów w kolejce;
            4) Średnie obciążenie systemu;

            Zainicjalizuj pozostałe parametry
            1) status serwera = 0;
            2) liczba pakietów w kolejce = 0;
            3) czas poprzedniego zdarzenia = 0;
            4) bieżący typ zdarzenia = null;
            5) inicjalizacja listy czasy przybycia klientów w kolejce;

            Zainicjalizuj listy zdarzeń
         */
        super.initialize();
        streams = new ArrayList<>();
        streamsStartTimes = new ArrayList<>();

        /* Zainicjalizuj pierwszy strumień */
        streams.add(new Stream(
                0,
                stream_burstLength-1
        ));
        events.add( new Event(
                Event.EventType.ARRIVAL,
                streams.get(0).getStartTime(),
                streams.get(0)
        ));

        /*  Uruchom algorytm losowania czasów startu napływu strumieni */
        super.initializeFlowsStartTimes(stream_onStateDuration + stream_offStateDuration);

        /*
            Stwórz tyle strumieni ile zostało podane przez użytkownika;
            Każdemu ze strumieni przypisz  wylosowany czas startu napływu;
            Dla każdego strumienia,  stwórz i dodaj do listy zdarzeń zdarzenie przyjścia pakietu do systemu (ARRIVAL);
        */
        for (int i=1; i<numberOfIncomingStreams; i++) {
            streams.add(new Stream(
                    super.streamsStartTimes.get(i-1),
                    stream_burstLength-1
            ));
            events.add( new Event(
                    Event.EventType.ARRIVAL,
                    streams.get(i).getStartTime(),  //pierwszy pakiet przychodzi w chwili rozpoczecia napływu
                    streams.get(i)
            ));
        }

        /* znajdź ten strumień, który ma największy start time i zamień tam flagę isLastOne na true */
        int indexOfTheLastOne = 0;
        double maxStartTime = 0.0;
        for (int i = 0; i < streams.size(); i++){
            if(streams.get(i).getStartTime() >= maxStartTime )
                indexOfTheLastOne = i;
        }

        /* zmień flagę isLastOne w jednym wybranym strumieniu */
        streams.get(indexOfTheLastOne).setIsLastOne(true);

        /* wyzeruj liczbę wykonanych stanów ON/OFF */
        numberOfCompletedOnOffStates = 0;

        /* wyzerowanie zmiennej finish state kończącej pętle symulacji jednostkowej */
        finishTime = false;

        /* wyzeruj zmienną raportowania */
        loggedStateNumber = 0;
        tmp = 0;
        tmp2= 0;
    }

    /**
     * Meotda służąca do wygenerowania nowego zdarzenia typu przyjście pakietu do systemu;
     * - wyznaczam nowy odstep czasowy;
     * - dodaję nowe zdarzenie do listy zdarzeń;
     * - zwiększ licznik oznaczajacy liczbe wygenerowanych zdarzeń typu arrival;
     * @param source Stumień, z którego pochodzi klient napływajacy.
     */
    @Override
    protected void generateNextArrivalEvent(Stream source) {
        /* wyznaczam nowy odstep czasowy */
        double newDistanceBetweenInflows = planNextEvent(Event.EventType.ARRIVAL, source);

        /* dodaję nowe zdarzenie do listy zdarzeń */
        double eventTime = ( simulationTime + newDistanceBetweenInflows );
        events.add(new Event(Event.EventType.ARRIVAL, eventTime, source));

        /* zwiększ licznik oznaczajacy liczbe wygenerowanych zdarzeń typu arrival */
        arrivalGeneratedEventsCounter++;
    }
    /**
     * Metoda służąca do wygenerowania nowego zdarzenia typu zakończenie obsługi
     * - wyznaczam nowy czas obsługi ;
     * - dodaję nowe zdarzenie do listy zdarzeń ;
     * - zwiększ licznik oznaczajacy liczbe wygenerowanych zdarzeń typu arrival;
     * @param source Strumień, z którego pochodzi klient obsłużony.
     */
    @Override
    protected void generateNextDepartureEvent(Stream source) {
        /* wyznaczam nowy czas obsługi */
        double newServiceTime = planNextEvent(Event.EventType.DEPARTURE, source);

        /* dodaję nowe zdarzenie do listy zdarzeń */
        double eventTime = (simulationTime + newServiceTime);
        events.add(new Event(Event.EventType.DEPARTURE, eventTime, source));

        /* zwiększ licznik oznaczajacy liczbe wygenerowanych zdarzeń typu arrival */
        departureGeneratedEventsCounter++;
    }
    /**
     * Metoda służąca do planowania czasu następnego zdarzenia
     * - metoda dodaje nowy czas do konkretnej listy w danym strumieniu w celach statystycznych;
     * @param type Typ nastepnego zdarzenia (ARRIVAL lub DEPARTURE)
     * @param source Strumień macierzysty dla zdarzenia
     * @return Odstep czasow pomiędzy napływami lub czas obsługi pakietu
     */
    private double planNextEvent(Event.EventType type, Stream source) {
        double nextTime = 0.0;
        switch (type) {
            case ARRIVAL:
                if (source.getNumberOfPacketsRemainingInBurst() > 0){
                    /* nastepny pakiet należy do tego samego stanu ON */

                    if (source.getNumberOfPacketsRemainingInBurst() == ( stream_burstLength - 1) ) {
                        /* dopiero tutaj można zwiększyć liczbę stanów, aby symulacja skończyła się w momencie przyjścia
                        * pierwszego pakietu z nowego stanu, a nie w momecie przyjścia ostatniego pakietu w stanie on*/

                        /* zwiększ liczbę obsłużonych stanów ON/OFF */
                        source.increaseNumberOfCompletedOnOffStates();
                    }
                    /* zmniejsz liczbe pozostałych pakietów w paczce */
                    source.decreaseNumberOfPacketsRemainingInBurst();

                    /* zwróc wynik jako ostęp pomiedzy pakietami w tej samej paczce */
                    nextTime = stream_intervalBetweenPacketsInOneBurst;
                }
                /* szczególny przypadek, gdy mamy tylko 1 pakiet w paczce */
                else if (stream_burstLength == 1) {
                    /* zwiększ liczbę obsłużonych stanów ON/OFF */
                    source.increaseNumberOfCompletedOnOffStates();

                    /* zwróc wynik jako odstep pomiędzy ostatnim a pierwszym pakietem w różnych paczkach */
                    nextTime = stream_intervalBetweenPacketsInDifferentBursts;
                }
                else {
                    /* nastepny pakiet należy do nastepnego stanu ON */

                    /* ustaw liczbę pozostałych pakietów w paczce na wartość liczba_pakietów_w_paczce -1 */
                    source.setNumberOfPacketsRemainingInBurst(stream_burstLength-1);

                    /* zwróc wynik jako odstep pomiędzy ostatnim a pierwszym pakietem w różnych paczkach */
                    nextTime = stream_intervalBetweenPacketsInDifferentBursts;
                }

                /* dodaj nowy czas do listy zbierającej statystyki */
                source.addInterval(nextTime);
                break;
            case DEPARTURE:
                /* zwróc stały czas obsługi pakietu przez serwer */
                nextTime = stream_serviceTime;

                /* dodaj nowy czas do listy zbierającej statystyki */
                source.addServiceTime(nextTime);
                break;
        }
        return nextTime;
    }

    /**
     * Metoda odpowiedzialna za wypisanie raportu
     * - pośredniego (zgodnie z okresem raportowania);
     * - końcowego;
     * @param reportType Wskazuje typ raportu (CURRENT, bieżacy status systemu; FINAL, ostateczny stan systemu);
     */
    @Override
    protected void report(ReportType reportType) {
        switch (reportType) {
            case CURRENT:
                if (simulation_reportingPeriod == 0) {
                    if (arrivalEventsCounter == (stream_burstLength + 1)) {
                        printCurrentReport();
                        loggedStateNumber++;
                    }
                }
                else {
                    if (numberOfCompletedOnOffStates != 0) {
                        /* szukam ostatniego strumienia */
                        for (int i=0; i< streams.size(); i++){
                            if (streams.get(i).isLastOne()) {
                                if (streams.get(i).getNumberOfCompletedOnOffStates() >  loggedStateNumber ) {
                                    loggedStateNumber++;
                                    if ((loggedStateNumber % simulation_reportingPeriod) == 0) {
                                        printCurrentReport();
                                        break;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            case FINAL:
                printFinalReport();
                break;
            default:
                break;
        }
    }

    /**
     * Metoda odpowiedzialna za wypisanie raportu pośredniego
     * Raport pośredni:
     *   1) Numer porządkowy symulacji jednostkowej;
     *   2) Liczba stanów ON/OFF;
     *   3) Prawdopodobieństwo odrzucenia pakietu;
     */
    private void printCurrentReport() {
        updateMeanNumberOfClientsInQueue();
        updateProbabilityOfClientRejected();
        updateMeanWaitingTimeInQueue();
        updateMeanServerBusyStatus();

        System.out.println("...........................................................");
        System.out.println("--> Raport pośredni dla symulacji jednostkowej:");

        System.out.print("-------> Numer porządkowy symulacji jednostkowej: ");
        System.out.println(simulation_Number);

        System.out.print("-------> liczba odbytych / liczba żądanych stanów ON/OFF: ");
        System.out.print(numberOfCompletedOnOffStates);
        System.out.print("/");
        System.out.println(simulationEndParameterValue);

        System.out.print("-------> Prawdopodobieństwo odrzucenia pakietu: ");
        System.out.println(probabilityOfClientRejection);

        System.out.print("-------> Średni czas oczekiwania pakietu w kolejce: ");
        System.out.println(meanWaitingTimeInQueue);

        System.out.print("-------> Średnia liczba pakietów w kolejce: ");
        System.out.println(meanNumberOfClientsInQueue);

        System.out.print("-------> Średnie obciążenie systemu: ");
        System.out.println(meanServerStatusBusy);


        if(tmp == 0 ) {
            tmp = arrivalGeneratedEventsCounter;
            tmp2 = numberOfClientsRejected;

            System.out.print("-------> Liczba wygenerowanych zdarzeń przyjścia: ");
            System.out.println(tmp);

            System.out.print("-------> Liczba odrzuconych pakietów: ");
            System.out.println(tmp2);
        }
        else {
            System.out.print("-------> Liczba wygenerowanych zdarzeń przyjścia: ");
            System.out.println(arrivalGeneratedEventsCounter - tmp);
            tmp = arrivalGeneratedEventsCounter;

            System.out.print("-------> Liczba odrzuconych pakietów: ");
            System.out.println(numberOfClientsRejected - tmp2);
            tmp2 = arrivalGeneratedEventsCounter;
        }
        System.out.print("-------> Całkowita liczba wygenerowanych zdarzeń przyjścia: ");
        System.out.println(arrivalGeneratedEventsCounter);

        System.out.print("-------> Całkowita liczba odrzuconych pakietów: ");
        System.out.println(numberOfClientsRejected);

        System.out.println("...........................................................");
    }
    /**
     * Metoda wypisująca raport końcowy symulacji jednostkowej
     * Raport końcowy:
     *    1) Numer porządkowy symulacji jednostkowej;
     *    2) Liczba stanów ON/OFF;
     *    3) Czas symulacji;
     *    4) Prawdopodobieństwo odrzucenia pakietu;
     *    5) Średni czas oczekiwania pakietu w kolejce;
     *    6) Średnia liczba pakietów w kolejce;
     *    7) Średnie obciążenie systemu;
     */
    private void printFinalReport() {
        /* zaktualizuj statystyki kończowe symulacji jednostkowej */
        updateMeanNumberOfClientsInQueue();
        updateProbabilityOfClientRejected();
        updateMeanWaitingTimeInQueue();
        updateMeanServerBusyStatus();

        if(simulation_Number % 1 == 0) {
            System.out.print("-------> B:" + queueSizeLimit + "Ukończono: ");
            System.out.print(simulation_Number);
            System.out.print("/");
            System.out.println(simulator_numberOfRequestedSingleSimulations);
        }

        /*
        System.out.println("...........................................................");
        System.out.println("--> Raport końcowy dla symulacji jednostkowej:");

        System.out.print("-------> Numer porządkowy symulacji jednostkowej: ");
        System.out.println(simulation_Number);

        //System.out.print("-------> liczba odbytych / liczba żądanych stanów ON/OFF: ");
        //System.out.print(numberOfCompletedOnOffStates);
        //System.out.print("/");
        //System.out.println(simulationEndParameterValue);

        //System.out.print("-------> Czas symulacji: ");
        //System.out.println(simulationTime);

        System.out.print("-------> Prawdopodobieństwo odrzucenia pakietu: ");
        System.out.println(probabilityOfClientRejection);

        System.out.print("-------> Średni czas oczekiwania pakietu w kolejce: ");
        System.out.println(meanWaitingTimeInQueue);

        System.out.print("-------> Średnia liczba pakietów w kolejce: ");
        System.out.println(meanNumberOfClientsInQueue);

        System.out.print("-------> Średnie obciążenie systemu: ");
        System.out.println(meanServerStatusBusy);

        System.out.print("-------> Całkowita liczba wygenerowanych zdarzeń przyjścia: ");
        System.out.println(arrivalGeneratedEventsCounter);

        System.out.print("-------> Całkowita liczba odrzuconych pakietów: ");
        System.out.println(numberOfClientsRejected);

        System.out.println("...........................................................");
        */
        ///* debugging */
        //printDebugStatistics();

    }



    /* --------------------------METODY SYMULATORA------------------------------ */
    /**
     * Metoda odpowiedzialna za inicjalizowanie wartośi początkowych parametrów symulatora
     * (symulator to zbór symulacji jednostkowych)
     * - każda symulacja ma swój numer porządkowy;
     */
    public void initializeSimulatorParameters() {
        this.simulator_meanWaitingTimeInQueue = 0.0;
        this.simulator_meanServerStatusBusy = 0.0;
        this.simulator_meanNumberOfClientsInQueue = 0.0;
        this.simulator_probabilityOfClientRejection = 0.0;

        this.total_meanNumberOfClientsInQueue = 0.0;
        this.total_meanWaitingTimeInQueue = 0.0;
        this.total_meanServerStatusBusy = 0.0;
        this.total_probabilityOfClientRejection = 0.0;

        lossProbabilityScoresList = new ArrayList<>();
    }

    /**
     * Publiczna metoda służąca do ustawienia liczby symulacji do wykonania przez symulator
     */
    public void setNumberOfRequestedSingleSimulations(int number) {
        this.simulator_numberOfRequestedSingleSimulations = number;
    }
    /**
     * Publiczna metoda służąca do odczytywania liczby symulacji jednostkowych do wykonania przez symulator;
     * @return Liczba symulacji jednostkowych do wykonania przez symulator
     */
    public int getNumberOfRequestedSingleSimulations() {
        return this.simulator_numberOfRequestedSingleSimulations;
    }

    /**
     * Publiczna metoda służąca do zwiększania licznika ukończonych symulacji jednostkowych o 1
     * - dodatkowo, zapisywane sa wszystkie zmienne niezbędne do statystyk symulatora;
     */
    public void increaseNumberOfCompletedSingleSimulations() {
        /* zwiększ liczbę ukończonych symulacji jednostkowych */
        this.simulator_numberOfCompletedSingleSimulations++;

        /* aktualizacja globalnych liczników */
        this.total_meanNumberOfClientsInQueue += meanNumberOfClientsInQueue;
        this.total_meanWaitingTimeInQueue += meanWaitingTimeInQueue;
        this.total_meanServerStatusBusy += meanServerStatusBusy;
        this.total_probabilityOfClientRejection += probabilityOfClientRejection;

        /* wyniki symulacji */
        lossProbabilityScoresList.add(probabilityOfClientRejection);

        /*------------------------------------------*/
        //saveIndirectTotalValues();
        //printIndirectFinalResult();
        //try {
        //    printE2();
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}
        /*------------------------------------------*/
    }
    /**
     * Publiczna metoda służąca do odczytu liczby ukończonych symulacji jednostkowych
     * @return
     */
    public int getNumberOfCompletedSingleSimulations() {
        return this.simulator_numberOfCompletedSingleSimulations;
    }

    /**
     * Metoda odpowiedzialna za wyliczanie średnich wartości parametrów na podstawie wyników jednostkowych symulacji
     * - uśrednione prawdopodobieństwo utraty pakietów;
     * - uśredniona zajętość serwera obsługi;
     * - uśredniona liczba pakietów w kolejce;
     * - uśredniony czas oczekiwania;
     */
    private void updateGeneralParameters(){
        /* uśrednione prawdopodobieństwo utraty pakietów */
        simulator_meanWaitingTimeInQueue =
                (total_meanWaitingTimeInQueue / simulator_numberOfCompletedSingleSimulations);

        /* uśredniona zajętość serwera obsługi */
        simulator_meanNumberOfClientsInQueue =
                (total_meanNumberOfClientsInQueue / simulator_numberOfCompletedSingleSimulations);

        /* uśredniona liczba pakietów w kolejce */
        simulator_meanServerStatusBusy =
                (total_meanServerStatusBusy / simulator_numberOfCompletedSingleSimulations);

        /* uśredniony czas oczekiwania */
        simulator_probabilityOfClientRejection =
                (total_probabilityOfClientRejection / simulator_numberOfCompletedSingleSimulations);
    }

    /**
     * Publiczna metoda odpowiadająca za wypisywanie raportów:
     *
     * Raport pośredni zawiera:
     *  1) parametry symulatora:
     *      - liczba odbytych symulacji;
     *
     *  2) dotychczasowy wynik symulacji:
     *      - wypisanie statystyk programu;
     *          - uśrednione prawdopodobieństwo utraty pakietów;
     *
     * Raport końcowy zawiera:
     *  1) parametry wejściowe:
     *      - parametry symulatora:
     *          - liczba stanów ON/OFF kończących symulację jednostkową;
     *          - rozmiar kolejki liczoną w pakietach;
     *          - liczba strumieni napływających;
     *          - przepustowość serwera (bajt/jednostka czasu);
     *      - parametry opisujące pojedyńczy strumień:
     *          - długość trwania stanu ON;
     *          - długość trwania stanu OFF;
     *          - długość pakietu;
     *          - średnia intensywność napływu pakietów (bajt/jednostka czasu);
     *      - wyliczone parametry:
     *          - maksymalna intensywność napływu pakietów (PR);
     *          - czas obsługi pakietu;
     *          - odstęp pomiędzy pakietami w paczce;
     *          - odstęp pomiędzy ostatnim a pierwszym pakietem w różnych paczkach;
     *          - długość paczki (mierzona w pakietach);
     *
     *  2) parametry symulatora:
     *      - liczba odbytych symulacji;
     *
     *  3) wynik całej symulacji:
     *      - wypisanie statystyk programu;
     *          - uśrednione prawdopodobieństwo utraty pakietów;
     *          - uśredniona zajętość serwera obsługi;
     *          - uśredniona liczba pakietów w kolejce;
     *          - uśredniony czas oczekiwania;
     */
    public void simulatorReport(ReportType type) {
        updateGeneralParameters();

        switch (type) {
            case CURRENT:
                if ((simulation_Number % simulator_reportingPeriod) == 0) {
                    /*
                    System.out.println("...........................................................");
                    System.out.println("-----> Raport pośredni symulatora:");

                    System.out.println("-----> Parametry symulatora:");
                    System.out.print("-------> liczba odbytych / liczba żądanych symulacji: ");
                    System.out.print(simulator_numberOfCompletedSingleSimulations);
                    System.out.print("/");
                    System.out.println(simulator_numberOfRequestedSingleSimulations);

                    System.out.println("------> Dotychczasowy wynik symulacji:");
                    System.out.print("-------> Uśrednione prawdopodobieństwo utraty pakietów: ");
                    System.out.println(simulator_probabilityOfClientRejection);

                    System.out.println("...........................................................");
                    */
                }
                break;
            case FINAL:
                /*
                System.out.println("...........................................................");
                System.out.println("-----> Raport końcowy symulatora: ");

                System.out.print("-------> liczba odbytych / liczba żądanych symulacji: ");
                System.out.print(simulator_numberOfCompletedSingleSimulations);
                System.out.print("/");
                System.out.println(simulator_numberOfRequestedSingleSimulations);

                System.out.println("-----> Parametry wejściowe: ");
                System.out.println("---->    Parametry symulatora: ");
                System.out.print("---->    Przepustowość serwera (bajt/jednostka czasu): ");
                System.out.println(simulator_serverCapacity);
                System.out.print("---->    Liczba strumieni napływających: ");
                System.out.println(numberOfIncomingStreams);
                System.out.print("---->    Rozmiar kolejki liczoną w pakietach: ");
                System.out.println(queueSizeLimit);
                System.out.print("---->    Liczba stanów ON/OFF kończących symulację jednostkową: ");
                System.out.println(simulationEndParameterValue);

                System.out.println("---->    Parametry opisujące pojedyńczy strumień: ");
                System.out.print("---->    Maksymalna intensywność napływu pakietów (bajt/jednostka czasu) (PR): ");
                System.out.println(stream_maxIntensityOfIncomingPackets);
                System.out.print("---->    Srednia intensywność napływu pakietów (bajt/jednostka czasu) (SR): ");
                System.out.println(stream_meanIntensityOfIncomingPackets);
                System.out.print("---->    Długość pakietu (bajt) (M): ");
                System.out.println(stream_packetLength);
                System.out.print("---->    Liczba pakietów w stanie ton (K): ");
                System.out.println(stream_burstLength);

                System.out.println("---->    Wyliczone czasy parametry:");
                System.out.print("---->    Czas trwania stanu ON: ");
                System.out.println(stream_onStateDuration);
                System.out.print("---->    Czas trwania stanu OFF: ");
                System.out.println(stream_offStateDuration);
                System.out.print("---->    Czas obsługi pakieta:");
                System.out.println(stream_serviceTime);
                System.out.print("---->    Odstęp pomiędzy pakietami w paczce: ");
                System.out.println(stream_intervalBetweenPacketsInOneBurst);
                System.out.print("---->    Odstęp pomiędzy ostatnim a pierwszym pakietem w różnych paczkach: ");
                System.out.println(stream_intervalBetweenPacketsInDifferentBursts);


                System.out.println("------> Wyniki całej symulacji:");
                System.out.print("-------> Uśrednione prawdopodobieństwo utraty pakietów: ");
                System.out.println(simulator_probabilityOfClientRejection);
                System.out.print("-------> Uśredniona zajętość serwera obsługi: ");
                System.out.println(simulator_meanServerStatusBusy);
                System.out.print("-------> Uśredniona liczba pakietów w kolejce: ");
                System.out.println(simulator_meanNumberOfClientsInQueue);
                System.out.print("-------> Uśredniony czas oczekiwania: ");
                System.out.println(simulator_meanWaitingTimeInQueue);

                System.out.print("-------> Suma wszystkich prawdopodobieństw: ");
                System.out.println(total_probabilityOfClientRejection);

                System.out.print("-------> Liczba ukończonych symulacji jednostkowych: ");
                System.out.println(simulator_numberOfCompletedSingleSimulations);

                System.out.println("LISTA Pjednostkowych: ");
                System.out.println(lossProbabilityScoresList.toString());

                System.out.println("...........................................................");
                */

                System.out.println("-----> Parametry wejściowe: ");
                System.out.println("C = " + simulator_serverCapacity);
                System.out.println("PR = " + stream_maxIntensityOfIncomingPackets);
                System.out.println("SR = " + stream_meanIntensityOfIncomingPackets);
                System.out.println("M = " + stream_packetLength);
                System.out.println("K = " + stream_burstLength);
                System.out.println("B = " + queueSizeLimit);
                System.out.println("N = " + numberOfIncomingStreams);
                System.out.println("n_all = " + simulator_numberOfRequestedSingleSimulations);
                System.out.println("n_one = " + simulationEndParameterValue);

                System.out.println("-----> Parametry wyliczone: ");
                System.out.println("t_on = " + stream_onStateDuration);
                System.out.println("t_off = " + stream_offStateDuration);
                System.out.println("service = " + stream_serviceTime);
                System.out.println("delta1 = " + stream_intervalBetweenPacketsInOneBurst);
                System.out.println("delta2 = " + stream_intervalBetweenPacketsInDifferentBursts);

                System.out.println("-----> Wyniki symulacji: ");
                System.out.println("Uśrednione prawdopodobieństwo utraty pakietów = " + simulator_probabilityOfClientRejection);
                System.out.println("Uśredniona zajętość serwera obsługi = " + simulator_meanServerStatusBusy);
                System.out.println("Uśredniona liczba pakietów w kolejce = " + simulator_meanNumberOfClientsInQueue);
                System.out.println("Uśredniony czas oczekiwania = " + simulator_meanWaitingTimeInQueue);


                System.out.println("-----> Tablice wyników jednostkowych: ");
                System.out.println(lossProbabilityScoresList.toString());


                break;
        }
    }
    /* --------------------------METODY SYMULATORA------------------------------ */



    /*-----------------------------------------------------------------------------------*/

    public void saveFinalReportToFile(int b, int k) {
        updateGeneralParameters();

        try {

            String fileName = ("K_" + k + "_B_" + b +".txt");
            File file = new File(fileName);
            FileWriter fileWriter = new FileWriter(file);

            if (simulator_numberOfCompletedSingleSimulations == 1) {
                fileWriter.write("-----> Parametry wejściowe: \n");
                fileWriter.write("C = " + simulator_serverCapacity + "\n");
                fileWriter.write("PR = " + stream_maxIntensityOfIncomingPackets + "\n");
                fileWriter.write("SR = " + stream_meanIntensityOfIncomingPackets + "\n");
                fileWriter.write("M = " + stream_packetLength + "\n");

                fileWriter.write("N = " + numberOfIncomingStreams + "\n");
                fileWriter.write("n_all = " + simulator_numberOfRequestedSingleSimulations + "\n");
                fileWriter.write("n_one = " + simulationEndParameterValue + "\n");

                fileWriter.write("-----> Parametry wyliczone: " + "\n");
                fileWriter.write("t_on = " + stream_onStateDuration + "\n");
                fileWriter.write("t_off = " + stream_offStateDuration + "\n");
                fileWriter.write("service = " + stream_serviceTime + "\n");
                fileWriter.write("delta1 = " + stream_intervalBetweenPacketsInOneBurst + "\n");
                fileWriter.write("delta2 = " + stream_intervalBetweenPacketsInDifferentBursts + "\n");
            }


            fileWriter.write("K = " + stream_burstLength + "\n");
            fileWriter.write("B = " + queueSizeLimit + "\n");

            fileWriter.write("-----> Wyniki symulacji: " + "\n");
            fileWriter.write("Uśrednione prawdopodobieństwo utraty pakietów = " + simulator_probabilityOfClientRejection + "\n");
            fileWriter.write("Uśredniona zajętość serwera obsługi = " + simulator_meanServerStatusBusy + "\n");
            fileWriter.write("Uśredniona liczba pakietów w kolejce = " + simulator_meanNumberOfClientsInQueue + "\n");
            fileWriter.write("Uśredniony czas oczekiwania = " + simulator_meanWaitingTimeInQueue + "\n");


            fileWriter.write("-----> Tablice wyników jednostkowych: " + "\n");
            fileWriter.write(lossProbabilityScoresList.toString() + "\n");


            fileWriter.flush();
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    public void initializeStaticParameters(int bValue, int kValue) {
        simulator_numberOfRequestedSingleSimulations = 500;
        simulationEndParameterValue = 500;
        simulationEndParameterType = EndType.NUMBER_OF_ON_OFF_STATES;

        queueSizeLimit = bValue;     //B
        numberOfIncomingStreams = 43;   //N
        simulator_serverCapacity = 100000000.0;     //C

        stream_packetLength = 100.0;    //M
        stream_maxIntensityOfIncomingPackets = 11000000.0;  //PR
        stream_meanIntensityOfIncomingPackets = 1000000.0;  //SR
        stream_burstLength = kValue;    //K

        calculateIndividualSimulationParameters();

        simulation_reportingPeriod = 10000;
        simulator_reportingPeriod = 10000;

    }
    /*-----------------------------------------------------------------------------------*/

    /*-----------------------------------------------------------------------------------*/
    @Override
    protected void saveIndirectValues(int number) {
        switch(number) {
            case 2:
                /*
                if(probabilityOfClientRejection_2 == 0.0) {
                    updateMeanNumberOfClientsInQueue();
                    updateProbabilityOfClientRejected();
                    updateMeanWaitingTimeInQueue();
                    updateMeanServerBusyStatus();
                    meanWaitingTimeInQueue_2 = meanWaitingTimeInQueue;
                    meanNumberOfClientsInQueue_2 = meanNumberOfClientsInQueue;
                    meanServerStatusBusy_2 = meanServerStatusBusy;
                    probabilityOfClientRejection_2 = probabilityOfClientRejection;

                    lossProbabilityScoresList_2.add(probabilityOfClientRejection_2);
                }
                */
                break;
            case 10:
                /*
                if(probabilityOfClientRejection_10 == 0.0) {
                    updateMeanNumberOfClientsInQueue();
                    updateProbabilityOfClientRejected();
                    updateMeanWaitingTimeInQueue();
                    updateMeanServerBusyStatus();
                    meanWaitingTimeInQueue_10 = meanWaitingTimeInQueue;
                    meanNumberOfClientsInQueue_10 = meanNumberOfClientsInQueue;
                    meanServerStatusBusy_10 = meanServerStatusBusy;
                    probabilityOfClientRejection_10 = probabilityOfClientRejection;

                    lossProbabilityScoresList_10.add(probabilityOfClientRejection_10);
                }
                */
                break;
            case 100:
                /*
                if (probabilityOfClientRejection_100 == 0.0) {
                    updateMeanNumberOfClientsInQueue();
                    updateProbabilityOfClientRejected();
                    updateMeanWaitingTimeInQueue();
                    updateMeanServerBusyStatus();
                    meanWaitingTimeInQueue_100 = meanWaitingTimeInQueue;
                    meanNumberOfClientsInQueue_100 = meanNumberOfClientsInQueue;
                    meanServerStatusBusy_100 = meanServerStatusBusy;
                    probabilityOfClientRejection_100 = probabilityOfClientRejection;

                    lossProbabilityScoresList_100.add(probabilityOfClientRejection_100);
                }
                */
                break;
            case 250:
                /*
                if(probabilityOfClientRejection_250 == 0.0) {
                    updateMeanNumberOfClientsInQueue();
                    updateProbabilityOfClientRejected();
                    updateMeanWaitingTimeInQueue();
                    updateMeanServerBusyStatus();
                    meanWaitingTimeInQueue_250 = meanWaitingTimeInQueue;
                    meanNumberOfClientsInQueue_250 = meanNumberOfClientsInQueue;
                    meanServerStatusBusy_250 = meanServerStatusBusy;
                    probabilityOfClientRejection_250 = probabilityOfClientRejection;

                    lossProbabilityScoresList_250.add(probabilityOfClientRejection_250);
                }
                */
                break;
            case 500:
                /*
                if (probabilityOfClientRejection_500 == 0.0 ) {
                    updateMeanNumberOfClientsInQueue();
                    updateProbabilityOfClientRejected();
                    updateMeanWaitingTimeInQueue();
                    updateMeanServerBusyStatus();
                    meanWaitingTimeInQueue_500 = meanWaitingTimeInQueue;
                    meanNumberOfClientsInQueue_500 = meanNumberOfClientsInQueue;
                    meanServerStatusBusy_500 = meanServerStatusBusy;
                    probabilityOfClientRejection_500 = probabilityOfClientRejection;

                    lossProbabilityScoresList_500.add(probabilityOfClientRejection_500);
                }
                */
                break;
        }

    }

    private void printE2() throws InterruptedException {
        if (simulator_numberOfCompletedSingleSimulations == 1000){
            System.out.println("-----> Raport pośredni: ");

       /*
            try {

                File file = new File("test1.txt");
                FileWriter fileWriter = new FileWriter(file);

                fileWriter.write("Dla 2:");
                fileWriter.write(lossProbabilityScoresList_2.toString());

                fileWriter.write("Dla 10:");
                fileWriter.write(lossProbabilityScoresList_10.toString());

                fileWriter.write("Dla 100:");
                fileWriter.write(lossProbabilityScoresList_100.toString());

                fileWriter.write("Dla 250:");
                fileWriter.write(lossProbabilityScoresList_250.toString());

                fileWriter.write("Dla 500:");
                fileWriter.write(lossProbabilityScoresList_500.toString());

                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
*/

/*
            System.out.println("Dla 2:");
            System.out.println(lossProbabilityScoresList_2.toString());

            System.out.println("Dla 10:");
            System.out.println(lossProbabilityScoresList_10.toString());

            System.out.println("Dla 100:");
            System.out.println(lossProbabilityScoresList_100.toString());

            System.out.println("Dla 250:");
            System.out.println(lossProbabilityScoresList_250.toString());

            System.out.println("Dla 500:");
            System.out.println(lossProbabilityScoresList_500.toString());
            */

        }
    }

    protected void saveIndirectTotalValues(){
        /*
        total_meanWaitingTimeInQueue_2 += meanWaitingTimeInQueue_2;
        total_meanNumberOfClientsInQueue_2 += meanNumberOfClientsInQueue_2;
        total_meanServerStatusBusy_2 += meanServerStatusBusy_2;
        total_probabilityOfClientRejection_2 += probabilityOfClientRejection_2;

        meanWaitingTimeInQueue_2 = 0.0;
        meanNumberOfClientsInQueue_2 = 0.0;
        meanServerStatusBusy_2 = 0.0;
        probabilityOfClientRejection_2 = 0.0;

        total_meanWaitingTimeInQueue_10 += meanWaitingTimeInQueue_10;
        total_meanNumberOfClientsInQueue_10 += meanNumberOfClientsInQueue_10;
        total_meanServerStatusBusy_10 += meanServerStatusBusy_10;
        total_probabilityOfClientRejection_10 += probabilityOfClientRejection_10;

        meanWaitingTimeInQueue_10 = 0.0;
        meanNumberOfClientsInQueue_10 = 0.0;
        meanServerStatusBusy_10 = 0.0;
        probabilityOfClientRejection_10 = 0.0;

        total_meanWaitingTimeInQueue_100 += meanWaitingTimeInQueue_100;
        total_meanNumberOfClientsInQueue_100 += meanNumberOfClientsInQueue_100;
        total_meanServerStatusBusy_100 += meanServerStatusBusy_100;
        total_probabilityOfClientRejection_100 += probabilityOfClientRejection_100;

        meanWaitingTimeInQueue_100 = 0.0;
        meanNumberOfClientsInQueue_100 = 0.0;
        meanServerStatusBusy_100 = 0.0;
        probabilityOfClientRejection_100 = 0.0;

        total_meanWaitingTimeInQueue_250 += meanWaitingTimeInQueue_250;
        total_meanNumberOfClientsInQueue_250 += meanNumberOfClientsInQueue_250;
        total_meanServerStatusBusy_250 += meanServerStatusBusy_250;
        total_probabilityOfClientRejection_250 += probabilityOfClientRejection_250;

        meanWaitingTimeInQueue_250 = 0.0;
        meanNumberOfClientsInQueue_250 = 0.0;
        meanServerStatusBusy_250 = 0.0;
        probabilityOfClientRejection_250 = 0.0;

        total_meanWaitingTimeInQueue_500 += meanWaitingTimeInQueue_500;
        total_meanNumberOfClientsInQueue_500 += meanNumberOfClientsInQueue_500;
        total_meanServerStatusBusy_500 += meanServerStatusBusy_500;
        total_probabilityOfClientRejection_500 += probabilityOfClientRejection_500;

        meanWaitingTimeInQueue_500 = 0.0;
        meanNumberOfClientsInQueue_500 = 0.0;
        meanServerStatusBusy_500 = 0.0;
        probabilityOfClientRejection_500 = 0.0;
        */

    }

    private void printIndirectFinalResult() {
        /*
        switch(simulator_numberOfCompletedSingleSimulations) {
            case 30:
                // dla 30 bierzemy długość jednostkowej symulacji na 100
                simulator_meanWaitingTimeInQueue_30 =
                        (total_meanWaitingTimeInQueue_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanNumberOfClientsInQueue_30 =
                        (total_meanNumberOfClientsInQueue_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanServerStatusBusy_30 =
                        (total_meanServerStatusBusy_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_probabilityOfClientRejection_30 =
                        (total_probabilityOfClientRejection_100 / simulator_numberOfCompletedSingleSimulations);

                System.out.println("-----> Raport pośredni: ");
                System.out.println("n_all = " + 30);
                System.out.println("n_one = " + 100);
                System.out.println("Uśrednione prawdopodobieństwo utraty pakietów = " + simulator_probabilityOfClientRejection_30);
                System.out.println("Uśredniona zajętość serwera obsługi = " + simulator_meanServerStatusBusy_30);
                System.out.println("Uśredniona liczba pakietów w kolejce = " + simulator_meanNumberOfClientsInQueue_30);
                System.out.println("Uśredniony czas oczekiwania = " + simulator_meanWaitingTimeInQueue_30);

                simulator_meanWaitingTimeInQueue_30 = 0.0;
                simulator_meanNumberOfClientsInQueue_30 = 0.0;
                simulator_meanServerStatusBusy_30 = 0.0;
                simulator_probabilityOfClientRejection_30 =0.0;
                break;
            case 100:
                simulator_meanWaitingTimeInQueue_100 =
                        (total_meanWaitingTimeInQueue_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanNumberOfClientsInQueue_100 =
                        (total_meanNumberOfClientsInQueue_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanServerStatusBusy_100 =
                        (total_meanServerStatusBusy_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_probabilityOfClientRejection_100 =
                        (total_probabilityOfClientRejection_100 / simulator_numberOfCompletedSingleSimulations);

                System.out.println("-----> Raport pośredni: ");
                System.out.println("n_all = " + 100);
                System.out.println("n_one = " + 100);
                System.out.println("Uśrednione prawdopodobieństwo utraty pakietów = " + simulator_probabilityOfClientRejection_100);
                System.out.println("Uśredniona zajętość serwera obsługi = " + simulator_meanServerStatusBusy_100);
                System.out.println("Uśredniona liczba pakietów w kolejce = " + simulator_meanNumberOfClientsInQueue_100);
                System.out.println("Uśredniony czas oczekiwania = " + simulator_meanWaitingTimeInQueue_100);

                simulator_meanWaitingTimeInQueue_100 = 0.0;
                simulator_meanNumberOfClientsInQueue_100 = 0.0;
                simulator_meanServerStatusBusy_100 = 0.0;
                simulator_probabilityOfClientRejection_100 =0.0;
                break;
            case 250:
                simulator_meanWaitingTimeInQueue_250=
                        (total_meanWaitingTimeInQueue_2 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanNumberOfClientsInQueue_250 =
                        (total_meanNumberOfClientsInQueue_2 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanServerStatusBusy_250 =
                        (total_meanServerStatusBusy_2 / simulator_numberOfCompletedSingleSimulations);
                simulator_probabilityOfClientRejection_250 =
                        (total_probabilityOfClientRejection_2 / simulator_numberOfCompletedSingleSimulations);

                System.out.println("-----> Raport pośredni: ");
                System.out.println("n_all = " + 250);
                System.out.println("n_one = " + 2);
                System.out.println("Uśrednione prawdopodobieństwo utraty pakietów = " + simulator_probabilityOfClientRejection_250);
                System.out.println("Uśredniona zajętość serwera obsługi = " + simulator_meanServerStatusBusy_250);
                System.out.println("Uśredniona liczba pakietów w kolejce = " + simulator_meanNumberOfClientsInQueue_250);
                System.out.println("Uśredniony czas oczekiwania = " + simulator_meanWaitingTimeInQueue_250);

                simulator_meanWaitingTimeInQueue_250=
                        (total_meanWaitingTimeInQueue_10 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanNumberOfClientsInQueue_250 =
                        (total_meanNumberOfClientsInQueue_10 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanServerStatusBusy_250 =
                        (total_meanServerStatusBusy_10 / simulator_numberOfCompletedSingleSimulations);
                simulator_probabilityOfClientRejection_250 =
                        (total_probabilityOfClientRejection_10 / simulator_numberOfCompletedSingleSimulations);

                System.out.println("-----> Raport pośredni: ");
                System.out.println("n_all = " + 250);
                System.out.println("n_one = " + 10);
                System.out.println("Uśrednione prawdopodobieństwo utraty pakietów = " + simulator_probabilityOfClientRejection_250);
                System.out.println("Uśredniona zajętość serwera obsługi = " + simulator_meanServerStatusBusy_250);
                System.out.println("Uśredniona liczba pakietów w kolejce = " + simulator_meanNumberOfClientsInQueue_250);
                System.out.println("Uśredniony czas oczekiwania = " + simulator_meanWaitingTimeInQueue_250);

                simulator_meanWaitingTimeInQueue_250=
                        (total_meanWaitingTimeInQueue_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanNumberOfClientsInQueue_250 =
                        (total_meanNumberOfClientsInQueue_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanServerStatusBusy_250 =
                        (total_meanServerStatusBusy_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_probabilityOfClientRejection_250 =
                        (total_probabilityOfClientRejection_100 / simulator_numberOfCompletedSingleSimulations);

                System.out.println("-----> Raport pośredni: ");
                System.out.println("n_all = " + 250);
                System.out.println("n_one = " + 100);
                System.out.println("Uśrednione prawdopodobieństwo utraty pakietów = " + simulator_probabilityOfClientRejection_250);
                System.out.println("Uśredniona zajętość serwera obsługi = " + simulator_meanServerStatusBusy_250);
                System.out.println("Uśredniona liczba pakietów w kolejce = " + simulator_meanNumberOfClientsInQueue_250);
                System.out.println("Uśredniony czas oczekiwania = " + simulator_meanWaitingTimeInQueue_250);

                simulator_meanWaitingTimeInQueue_250=
                        (total_meanWaitingTimeInQueue_250 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanNumberOfClientsInQueue_250 =
                        (total_meanNumberOfClientsInQueue_250 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanServerStatusBusy_250 =
                        (total_meanServerStatusBusy_250 / simulator_numberOfCompletedSingleSimulations);
                simulator_probabilityOfClientRejection_250 =
                        (total_probabilityOfClientRejection_250 / simulator_numberOfCompletedSingleSimulations);

                System.out.println("-----> Raport pośredni: ");
                System.out.println("n_all = " + 250);
                System.out.println("n_one = " + 250);
                System.out.println("Uśrednione prawdopodobieństwo utraty pakietów = " + simulator_probabilityOfClientRejection_250);
                System.out.println("Uśredniona zajętość serwera obsługi = " + simulator_meanServerStatusBusy_250);
                System.out.println("Uśredniona liczba pakietów w kolejce = " + simulator_meanNumberOfClientsInQueue_250);
                System.out.println("Uśredniony czas oczekiwania = " + simulator_meanWaitingTimeInQueue_250);

                simulator_meanWaitingTimeInQueue_250=
                        (total_meanWaitingTimeInQueue_500 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanNumberOfClientsInQueue_250 =
                        (total_meanNumberOfClientsInQueue_500 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanServerStatusBusy_250 =
                        (total_meanServerStatusBusy_500 / simulator_numberOfCompletedSingleSimulations);
                simulator_probabilityOfClientRejection_250 =
                        (total_probabilityOfClientRejection_500 / simulator_numberOfCompletedSingleSimulations);

                System.out.println("-----> Raport pośredni: ");
                System.out.println("n_all = " + 250);
                System.out.println("n_one = " + 500);
                System.out.println("Uśrednione prawdopodobieństwo utraty pakietów = " + simulator_probabilityOfClientRejection_250);
                System.out.println("Uśredniona zajętość serwera obsługi = " + simulator_meanServerStatusBusy_250);
                System.out.println("Uśredniona liczba pakietów w kolejce = " + simulator_meanNumberOfClientsInQueue_250);
                System.out.println("Uśredniony czas oczekiwania = " + simulator_meanWaitingTimeInQueue_250);

                simulator_meanWaitingTimeInQueue_250 = 0.0;
                simulator_meanNumberOfClientsInQueue_250 = 0.0;
                simulator_meanServerStatusBusy_250 = 0.0;
                simulator_probabilityOfClientRejection_250 =0.0;
                break;
            case 500:
                simulator_meanWaitingTimeInQueue_500 =
                        (total_meanWaitingTimeInQueue_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanNumberOfClientsInQueue_500 =
                        (total_meanNumberOfClientsInQueue_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanServerStatusBusy_500 =
                        (total_meanServerStatusBusy_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_probabilityOfClientRejection_500 =
                        (total_probabilityOfClientRejection_100 / simulator_numberOfCompletedSingleSimulations);

                System.out.println("-----> Raport pośredni: ");
                System.out.println("n_all = " + 500);
                System.out.println("n_one = " + 100);
                System.out.println("Uśrednione prawdopodobieństwo utraty pakietów = " + simulator_probabilityOfClientRejection_500);
                System.out.println("Uśredniona zajętość serwera obsługi = " + simulator_meanServerStatusBusy_500);
                System.out.println("Uśredniona liczba pakietów w kolejce = " + simulator_meanNumberOfClientsInQueue_500);
                System.out.println("Uśredniony czas oczekiwania = " + simulator_meanWaitingTimeInQueue_500);

                simulator_meanWaitingTimeInQueue_500 = 0.0;
                simulator_meanNumberOfClientsInQueue_500 = 0.0;
                simulator_meanServerStatusBusy_500 = 0.0;
                simulator_probabilityOfClientRejection_500 =0.0;
                break;
            case 1000:
                simulator_meanWaitingTimeInQueue_1000 =
                        (total_meanWaitingTimeInQueue_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanNumberOfClientsInQueue_1000 =
                        (total_meanNumberOfClientsInQueue_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_meanServerStatusBusy_1000 =
                        (total_meanServerStatusBusy_100 / simulator_numberOfCompletedSingleSimulations);
                simulator_probabilityOfClientRejection_1000 =
                        (total_probabilityOfClientRejection_100 / simulator_numberOfCompletedSingleSimulations);

                System.out.println("-----> Raport pośredni: ");
                System.out.println("n_all = " + 1000);
                System.out.println("n_one = " + 100);
                System.out.println("Uśrednione prawdopodobieństwo utraty pakietów = " + simulator_probabilityOfClientRejection_1000);
                System.out.println("Uśredniona zajętość serwera obsługi = " + simulator_meanServerStatusBusy_1000);
                System.out.println("Uśredniona liczba pakietów w kolejce = " + simulator_meanNumberOfClientsInQueue_1000);
                System.out.println("Uśredniony czas oczekiwania = " + simulator_meanWaitingTimeInQueue_1000);

                simulator_meanWaitingTimeInQueue_100 = 0.0;
                simulator_meanNumberOfClientsInQueue_100 = 0.0;
                simulator_meanServerStatusBusy_100 = 0.0;
                simulator_probabilityOfClientRejection_100 =0.0;
                break;
        }
        */
    }

    /* debug methodes */
    private void printDebugStatistics(){
        ///* wypisz po kolei jakie byly zdarzenia */
        //System.out.println(eventsListsDebug.toString());
        //
        ///* wypisz po kolei jakie byly zdarzenia */
        //System.out.println(simulationTimeListsDebug.toString());
        //
        //System.out.println("1111111111111");
        // /* wypisz czasy */
        //streams.get(0).printLists();
        //
        //System.out.println("2222222222222");
        ///* wypisz czasy */
        //streams.get(1).printLists();
        //System.out.println(streams.get(1).getStartTime());
        //
        //System.out.println("333333333333");
        ///* wypisz czasy */
        //streams.get(2).printLists();
        //System.out.println(streams.get(2).getStartTime());
    }
    /*-----------------------------------------------------------------------------------*/
}

