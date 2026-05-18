package org.isf.stat.gui.report;

import org.isf.menu.manager.Context;
import org.isf.stat.dto.JasperReportResultDto;
import org.isf.stat.manager.JasperReportsManager;
import org.isf.utils.jobjects.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericReportPregnancy extends DisplayReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericReportPharmaceuticalStockWard.class);
    private JasperReportsManager jasperReportsManager = Context.getApplicationContext().getBean(JasperReportsManager.class);

    public GenericReportPregnancy(Integer pregId) {
        try {
            JasperReportResultDto jasperReportResultDto = jasperReportsManager.getPregnancyReportPdf(Long.valueOf(pregId));
            showReport(jasperReportResultDto);
        } catch (Exception e) {
            LOGGER.error("", e);
            MessageDialog.error(null, "angal.stat.reporterror.msg");
        }
    }
}
