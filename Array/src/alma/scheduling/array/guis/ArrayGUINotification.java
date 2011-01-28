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
package alma.scheduling.array.guis;

import alma.scheduling.ArrayGUIOperation;


/**
 * @author rhiriart
 *
 */
public class ArrayGUINotification {

    private ArrayGUIOperation operation;
    private String name;
    private String role;
    
    public ArrayGUINotification(ArrayGUIOperation op,
    		                    Object            name,
    		                    Object            role) {
        this(op, (String) name, (String) role);
    }
    
    public ArrayGUINotification(ArrayGUIOperation operation,
    		                    String            name,
    		                    String            role) {
        this.operation = operation;
        this.name = name;
        this.role = role;
    }

	public ArrayGUIOperation getOperation() {
		return operation;
	}

	public void setOperation(ArrayGUIOperation operation) {
		this.operation = operation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
