package org.xson.tangyuan.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.LinkedList;

public class SoftCache extends AbstractCache {

	private final LinkedList<Object>		hardLinksToAvoidGarbageCollection;
	private final ReferenceQueue<Object>	queueOfGarbageCollectedEntries;
	private final ICache					delegate;
	private int								numberOfHardLinks;

	public SoftCache(ICache delegate, int size) {
		this.delegate = delegate;
		// this.numberOfHardLinks = 256;
		this.numberOfHardLinks = size;
		this.hardLinksToAvoidGarbageCollection = new LinkedList<Object>();
		this.queueOfGarbageCollectedEntries = new ReferenceQueue<Object>();
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public int getSize() {
		removeGarbageCollectedItems();
		return delegate.getSize();
	}

	public void setSize(int size) {
		this.numberOfHardLinks = size;
	}

	// @Override
	// public void putObject(Object key, Object value) {
	// removeGarbageCollectedItems();
	// delegate.putObject(key, new SoftEntry(key, value, queueOfGarbageCollectedEntries));
	// }

	@Override
	public void putObject(Object key, Object value, Integer time) {
		removeGarbageCollectedItems();
		delegate.putObject(key, new SoftEntry(key, value, queueOfGarbageCollectedEntries));
	}

	@Override
	public Object getObject(Object key) {
		Object result = null;
		@SuppressWarnings("unchecked")
		// assumed delegate cache is totally managed by this cache
		SoftReference<Object> softReference = (SoftReference<Object>) delegate.getObject(key);
		if (softReference != null) {
			result = softReference.get();
			if (result == null) {
				delegate.removeObject(key);
			} else {
				// See #586 (and #335) modifications need more than a read lock
				synchronized (hardLinksToAvoidGarbageCollection) {
					hardLinksToAvoidGarbageCollection.addFirst(result);
					if (hardLinksToAvoidGarbageCollection.size() > numberOfHardLinks) {
						hardLinksToAvoidGarbageCollection.removeLast();
					}
				}
			}
		}
		return result;
	}

	@Override
	public Object removeObject(Object key) {
		removeGarbageCollectedItems();
		return delegate.removeObject(key);
	}

	@Override
	public void clear() {
		synchronized (hardLinksToAvoidGarbageCollection) {
			hardLinksToAvoidGarbageCollection.clear();
		}
		removeGarbageCollectedItems();
		delegate.clear();
	}

	private void removeGarbageCollectedItems() {
		SoftEntry sv;
		while ((sv = (SoftEntry) queueOfGarbageCollectedEntries.poll()) != null) {
			delegate.removeObject(sv.key);
		}
	}

	private static class SoftEntry extends SoftReference<Object> {
		private final Object	key;

		private SoftEntry(Object key, Object value, ReferenceQueue<Object> garbageCollectionQueue) {
			super(value, garbageCollectionQueue);
			this.key = key;
		}
	}

}