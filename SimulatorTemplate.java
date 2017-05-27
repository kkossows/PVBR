import java.util.*;

/**
 * Klasa będąca szablonem wspólnych metod i zmiennych dla każdego typu symulacji;
 * - zakładamy obecnośc tylko jednego serwera obsługi;
 */
public abstract class SimulatorTemplate {

    /* główne zmienne symulacji */
    protected double simulationTime;
    protected double previousEventTime;
    protected boolean finishTime = false;
    protected boolean loadError = false;

    public enum SimulationType {
        MANUAL, FROM_FILE
    }

    protected int queueSizeLimit;
    protected int clientsInTheQueue;
    protected SimulationType simulationType;
    protected Event currentEvent;

    /* zmienne związane z napływami klientów */
    protected int numberOfIncomingStreams;
    protected boolean eachStreamsTheSame;
    protected enum StreamsType {
        CONSTANT, DIFFERENT
    }

    /* zmienne zwiazane z zakończeniem symulacji */
    protected enum EndType {
        NUMBER_OF_DELAYS,
        NUMBER_OF_SERVED_CLIENTS,
        NUMBER_OF_ON_OFF_STATES
    }
    protected EndType simulationEndParameterType;
    protected int simulationEndParameterValue;

    protected int numberOfDelays;
    protected int numberOfServedClients;
    protected int numberOfClientsRejected;
    protected int numberOfCompletedOnOffStates;

    /* zmienne związane z serwerem obsługi */
    protected ServerStateType serverState;
    protected enum ServerStateType {
        SERVER_STATE_IDLE, SERVER_STATE_BUSY
    }

    /* zmienne związane z raportowaniem */
    protected enum ReportType {
        CURRENT, FINAL
    }
    protected int reportIntervalNumber;

    /* listy napływów, zdarzeń oraz czasów poszczególnych klientów w kolejce */
    protected List<Stream> streams;
    protected List<Event> events;
    protected List<ClientInQueueTime> clientsInTheQueueArrivalsTimes;
    protected List<Double> streamsStartTimes;

    /* statystyki */
    protected double sumOfDelays;
    protected double areaNumberClientsInQueue;
    protected double areaServerStatusBusy;

    protected double meanWaitingTimeInQueue;
    protected double meanNumberOfClientsInQueue;
    protected double meanServerStatusBusy;
    protected double probabilityOfClientRejection;

    /* liczniki zdarzeń */
    protected int arrivalEventsCounter;
    protected int departureEventsCounter;

    protected int arrivalGeneratedEventsCounter;
    protected int departureGeneratedEventsCounter;

    protected List<String> eventsListsDebug;
    protected List<Double> simulationTimeListsDebug;

    /* inne zmienne */
    int indexOfLastOneInStreamList;
    Random randomGenerator;


    /**
     * Konstruktor klasy
     * - pozwala na wybranie rodzaju wczytywania danych potrzebnych do konfiguracji symulacji;
     */
    public SimulatorTemplate() {

        /*
        System.out.println("--------------------->  <-----------------------");
        System.out.println("--->Wybierz rodzaj wczytywania danych symulacji:            <---");
        System.out.println("-->     1 - dane z pliku                                    <--");
        System.out.println("-->     2 - ręczne wprowadzanie danych                      <--");

        Scanner scanner = new Scanner(System.in);
        int value = scanner.nextInt();
        if (value == 1)
            simulationType = SimulationType.FROM_FILE;
        else if (value == 2)
            simulationType = SimulationType.MANUAL;
        else {
            System.out.println("--> Nieznany rodzaj wczytywania danych! <---");
            loadError = true;
            return;
        }
        */

        simulationType = SimulationType.FROM_FILE;
    }


    /* Metody algorytmu inicjalizującego */
    /**
     * Metoda odpowiada za inicjalizację zmiennych startowych symulacji.
     * Zmienne te będą się zmieniać w trakcie symulacji i nie zależą od danych wejściowych;
     *
     * Metoda powinna być wywołana na końcu każdej nadpisanej metody initialize klasy potomnej;
     */
    protected void initialize() {
        /* inicjalizacja zmiennych */
        simulationTime = 0.0;
        previousEventTime = 0.0;

        sumOfDelays = 0.0;
        areaNumberClientsInQueue = 0.0;
        areaServerStatusBusy = 0.0;

        meanWaitingTimeInQueue = 0.0;
        meanNumberOfClientsInQueue = 0.0;
        meanServerStatusBusy = 0.0;
        probabilityOfClientRejection = 0.0;

        /* inicjalizacja list */
        events = new LinkedList<>();
        clientsInTheQueueArrivalsTimes = new LinkedList<>();

        /* inicjalizacja początkowego stanu serwera oraz zerowej wartości czekających klientów */
        serverState = ServerStateType.SERVER_STATE_IDLE;
        clientsInTheQueue = 0;
        currentEvent = null;

        /* reszta parametrów */
        randomGenerator = new Random();
        arrivalGeneratedEventsCounter = 0;
        departureGeneratedEventsCounter = 0;
        arrivalEventsCounter = 0;
        departureEventsCounter = 0;
        numberOfDelays = 0;
        numberOfServedClients = 0;
        numberOfClientsRejected = 0;

        /* debbuging */
        eventsListsDebug = new LinkedList<>();
        simulationTimeListsDebug = new LinkedList<>();
    }
    /**
     * Abstrakcyjna metoda służąca do ręcznego wczytywania odpowiednich dla danego typu symulacji parametrów;
     */
    protected abstract void manualInitialize();
    /**
     * Abstrakcyjna metoda służąca do wczytywania odpowiednich dla danego typu symulacji parametrów z pliku;
     */
    protected abstract void fromFileInitialize();
    /**
     * Metoda odpowiedzialna za wczytanie typu oraz wartoś parametru kończącego symulację.
     * (wykorzystywana w każdej ręcznej metodzie wprowadzania parametrów symulacji)
     * @return Flaga oznaczająco powodzenie operacji, lub brak powodzenia (wystąpienie błedu, false);
     */
    protected boolean initializeSimulationEnd() {
        Scanner scanner = new Scanner(System.in);
        /* Wczytanie parametru kończącego symulację */
        System.out.println("--> Podaj typ parametru kończącego symulację:");
        System.out.println("--->     1 - liczba opóźnień");
        System.out.println("--->     2 - liczba obsłużonych klientów");
        switch (scanner.nextInt()) {
            case 1:
                simulationEndParameterType = EndType.NUMBER_OF_DELAYS;
                System.out.println("--> Podaj liczbe opóźnień kończącą symulację:");
                simulationEndParameterValue = scanner.nextInt();
                return true;
            case 2:
                simulationEndParameterType = EndType.NUMBER_OF_SERVED_CLIENTS;
                System.out.println("--> Podaj liczbe obsłużonych klientów kończącą symulację:");
                simulationEndParameterValue = scanner.nextInt();
                return true;
            default:
                System.out.println("--> Błąd: Podano nieznany typ parametru kończącego symulację!");
                return false;
        }
    }
    /**
     * Metoda służąca do wyliczania czasów startowych strumieni napływów klientów.
     * - opiera się na losowaniu wartości z rozkładu jednostajnego i przemnożeniu jej przez przedziałc czasowy;
     *
     * Metoda należy uruchamiać jedynie w przypadku, gdy liczba strumieni jest większa od 1;
     *
     * @param randomTimeRange   Przedział czasowy (od 0 do randomTimeRange), z którego losowane sa wartości.
     */
    protected void initializeFlowsStartTimes(double randomTimeRange) {
        Random random = new Random();
        double newStartTime;
        for (int i=1; i<numberOfIncomingStreams; i++) {
            newStartTime = (random.nextDouble() * randomTimeRange);

            /* jeżeli taki czas znajduje się już w liście, to wylosuj nowy */
            while (streamsStartTimes.contains(newStartTime))
                newStartTime = (random.nextDouble() * randomTimeRange);

            streamsStartTimes.add(newStartTime);
        }
    }
    /**
     * Abstrakcyjna metoda służąca do aktualizacji indywidualnych parametrów symulaci
     * ( wywoływana jednokrotnie, w momencie inicjalizacji symulacji )
     */
    protected abstract void calculateIndividualSimulationParameters();


    /* Metody algorytmu czasowego */
    /**
     * Algorytm czasowy
     * - sortuje listę zdarzeń;
     * - zapisuje obecny czas symulacji do zmiennej oznaczającej czas wystąpienia poprzedniego zdarzenia;
     * - przypisuje pierwsze zdarzenie z uporzadkowanej listy do bieżącego zdarzenia
     * - zwiększa czas symulacji na czas wystąpienia tego zdarzenia;
     * - usuwa zdarzenie z listy zdarzeń;
     */
    protected void timing() {
        /* Przesortuj listę */
        Collections.sort(events);

        previousEventTime = simulationTime;
        currentEvent = events.get(0);

        ///* debugging */
        //eventsListsDebug.add(currentEvent.type.toString());
        //simulationTimeListsDebug.add(currentEvent.time);
        ///* end of debugging */

        simulationTime = currentEvent.time;
        events.remove(0);
    }


    /* Metody algorytmów konkretnych zdarzeń */
    /**
     * Algorytm obsługujący zdarzenie przybycia klienta:
     *
     * Serwer jest zajęty:
     * - jeżeli kolejka jest przepełniona, potraktuj klienta jako odrzuconego;
     * - jezeli kolejka nie jest pełna, oblicz statystyki za poprzedni okres oraz dodaj klienta do kolejki;
     *
     * Serwer jest wolny:
     * - zmieniam status serwera na zajęty;
     * - zwiększam liczbe opóźnień o jeden;
     * - jeżeli klient trafi na wolny serwer, to jego opóźnienie wynosi 0;
     * - zaplanuj zdarzenie zakończenia obsługi;
     *
     * - wygeneruj nowy odstęp między klientami (czas przybycia klienta) zgodnie z zadanym rozkładem;
     * - dodaj nowe zdarzenie typu ARRIVAL do listy zdarzeń;
     * - czas zdarzenia to czas symulacji powiększony o nowowylosowany odstęp pomiedzy klientami;
     * - przesortuj zdarzenia (zdarzenie o najmniejszym czasie powinno znaleźć się na początku);
     */
    private void arrival() {
        /* zaplanuj kolejne zdarzenie przybycia */
        generateNextArrivalEvent(currentEvent.source);


        if( serverState == ServerStateType.SERVER_STATE_BUSY ) {
            /* Serwer zajęty */
            /* Statystyki za poprzedni okres */
            updateAreaNumberClientsInQueue();
            updateAreaServerStatusBusy();

            if ( clientsInTheQueue == queueSizeLimit ) {
                /* odrzucam klienta */
                numberOfClientsRejected++;

            }
            else {
                /* Dodanie klienta do kolejki */
                clientsInTheQueue++;
                clientsInTheQueueArrivalsTimes.add(new ClientInQueueTime(
                        currentEvent.time,
                        currentEvent.source
                ));
            }
        }
        else if ( serverState == ServerStateType.SERVER_STATE_IDLE ) {
            /* Serwer wolny */
            serverState = ServerStateType.SERVER_STATE_BUSY;

            /* Statystyki */
            numberOfDelays++;
            // jeżeli serwer był wolny to klient nie czekał więc nie zwiększamy zmiennej sumOfDelays

            /* Zaplanuj zdarzenie zakończenia obsługi */
            generateNextDepartureEvent(currentEvent.source);
        }
    }
    /**
     * Algorytm obsługujący zdarzenie zakończenia obsługi klienta:
     * - zwiększ liczbe obsłużonych klientów o jeden;
     *
     * Kolejka jest pusta:
     * - zmień status serwera na wolny;
     * - oblicz statystyki związane z czasem zajętości serwera;
     *
     * Kolejka nie jest pusta (są oczekujący klienci):
     * - bierzemy klienta do obsługi;
     * - zmniejsz o 1 liczbe klientów w kolejce;
     * - oblicz statystyki (czas oczekiwania klienta, ;
     * - przesuń klientów w kolejce o jeden do przodu;
     * - zaplanuj nowe zdarzenie zakończenia obsługi;
     */
    protected void departure() {
        /* zwiększ liczbę obsłużonych klientów */
        numberOfServedClients++;

        /* Oblicz statystyki związane z zajętością serwera za poprzedni okres działania */
        updateAreaServerStatusBusy();

        if ( clientsInTheQueue == 0 ) {
            /* Brak oczekujących klientów w kolejce */
            serverState = ServerStateType.SERVER_STATE_IDLE;
        }
        else {
            /* W kolejce znajduje się co najmniej jeden oczekujący klient */
            /* Bierzemy pierwszego z kollejki do obsługi */

            /* Statystyki */
            numberOfDelays++;
            updateDeltaWaitingTimeInQueue();
            updateAreaNumberClientsInQueue();

            /* Planuje zdarzenie zakończenia obsługi */
            generateNextDepartureEvent(clientsInTheQueueArrivalsTimes.get(0).sourceStream);

            /* Przesuwam klientów w kolejce */
            clientsInTheQueue--;
            clientsInTheQueueArrivalsTimes.remove(0);
        }
    }


    /* Metody algorytmu generowania nowych zdarzeń */
    /**
     * Metoda generująca nowe zdarzenie typu przyjście nowego klienta do systemu;
     * - uzupełnia zmienną określającą odstęp między klientami napływającymi do systemu
     * - jeżeli napływy były podane jawnie w pliku, to odczytuje kolejny z nich;
     * - jeżel nie, to losuje wartośc z zadanym rozkładem i zapisuje ją do listy statystyk;
     * - tworzy i dodaje nowe zdarzenie do listy zdarzeń;
     * - zwiększa licznik wygenerownych zdarzęn typu ARRIVAL
     *  @param source Stumień, z którego pochodzi klient napływajacy.
     */
    protected void generateNextArrivalEvent(Stream source) {
        double newDistanceBetweenInflows;
        double eventTime;

        /* uzupełniam zmienną okręslającą odstęp między klientami napływającymi do systemu*/
        /* (odstęp między klientem, na którym wykonano już zdarzenie arrival a nastepnym w danym strumieniu */
        if (source.getInflowsDefineFlag()) {
            /* czasy zostały podane jawnie w pliku */
            newDistanceBetweenInflows = source.getOneInterval(arrivalGeneratedEventsCounter);
        }
        else {
            /* czasy nie zostały podane, należy je wylosować */
            newDistanceBetweenInflows = generateRandomValue
                    (source.getArrivalDistribution(), source.getArrivalDistributionParameter());
            source.addInterval(newDistanceBetweenInflows);
        }

        /* dodaję nowe zdarzenie do listy zdarzeń */
        eventTime = ( simulationTime + newDistanceBetweenInflows );
        events.add(new Event(Event.EventType.ARRIVAL, eventTime, source));

        /* zwiększ licznik oznaczajacy liczbe wygenerowanych zdarzeń typu arrival */
        arrivalGeneratedEventsCounter++;
    }
    /**
     * Metoda generująca nowe zdarzenie typu zakończenia obsługi klienta;
     * - uzupełnia zmienną określającą czas obsługi klienta;
     * - jeżeli czasy były podane jawnie w pliku, to odczytuje kolejny z nich;
     * - jeżel nie, to losuje wartośc z zadanym rozkładem i zapisuje ją do listy statystyk;
     * - tworzy i dodaje nowe zdarzenie do listy zdarzeń;
     * - zwiększa licznik wygenerownych zdarzęn typu DEPARTURE
     * @param source Strumień, z którego pochodzi klient obsłużony.
     */
    protected void generateNextDepartureEvent(Stream source) {
        double newServiceTime;
        double eventTime;

        /* uzupełnia zmienną określającą czas obsługi klienta */
        if( source.getServiceTimeDefineFlag() ) {
            /* czasy zostały podane jawnie w pliku */
            newServiceTime = source.getOneServiceTime(departureGeneratedEventsCounter);
        }
        else {
            /* czasy nie zostały podane, należy je wylosować */
            newServiceTime = generateRandomValue
                    (source.getDepartureDistribution(), source.getDepartureDistributionParameter());
            source.addServiceTime(newServiceTime);
        }

        /* dodaję nowe zdarzenie do listy zdarzeń */
        eventTime = (simulationTime + newServiceTime);
        events.add(new Event(Event.EventType.DEPARTURE, eventTime, source));

        /* zwiększ licznik oznaczajacy liczbe wygenerowanych zdarzeń typu arrival */
        departureGeneratedEventsCounter++;
    }
    /**
     * Metoda służąca do generowania liczb losowych zgodnych z zadanym rozkładem;
     *
     * @param distributionType      Typ rozkładu (np: wykładniczy).
     * @param distributionParameter Parametr rozkładu (np. lambda jako intensywnośc napływu).
     * @return Wartość liczby losowej zadanej zgodnie z podanym rozkładem.
     */
    protected double generateRandomValue(Stream.DistributionTypes distributionType, double distributionParameter) {
        double randomNumber = 0.0;
        switch (distributionType) {
            case EXPONENTIAL:   // Math.log(1-rand.nextDouble())/(-distributionParameter);
                double counting = Math.log(1-randomGenerator.nextDouble());
                randomNumber = ( counting / distributionParameter);
                randomNumber = ( randomNumber / -1.0 );
                break;
        }
        return randomNumber;
    }


    /* Metody algorytmu raportowania wyników przebiegu symulacji */
    /**
     * Metoda odpowiedzialna za uruchomienie odpowiedniego algorytmu związanego z danym typem raportu;
     * - jeżeli interwał czasowy wynosi 0 to wyświetlaj raport pośredni za każdym razem;
     * - jeżeli suma zdarzeń jest podzielna przez interwał raportowania to wypisz  raport pośredni;
     * @param reportType Wskazuje typ raportu (CURRENT, bieżacy status systemu; FINAL, ostateczny stan systemu);
     */
    protected void report(ReportType reportType) {
        switch (reportType) {
            case CURRENT:
                if ( reportIntervalNumber == 0 )
                    printCurrentReport();
                else
                    /* Jeżli suma zdarzeń jest podzielna przez interwał to wypisz raport */
                    if( (arrivalEventsCounter + departureEventsCounter) % reportIntervalNumber == 0 ) {
                        printCurrentReport();
                    }
                break;
            case FINAL:
                printFinalReport();
                break;
            default:
                break;
        }
    }


    /* Metody zarządzające symulacją + główna pętla symulacji */
    /**
     * Publiczna metoda służąca do rozpoczęcia symulacji;
     * - wypisuje komunikat o rozpoczęciu symulacji;
     * - uruchamia główna pętle symulacji;
     */
    public void start() {
        //System.out.println("---> Rozpoczęcie symulacji... ");


        /* Odczytujemy liczbe ukończonych stanów z najpóźniej zaczynającego strumienia */
        for (int i = 1; i < streams.size(); i++) {
            if (streams.get(i).isLastOne()) {
                indexOfLastOneInStreamList = i;
            }
        }

        simulationLoop();
    }
    /**
     * Główna pętla symulacji:
     * - wykonuje się dopóki flaga finishTime jest równa false;
     * - uruchamia algorytm timing();
     * - zaleznie od typu aktualnego zdarzenia, wybiera odpowiedni algorytm oraz zwieksza licznik wystąpień danego zdarzenia;
     * - sprawdza warunek końcowy symulacji;
     * - jeżeli warunek jest spełniony to kończy symulację oraz wyświetla statystyki końcowe;
     * - jezeli warunek nie jest spełniony, wyświetla statystyki pośrednie i przechodzi do nastepnej iteracji;
     */
    protected void simulationLoop() {
        while (!finishTime) {
            timing();
            switch (currentEvent.type) {
                case ARRIVAL:
                    arrival();
                    arrivalEventsCounter++;
                    break;
                case DEPARTURE:
                    departure();
                    departureEventsCounter++;
                    break;
                default:
                    break;
            }

            /* Sprawdzamy warunek końcowy */
            switch (simulationEndParameterType) {
                case NUMBER_OF_DELAYS:
                    if (numberOfDelays == simulationEndParameterValue) finishTime = true;
                    break;
                case NUMBER_OF_SERVED_CLIENTS:
                    if (numberOfServedClients == simulationEndParameterValue) finishTime = true;
                    break;
                case NUMBER_OF_ON_OFF_STATES:
                    /* zaktualizuj liczbe obsłużonych stanów ON/OFF */
                    numberOfCompletedOnOffStates = streams.get(indexOfLastOneInStreamList).getNumberOfCompletedOnOffStates();

                    /* sprawdz, czy to koniec symulacji */
                    if (simulationEndParameterValue == numberOfCompletedOnOffStates)
                        finishTime = true;

                    /*------------------------------------------*/
                    //saveIndirectValues(numberOfCompletedOnOffStates);
                    /*------------------------------------------*/
                    break;
                default:
                    break;
            }

            /* Wypisz raport pośredni */
            //if (!finishTime)
                //report(ReportType.CURRENT);
        }

        /* Zakończono symulację, wypisz raport końcowy */
        report(ReportType.FINAL);
    }
    /**
     * Publiczna metoda służaca do zakończenia symulacji z zewnątrz;
     * - wypisuje komunikat o przerwaniu symulacji
     * - ustawia flagę końca symulacji na true;
     */
    public void stop() {
        System.out.println("--> Symulacja przerwana.........................");
        finishTime = true;
    }


    /* Metody statystyczne */

    /**
     * Metoda odpowiedzialna za obliczanie statystyk związancyh z liczbą klientów w okresie deltaT
     */
    private void updateAreaNumberClientsInQueue() {
        double delta_areaNumberClientsInQueue = (simulationTime - previousEventTime);
        delta_areaNumberClientsInQueue = (delta_areaNumberClientsInQueue * clientsInTheQueue);
        areaNumberClientsInQueue += delta_areaNumberClientsInQueue;
    }
    /**
     * Metoda odpowiedzialna za aktualizacje statystyk związanych z zajętością serwera w okresie deltaT
     */
    private void updateAreaServerStatusBusy() {
        /* zakładamy stan serwera równy 1, wiec nie musimy zbędnie mnożyć delty przez 1 */
        double delta_areaServerStatusBusy = (simulationTime - previousEventTime);
        areaServerStatusBusy += delta_areaServerStatusBusy;
    }
    /**
     * Metoda odpowiedzialna za aktualizację statystyk związanych z czasem oczekiwania w chwili deltT
     */
    private void updateDeltaWaitingTimeInQueue() {

        double delta = (simulationTime - clientsInTheQueueArrivalsTimes.get(0).time);
        sumOfDelays += delta;
    }

    /**
     * Metoda wyliczająca statystykę związaną ze średnią liczbą klientów w kolejce;
     * - średnia liczba klientów w koljece jest wynikiem dzielenia sumy liczby kientów w kolejce w każdej chwili czasowej przez czas symulacji;
     */
    protected void updateMeanNumberOfClientsInQueue(){
        meanNumberOfClientsInQueue = (areaNumberClientsInQueue / simulationTime);
    }
    /**
     * Metoda wyliczająca statystykę związaną ze średnią zajętością serwera;
     * - średnia zajętośc jest wynikiem dzielenia sumy funkcji zajętości serwera w każdj chwili czasowej przez czas symulacji;
     */
    protected void updateMeanServerBusyStatus() {
        meanServerStatusBusy = (areaServerStatusBusy / simulationTime);
    }
    /**
     * Metoda wyliczająca statystykę związaną ze średnim czasem oczekiwania klientów w kolejce;
     * - średni czas czekania to wynik dzielenia całkowitego opóźnienia przez liczbę opóźnień;
     */
    protected void updateMeanWaitingTimeInQueue(){
        meanWaitingTimeInQueue = (sumOfDelays / numberOfDelays);
    }
    /**
     * Metoda odpowiedzialna za aktualizowanie prawdopodobieństwa odrzucenia klienta ze względu na przepełnienie kolejki;
     * - prawdopodobieństwo jest liczone jako wynik dzielenia liczby klientów odrzuconych do liczby kleintów przybyłych;
     */
    protected void updateProbabilityOfClientRejected() {
        probabilityOfClientRejection = ((double)numberOfClientsRejected / (double)arrivalGeneratedEventsCounter);
    }


    /* Metody raportowania */
    /**
     * Metoda odpowiedzialna za wypisanie raportu bieżącego stanu systemu:
     * - wypisuje główne parametry symulacji oraz główne statystyki końcowe;
     */
    private void printCurrentReport() {
        /* obliczenie końcowych wyników */
        updateMeanNumberOfClientsInQueue();
        updateProbabilityOfClientRejected();
        updateMeanWaitingTimeInQueue();
        updateMeanServerBusyStatus();

        System.out.println("...........................................................");
        System.out.println("--> Raport pośredni dla symulacji");
        printSimulationParameters();
        printMainStatisticsValues();
        System.out.println("...........................................................");

        /* wstrzymanie symulacji na 2 sekundy w celu odczytu raportu */
        try {
            Thread.sleep(2000);
        } catch(InterruptedException ex) {
        }
    }
    /**
     *  Metoda odpowiedzialna za wypisanie raportu końcowego
     *  - raport składa się z parametrów wejsciowych symulacji, statystyk symulacji oraz wyników symulacji;
     *  - dodatkowow wypisuje parametry strumieni oraz dane charakterystyczne dla danego typu symulacji;
     */
    private void printFinalReport() {
        /* obliczenie końcowych wyników */
        updateMeanNumberOfClientsInQueue();
        updateProbabilityOfClientRejected();
        updateMeanWaitingTimeInQueue();
        updateMeanServerBusyStatus();

        System.out.println("...........................................................");
        System.out.println("---------> Raport końcowy:");

        System.out.println("-------> Parametry wejściowe symulacji:");
        if( simulationType == SimulationType.FROM_FILE ) {
            printConfigurationFromFile();
        }
        else if ( simulationType == SimulationType.MANUAL ) {
            printManualConfiguration();
        }

        System.out.println("-----> Wynik symulacji:");
        printSimulationParameters();
        printSimulationDetailParameters();
        printMainStatisticsValues();
        printMainStatisticsDetails();

        System.out.println("-----> Dane na temat strumieni...");
        printDetailsStatisticsAboutStreams();

        System.out.println("-----> Dane charakterystyczne dla danego typu symulacji...");
        printIndividualSimulationParameters();
    }
    /**
     * Metoda odpowiedzialna jest za wypisywanie wszystkich najważniejszych wyników symulacyjnych;
     * - średni czas oczekiwania w kolejce przez n klientów;
     * - średnia liczba klientów w klejce;
     * - średnia zajętość serwera;
     * - prawdopodobieństwo odrzucenia klienta ze względu na zapełnioną kolejkę;
     */
    protected void printMainStatisticsValues() {
        System.out.println("...........................................................");
        System.out.println("-----> Rzeczywiste statystyki symulacji:");

        System.out.print("-------> Średni czas oczekiwania w kolejce przez n klientów: ");
        System.out.println(meanWaitingTimeInQueue);

        System.out.print("-------> Średna liczba klientów w kolejce: ");
        System.out.println(meanNumberOfClientsInQueue);

        System.out.print("-------> Średnia zajętość serwera: ");
        System.out.println(meanServerStatusBusy);

        System.out.print("-------> Prawdopodobieństwo odrzucenia klienta: ");
        System.out.println(probabilityOfClientRejection);
    }
    /**
     * Metoda odpowiedzialna za wypisanie szczegółów na temat głównych statystyk;
     * - sumaryczne opóźnienie wszystkich klientów;
     * - obszar pod wykresem funkcji liczby klientów w kolejce w czasie;
     * - obszar pod wykresem funkcji zajętości serwera w czasie;
     */
    protected void printMainStatisticsDetails() {
        System.out.println("...........................................................");
        System.out.println("-----> Szczegóły statystyk:");

        System.out.print("-------> Sumaryczne opóźnienie wszystkich klientów: ");
        System.out.println(sumOfDelays);

        System.out.print("-------> Obszar pod wykresem funkcji liczby klientów w kolejce w czasie: ");
        System.out.println(areaNumberClientsInQueue);

        System.out.print("-------> Obszar pod wykresem funkcji zajętości serwera w czasie: ");
        System.out.println(areaServerStatusBusy);
    }
    /**
     * Metoda odpowiedzialna za wypisywanie parametrów symulacji, takich jak:
     * - czas symulacji;
     * - liczba klientów wziętych do obsługi (liczba opóźnień);
     * - liczba obsłużonych klientów;
     * - liczba odrzuconych klientów;
     */
    protected void printSimulationParameters() {
        System.out.println("...........................................................");
        System.out.println("-----> Parametry symulacji:");

        System.out.print("-------> Czas symulacji: ");
        System.out.println(simulationTime);

        System.out.print("-------> Liczba klientów wziętych do obsługi (liczb opóźnień): ");
        System.out.println(numberOfDelays);

        System.out.print("-------> Liczba obsłużonych klientów: ");
        System.out.println(numberOfServedClients);

        System.out.print("-------> Liczba odrzuconych klientów: ");
        System.out.println(numberOfClientsRejected);

    }
    /**
     * - liczba powstałych zdarzeń typu ARRICAL;
     * - liczba obsłużonych zdarzeń typu ARRIVAL;
     * - lliczba powstałych zdarzeń typu DEPARTURE;
     * - liczba obsłużonych zdarzeń typu DEPARTURE;
     */
    protected void printSimulationDetailParameters() {
        System.out.println("...........................................................");
        System.out.println("-----> Szczegółowe parametry symulacji:");

        System.out.print("-------> Liczba powstałych zdarzeń typu przybycie klienta: ");
        System.out.println(arrivalGeneratedEventsCounter);

        System.out.print("-------> Liczba obsłużonych zdarzeń typu przybycie klienta: ");
        System.out.println(arrivalEventsCounter);

        System.out.print("-------> Liczba powstałych zdarzeń typu zakończenie obsługi klienta: ");
        System.out.println(departureGeneratedEventsCounter);

        System.out.print("-------> Liczba obsłużonych zdarzeń typu zakończenie obsługi klienta: ");
        System.out.println(departureEventsCounter);
    }

    /**
     * Abstrakcyjna metoda odpowiedzialna za wypisanie wszystkich parametrów wejściowych wpisanych podczas inicjalizacji;
     * (każdy typ symulacji wymaga innych parametrów wejściowych)
     */
    protected abstract void printManualConfiguration();
    /**
     * Abstrakcyjna metoda odpowiedzialna za wypisanie wszystkich parametrów wejściowych wczytanych z pliku konfiguracyjnego;
     */
    protected abstract void printConfigurationFromFile();
    /**
     * Metoda odpowiedzialna za wypisanie indywidualnych dla danej symulacji parametrów;
     */
    protected abstract void printIndividualSimulationParameters();

    /**
     * Metoda dopowiedzialna za wypisywanie raportów dotyczących szczegółowych informacji na temat każdego strumienia:
     * - parametrów opisujących każdy strumień;
     * - parametrów potwierdzajacych zgodność wylosowanych wartości z zadanym rozkładem;
     *
     * - jeżeli każdy sturmień miał takie same parametry wejściowe, to wyświetl parametry wejściowe raz
     * oraz dla kazdego strumienia jesto parametry potwierdzajace zgodnośc wylosowanych wartości z zadanym rozkładem;
     */
    protected void printDetailsStatisticsAboutStreams() {
        if (eachStreamsTheSame) {
            System.out.println("...........................................................");
            System.out.println("-----> Szczegółowe parametry strumieni:");

            System.out.println("-------> Każdy strumień zdefiniowany przez następujące parametry:: ");
            streams.get(0).printDetailsAboutStreamEntryValues();
        }

        for (int i=0; i<streams.size(); i++) {
            if( (!eachStreamsTheSame) && (i > 0) )
                streams.get(i).printDetailsAboutStreamEntryValues();

            streams.get(i).printDetailedStatisticsAboutProbabilityDistributions();
        }
    }





    /**
     * Metoda służąca do wypisania zawartości listy na ekran;
     * @param list Lista, którą chcielibyśmy wypisać;
     */
    protected void printList(List<Double> list) {
        System.out.print("[ ");
        for (int i=0; i<list.size(); i++) {
            System.out.print(list.get(i).toString());
            if ( i != (list.size()-1) )
                System.out.print("; ");
        }
        System.out.println(" ]");
    }
    protected void saveIndirectValues(int number){
    }
}
