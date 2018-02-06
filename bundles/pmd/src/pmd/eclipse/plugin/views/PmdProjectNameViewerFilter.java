package pmd.eclipse.plugin.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import pmd.eclipse.plugin.markers.CheckstyleViolationMarker;

public class PmdProjectNameViewerFilter extends ViewerFilter {

	private String projectName;

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
		if (projectName == null) {
			return true;
		}
		return marker.getProjectName().equals(projectName);
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

}
