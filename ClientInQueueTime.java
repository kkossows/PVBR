/**
 * Klasa odpowiedzialna za przechowywanie:
 * - czasu przyjścia klienta do systemu;
 * - odnośnikia do strumienia macierzystego;
 */
public class ClientInQueueTime {
    public double time;
    public Stream sourceStream;

    public ClientInQueueTime(double time, Stream sourceStream){
        this.time = time;
        this.sourceStream = sourceStream;
    }
}
