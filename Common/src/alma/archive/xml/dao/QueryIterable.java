package alma.archive.xml.dao;

import java.util.Iterator;

import org.hibernate.Query;

import alma.archive.xml.XmlEntity;

public class QueryIterable <T extends XmlEntity> implements Iterable<T> {

	private final Query query;
	
	public QueryIterable(Query query) {
		this.query = query;
	}
	
	@Override
	public Iterator<T> iterator() {
		ScrollabeResultIteratorWrapper<T> iterator = 
				new ScrollabeResultIteratorWrapper<T>(query.scroll());
		return iterator;
	}

}
