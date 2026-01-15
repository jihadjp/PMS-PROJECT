package com.jptechgenius.payroll.controller;

import com.jptechgenius.payroll.model.Employee;
import com.jptechgenius.payroll.repository.DesignationRepository;
import com.jptechgenius.payroll.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * [Employee Controller]
 * ---------------------
 * Ei class ta Employee related sobkaj handle kore.
 * Jemon: Employee list dekha, notun employee add kora, edit kora, delete kora.
 * Sob URL '/employees' diye shuru hobe.
 */
@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DesignationRepository designationRepository;

    // Image upload korle ei folder e save hobe. (Project er root folder e)
    private final String UPLOAD_DIR = "user-photos/";

    // Constructor Injection: Spring automatic service gulo load kore dibe.
    public EmployeeController(EmployeeService employeeService, DesignationRepository designationRepository) {
        this.employeeService = employeeService;
        this.designationRepository = designationRepository;
    }

    /**
     * [View Home Page]
     * Employee list dekhano hoy. Jodi search keyword thake, tahole filter kore dekhay.
     */
    @GetMapping
    public String viewHomePage(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            // Search box e kichu likhle ei part kaj korbe
            model.addAttribute("listEmployees", employeeService.searchEmployees(keyword));
            model.addAttribute("keyword", keyword);
        } else {
            // Kichu na likhle sob employee show korbe
            model.addAttribute("listEmployees", employeeService.getAllEmployees());
        }
        return "employee/index"; // employee/index.html page load hobe
    }

    /**
     * [Show Add Form]
     * Notun employee add korar jonno form open kore.
     * Dropdown e designation dekhanor jonno designation list o pathano hoy.
     */
    @GetMapping("/add")
    public String showNewEmployeeForm(Model model) {
        Employee employee = new Employee();
        model.addAttribute("employee", employee);
        model.addAttribute("designationList", designationRepository.findAll());
        return "employee/add";
    }

    /**
     * [Save Employee]
     * Form submit korle ei method call hoy. Ekhane validation, duplicate check,
     * ar image upload handle kora hoy.
     */
    @PostMapping("/save")
    public String saveEmployee(@Valid @ModelAttribute("employee") Employee employee,
                               BindingResult result,
                               @RequestParam("image") MultipartFile file,
                               Model model) throws IOException {

        // 1. Validation Check: Form e kono vul thakle (jemon nam khali) abar form e ferot pathabe.
        if (result.hasErrors()) {
            model.addAttribute("designationList", designationRepository.findAll());
            // ID thakle edit page, na thakle add page e jabe
            return (employee.getId() == null) ? "employee/add" : "employee/update";
        }

        // 2. Duplicate Phone Number and Email Check
        // Database e check kora hocche ei phone number and Email aage theke ache kina.
        List<Employee> allEmployees = employeeService.getAllEmployees();
        for (Employee emp : allEmployees) {
            // Check Phone
            if (emp.getPhoneNumber() != null &&
                    emp.getPhoneNumber().equals(employee.getPhoneNumber()) &&
                    !emp.getId().equals(employee.getId())) {

                model.addAttribute("error", "Duplicate Entry! Phone number already exists.");
                model.addAttribute("designationList", designationRepository.findAll());
                return (employee.getId() == null) ? "employee/add" : "employee/update";
            }

            // Check Email
            if (emp.getEmail() != null &&
                    emp.getEmail().equalsIgnoreCase(employee.getEmail()) &&
                    !emp.getId().equals(employee.getId())) {

                model.addAttribute("error", "Duplicate Entry! Email address already exists.");
                model.addAttribute("designationList", designationRepository.findAll());
                return (employee.getId() == null) ? "employee/add" : "employee/update";
            }
        }

        // 3. Image Upload Logic
        if (!file.isEmpty()) {
            // Jodi notun chobi upload kore
            // Unique nam deyar jonno UUID use kora hoyeche jate ek namer dui chobi mix na hoy.
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);

            // Folder na thakle create korbe
            if (!Files.exists(Paths.get(UPLOAD_DIR))) {
                Files.createDirectories(Paths.get(UPLOAD_DIR));
            }

            // Chobi ta folder e save kora holo
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // Database e shudhu chobir path ta save hobe
            employee.setImageUrl("/user-photos/" + fileName);
        } else {
            // Jodi Edit er somoy notun chobi na dey, tahole purono chobi tai rekhe dibo.
            if (employee.getId() != null) {
                Employee existing = employeeService.getEmployeeById(employee.getId());
                if (existing.getImageUrl() != null) {
                    employee.setImageUrl(existing.getImageUrl());
                }
            }
        }

        // Sob thik thakle database e save kora holo
        employeeService.saveEmployee(employee);
        return "redirect:/employees";
    }

    /**
     * [Show Edit Form]
     * Edit button chaple ei method call hoy.
     * ID diye employee khuje form e data bosiye dey (Pre-fill).
     */
    @GetMapping("/edit/{id}")
    public String showFormForUpdate(@PathVariable(value = "id") Long id, Model model) {
        Employee employee = employeeService.getEmployeeById(id);
        model.addAttribute("employee", employee);
        model.addAttribute("designationList", designationRepository.findAll());
        return "employee/update";
    }

    /**
     * [Delete Employee - SECURITY CHECK]
     * Ekhane check kora hoy ke delete korte chacche.
     * Sudhu 'SUPER_ADMIN' holei delete kora jabe, onno Admin ra parbe na.
     */
    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable(value = "id") Long id, RedirectAttributes redirectAttributes) {

        // 1. Bortomane login kora user er authentication details nilam
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 2. Check korchi tar role ki 'SUPER_ADMIN' kina?
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().contains("SUPER_ADMIN"));

        if (!isSuperAdmin) {
            // Jodi Super Admin na hoy, tahole error message diye list page e pathiye dibo.
            // Delete hobe na.
            redirectAttributes.addFlashAttribute("error", "Access Denied! Only Super Admin can delete employees.");
            return "redirect:/employees";
        }

        // 3. Jodi Super Admin hoy, tahole delete kora hobe.
        this.employeeService.deleteEmployeeById(id);

        // Success message show kora hobe
        redirectAttributes.addFlashAttribute("success", "Employee deleted successfully.");
        return "redirect:/employees";
    }




}