package alma.scheduling.psm.web;

import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

public class Ph1MSychGridRowRenderer implements RowRenderer {

	@Override
	public void render(Row row, Object data) throws Exception {
		String[] _data =  (String[]) data;
		new Label(_data[0]).setParent(row);
		new Label(_data[1]).setParent(row);
		new Label(_data[2]).setParent(row);
		new Label(_data[3]).setParent(row);
		new Label(_data[4]).setParent(row);
		new Label(_data[5]).setParent(row);
		new Label(_data[6]).setParent(row);
		new Label(_data[7]).setParent(row);
		if(_data[1].compareTo(_data[5]) != 0 || _data[2].compareTo(_data[6]) != 0
				|| _data[3].compareTo(_data[7]) != 0){
			row.setStyle("background-color: #FFD854");
		}
	}

}
