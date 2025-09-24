package com.mini2550.CourtCaseManagementSystem.Services;

import com.mini2550.CourtCaseManagementSystem.Dtos.CaseDto;
import com.mini2550.CourtCaseManagementSystem.Dtos.CaseFileRequestDto;
import com.mini2550.CourtCaseManagementSystem.Enums.CaseStatus;
import com.mini2550.CourtCaseManagementSystem.Models.Case;
import com.mini2550.CourtCaseManagementSystem.Models.User;
import com.mini2550.CourtCaseManagementSystem.Repositories.CaseRepository;
import com.mini2550.CourtCaseManagementSystem.Repositories.UserRepository;
import com.mini2550.CourtCaseManagementSystem.Security.JwtService;
import com.mini2550.CourtCaseManagementSystem.Security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final HttpServletRequest request;
    private final JwtUtils jwtUtils;

    public ClientServiceImpl(CaseRepository caseRepository,
                             UserRepository userRepository,
                             JwtService jwtService, HttpServletRequest request, JwtUtils jwtUtils) {
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.request = request;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void fileCase(CaseFileRequestDto dto) {
        Long clientId = Long.valueOf(jwtService.extractId(jwtUtils.getJwtFromRequest(request)));
        User client = userRepository.findById(clientId).orElseThrow();
        Case c = new Case();
        c.setTitle(dto.getTitle());
        c.setType(dto.getType());
        c.setDescription(dto.getDescription());
        c.setClient(client);
        c.setStatus(CaseStatus.NEW);
        if(!dto.getClientLawyerId().isEmpty()){
            Optional<User> optionalUser = userRepository.findById(Long.valueOf(dto.getClientLawyerId()));
            if(optionalUser.isPresent()){
                c.setClientLawyer(optionalUser.get());
            }else{
                throw new RuntimeException("the specieified client lwayer id is not present in the db");
            }
        }


        caseRepository.save(c);
    }

    @Override
    public List<CaseDto> getMyCases() {
        Long clientId = Long.valueOf(jwtService.extractId(jwtUtils.getJwtFromRequest(request)));
        User client = userRepository.findById(clientId).orElseThrow();

        return caseRepository.findByClient(client).stream().map(c -> {
            CaseDto dto = new CaseDto();
            dto.setId(c.getId());
            dto.setTitle(c.getTitle());
            dto.setType(c.getType());
            dto.setStatus(c.getStatus());
            dto.setClientName(c.getClient().getName());
            dto.setClientLawyerName(c.getClientLawyer() != null ? c.getClientLawyer().getName() : null);
            dto.setOpposingLawyerName(c.getOpposingLawyer() != null ? c.getOpposingLawyer().getName() : null);
            dto.setJudgeName(c.getJudge() != null ? c.getJudge().getName() : null);
            dto.setNextHearingDate(c.getNextHearingDate());
            return dto;
        }).collect(Collectors.toList());
    }


}

