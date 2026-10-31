package com.mb.modulith.playermarket.api.controller;

import com.mb.modulith.playermarket.service.TeamService;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamRequest;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    /**
     * Create team.
     *
     * @param apiTeamRequest to create team.
     */
    @PostMapping("/teams")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create team.")
    public ResponseEntity<ApiTeamResponse> createTeam(@RequestBody ApiTeamRequest apiTeamRequest) {
        return ResponseEntity.ok().body(teamService.createTeam(apiTeamRequest));
    }

    /**
     * return all teams
     *
     */
    @GetMapping("/teams")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "return all teams.")
    public ResponseEntity<List<ApiTeamResponse>> getTeams() {
        return ResponseEntity.ok().body(teamService.getTeams());
    }

    /**
     * delete team_info record
     *
     * @param id team_info id.
     */
    @DeleteMapping("/teams/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "delete team_info record.")
    public ResponseEntity<List<ApiTeamResponse>> deleteTeam(@PathVariable UUID id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok().body(teamService.getTeams());
    }


    /**
     * Update team.
     *
     * @param apiTeamRequest to update team.
     * @param id             team_info id.
     */
    @PutMapping("/teams/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update team.")
    public ResponseEntity<ApiTeamResponse> updateTeam(@PathVariable("id") UUID id, @RequestBody ApiTeamRequest apiTeamRequest) {
        return ResponseEntity.ok().body(teamService.updateTeam(id, apiTeamRequest));
    }

}
