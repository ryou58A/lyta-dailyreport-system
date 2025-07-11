package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Report report) {

        Optional<Report> existingReport = reportRepository
                .findByEmployeeCodeAndReportDate(report.getEmployee().getCode(), report.getReportDate());

        if (existingReport.isPresent()) {
            return ErrorKinds.DATECHECK_ERROR;
        }

        report.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 日報更新
    @Transactional
    public ErrorKinds update(Report report) {
        Report currentReport = findByCode(report.getId());
        
        Optional<Report> existingReport = reportRepository
                .findByEmployeeCodeAndReportDate(report.getEmployee().getCode(), report.getReportDate());
        
        if (existingReport.isPresent() && !existingReport.get().getId().equals(report.getId())) {
            return ErrorKinds.DATECHECK_ERROR;
        }

        currentReport.setReportDate(report.getReportDate());
        currentReport.setTitle(report.getTitle());
        currentReport.setContent(report.getContent());
        currentReport.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(currentReport);
        return ErrorKinds.SUCCESS;
    }

    // 日報削除
    @Transactional
    public ErrorKinds delete(Integer id) {

        Report report = findByCode(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    // 日報一覧表示処理
    public List<Report> findAll() {
        return reportRepository.findAll();
    }
    
    // 日報一覧表示処理 一般ユーザー
    public List<Report> findByEmployee(Employee employee) {
        return reportRepository.findByEmployee(employee);
    }

    // 1件を検索
    public Report findByCode(Integer id) {
        // findByIdで検索
        Optional<Report> option = reportRepository.findById(id);
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        return report;
    }

}
