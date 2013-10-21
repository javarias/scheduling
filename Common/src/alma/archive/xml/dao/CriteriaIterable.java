package alma.archive.xml.dao;

import java.util.Iterator;

import org.hibernate.Criteria;

import alma.archive.xml.XmlEntity;

public class CriteriaIterable<T extends XmlEntity> implements Iterable<T>{
	private final Criteria criteria;
	
	public CriteriaIterable(Criteria criteria) {
		this.criteria = criteria;
	}
	
	@Override
	public Iterator<T> iterator() {
		ScrollabeResultIteratorWrapper<T> iterator = 
				new ScrollabeResultIteratorWrapper<T>(criteria.scroll());
		return iterator;
	}

}
