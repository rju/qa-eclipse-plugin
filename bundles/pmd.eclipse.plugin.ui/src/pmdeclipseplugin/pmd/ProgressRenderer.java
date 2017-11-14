package pmdeclipseplugin.pmd;

import java.io.IOException;

import org.eclipse.core.runtime.SubMonitor;

import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.renderers.AbstractRenderer;
import net.sourceforge.pmd.util.datasource.DataSource;

public class ProgressRenderer extends AbstractRenderer {

	private SubMonitor subMonitor;

	public ProgressRenderer(SubMonitor subMonitor) {
		super(ProgressRenderer.class.getName(), "Renderer that informs about the progress");
		this.subMonitor = subMonitor;
	}

	@Override
	public String defaultFileExtension() {
		return null;
	}

	@Override
	public void start() throws IOException {
		// do nothing
	}

	@Override
	public void startFileAnalysis(DataSource dataSource) {
		subMonitor.split(1);
	}

	@Override
	public void renderFileReport(Report report) throws IOException {
		// do nothing
	}

	@Override
	public void end() throws IOException {
		// do nothing
	}

}
