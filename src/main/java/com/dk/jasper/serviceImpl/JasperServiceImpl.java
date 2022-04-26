package com.dk.jasper.serviceImpl;


import com.dk.jasper.dao.JasperDAO;
import com.dk.jasper.model.Jasper;
import com.dk.jasper.service.JasperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JasperServiceImpl implements JasperService {
    @Autowired
    private JasperDAO jasperDAO;

    @Override
    public Jasper getFormDataAndReport(long groupId, String templateNo, String fileTemplateNo) {

        return jasperDAO.getJasper(groupId, templateNo, fileTemplateNo);
    }

    @Override
    public String getFormReportByPath(String path) {
        return null;
    }

    @Override
    public String getPathFile(String formReport, String formData) {
        return null;
    }


}
