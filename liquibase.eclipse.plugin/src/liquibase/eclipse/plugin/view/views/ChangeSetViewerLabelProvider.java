package liquibase.eclipse.plugin.view.views;

import liquibase.eclipse.plugin.Activator;
import liquibase.eclipse.plugin.model.ChangeSet;
import liquibase.eclipse.plugin.model.ChangeSetStatus;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.swt.graphics.Image;


/**
 * Provides labels for the TableViewer in {@link LiquibaseView}.
 * 
 * @author afinke
 *
 */
public class ChangeSetViewerLabelProvider extends ObservableMapLabelProvider {

	private static final Image CHECKED = 
			Activator.getImageDescriptor("icons/checked.gif").createImage();
	private static final Image UNCHECKED = 
			Activator.getImageDescriptor("icons/unchecked.gif").createImage();
	private static final Image CHANGESET = 
			Activator.getImageDescriptor("icons/changeset.gif").createImage();
	private static final Image HOURGLASS = 
			Activator.getImageDescriptor("icons/hourglass.gif").createImage();
	private static final Image ERROR = 
			Activator.getImageDescriptor("icons/error.gif").createImage();

	public ChangeSetViewerLabelProvider(IObservableMap[] labelMaps) {
		super(labelMaps);
	}

	@Override
	public String getText(Object element) {
		return null;
	}
	
	@Override
	public Image getImage(Object element) {
		return null;
	}
	
	@Override
	public String getColumnText(Object element, int columnIndex) {
		switch (columnIndex) {
			case 0:
				return ((ChangeSet) element).getId();
			case 1:
				switch(((ChangeSet) element).getStatus()) {
					case UNRUN: return "";
					case RUNNING: return " running";
					case EXECUTED: 
						if(((ChangeSet) element).getStatus().equals(ChangeSetStatus.FORMER_EXECUTED)) {
							return null;
						}else {
							// convert time from ms in s
							long executionTimeInMS = ((ChangeSet) element).getExecutionTime();
							long executionTimeInS = (executionTimeInMS / 1000) % 60;
							if(executionTimeInS < 1) {
								return " in < 1 s";
							}else {
								return " in " + executionTimeInS + " s";
							}
						}
					default: return null;
				}
			default:
				return null;
		}
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		switch (columnIndex) {
			case 0:
				return CHANGESET;
			case 1:
				switch(((ChangeSet) element).getStatus()) {
					case UNRUN: return UNCHECKED;
					case RUNNING: return HOURGLASS;
					case EXECUTED: return CHECKED;
					case ERROR: return ERROR;
					case FORMER_EXECUTED: return CHECKED;
					default: return null;
				}
			default:
				return null;
		}
	}

}
