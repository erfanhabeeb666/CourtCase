package com.mini2550.CourtCaseManagementSystem.Services;


import com.mini2550.CourtCaseManagementSystem.Dtos.UploadDocumentRequest;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import com.mini2550.CourtCaseManagementSystem.Repositories.UserRepository;
import com.mini2550.CourtCaseManagementSystem.Security.JwtService;
import com.mini2550.CourtCaseManagementSystem.Security.JwtUtils;
import com.mini2550.CourtCaseManagementSystem.Utils.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class JudgeService {
    private final FileUploadUtil fileUploadUtil;
    private final HttpServletRequest servletRequest;
    private final JwtService jwtService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    public JudgeService(FileUploadUtil fileUploadUtil, HttpServletRequest servletRequest, JwtService jwtService, JwtUtils jwtUtils, UserRepository userRepository) {
        this.fileUploadUtil = fileUploadUtil;
        this.servletRequest = servletRequest;
        this.jwtService = jwtService;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    public void uploadDocument(UploadDocumentRequest request) {
        Long userId = Long.valueOf(jwtService.extractId(jwtUtils.getJwtFromRequest(servletRequest)));
        User user = userRepository.getById(userId);
        fileUploadUtil.uploadDocument(request,user);
    }
}
