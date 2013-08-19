package upnpclient.model;

import java.util.ArrayList;

import com.elevenquest.sol.upnp.control.ControlPoint;
import com.elevenquest.sol.upnp.model.UPnPBase;

public class ControlPointManager {
	ControlPoint cp = null;
	UPnPBase selectedItem = null;
	ArrayList<IControlPointUserSelectionChangeListener> listeners = new ArrayList<IControlPointUserSelectionChangeListener>();
	
	private ControlPointManager() {
	}
	
	static ControlPointManager manager = null;
	
	public static ControlPointManager getManager() {
		if ( manager == null ) 
			manager = new ControlPointManager();
		return manager;
	}
	
	public ControlPoint getDefaultControlPoint() {
		if ( cp == null )
			cp = new ControlPoint();
		return cp;
	}
	
	public void setUserSelectedItem(UPnPBase selectedItem) {
		this.selectedItem = selectedItem;
		for (IControlPointUserSelectionChangeListener listener:listeners) {
			listener.updateUserSelection(selectedItem);
		}
	}
	
	public UPnPBase getUserSelectedItem() {
		return this.selectedItem;
	}
	
	public void addUserSelectionChangeListener(IControlPointUserSelectionChangeListener listener) {
		if ( !listeners.contains(listener) )
			listeners.add(listener);
	}
	
	public void removeUserSelectionChangeListener(IControlPointUserSelectionChangeListener listener) {
		listeners.remove(listener);
	}
	
}
