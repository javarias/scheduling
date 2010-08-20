package alma.scheduling.array.sbQueue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import alma.scheduling.QueueOperation;

/**
 * A decorator for a <tt>ReorderingBlockingQueue</tt>. Adds the capability to be
 * observed. An instance of this class will notify its <tt>Observers</tt> when
 * the queue changes.
 * 
 * @author Rafael Hiriart
 * @param <E> the type of elements held in this collection
 */
public class ObservableReorderingBlockingQueue<E> extends Observable
    implements ReorderingBlockingQueue<E> {

    /** The queue this decorator uses to delegate all its operations. */
    private ReorderingBlockingQueue<E> queue;
    
    public ObservableReorderingBlockingQueue(ReorderingBlockingQueue<E> queue) {
        this.queue = queue;
    }
    
    @Override
    public boolean add(E e) {
        boolean retVal = queue.add(e);
        if (retVal) {
            setChanged();
            notifyObservers(new QueueNotification(QueueOperation.PUSH, e));
        }
        return retVal;
    }

    @Override
    public boolean offer(E e) {
        boolean retVal = queue.offer(e);
        if (retVal) {
            setChanged();
            notifyObservers(new QueueNotification(QueueOperation.PUSH, e));
        }
        return retVal;
    }

    @Override
    public void put(E e) throws InterruptedException {
        queue.put(e);
        setChanged();
        notifyObservers(new QueueNotification(QueueOperation.PUSH, e));
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit)
            throws InterruptedException {
        boolean retVal = queue.offer(e, timeout, unit);
        if (retVal) {
            setChanged();
            notifyObservers(new QueueNotification(QueueOperation.PUSH, e));
        }
        return retVal;
    }

    @Override
    public E take() throws InterruptedException {
        E e = queue.take();
        setChanged();
        notifyObservers(new QueueNotification(QueueOperation.PULL, e));
        return e;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E e = queue.poll(timeout, unit);
        if (e != null) {
            setChanged();
            notifyObservers(new QueueNotification(QueueOperation.PULL, e));
        }
        return e;
    }

    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public boolean remove(Object o) {
        boolean retVal = queue.remove(o);
        if (retVal) {
            setChanged();
            notifyObservers(new QueueNotification(QueueOperation.REMOVE, o));
        }
        return retVal;
    }

    @Override
    public boolean contains(Object o) {
        return queue.contains(o);
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        int retVal = queue.drainTo(c);
        setChanged();
        notifyObservers(new QueueNotification(QueueOperation.REMOVE_MANY, null));
        return retVal;
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        int retVal = queue.drainTo(c, maxElements);
        setChanged();
        notifyObservers(new QueueNotification(QueueOperation.REMOVE_MANY, null));
        return retVal;
    }

    @Override
    public E remove() {
        E e = queue.remove();
        setChanged();
        notifyObservers(new QueueNotification(QueueOperation.PULL, e));
        return e;
    }

    @Override
    public E poll() {
        E e = queue.poll();
        if (e != null) {
            setChanged();
            notifyObservers(new QueueNotification(QueueOperation.PULL, e));
        }
        return e;
    }

    @Override
    public E element() {
        return queue.element();
    }

    @Override
    public E peek() {
        return queue.peek();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return queue.iterator();
    }

    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return queue.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return queue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean retVal = queue.addAll(c);
        if (retVal) {
            setChanged();
            notifyObservers(new QueueNotification(QueueOperation.PUSH_MANY, null));
        }
        return retVal;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean retVal = queue.removeAll(c);
        if (retVal) {
            setChanged();
            notifyObservers(new QueueNotification(QueueOperation.REMOVE_MANY, null));
        }
        return retVal;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean retVal = queue.retainAll(c);
        if (retVal) {
            setChanged();
            notifyObservers(new QueueNotification(QueueOperation.REMOVE_MANY, null));
        }
        return retVal;
    }

    @Override
    public void clear() {
        queue.clear();
        setChanged();
        notifyObservers(new QueueNotification(QueueOperation.REMOVE_MANY, null));
    }

    @Override
    public boolean moveDown(Object o) {
        boolean retVal = queue.moveDown(o);
        if (retVal) {
            setChanged();
            notifyObservers(new QueueNotification(QueueOperation.DOWN, o));
        }
        return retVal;
    }

    @Override
    public boolean moveUp(Object o) {
        boolean retVal = queue.moveUp(o);
        if (retVal) {
            setChanged();
            notifyObservers(new QueueNotification(QueueOperation.UP, o));
        }
        return retVal;
    }
}
