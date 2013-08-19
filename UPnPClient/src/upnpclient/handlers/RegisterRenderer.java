package upnpclient.handlers;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import com.elevenquest.sol.upnp.common.Logger;
import com.elevenquest.sol.upnp.model.UPnPDevice;
import com.elevenquest.sol.upnp.model.UPnPService;
import com.elevenquest.sol.upnp.service.rederingcontrol.RenderingControlService;

import upnpclient.model.ControlPointManager;

public class RegisterRenderer extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		ControlPointManager manager = ControlPointManager.getManager();
		UPnPDevice device = null;
		if (ControlPointManager.getManager().getUserSelectedItem() instanceof UPnPService) {
			UPnPService service = (UPnPService)ControlPointManager.getManager().getUserSelectedItem();
			device = service.getDevice();
		} else if (ControlPointManager.getManager().getUserSelectedItem() instanceof UPnPDevice) {
			device = (UPnPDevice)ControlPointManager.getManager().getUserSelectedItem();
		}
		Vector<UPnPService> serviceList = device.getSerivces();
		boolean isRenderer = false;
		RenderingControlService rcs = null;
		for ( UPnPService oneService : serviceList) {
			if ( oneService instanceof RenderingControlService )
				isRenderer = true;
		}
		if ( isRenderer )
			ControlPointManager.getManager().getDefaultControlPoint().setRenderingService(rcs);
		else
			Logger.println(Logger.WARNING, "[Register Renderer] failed. User selected item or its parent item is not a Renderer service.");
		manager.getDefaultControlPoint().setRenderingService(rcs);
		return null;
	}

}
