package alma.archive.xml.dao;

import java.util.Iterator;

import org.hibernate.ScrollableResults;

import alma.archive.xml.XmlEntity;

public class ScrollabeResultIteratorWrapper <E extends XmlEntity> implements Iterator<E> {

	private ScrollableResults scroll;
	
	public ScrollabeResultIteratorWrapper(ScrollableResults scroll) {
		this.scroll = scroll;
	}
	
	@Override
	public boolean hasNext() {
		boolean hasMoreResults = scroll.next();
		if(!hasMoreResults)
			scroll.close();
		return hasMoreResults;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E next() {
		return (E) scroll.get()[0];
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
