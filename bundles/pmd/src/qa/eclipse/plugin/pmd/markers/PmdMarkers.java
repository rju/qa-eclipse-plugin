/***************************************************************************
 * Copyright (C) 2019 Christian Wulf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package qa.eclipse.plugin.pmd.markers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleViolation;

public final class PmdMarkers {

	/** marker to delete violation and error markers */
	public static final String ABSTRACT_PMD_VIOLATION_COMMON = "qa.eclipse.plugin.pmd.markers.common";
	/** marker to identify violation marker for the violations view. */
	public static final String ABSTRACT_PMD_VIOLATION_MARKER = "qa.eclipse.plugin.pmd.markers.violation";
	public static final String HIGH_PMD_VIOLATION_MARKER = PmdMarkers.ABSTRACT_PMD_VIOLATION_MARKER + ".high";
	public static final String MEDIUMHIGH_PMD_VIOLATION_MARKER = PmdMarkers.ABSTRACT_PMD_VIOLATION_MARKER + ".mediumhigh";
	public static final String MEDIUM_PMD_VIOLATION_MARKER = PmdMarkers.ABSTRACT_PMD_VIOLATION_MARKER + ".medium";
	public static final String MEDIUMLOW_PMD_VIOLATION_MARKER = PmdMarkers.ABSTRACT_PMD_VIOLATION_MARKER + ".mediumlow";
	public static final String LOW_PMD_VIOLATION_MARKER = PmdMarkers.ABSTRACT_PMD_VIOLATION_MARKER + ".low";
	public static final String PMD_ERROR_MARKER = PmdMarkers.ABSTRACT_PMD_VIOLATION_MARKER + ".error";

	private static final Map<Integer, String> MARKER_TYPE_BY_PRIORITY = new HashMap<Integer, String>();

	static {
		PmdMarkers.MARKER_TYPE_BY_PRIORITY.put(RulePriority.HIGH.getPriority(), PmdMarkers.HIGH_PMD_VIOLATION_MARKER);
		PmdMarkers.MARKER_TYPE_BY_PRIORITY.put(2, PmdMarkers.MEDIUMHIGH_PMD_VIOLATION_MARKER);
		PmdMarkers.MARKER_TYPE_BY_PRIORITY.put(3, PmdMarkers.MEDIUM_PMD_VIOLATION_MARKER);
		PmdMarkers.MARKER_TYPE_BY_PRIORITY.put(4, PmdMarkers.MEDIUMLOW_PMD_VIOLATION_MARKER);
		PmdMarkers.MARKER_TYPE_BY_PRIORITY.put(5, PmdMarkers.LOW_PMD_VIOLATION_MARKER);
	}

	private PmdMarkers() {
		// utility class
	}

	/**
	 * @see {@link net.sourceforge.pmd.RulePriority}
	 */
	public static final String ATTR_KEY_PRIORITY = "pmd.priority";
	public static final String ATTR_KEY_RULENAME = "pmd.rulename";
	public static final String ATTR_KEY_RULESETNAME = "pmd.rulesetname";

	public static void appendViolationMarker(final IFile eclipseFile, final RuleViolation violation)
			throws CoreException {
		final int priority = violation.getRule().getPriority().getPriority();
		final String markerType = PmdMarkers.MARKER_TYPE_BY_PRIORITY.get(priority);

		final IMarker marker = eclipseFile.createMarker(markerType);
		marker.setAttribute(IMarker.MESSAGE, violation.getDescription());
		marker.setAttribute(IMarker.LINE_NUMBER, violation.getBeginLine());
		// marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);

		marker.setAttribute(PmdMarkers.ATTR_KEY_PRIORITY, priority);
		marker.setAttribute(PmdMarkers.ATTR_KEY_RULENAME, violation.getRule().getName());
		marker.setAttribute(PmdMarkers.ATTR_KEY_RULESETNAME, violation.getRule().getRuleSetName());

		// marker.setAttribute(IMarker.CHAR_START, violation.getBeginColumn());
		// marker.setAttribute(IMarker.CHAR_END, violation.getEndColumn());

		// whether it is displayed as error, warning, info or other in the Problems View
		// marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	}

	public static void appendViolationMarker(final IProject eclipseFile, final String message) throws CoreException {
		final String markerType = PmdMarkers.HIGH_PMD_VIOLATION_MARKER;

		final IMarker marker = eclipseFile.createMarker(markerType);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.LINE_NUMBER, "");

		marker.setAttribute(PmdMarkers.ATTR_KEY_PRIORITY, RulePriority.HIGH.getPriority());
		marker.setAttribute(PmdMarkers.ATTR_KEY_RULENAME, "ConfigurationError");
		marker.setAttribute(PmdMarkers.ATTR_KEY_RULESETNAME, "");
	}

	public static IMarker[] findAllInWorkspace() {
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		final IMarker[] markers;
		try {
			markers = workspaceRoot.findMarkers(PmdMarkers.ABSTRACT_PMD_VIOLATION_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (final CoreException e) {
			throw new IllegalStateException(e);
		}
		return markers;
	}

	public static void deleteMarkers(final IResource resource) throws CoreException {
		resource.deleteMarkers(PmdMarkers.ABSTRACT_PMD_VIOLATION_COMMON, true, IResource.DEPTH_INFINITE);
	}
}
