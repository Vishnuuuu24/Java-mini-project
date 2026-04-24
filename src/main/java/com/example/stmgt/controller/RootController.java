package com.example.stmgt.controller;

import com.example.stmgt.domain.entity.CustomUser;
import com.example.stmgt.dto.validation.DjangoPasswordVerifier;
import com.example.stmgt.repository.CustomUserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping
public class RootController {

    private final SessionAuthHelper sessionAuthHelper;
    private final CustomUserRepository customUserRepository;
    private final DjangoPasswordVerifier passwordVerifier;
    private final DjangoPasswordHasher passwordHasher;

    public RootController(
        SessionAuthHelper sessionAuthHelper,
        CustomUserRepository customUserRepository,
        DjangoPasswordVerifier passwordVerifier,
        DjangoPasswordHasher passwordHasher
    ) {
        this.sessionAuthHelper = sessionAuthHelper;
        this.customUserRepository = customUserRepository;
        this.passwordVerifier = passwordVerifier;
        this.passwordHasher = passwordHasher;
    }

    @ModelAttribute("passwordChangeForm")
    public PasswordChangeForm passwordChangeForm() {
        return new PasswordChangeForm();
    }

    @GetMapping("/")
    public String homeRedirect() {
        return "redirect:/users/login/";
    }

    @GetMapping({"/admin", "/admin/", "/st_mgt/admin", "/st_mgt/admin/"})
    public String adminRedirect() {
        return "redirect:/users/admin/dashboard/";
    }

    @GetMapping("/change-password/")
    public String changePasswordRedirect() {
        return "redirect:/users/student/dashboard/";
    }

    @PostMapping("/change-password/")
    public String changePassword(
        @Valid @ModelAttribute("passwordChangeForm") PasswordChangeForm passwordChangeForm,
        BindingResult bindingResult,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please correct the error below.");
            return "redirect:/users/student/dashboard/";
        }

        CustomUser user;
        try {
            user = sessionAuthHelper.requireAuthenticated(session);
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", "Please log in first.");
            return "redirect:/users/login/";
        }

        if (!passwordChangeForm.getNew_password().equals(passwordChangeForm.getConfirm_password())) {
            redirectAttributes.addFlashAttribute("error", "Please correct the error below.");
            return "redirect:/users/student/dashboard/";
        }

        if (!passwordVerifier.matches(passwordChangeForm.getCurrent_password(), user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Please correct the error below.");
            return "redirect:/users/student/dashboard/";
        }

        user.setPassword(passwordHasher.hash(passwordChangeForm.getNew_password()));
        customUserRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Your password was successfully updated!");
        return "redirect:/users/student/dashboard/";
    }

    public static class PasswordChangeForm {

        @NotBlank(message = "Current password is required")
        @Size(max = 128, message = "Current password is too long")
        private String current_password;

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 128, message = "New password must be between 8 and 128 characters")
        private String new_password;

        @NotBlank(message = "Confirm password is required")
        @Size(min = 8, max = 128, message = "Confirm password must be between 8 and 128 characters")
        private String confirm_password;

        public String getCurrent_password() {
            return current_password;
        }

        public void setCurrent_password(String current_password) {
            this.current_password = current_password;
        }

        public String getNew_password() {
            return new_password;
        }

        public void setNew_password(String new_password) {
            this.new_password = new_password;
        }

        public String getConfirm_password() {
            return confirm_password;
        }

        public void setConfirm_password(String confirm_password) {
            this.confirm_password = confirm_password;
        }
    }
}
