package com.dk.jasper.dao;


import com.dk.jasper.model.Jasper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional
@Repository
public class JasperDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private Log LOGGER = LogFactory.getLog(JasperDAO.class);

    public Jasper getJasper(long groupId, String templateNo, String fileTemplateNo) {
        String sql = " select groupId, formReport, sampleData as formData from opencps_dossierpart where dossierPartId = 111 ";
        Jasper jasper = null;
        try {
            List<Jasper> jasperList = jdbcTemplate.query(sql,
                    new BeanPropertyRowMapper<Jasper>(Jasper.class));
            if (jasperList != null && jasperList.size() > 0) {
                jasper = jasperList.get(0);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return jasper;
    }
}
