package com.techacademy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee.Role;
import com.techacademy.entity.Report;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // 日報一覧画面
    @GetMapping
    public String list(Model model, @AuthenticationPrincipal UserDetail userDetail) {
        
         List<Report> reportList;
         if (userDetail.getEmployee().getRole() == Role.ADMIN) {
             reportList = reportService.findAll();
         }else {
             reportList = reportService.findByEmployee(userDetail.getEmployee());
         }

        model.addAttribute("listSize", reportList.size());
        model.addAttribute("reportList", reportList);

        return "reports/list";
    }

    // 日報詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable("id") Integer id, Model model) {

        model.addAttribute("report", reportService.findByCode(id));
        return "reports/detail";
    }
    
 // 日報更新画面
    @GetMapping(value = "/{id}/update")
    public String showUpdateForm(@PathVariable("id") Integer id, Model model) {

        model.addAttribute("report", reportService.findByCode(id));
        return "reports/update";
    }

    // 日報更新処理
    @PostMapping(value = "/{id}/update")
    public String update(@PathVariable("id") Integer id, @Validated Report report, BindingResult res,
            Model model, @AuthenticationPrincipal UserDetail userDetail) {
        Report currentReport = reportService.findByCode(id);
        report.setId(currentReport.getId());
        report.setEmployee(currentReport.getEmployee());

        if (res.hasErrors()) {
            model.addAttribute("report", report);
            return "reports/update";
        }
        
        ErrorKinds result = reportService.update(report);
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", report);
            return "reports/update";
        }
        
        reportService.update(report);
        return "redirect:/reports";
    }

    // 日報新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Report report, @AuthenticationPrincipal UserDetail userDetail) {

        report.setEmployee(userDetail.getEmployee());

        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res, Model model,
            @AuthenticationPrincipal UserDetail userDetail) {

        // 入力チェック
        if (res.hasErrors()) {
            System.out.println("入力エラー");
            return create(report, userDetail);
        }

        // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
        // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)

        try {
            report.setEmployee(userDetail.getEmployee());
            ErrorKinds result = reportService.save(report);
            System.out.println(result);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(report, userDetail);
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DATECHECK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DATECHECK_ERROR));
            return create(report, userDetail);
        }

        return "redirect:/reports";
    }
    
    // 日報削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable("id") Integer id, @AuthenticationPrincipal UserDetail userDetail,
            Model model) {

        ErrorKinds result = reportService.delete(id);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", reportService.findByCode(id));
            return "reports/detail";
        }

        return "redirect:/reports";
    }

}
