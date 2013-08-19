package upnpclient.view;

import java.util.Iterator;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import upnpclient.model.ControlPointManager;

import com.elevenquest.sol.upnp.common.Logger;
import com.elevenquest.sol.upnp.model.IUPnPDeviceListChangeListener;
import com.elevenquest.sol.upnp.model.UPnPBase;
import com.elevenquest.sol.upnp.model.UPnPChangeStatusValue;
import com.elevenquest.sol.upnp.model.UPnPDevice;
import com.elevenquest.sol.upnp.model.UPnPDeviceManager;
import com.elevenquest.sol.upnp.model.UPnPService;

public class DeviceList extends ViewPart {
	public static final String ID = "UPnPClient.view.DeviceList";
	TreeViewer viewer = null;
	
	class DeviceViewContentProvider implements ITreeContentProvider,IUPnPDeviceListChangeListener {
		
		public DeviceViewContentProvider() {
			UPnPDeviceManager.getDefaultDeviceManager().addDeviceListChangeListener(DeviceViewContentProvider.this);
		}
		
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if ( oldInput != newInput ) {
				if ( oldInput instanceof UPnPDeviceManager ) {
					UPnPDeviceManager oldManager = (UPnPDeviceManager)oldInput;
					oldManager.removeDeviceListChangeListener(DeviceViewContentProvider.this);
				}
				if ( newInput instanceof UPnPDeviceManager ) {
					UPnPDeviceManager newManager = (UPnPDeviceManager)newInput;
					newManager.addDeviceListChangeListener(DeviceViewContentProvider.this);
				}
			}
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if ( parentElement instanceof UPnPDeviceManager ) {
				UPnPDeviceManager manager = (UPnPDeviceManager)parentElement;
				return manager.getDeviceList().toArray(new UPnPDevice[0]);
			} else if ( parentElement instanceof UPnPDevice ) {
				UPnPDevice device = (UPnPDevice)parentElement;
				return device.getSerivces().toArray(new UPnPService[0]); 
			}
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			if ( element instanceof UPnPService ) {
				UPnPService service = (UPnPService)element;
				return service.getDevice();
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if ( element instanceof UPnPDeviceManager ) {
				UPnPDeviceManager manager = (UPnPDeviceManager)element;
				return ( manager.getDeviceList().size() > 0 );
			} else if ( element instanceof UPnPDevice ) {
				UPnPDevice device = (UPnPDevice)element;
				return ( device.getSerivces().size() > 0 );
			}
			return false;
		}

		@Override
		public void updateDeviceList(UPnPChangeStatusValue value,
				UPnPDevice device) {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					viewer.refresh();
				}
				
			});
		}

	}

	class DeviceViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if ( obj instanceof UPnPDevice ) {
				UPnPDevice device = (UPnPDevice)obj;
				return ( device.getFriendlyName() != null && device.getFriendlyName().length() > 0 )? device.getFriendlyName() : device.getUuid();
			} else if ( obj instanceof UPnPService ) {
				UPnPService service = (UPnPService)obj;
				return service.getServiceId();
			}
			return "";
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		}
	}	
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.setContentProvider(new DeviceViewContentProvider());
		viewer.setLabelProvider(new DeviceViewLabelProvider());
		// Provide the input to the ContentProvider
		viewer.setInput(UPnPDeviceManager.getDefaultDeviceManager());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Logger.println(Logger.DEBUG, "[DeviceList] event:" + event);
				if ( event.getSelection() instanceof IStructuredSelection ) {
					IStructuredSelection selection = (IStructuredSelection)event.getSelection();
					for (Iterator iter = selection.iterator(); iter.hasNext() ;) {
						UPnPBase base = (UPnPBase)iter.next();
						Logger.println(Logger.DEBUG, "[DeviceList] iterator:" + base);
						ControlPointManager.getManager().setUserSelectedItem(base);
					}
				}
			}
		});
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
