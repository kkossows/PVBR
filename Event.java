import java.math.BigDecimal;

/**
 * Klasa opisująca zdarzenie możliwe do zaistnienia w symulacji.
 * - klasa posiada nadpisanie metody compareTo porównujące obiekty tej klasy pod zwględem zmiennej time;
 */
public class Event implements Comparable<Event>{
    public enum EventType {
        ARRIVAL,
        DEPARTURE
    }

    public double time;
    public EventType type;
    public Stream source;

    public Event( EventType type, double time, Stream source) {
        this.time = time;
        this.type = type;
        this.source = source;
    }

    @Override
    public int compareTo(Event o) {
        int compareValue;

        if ( time > o.time )
            compareValue = 1;
        else if (time < o.time )
            compareValue = -1;
        else
            compareValue = 0;

        if ( compareValue > 0 ) return 1;
        else if ( compareValue < 0 ) return -1;
        else return 0;
    }
}