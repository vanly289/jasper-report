package com.dk.jasper.controller;

import com.dk.jasper.model.Jasper;
import com.dk.jasper.serviceImpl.JasperServiceImpl;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
public class JasperController {
    @Autowired
    private JasperServiceImpl jasperService;

    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> exportPDFByString(HttpServletRequest request, HttpSession session,
                                               @RequestParam(value = "groupId", required = false) long groupId, @RequestParam(value = "templateNo", required = false) String templateNo,
                                               @RequestParam(value = "fileTemplateNo", required = false) String fileTemplateNo,
                                               @RequestParam(value = "destFileName", required = false) String destFileName,
                                               @RequestParam(value = "formData", required = false) String formData,
                                               @RequestParam(value = "pathFileName", required = false) String pathFileName)
            throws IOException, JRException {

        InputStream targetStream = null;
        String reportContents = "";
        if (pathFileName == null || "".equals(pathFileName)) {
            Jasper jasper = jasperService.getFormDataAndReport(groupId, templateNo, fileTemplateNo);
            targetStream = new ByteArrayInputStream(jasper.getFormReport().getBytes("UTF-8"));
            reportContents = jasper.getFormData();
        } else {
            targetStream = getClass().getResourceAsStream(pathFileName);
            reportContents = formData;
        }
        JasperReport jasperReport = JasperCompileManager.compileReport(targetStream);
        JSONObject result = new JSONObject();
        try {
            try {
                ByteArrayInputStream jsonDataStream = new ByteArrayInputStream(reportContents.getBytes("UTF-8"));
                JsonDataSource ds = new JsonDataSource(jsonDataStream);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, ds);
                JasperExportManager.exportReportToPdfFile(jasperPrint, destFileName);
                result.put("destFileName", destFileName);
            } catch (JRException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return ResponseEntity.status(HttpStatus.OK).body(result.toString());
    }

    @RequestMapping(value = "/api/export/report", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> exportReport(HttpServletRequest request, HttpSession session,
                                          @FormParam(value = "formData") String formData,
                                          @RequestParam(value = "destFileName", required = false) String destFileName,
                                          @RequestParam(value = "pathFileTemp", required = false) String pathFileTemp,
                                          @RequestParam(value = "fileName", required = false) String fileName,
                                          @RequestParam(value = "typeReport", required = false) String typeReport)
            throws IOException, JRException {

        System.out.println("destFileName: " + destFileName);
        System.out.println("pathFileName: " + pathFileTemp);
        System.out.println("typeReport: " + typeReport);
        System.out.println("fileName: " + fileName);
        System.out.println("formData: " + formData);
        InputStream inputStream = new FileInputStream(pathFileTemp + fileName);
//        JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
//        JasperReport jasperReport = JasperCompileManager
//                .compileReport(jasperDesign);
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
        Map parameters = new HashMap();
        parameters.put("SUBREPORT_DIR", pathFileTemp);
        JSONObject result = new JSONObject();
        try {
            try {
                ByteArrayInputStream jsonDataStream = new ByteArrayInputStream(formData.getBytes("UTF-8"));

                JsonDataSource ds = new JsonDataSource(jsonDataStream);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, ds);
                if ("pdf".equals(typeReport)) {
                    destFileName = destFileName + ".pdf";
                    JasperExportManager.exportReportToPdfFile(jasperPrint, destFileName);
                } else {
                    destFileName = destFileName + ".xls";
                    JRXlsExporter xlsExporter = new JRXlsExporter();

                    xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(destFileName));
                    SimpleXlsReportConfiguration xlsReportConfiguration = new SimpleXlsReportConfiguration();
                    xlsReportConfiguration.setOnePagePerSheet(false);
                    xlsReportConfiguration.setRemoveEmptySpaceBetweenRows(true);
                    xlsReportConfiguration.setDetectCellType(false);
                    xlsReportConfiguration.setWhitePageBackground(false);
                    xlsExporter.setConfiguration(xlsReportConfiguration);
                    xlsExporter.exportReport();
                }
                result.put("destFileName", destFileName);
            } catch (JRException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return ResponseEntity.status(HttpStatus.OK).body(result.toString());
    }

    @RequestMapping(value = "/api/export", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> export(HttpServletRequest request, HttpSession session,
                                    @RequestParam(value = "destFileName", required = false) String destFileName,
                                    @FormParam(value = "formReport") String formReport,
                                    @FormParam(value = "formData") String formData,
                                    @RequestParam(value = "typeReport", required = false) String typeReport)
            throws IOException, JRException {
        Jasper jasper = jasperService.getFormDataAndReport(0, "templateNo", "fileTemplateNo");
        formReport = jasper.getFormReport();
        InputStream targetStream = new ByteArrayInputStream(formReport.getBytes("UTF-8"));

        System.out.println("formData: " + formData);
        System.out.println("destFileName: " + destFileName);
        System.out.println("formReport: " + formReport);
        System.out.println("typeReport: " + typeReport);
        JasperReport jasperReport = JasperCompileManager.compileReport(targetStream);

        JSONObject result = new JSONObject();
        try {
            try {
                ByteArrayInputStream jsonDataStream = new ByteArrayInputStream(formData.getBytes("UTF-8"));
                JsonDataSource ds = new JsonDataSource(jsonDataStream);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, ds);
                if ("pdf".equals(typeReport)) {
                    destFileName = destFileName + ".pdf";
                    JasperExportManager.exportReportToPdfFile(jasperPrint, destFileName);
                } else {
                    destFileName = destFileName + ".xls";
                    JRXlsExporter xlsExporter = new JRXlsExporter();

                    xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(destFileName));
                    SimpleXlsReportConfiguration xlsReportConfiguration = new SimpleXlsReportConfiguration();
                    xlsReportConfiguration.setOnePagePerSheet(false);
                    xlsReportConfiguration.setRemoveEmptySpaceBetweenRows(true);
                    xlsReportConfiguration.setDetectCellType(false);
                    xlsReportConfiguration.setWhitePageBackground(false);
                    xlsExporter.setConfiguration(xlsReportConfiguration);
                    xlsExporter.exportReport();
                }
                result.put("destFileName", destFileName);
            } catch (JRException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return ResponseEntity.status(HttpStatus.OK).body(result.toString());
    }

    @RequestMapping(value = "/api/export/1", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> export1(HttpServletRequest request, HttpSession session,
                                     @RequestParam(value = "pathJrxml", required = false) String pathJrxml,
                                     @RequestParam(value = "pathData", required = false) String pathData,
                                     @RequestParam(value = "destFileName", required = false) String destFileName,
                                     @RequestParam(value = "typeReport", required = false) String typeReport)
            throws IOException, JRException {
        InputStream inputStream = new FileInputStream(pathJrxml);
        File fileData = new File(pathData);
        byte[] bytes = FileUtils.readFileToByteArray(fileData);
        JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

        JSONObject result = new JSONObject();
        try {
            try {
                ByteArrayInputStream jsonDataStream = new ByteArrayInputStream(bytes);
                JsonDataSource ds = new JsonDataSource(jsonDataStream);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, ds);
                if ("pdf".equals(typeReport)) {
                    destFileName = destFileName + ".pdf";
                    JasperExportManager.exportReportToPdfFile(jasperPrint, destFileName);
                } else {
                    destFileName = destFileName + ".xls";
                    JRXlsExporter xlsExporter = new JRXlsExporter();

                    xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(destFileName));
                    SimpleXlsReportConfiguration xlsReportConfiguration = new SimpleXlsReportConfiguration();
                    xlsReportConfiguration.setOnePagePerSheet(false);
                    xlsReportConfiguration.setRemoveEmptySpaceBetweenRows(true);
                    xlsReportConfiguration.setDetectCellType(false);
                    xlsReportConfiguration.setWhitePageBackground(false);
                    xlsExporter.setConfiguration(xlsReportConfiguration);
                    xlsExporter.exportReport();
                }
                result.put("destFileName", destFileName);
            } catch (JRException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
        return ResponseEntity.status(HttpStatus.OK).body(result.toString());
    }
}
