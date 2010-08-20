/* ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2010
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */
package alma.scheduling.array.sbQueue;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

/**
 * @author rhiriart
 *
 */
public class ReorderingBlockingQueueTest extends TestCase {

    private class QueueConsumer implements Runnable {

        private final LinkedReorderingBlockingQueue<String> queue;
        private final AtomicBoolean consume = new AtomicBoolean();
        
        public QueueConsumer(LinkedReorderingBlockingQueue<String> queue) {
            this.queue = queue;
            consume.set(true);
        }
        
        public void stopConsuming() {
            consume.set(false);
        }
        
        @Override
        public void run() {
            while(consume.get()) {
                String item = null;
                try {
                    item = queue.take();
                } catch (InterruptedException e) {
                    System.out.println("Consumer has been interrupted");
                    return;
                }
                System.out.println("Consumer took item " + item);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
            }
            System.out.println("Consumer has been stopped");
        }
        
        
    }
    
    /**
     * @param name
     */
    public ReorderingBlockingQueueTest(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testQueue() {
        
        LinkedReorderingBlockingQueue<String> queue = new LinkedReorderingBlockingQueue<String>();
        queue.add("one");
        printQueue(queue);

        queue.moveUp("one");
        printQueue(queue);
        queue.moveDown("one");
        printQueue(queue);
        
        queue.add("two");
        queue.add("three");
        printQueue(queue);
        
        queue.moveUp("two");        
        printQueue(queue);
        
        queue.moveDown("two");
        printQueue(queue);
    }
    
    private void printQueue(LinkedReorderingBlockingQueue<String> queue) {
        System.out.println("------");
        Iterator<String> iter = queue.iterator();
        while (iter.hasNext()) {
            String item = iter.next();
            System.out.println("item: " + item);
        }        
    }

    public void testConsumer() throws Exception {
        LinkedReorderingBlockingQueue<String> queue = new LinkedReorderingBlockingQueue<String>();
        QueueConsumer consumer = new QueueConsumer(queue);
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();
        Thread.sleep(200);
        queue.offer("one");
        Thread.sleep(200);
        queue.offer("two");
        Thread.sleep(1000);
        queue.offer("three");
        Thread.sleep(1000);
        consumer.stopConsuming();
        
        consumer = new QueueConsumer(queue);
        consumerThread = new Thread(consumer);
        consumerThread.start();
        queue.offer("four");
        queue.offer("five");
        queue.offer("six");
        consumerThread.interrupt();
    }
    
}
