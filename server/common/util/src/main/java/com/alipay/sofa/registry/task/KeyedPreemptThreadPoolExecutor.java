package com.alipay.sofa.registry.task;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KeyedPreemptThreadPoolExecutor extends KeyedThreadPoolExecutor {
    private final Comparator<Runnable> comparator;

    public KeyedPreemptThreadPoolExecutor(String executorName, int coreSize, int coreBufferSize,
                                          Comparator<Runnable> comparator) {
        super(executorName, coreSize, coreBufferSize);
        this.comparator = comparator;
    }

    @Override
    protected AbstractWorker createWorker(int idx, int coreBufferSize) {
        return new WorkerImpl(idx);
    }

    private final class WorkerImpl extends AbstractWorker {
        final Map<Object, KeyedTask> map  = new HashMap<>(128);
        final LinkedList<Object>     keysQueue;
        final Lock                   lock = new ReentrantLock();
        final Condition              cond = lock.newCondition();

        WorkerImpl(int idx) {
            super(idx);
            this.keysQueue = new LinkedList<>();
        }

        public int size() {
            lock.lock();
            try {
                return keysQueue.size();
            } finally {
                lock.unlock();
            }

        }

        public KeyedTask poll() throws InterruptedException {
            // get the key,
            lock.lock();
            try {
                if (keysQueue.isEmpty()) {
                    cond.await(180, TimeUnit.SECONDS);
                }
                final Object key = keysQueue.poll();
                if (key == null) {
                    return null;
                }
                KeyedTask task = map.remove(key);
                return task;
            } finally {
                lock.unlock();
            }
        }

        public boolean offer(KeyedTask task) {
            lock.lock();
            try {
                final KeyedTask prev = map.get(task.key);
                if (prev != null) {
                    int comp = comparator.compare(prev.runnable, task.runnable);
                    if (comp >= 0) {
                        // the prev is high priority than current, ignored
                        return true;
                    }
                } else {
                    // try add to keys queue to poll
                    if (!keysQueue.offer(task.key)) {
                        // queue is full
                        return false;
                    }
                }
                // at last, overwrite the prev or insert the current
                // promise that: if map.task exist, the queue.key must be exist
                map.put(task.key, task);
                cond.signal();
                return true;
            } finally {
                lock.unlock();
            }
        }
    }

}