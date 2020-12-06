package com.ing.fmjavaguild.worker;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportRowMapper implements RowMapper<Report> {

    @Override
    public Report mapRow(ResultSet resultSet, int i) throws SQLException {
        Report report = new Report();
        report.setId(resultSet.getLong("id"));
        report.setTicker(resultSet.getString("ticker"));
        report.setSecurity(resultSet.getString("security"));
        report.setYear(resultSet.getInt("year"));
        report.setEps(resultSet.getDouble("eps"));
        report.setYearGrossProfit(resultSet.getDouble("year_gross_profit"));
        report.setYearTotalAssets(resultSet.getDouble("year_total_assets"));
        report.setYearTotalLiabilities(resultSet.getDouble("year_total_liabilities"));
        report.setYearMaxHigh(resultSet.getDouble("year_max_high"));
        report.setYearMinLow(resultSet.getDouble("year_min_low"));
        return report;
    }
}
