package com.dk.jasper.service;

import com.dk.jasper.model.Jasper;
import org.springframework.stereotype.Service;

@Service
public interface JasperService {
    Jasper getFormDataAndReport(long groupId, String templateNo, String fileTemplateNo);
    String getFormReportByPath(String path);
    String getPathFile(String formReport, String formData);
}
