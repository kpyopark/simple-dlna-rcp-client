package upnpclient.view;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import upnpclient.model.ControlPointManager;
import upnpclient.model.IControlPointUserSelectionChangeListener;
import upnpclient.view.DeviceList.DeviceViewContentProvider;
import upnpclient.view.DeviceList.DeviceViewLabelProvider;

import com.elevenquest.sol.upnp.common.Logger;
import com.elevenquest.sol.upnp.model.UPnPBase;
import com.elevenquest.sol.upnp.model.UPnPDevice;
import com.elevenquest.sol.upnp.model.UPnPDeviceManager;

public class DetailInfo extends ViewPart {
	
	static Object[] EMPTY_ARRAY = new Object[0];

	public static final String ID = "UPnPClient.view.DetailInfo";
	TableViewer viewer = null;
	
	private TableViewerColumn createTableViewerColumn(String title, int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	class PropertyNameAndValue {
		public String name;
		public String value;
		public PropertyNameAndValue(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
	
	class DetailInfoViewContentProvider implements IStructuredContentProvider, IControlPointUserSelectionChangeListener  {
		
		public DetailInfoViewContentProvider() {
			ControlPointManager.getManager().addUserSelectionChangeListener(DetailInfoViewContentProvider.this);
		}
		
		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof UPnPBase) {
				ArrayList<PropertyNameAndValue> properties = new ArrayList<PropertyNameAndValue>();
				UPnPBase baseStruct = (UPnPBase) inputElement;
				Method[] methods = baseStruct.getClass().getDeclaredMethods();
				for (int inx = 0; inx < methods.length; inx++) {
					try {
						if (methods[inx].getName().indexOf("get") != 0) {
							continue;
						}
						if (!Modifier.isPublic(methods[inx].getModifiers())
								&& !Modifier.isProtected(methods[inx]
										.getModifiers())) {
							properties.add(new PropertyNameAndValue(
									methods[inx].getName(), "[no authority]"
											+ methods[inx].getModifiers()));
							continue;
						}
						Class[] parameters = methods[inx].getParameterTypes();
						if (parameters != null && parameters.length > 0) {
							properties
									.add(new PropertyNameAndValue(methods[inx]
											.getName(), "[need parameters]"));
							continue;
						}
						String canonicalNameOfReturnType = methods[inx]
								.getReturnType().getCanonicalName();
						if ((canonicalNameOfReturnType
								.equals("java.util.Vector")
								|| canonicalNameOfReturnType
										.equals("java.util.ArrayList") || canonicalNameOfReturnType
									.equals("java.util.Collection"))) {
							Collection<Object> vec = (Collection<Object>) methods[inx]
									.invoke(baseStruct, (Object[]) null);
							try {
								StringBuffer sb = new StringBuffer();
								if (vec == null) {
									sb.append("null");
								} else {
									for (Iterator<Object> iter = vec.iterator(); iter
											.hasNext();) {
										sb.append("[").append(iter.next())
												.append("]");
									}
								}
								properties.add(new PropertyNameAndValue(
										methods[inx].getName(), sb.toString()));
							} catch (Exception e1) {
								e1.printStackTrace();
								Logger.println(Logger.ERROR, e1.getMessage());
							}
						} else {
							try {
								Object rtnValue = methods[inx].invoke(baseStruct,
										(Object[]) null);
								if (rtnValue != null)
									properties.add(new PropertyNameAndValue(
											methods[inx].getName(), rtnValue
													.toString()));
								else
									properties.add(new PropertyNameAndValue(
											methods[inx].getName(), "[NULL]"));
							} catch ( Exception e1 ) {
								e1.printStackTrace();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return properties.toArray(new PropertyNameAndValue[0]);
			} else {
				return EMPTY_ARRAY;
			}
		}

		@Override
		public void updateUserSelection(UPnPBase userSelection) {
			Logger.println(Logger.DEBUG, "[Detail Info] user slection:" + userSelection);
			/*
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					viewer.refresh();
				}
				
			});
			*/
			viewer.setInput(userSelection);
		}

	}

	private void createTableColumns() {
		TableViewerColumn columnName = createTableViewerColumn("Property Name", 200, 0);
		columnName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if ( element instanceof PropertyNameAndValue ) {
					return ((PropertyNameAndValue)element).name;
				}
				return null;
			}

			@Override
			public Image getImage(Object element) {
				return null;
			}
		});
		TableViewerColumn columnValue = createTableViewerColumn("Property Value", 200, 0);
		columnValue.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if ( element instanceof PropertyNameAndValue ) {
					return ((PropertyNameAndValue)element).value;
				}
				return null;
			}

			@Override
			public Image getImage(Object element) {
				return null;
			}
		});
	}
	
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.setContentProvider(new DetailInfoViewContentProvider());
		createTableColumns();
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		// Provide the input to the ContentProvider
		viewer.setInput(UPnPDeviceManager.getDefaultDeviceManager());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
			}
		});
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
