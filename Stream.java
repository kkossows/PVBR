import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Klasa charakteryzująca pojedyńczy napływ klientów do systemu.
 *
 * Deklaracja obiektów odbywa się za pomocą publicznych metod.
 *
 * Posiada typ typ wyliczeniowy okreslający możliwy typ rozkładu
 * ( zarówno napływu jak i obsługi klientów )
 */
public class Stream {
    public enum DistributionTypes {
        EXPONENTIAL,
        DETERMINISTIC
    }

    private double startTime;

    private DistributionTypes arrivalDistribution;
    private double arrivalDistributionParameter;

    private DistributionTypes departureDistribution;
    private double departureDistributionParameter;

    private List<Double> intervalsBetweenCustomerInflows;
    private List<Double> customersServiceTimes;

    private boolean inflowsDefineFlag;
    private boolean serviceTimeDefineFlag;

    /* statystyki związane z rozkładem napłwu*/
    private enum TypesOfTime {
        INTERVAL, SERVICE
    }
    private double sumOfArrivalDistributionRandomValues;
    private double sumOfDepartureDistributionRandomValues;

    private double arrivalTheoreticalDistributionParameter;
    private double departureTheoreticalDistributionParameter;

    private double arrivalDistributionMeanParameter;
    private double departureDistributionMeanParameter;

    private double newParameter;
    private double arrivalCounter = 0;
    private double departureCounter = 0;

    /* PVBR */
    private int numberOfPacketsRemainingInBurst;
    private int numberOfCompletedOnOffStates;
    private boolean isLastOne;


    /**
     * Konstruktor domyślny, bezparametrowy - inicalizuje przestrzeń w pamięci.
     * - inicjalizacja wartości początkowych zmiennych;
     */
    public Stream() {
        intervalsBetweenCustomerInflows = new ArrayList<>();
        customersServiceTimes = new ArrayList<>();

        sumOfArrivalDistributionRandomValues = 0.0;
        sumOfDepartureDistributionRandomValues = 0.0;
    }

    /* Metody umożliwiające zewnętrzne ustawienie zmiennych obiektu */
    /**
     * Publiczna metoda ustawiająca wartość zmiennej okreslającej czas startu danego strumienia;
     * @param startTime Czas startu strumienia;
     */
    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    /**
     * Publiczna metoda służąca do dodania typu oraz parametr danego rozkładu napływu klientów do systemu:
     * - metoda wylicza również teoretyczną wartość charakterystyczną dla danego rozkłądu
     * @param type Typ rozkładu;
     * @param parameter Parametr danego rozkłądu;
     */
    public void setArrivalProbabilityDistribution(DistributionTypes type, double parameter) {
        this.arrivalDistribution = type;
        this.arrivalDistributionParameter = parameter;
        this.arrivalTheoreticalDistributionParameter = countTheoreticalDistributionParameter(
                arrivalDistribution, arrivalDistributionParameter);
    }
    /**
     * Publiczna metoda służąca do dodania typu oraz parametr danego rozkładu czasu obsługi klientów przez system:
     * - metoda wylicza również teoretyczną wartość charakterystyczną dla danego rozkłądu
     * @param type Typ rozkładu;
     * @param parameter Parametr danego rozkładu;
     */
    public void setDepartureProbabilityDistribution(DistributionTypes type, double parameter) {
        this.departureDistribution = type;
        this.departureDistributionParameter = parameter;
        this.departureTheoreticalDistributionParameter = countTheoreticalDistributionParameter(
                departureDistribution, departureDistributionParameter);
    }

    /**
     * Publiczna metoda ustawiająca flagę oznaczającą, czy odstepy miedzy napływami były podane w pliku konfiguracyjnym;
     * @param value Nowa wartośc flagi;
     */
    public void setInflowsDefineFlag(boolean value){
        inflowsDefineFlag = value;
    }
    /**
     * Publiczna metoda ustawiająca flagę oznaczającą, czy czasy obsługi klientów były podane w pliku konfiguracyjnym;
     * @param value Nowa wartośc flagi;
     */
    public void setServiceTimeDefineFlag(boolean value){
        serviceTimeDefineFlag = value;
    }


    /* Metody umożliwiające zewnętrzne pobranie wartości zmiennych obiektu*/
    /**
     * Publiczna metoda pobierajaca typ rozkładu napływu klientów do systemu;
     * @return Typ rozkładu napływu klientów do systemu;
     */
    public DistributionTypes getArrivalDistribution() {
        return this.arrivalDistribution;
    }
    /**
     * Publiczna metoda pobierajaca parametr rozkładu napływu klientów do systemu;
     * @return Typ Parametr rozkładu napływu klientów do systemu;
     */
    public double getArrivalDistributionParameter() {
        return this.arrivalDistributionParameter;
    }

    /**
     * Publiczna metoda pobierajaca typ rozkładu czasu obsługi klienta przez serwer;
     * @return Typ rozkładu czasu obsługi klienta przez serwer;
     */
    public DistributionTypes getDepartureDistribution() {
        return this.departureDistribution;
    }
    /**
     * Publiczna metoda pobierajaca parametr rozkładu czasu obsługi klienta przez serwer;
     * @return Typ Parametr rozkładu czasu obsługi klienta przez serwer;
     */
    public double getDepartureDistributionParameter() {
        return this.departureDistributionParameter;
    }

    /**
     * Publiczna metoda odczytująca flagę oznaczającą, czy odstepy miedzy napływami były podane w pliku konfiguracyjnym;
     * @return Obecna wartośc flagi;
     */
    public boolean getInflowsDefineFlag(){
        return inflowsDefineFlag;
    }
    /**
     * Publiczna metoda odczytująca flagę oznaczającą, czy czasy obsługi klientów były podane w pliku konfiguracyjnym;
     * @return Obecna wartośc flagi;
     */
    public boolean getServiceTimeDefineFlag(){
        return serviceTimeDefineFlag;
    }
    /**
     * Publiczna metoda odczytująca wartość zmiennej okreslającej czas startu danego strumienia;
     * @return Czas startu strumienia;
     */
    protected double getStartTime() {
        return startTime;
    }


    /* metody związane z listami */
    /**
     * Publiczna metoda dodająca jeden element do listy zawierającej odstępy czasowe pomiędzy napływającymi klientami;
     * - metoda wywołuję aktualizację rzeczywistego parametru charakteryzującego dany rozklad;
     * @param newInterval Wartość nowego elementu (nowy ostęp czasowy)
     */
    public void addInterval ( double newInterval ) {
        newParameter = newInterval;
        arrivalCounter++;
        //intervalsBetweenCustomerInflows.add(newInterval);
        updateRealDistributionParameter(arrivalDistribution, TypesOfTime.INTERVAL);
    }
    /**
     * Publiczna metoda pobierajaca wskazaną wartość z listy zawierającej odstępy czasowe pomiędzy napływającymi klientami
     * z danego napływu;
     * @param position Pozycja w liscie;
     * @return  Wartość wskazanej pozycji;
     */
    public double getOneInterval ( int position ) {
        return intervalsBetweenCustomerInflows.get(position);
    }

    /**
     * Publiczna metoda dodająca jeden element do listy zawierającej czasy obsługi klientów;
     * - metoda wywołuję aktualizację rzeczywistego parametru charakteryzującego dany rozklad;
     * @param newServiceTime Wartość nowego elementu (czas obsługi klienta przez serwer)
     */
    public void addServiceTime ( double newServiceTime ) {
        newParameter = newServiceTime;
        departureCounter++;
        //customersServiceTimes.add(newServiceTime);
        updateRealDistributionParameter(departureDistribution, TypesOfTime.SERVICE);
    }
    /**
     * Publiczna metoda pobierajaca wskazaną wartośc z listy zawierającej czasy obsługi klientów danego napływu;
     * @param position Pozycja w liście;
     * @return  Wartość wskazanej pozycji;
     */
    public double getOneServiceTime ( int position ) {
        return customersServiceTimes.get(position);
    }


    /* metody związane ze statystyką rozkładów napłwów */
    /**
     * Metoda obliczająca wartość teoretyczną charakterystyczną dla danego rozkładu prawdopodobieństwa.
     * @param distribution Typ rozkładu prawdopodobieństwa.
     * @param distributionParameter Parametr rozkładu.
     * @return theoreticalParameter Wartość teoretyczna charakteryzująca dany rozkład (np. wartość średnia oczekiwana)
     */
    private double countTheoreticalDistributionParameter(DistributionTypes distribution, double distributionParameter) {
        double theoreticalParameter = 0.0;
        switch (distribution) {
            case EXPONENTIAL:
                theoreticalParameter = ( 1 / distributionParameter );
                break;
            case DETERMINISTIC:
                break;
        }
        return theoreticalParameter;
    }
    /**
     * Metoda wywoływana przy każdym wywołaniu metody addInterval lub addServiceTime
     * - oblicza rzeczywistą wartość parametru charakteryzującego rozkład;
     * @return Nowa wartość parametru;
     */
    private void updateRealDistributionParameter(DistributionTypes distributionType, TypesOfTime newTimeType) {
        switch ( distributionType ) {
            /* dla wykładniczego parametrem jest oczekiwana średnia, która powinna wynosić 1/lambda */
            case EXPONENTIAL:
                updateRealExponentialParameter(newTimeType);
                break;
            case DETERMINISTIC:

                break;
        }
    }

    /**
     * Metoda odpowiedzialna za aktualizację rzeczywistych parametrów rozkładu wykładniczego (wartości średniej)
     * - wartość średnia liczona jest jako wynik dzielenia sumy wszystkich czasów przez liczbę wygenerowanych czasów;
     * - w zalezności o typu wygenerowanego czasu, uzupełniane są parametry związane z rozkładem napływu lub
     * czasów obsługi klientów;
     * @param newTimeType Typ czasu: SERVICE (rozkład czasów obsługi) lub INTERVAL (rozkład napływu)
     */
    private void updateRealExponentialParameter(TypesOfTime newTimeType) {
        switch (newTimeType) {
            case SERVICE:
                sumOfDepartureDistributionRandomValues += newParameter;
                //sumOfDepartureDistributionRandomValues += customersServiceTimes.get(
                //        customersServiceTimes.size()-1);
                break;
            case INTERVAL:
                sumOfArrivalDistributionRandomValues += newParameter;
                //sumOfArrivalDistributionRandomValues += intervalsBetweenCustomerInflows.get(
                //        intervalsBetweenCustomerInflows.size()-1);
                break;
        }
    }
    /**
     * Publiczna metoda służąca do wyliczania końcowych wartości parametrów rozkładów
     */
    public void countFinalRealExponentialParameter(){
        departureDistributionMeanParameter = (
                sumOfDepartureDistributionRandomValues / arrivalCounter);

        arrivalDistributionMeanParameter = (
                sumOfArrivalDistributionRandomValues / departureCounter);
    }


    /* publiczne metody związane z algorytmem raportowania */
    /**
     * Publiczna metoda wypisująca parametry dotyczące zadanych rozkładów:
     * - wartość teoretyczną dla rozkładu napływu;
     * - wartość rzeczywistą dla rozkładu napływu;
     * - wartośc teoretyczną dla rozkładu czasów obsługi;
     * - wartośc rzeczywistą dla rozkładu czasów obsługi;
     */
    public void printDetailedStatisticsAboutProbabilityDistributions() {
        /* oblicz końcowe wartości statystyk rozkładów */
        countFinalRealExponentialParameter();

        System.out.println("...........................................................");
        System.out.println("-----> Parametry potwierdzające zgodność wylosowanych wartości z zadanym rozkładem:");

        System.out.print("-------> ARRIVAL: Teoretyczne : ");
        System.out.println(arrivalTheoreticalDistributionParameter);

        System.out.print("-------> ARRIVAL: Rzeczywiste: ");
        System.out.println(arrivalDistributionMeanParameter);

        System.out.print("-------> DEPARTURE: Teoretyczne : ");
        System.out.println(departureTheoreticalDistributionParameter);

        System.out.print("-------> DEPARTURE: Rzeczywiste: ");
        System.out.println(departureDistributionMeanParameter);
    }
    /**
     * Publiczna metoda wypisująca parametry wejściowe strumienia:
     * - czas rozpoczęcia;
     *
     * Jeżeli czasy napłwywów klientów nie zostały podane w pliku to wypisuje:
     * - typ rozkładu napływu klientów;
     * - wartość parametru rozkładu napływu klientów;
     *
     * Jeżeli czasy obsługi klientów nie zostały podane w pliku to wypisuje:
     * - typ rozkładu obsługi klientów przez serwer;
     * - wartość parametru rozkłądu obłsugi klientów przez serwer;
     */
    public void printDetailsAboutStreamEntryValues() {
        System.out.println("...........................................................");
        System.out.println("-----> Parametry wejściowe strumienia:");

        System.out.print("-------> Czas rozpoczęcia napływu: ");
        System.out.println(startTime);

        if (!inflowsDefineFlag) {
            System.out.print("-------> Typ rozkładu napływu klientów: ");
            System.out.println(arrivalDistribution.name());

            System.out.print("-------> Wartość parametru rozkładu napływu klientów: ");
            System.out.println(arrivalDistributionParameter);
        }
        else
            System.out.println("-------> Czasy napływów klientów pobrane z pliku konfiguracyjnego;");

        if (!serviceTimeDefineFlag) {
            System.out.print("-------> Typ rozkładu obsługi klientów przez serwer: ");
            System.out.println(departureDistribution.name());

            System.out.print("-------> Wartość parametru rozkładu obsługi klientów prze zserwer: ");
            System.out.println(departureDistributionParameter);
        }
        else
            System.out.println("-------> Czasy obsługi klientów pobrane z pliku konfiguracyjnego;");
    }


    /* metody związane ze zmiennymi dotyczącymi usługi PVBR */
    /**
     * Konstruktor pozwalający w prosty sposób stworzyc obiekt niezbędny do symulacji usługi PVBR
     * @param startTime Czas rozpoczęcia generowania napływu
     * @param numberOfPacketsRemainingInBurst Liczba pozostałych pakietów w paczce
     */
    public Stream(double startTime,int numberOfPacketsRemainingInBurst){
        this.startTime = startTime;
        this.numberOfCompletedOnOffStates = 0;
        this.numberOfPacketsRemainingInBurst = numberOfPacketsRemainingInBurst;

        arrivalDistribution = DistributionTypes.DETERMINISTIC;
        departureDistribution = DistributionTypes.DETERMINISTIC;

        intervalsBetweenCustomerInflows = new ArrayList<>();
        customersServiceTimes = new ArrayList<>();

        //intervalsBetweenCustomerInflows.add(0.0);
        isLastOne = false;
    }
    /**
     * Publiczna metoda ustawiająca nową wartość liczby pozostałych pakietow w paczce
     * @param value Nowa liczba pozostałych pakietów w paczce;
     */
    public void setNumberOfPacketsRemainingInBurst(int value) {
        this.numberOfPacketsRemainingInBurst = value;
    }
    /**
     * Publiczna metoda służąca do zmniejszenia liczby pakietów pozostalych w paczce o 1
     */
    public void decreaseNumberOfPacketsRemainingInBurst() {
        this.numberOfPacketsRemainingInBurst--;
    }
    /**
     * Publiczna metoda pobierająca pozostałą liczbę pakietów w paczce
     * @return Pozostała liczba pakietów w paczce
     */
    public int getNumberOfPacketsRemainingInBurst() {
        return this.numberOfPacketsRemainingInBurst;
    }
    /**
     * Publiczna metoda zwiększająca licznik obsłużonych stanów ON/OFF o 1
     */
    public void increaseNumberOfCompletedOnOffStates() {
        if(arrivalCounter != 0)
            this.numberOfCompletedOnOffStates++;
    }
    /**
     * Publiczna metoda pobierająca liczbę obsłużónych stanów ON/OFF danego strumienia;
     * @return Liczba obsłużonych stanów ON/OFF
     */
    public int getNumberOfCompletedOnOffStates() {
        return this.numberOfCompletedOnOffStates;
    }
    /**
     * Publiczna metoda służąca do odczytywania flagi isLastOne
     * @return isLastOne - flaga mówiąca, czy dany strumień jest tym, który zaczął generowac najpóźniej
     */
    public boolean isLastOne(){
        return this.isLastOne;
    }
    /**
     * Publiczna metoda służąca do ustawiania flagi isLastOne
     * @param value nowa wartośc flagi, true oznacza, że dany strumień jest tym, który ma najwyższy startTime
     */
    public void setIsLastOne(boolean value ){
        this.isLastOne = value;
    }




    /* debug functions */
    public void printLists(){
        /* lista odstępów */
        System.out.println(intervalsBetweenCustomerInflows.toString());

        /* lista czasów obsługi */
        System.out.println(customersServiceTimes.toString());
    }

}
