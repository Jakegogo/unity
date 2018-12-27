import java.util.concurrent.LinkedBlockingQueue;

/**
 * 定量缓冲Queue
 * 超出maxSize时offer操作会被阻塞
 * @param <E>
 */
public class LimitedQueue<E> extends LinkedBlockingQueue<E> {
    public LimitedQueue(int maxSize) {
        super(maxSize);
    }

    @Override
    public boolean offer(E e) {
        // turn offer() and add() into a blocking calls (unless interrupted)
        try {
            put(e);
            return true;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (Exception oe) {
            oe.printStackTrace();
        }
        return false;
    }

}
