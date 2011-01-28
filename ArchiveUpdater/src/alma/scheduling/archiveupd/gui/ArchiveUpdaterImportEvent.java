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
 * 
 * $Id: ArchiveUpdaterImportEvent.java,v 1.2 2011/01/28 00:35:30 javarias Exp $
 */

package alma.scheduling.archiveupd.gui;

import javax.swing.ImageIcon;

import alma.acs.gui.standards.StandardIcons;
import alma.scheduling.datamodel.obsproject.dao.ProjectImportEvent;

public class ArchiveUpdaterImportEvent extends ProjectImportEvent{
	
	public ImageIcon getIconStatus(){
		switch(getStatus()){
		case STATUS_INFO:
			return StandardIcons.INFO.icon;
		case STATUS_OK:
			return StandardIcons.STATUS_OKAY.icon;
		case STATUS_WARNING:
			return StandardIcons.STATUS_WARNING.icon;
		case STATUS_ERROR:
			return StandardIcons.STATUS_ERROR.icon;
		default:
			return null;
		}
	}
}
