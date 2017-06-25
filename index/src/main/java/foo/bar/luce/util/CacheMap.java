package foo.bar.luce.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CacheMap<K, V> implements Map<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(CacheMap.class);

    private ReferenceQueue<CacheReference<K, V>> queue;
    private Map<K, CacheReference<K, V>> map;

    public CacheMap() {
        queue = new ReferenceQueue<>();
        map = new ConcurrentHashMap<>();

        new Thread(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    //noinspection unchecked
                    CacheReference ref = (CacheReference) queue.remove();
                    ref.evict();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new NotImplementedException();
    }

    @Override
    public V get(Object key) {
        CacheReference<K, V> ref = map.get(key);
        if (ref == null) {
            return null;
        } else {
            return ref.get();
        }
    }

    @Override
    public V put(K key, V value) {
        map.put(key, new CacheReference<>(value, queue, key));
        return value;
    }

    @Override
    public V remove(Object key) {
        return map.remove(key).get();
    }

    @Override
    public void putAll(Map m) {
        throw new NotImplementedException();

    }

    @Override
    public void clear() {
        map.clear();

    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        throw new NotImplementedException();

    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new NotImplementedException();
    }


    class CacheReference<RK, RV> extends SoftReference<RV> {
        private RK key;

        public CacheReference(RV referent, ReferenceQueue<CacheReference<RK, RV>> q, RK key) {
            //noinspection unchecked
            super(referent, (ReferenceQueue<? super RV>) q);
            this.key = key;
        }

        void evict() {
            LOG.debug("removing stale reference to key {}", key);
            //noinspection SuspiciousMethodCalls
            map.remove(key);
        }
    }
}
