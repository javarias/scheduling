package alma.scheduling.psm.web;

import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

public class GenericRowRenderer implements RowRenderer {

	@Override
	public void render(Row row, Object data) throws Exception {
		Object[] _data =  (Object[]) data;
		for(int i = 0; i < _data.length; i++) {
			if (_data[i] instanceof String) {
				new Label((String)_data[i]).setParent(row);
			}
			else if (_data[i] instanceof AbstractComponent) {
				((AbstractComponent)(_data[i])).setParent(row);
			}
		}

	}

}
