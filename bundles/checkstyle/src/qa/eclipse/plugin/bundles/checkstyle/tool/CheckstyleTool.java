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
package qa.eclipse.plugin.bundles.checkstyle.tool;
// may not contain anything from the Eclipse API

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader.IgnoredModulesOptions;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.PropertyResolver;
import com.puppycrawl.tools.checkstyle.ThreadModeSettings;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

import qa.eclipse.plugin.bundles.checkstyle.EclipsePlatform;
import qa.eclipse.plugin.bundles.checkstyle.preference.CheckstylePreferences;
import qa.eclipse.plugin.bundles.common.FileUtil;
import qa.eclipse.plugin.bundles.common.PreferencesUtil;
import qa.eclipse.plugin.bundles.common.ProjectUtil;

public class CheckstyleTool {

	private final Checker checker;

	public CheckstyleTool() {
		this.checker = new Checker();
	}

	/**
	 * You need to catch potential runtime exceptions if you call this method.
	 *
	 * @param eclipseFiles
	 * @param checkstyleListener
	 */
	// FIXME remove Eclipse API
	public void startAsyncAnalysis(final List<IFile> eclipseFiles, final CheckstyleListener checkstyleListener) {
		final IFile file = eclipseFiles.get(0);
		final IProject project = file.getProject();

		this.checker.setBasedir(null);
		// checker.setCacheFile(fileName);

		try {
			this.checker.setCharset(project.getDefaultCharset());
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		} catch (final CoreException e) {
			throw new IllegalStateException(e);
		}

		final IEclipsePreferences projectPreferences = CheckstylePreferences.INSTANCE.getProjectScopedPreferences(project);
		final File eclipseProjectPath = ProjectUtil.getProjectPath(project);

		final Locale platformLocale = EclipsePlatform.getLocale();
		this.checker.setLocaleLanguage(platformLocale.getLanguage());
		this.checker.setLocaleCountry(platformLocale.getCountry());

		// ClassLoader classLoader2 = CommonUtils.class.getClassLoader();
		// URL emptyResourceName = CommonUtils.class.getResource("");
		// URL slashResourceName = CommonUtils.class.getResource("/");
		// URL relResourceName =
		// CommonUtils.class.getResource("config/cs-suppressions.xml");
		// URL absResourceName =
		// CommonUtils.class.getResource("/config/cs-suppressions.xml");
		// adds the Eclipse project's path to Checkstyle's class loader to find the file
		// of the SuppressFilter module
		// DOES NOT WORK since the class loader is not used to resolve the file path

		// Possibilities: pass URL, absolute file path, or class path file path

		// URL[] classLoaderUrls;
		// try {
		// classLoaderUrls = new URL[] { eclipseProjectPath.toURI().toURL() };
		// } catch (MalformedURLException e) {
		// throw new IllegalStateException(e);
		// }
		// ClassLoader classLoader = new URLClassLoader(classLoaderUrls,
		// Thread.currentThread().getContextClassLoader());
		// checker.setClassLoader(classLoader);

		final String configFilePath = CheckstylePreferences.INSTANCE.loadConfigFilePath(projectPreferences);
		final File configFile = FileUtil.makeAbsoluteFile(configFilePath, eclipseProjectPath);
		final String absoluteConfigFilePath = configFile.toString();

		/** Auto-set the config loc directory. */
		final Properties properties = new Properties();
		properties.put("config_loc", configFile.getAbsoluteFile().getParent());

		final PropertyResolver propertyResolver = new PropertiesExpander(properties);

		final IgnoredModulesOptions ignoredModulesOptions = IgnoredModulesOptions.OMIT;
		final ThreadModeSettings threadModeSettings = ThreadModeSettings.SINGLE_THREAD_MODE_INSTANCE;
		Configuration configuration;
		try {
			configuration = ConfigurationLoader.loadConfiguration(absoluteConfigFilePath, propertyResolver,
					ignoredModulesOptions, threadModeSettings);
		} catch (final CheckstyleException e) {
			final String message = String.format("Could not load Checkstyle configuration from '%s'.",
					absoluteConfigFilePath);
			throw new IllegalStateException(message, e);
		}

		// Configuration suppressFilterConfiguration =
		// resolveSuppressFilterConfiguration(configuration);
		// if (suppressFilterConfiguration != null) {
		// try {
		// String filePath = suppressFilterConfiguration.getAttribute(CONFIG_PROP_FILE);
		//
		// } catch (CheckstyleException e) {
		// throw new IllegalStateException(e);
		// }
		// }

		final String[] customModuleJarPaths = PreferencesUtil.loadCustomJarPaths(projectPreferences,
				CheckstylePreferences.PROP_KEY_CUSTOM_MODULES_JAR_PATHS);

		final URL[] moduleClassLoaderUrls = FileUtil.filePathsToUrls(eclipseProjectPath, customModuleJarPaths);
		try (URLClassLoader moduleClassLoader = new URLClassLoader(moduleClassLoaderUrls,
				this.getClass().getClassLoader())) {
			this.checker.setModuleClassLoader(moduleClassLoader);

			try {
				this.checker.configure(configuration);
			} catch (final CheckstyleException e) {
				throw new IllegalStateException(e);
			}

			this.checker.addListener(checkstyleListener);
			this.checker.addBeforeExecutionFileFilter(checkstyleListener);

			// https://github.com/checkstyle/eclipse-cs/blob/master/net.sf.eclipsecs.core/src/net/sf/eclipsecs/core/builder/CheckerFactory.java#L275

			final List<File> files = new ArrayList<>();

			for (final IFile eclipseFile : eclipseFiles) {
				final File sourceCodeFile = eclipseFile.getLocation().toFile().getAbsoluteFile();
				files.add(sourceCodeFile);
			}

			try {
				this.checker.process(files);
			} catch (final CheckstyleException e) {
				throw new IllegalStateException(e);
			}
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}

		// process each file separately to be able to skip it
		// for (File fileToCheck : files) {
		// for (IFile eclipseFile : eclipseFiles) {
		// final File sourceCodeFile =
		// eclipseFile.getLocation().toFile().getAbsoluteFile();
		//
		// List<File> filesToCheck = Arrays.asList(sourceCodeFile);
		// try {
		// checker.process(filesToCheck);
		// } catch (CheckstyleException e) {
		// if (e.getCause() instanceof OperationCanceledException) {
		// throw new IllegalStateException(e);
		// } else {
		// // skip file upon syntax error
		// try {
		// CheckstyleMarkers.appendProcessingErrorMarker(eclipseFile, e);
		// } catch (CoreException e1) {
		// // ignore if marker could not be created
		// }
		// }
		// }
		// }
	}

}
