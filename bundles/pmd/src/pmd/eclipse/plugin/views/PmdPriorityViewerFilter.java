package pmd.eclipse.plugin.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import net.sourceforge.pmd.RulePriority;
import pmd.eclipse.plugin.markers.CheckstyleViolationMarker;

class PmdPriorityViewerFilter extends ViewerFilter {

	private int lowestPriority = RulePriority.LOW.getPriority();

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		CheckstyleViolationMarker marker = (CheckstyleViolationMarker) element;
		return marker.getPriority() <= lowestPriority;
	}

	public void setLowestPriority(int lowestPriority) {
		this.lowestPriority = lowestPriority;
	}

	public int getLowestPriority() {
		return lowestPriority;
	}

}
