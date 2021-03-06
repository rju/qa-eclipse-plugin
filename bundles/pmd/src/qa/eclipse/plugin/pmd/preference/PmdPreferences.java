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
package qa.eclipse.plugin.pmd.preference;

import java.io.File;
// architectural hints: do not use plugin-specific types to avoid a cascade of class compilings
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import net.sourceforge.pmd.RuleSets;
import qa.eclipse.plugin.bundles.common.FileUtil;

public class PmdPreferences {

	public static final String INVALID_RULESET_FILE_PATH = "invalid/ruleset/file/path";

	public static final PmdPreferences INSTANCE = new PmdPreferences("qa.eclipse.plugin.pmd");

	public static final String PROP_KEY_CUSTOM_RULES_JARS = "customRulesJars";
	public static final String PROP_KEY_RULE_SET_FILE_PATH = "ruleSetFilePath";
	public static final String PROP_KEY_ENABLED = "enabled";

	private final Map<IProject, IScopeContext> projectScopeByProject = new HashMap<>();
	private final RuleSetFileLoader ruleSetFileLoader = new RuleSetFileLoader();

	private final String node;

	private PmdPreferences(final String node) {
		// private singleton constructor
		this.node = node;
	}

	public IEclipsePreferences getDefaultPreferences() {
		final IEclipsePreferences preferences = DefaultScope.INSTANCE.getNode(this.node);
		return preferences;
	}

	public IEclipsePreferences getEclipseScopedPreferences() {
		final IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(this.node);
		return preferences;
	}

	public IEclipsePreferences getEclipseEditorPreferences() {
		return InstanceScope.INSTANCE.getNode("org.eclipse.ui.editors");
	}

	public synchronized IEclipsePreferences getProjectScopedPreferences(final IProject project) {
		final IEclipsePreferences preferences;

		final IScopeContext projectPref;
		if (this.projectScopeByProject.containsKey(project)) {
			projectPref = this.projectScopeByProject.get(project);
			preferences = projectPref.getNode(this.node);
		} else {
			projectPref = new ProjectScope(project);
			this.projectScopeByProject.put(project, projectPref);

			preferences = projectPref.getNode(this.node);
			preferences.addPreferenceChangeListener(new PmdPreferenceChangeListener(project));
		}

		return preferences;
	}

	public RuleSets loadUpdatedRuleSet(final IEclipsePreferences preferences, final IProject project,
			final File eclipseProjectPath, final ClassLoader osgiClassLoaderWithCustomRules) {
		final String ruleSetFilePathValue = preferences.get(PmdPreferences.PROP_KEY_RULE_SET_FILE_PATH, PmdPreferences.INVALID_RULESET_FILE_PATH);
		final File ruleSetFile = FileUtil.makeAbsoluteFile(ruleSetFilePathValue, eclipseProjectPath);
		final String ruleSetFilePath = ruleSetFile.toString();
		// (re)load the project-specific ruleset file
		return this.ruleSetFileLoader.load(ruleSetFilePath, project, osgiClassLoaderWithCustomRules);
	}

}
