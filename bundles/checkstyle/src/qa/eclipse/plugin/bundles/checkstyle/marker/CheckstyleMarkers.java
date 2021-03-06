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
package qa.eclipse.plugin.bundles.checkstyle.marker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import qa.eclipse.plugin.bundles.checkstyle.SplitUtils;

public final class CheckstyleMarkers {

	private static final int IMARKER_SEVERITY_OTHERS = 3;

	/** marker to delete violation and error markers */
	public static final String ABSTRACT_CHECKSTYLE_COMMON_MARKER = "qa.eclipse.plugin.checkstyle.markers.common";
	/** marker to identify violation marker for the violations view */
	public static final String ABSTRACT_CHECKSTYLE_VIOLATION_MARKER = "qa.eclipse.plugin.checkstyle.markers.violation";
	public static final String ERROR_CHECKSTYLE_VIOLATION_MARKER = CheckstyleMarkers.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER + ".error";
	public static final String WARNING_CHECKSTYLE_VIOLATION_MARKER = CheckstyleMarkers.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER + ".warning";
	public static final String INFO_CHECKSTYLE_VIOLATION_MARKER = CheckstyleMarkers.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER + ".info";
	public static final String IGNORE_CHECKSTYLE_VIOLATION_MARKER = CheckstyleMarkers.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER + ".ignore";
	public static final String EXCEPTION_CHECKSTYLE_MARKER = CheckstyleMarkers.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER + ".exception";

	private static final Map<Integer, String> MARKER_TYPE_BY_PRIORITY = new HashMap<Integer, String>();

	static {
		CheckstyleMarkers.MARKER_TYPE_BY_PRIORITY.put(SeverityLevel.ERROR.ordinal(), CheckstyleMarkers.ERROR_CHECKSTYLE_VIOLATION_MARKER);
		CheckstyleMarkers.MARKER_TYPE_BY_PRIORITY.put(SeverityLevel.WARNING.ordinal(), CheckstyleMarkers.WARNING_CHECKSTYLE_VIOLATION_MARKER);
		CheckstyleMarkers.MARKER_TYPE_BY_PRIORITY.put(SeverityLevel.INFO.ordinal(), CheckstyleMarkers.INFO_CHECKSTYLE_VIOLATION_MARKER);
		CheckstyleMarkers.MARKER_TYPE_BY_PRIORITY.put(SeverityLevel.IGNORE.ordinal(), CheckstyleMarkers.IGNORE_CHECKSTYLE_VIOLATION_MARKER);
	}

	private CheckstyleMarkers() {
		// utility class
	}

	/**
	 * @see {@link com.puppycrawl.tools.checkstyle.api.SeverityLevel}
	 */
	public static final String ATTR_KEY_PRIORITY = "checkstyle.priority";
	public static final String ATTR_KEY_CHECK_PACKAGE = "checkstyle.check_package";
	public static final String ATTR_KEY_CHECK_NAME = "checkstyle.check_name";

	public static void appendViolationMarker(final IFile eclipseFile, final AuditEvent violation) throws CoreException {
		final int priority = violation.getSeverityLevel().ordinal();
		final String markerType = CheckstyleMarkers.MARKER_TYPE_BY_PRIORITY.get(priority);

		final IMarker marker = eclipseFile.createMarker(markerType);
		marker.setAttribute(IMarker.MESSAGE, violation.getMessage());
		marker.setAttribute(IMarker.LINE_NUMBER, violation.getLine());
		// marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);

		marker.setAttribute(CheckstyleMarkers.ATTR_KEY_PRIORITY, priority);
		// getModuleId() always returns null
		final String checkClassName = violation.getSourceName();
		final List<String> checkClassNameParts = SplitUtils.split(checkClassName).once().at('.').fromTheRight();
		marker.setAttribute(CheckstyleMarkers.ATTR_KEY_CHECK_PACKAGE, checkClassNameParts.get(0));
		marker.setAttribute(CheckstyleMarkers.ATTR_KEY_CHECK_NAME, checkClassNameParts.get(1));

		// marker.setAttribute(IMarker.CHAR_START, violation.getBeginColumn());
		// marker.setAttribute(IMarker.CHAR_END, violation.getEndColumn());

		// whether it is displayed as error, warning, info or other in the Problems View
		// marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	}

	public static IMarker[] findAllInWorkspace() {
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		final IMarker[] markers;
		try {
			markers = workspaceRoot.findMarkers(CheckstyleMarkers.ABSTRACT_CHECKSTYLE_VIOLATION_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (final CoreException e) {
			throw new IllegalStateException(e);
		}
		return markers;
	}

	public static void deleteMarkers(final IResource resource) throws CoreException {
		resource.deleteMarkers(CheckstyleMarkers.ABSTRACT_CHECKSTYLE_COMMON_MARKER, true, IResource.DEPTH_INFINITE);
	}

	public static void appendProcessingErrorMarker(final IFile eclipseFile, final Throwable throwable) throws CoreException {
		final IMarker marker = eclipseFile.createMarker(CheckstyleMarkers.EXCEPTION_CHECKSTYLE_MARKER);
		// whether it is displayed as error, warning, info or other in the Problems View
		marker.setAttribute(IMarker.SEVERITY, CheckstyleMarkers.IMARKER_SEVERITY_OTHERS);
		marker.setAttribute(IMarker.MESSAGE, throwable.toString());
		// marker.setAttribute(IMarker.LINE_NUMBER, violation.getBeginLine());
		marker.setAttribute(IMarker.LOCATION, eclipseFile.getFullPath().toString());
	}
}
