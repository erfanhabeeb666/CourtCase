package com.mini2550.CourtCaseManagementSystem.Security;

import com.mini2550.CourtCaseManagementSystem.Dtos.UserDto;
import com.mini2550.CourtCaseManagementSystem.Enums.Status;
import com.mini2550.CourtCaseManagementSystem.Enums.UserType;
import com.mini2550.CourtCaseManagementSystem.Models.Client;
import com.mini2550.CourtCaseManagementSystem.Models.Lawyer;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import com.mini2550.CourtCaseManagementSystem.Repositories.ClientRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.LawyerRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.UserRepository;
import com.mini2550.CourtCaseManagementSystem.Security.Dto.AuthenticationRequest;
import com.mini2550.CourtCaseManagementSystem.Security.Dto.AuthenticationResponse;
import com.mini2550.CourtCaseManagementSystem.Security.Dto.ExtractEmailDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final ClientRepository clientRepository;
    private final LawyerRepository lawyerRepository;

    public AuthenticationService(UserRepository userRepository, JwtService jwtService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, ClientRepository clientRepository, LawyerRepository lawyerRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.clientRepository = clientRepository;
        this.lawyerRepository = lawyerRepository;
    }

    public User registerUser(User registrationDto) {
        try {
            User user = new User();
            user.setEmail(registrationDto.getEmail());
            user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
            user.setUserType(registrationDto.getUserType());
            user.setName(registrationDto.getName());
            user.setStatus(Status.ACTIVE);
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Email already exists: " + registrationDto.getEmail());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred during registration.", e);
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        var jwtToken = jwtService.generateToken(user);
        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setToken(jwtToken);
        return authenticationResponse;
    }

    public Boolean validateToken(String jwtFromRequest) {
      return  jwtService.isTokenExpired(jwtFromRequest);
    }

    public ResponseEntity<Long> getIdFromToken(HttpServletRequest request) {
        String jwtUserId = jwtService.extractId(jwtUtils.getJwtFromRequest(request));
        Long response = Long.valueOf(jwtUserId);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<String> getEmailFromToken(HttpServletRequest request) {
        String jwtUserEmail = jwtService.extractUsername(jwtUtils.getJwtFromRequest(request));

        return ResponseEntity.ok(jwtUserEmail);
    }

    public ResponseEntity<String> getEmailFromTokenUsingBody(ExtractEmailDto extractEmailDto) {
        String jwtUserEmail = jwtService.extractUsername(extractEmailDto.token);
        return ResponseEntity.ok(jwtUserEmail);

    }

    public ResponseEntity<Long> getIdFromTokenUsingBody(ExtractEmailDto extractEmailDto) {
        String jwtUserId = jwtService.extractId(extractEmailDto.token);
        return ResponseEntity.ok(Long.valueOf(jwtUserId));
    }

    public void registerClient(UserDto user) {
        try {
            Client client = new Client();
            client.setEmail(user.getEmail());
            client.setName(user.getName());
            client.setPassword(passwordEncoder.encode(user.getPassword()));
            client.setUserType(UserType.CLIENT);
            client.setStatus(Status.ACTIVE);
            clientRepository.save(client);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred during registration.", e);
        }
    }
    public void registerLawyer(UserDto user) {
        try {
            Lawyer lawyer = new Lawyer(user.getLegalIdentity());
            lawyer.setEmail(user.getEmail());
            lawyer.setPassword(passwordEncoder.encode(user.getPassword()));
            lawyer.setUserType(UserType.LAWYER);
            lawyer.setName(lawyer.getName());
            lawyer.setStatus(Status.ACTIVE);
            lawyerRepository.save(lawyer);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred during registration.", e);
        }
    }

}
