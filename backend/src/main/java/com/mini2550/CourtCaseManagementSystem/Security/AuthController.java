package com.mini2550.CourtCaseManagementSystem.Security;

import com.mini2550.CourtCaseManagementSystem.Dtos.UserDto;
import com.mini2550.CourtCaseManagementSystem.Enums.UserType;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import com.mini2550.CourtCaseManagementSystem.Security.Dto.AuthenticationRequest;
import com.mini2550.CourtCaseManagementSystem.Security.Dto.AuthenticationResponse;
import com.mini2550.CourtCaseManagementSystem.Security.Dto.ExtractEmailDto;
import com.mini2550.CourtCaseManagementSystem.Security.Dto.CurrentUserResponse;
import com.mini2550.CourtCaseManagementSystem.Repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")

public class AuthController {

    private final AuthenticationService authenticationService;
    private final HttpServletRequest request;
    private final JwtUtils jwtUtils;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationService authenticationService, HttpServletRequest request, JwtUtils jwtUtils, JwtService jwtService, UserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.request = request;
        this.jwtUtils = jwtUtils;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/registerAdmin")
    public ResponseEntity<String> registerUser(@Valid @RequestBody User registrationDto) {
        registrationDto.setUserType(UserType.ADMIN);
        User registeredUser = authenticationService.registerUser(registrationDto);
        String response = "User registered successfully";
        return ResponseEntity.ok(response);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me() {
        String token = jwtUtils.getJwtFromRequest(request);
        Long userId = Long.valueOf(jwtService.extractId(token));
        User user = userRepository.findById(userId).orElseThrow();
        CurrentUserResponse res = new CurrentUserResponse();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setUserType(user.getUserType() != null ? user.getUserType().name() : null);
        return ResponseEntity.ok(res);
    }
    @GetMapping("/extractId")
    public ResponseEntity<Long> printId() {
        return authenticationService.getIdFromToken(request);
    }
    @PostMapping("/extractId")
    public ResponseEntity<Long> printIdUsingBody(@RequestBody ExtractEmailDto extractEmailDto) {
        return authenticationService.getIdFromTokenUsingBody(extractEmailDto);
    }
    @GetMapping("/extractEmail")
    public ResponseEntity<String> printEmail() {
        return authenticationService.getEmailFromToken(request);
    }
    @PostMapping("/extractEmail")
    public ResponseEntity<String> printEmailUsingBody(@RequestBody ExtractEmailDto extractEmailDto) {
        return authenticationService.getEmailFromTokenUsingBody(extractEmailDto);
    }
    @PostMapping("/register-client")
    public ResponseEntity<String> registerClient(@RequestBody UserDto client){
        try {
            authenticationService.registerClient(client);
            return ResponseEntity.ok("client registered successfully");
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Registration failed";
            if (msg.toLowerCase().contains("email already exists")) {
                return ResponseEntity.status(409).body(msg);
            }
            return ResponseEntity.badRequest().body(msg);
        }
    }
    @PostMapping("/register-lawyer")
    public ResponseEntity<String> registerLawyer(@RequestBody UserDto client){
        try {
            authenticationService.registerLawyer(client);
            return ResponseEntity.ok("client registered successfully");
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Registration failed";
            if (msg.toLowerCase().contains("email already exists")) {
                return ResponseEntity.status(409).body(msg);
            }
            return ResponseEntity.badRequest().body(msg);
        }
    }

}
