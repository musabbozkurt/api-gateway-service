package com.mb.modulith.playermarket.api.controller;

import com.mb.modulith.playermarket.service.SoccerService;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SoccerController {

    private final SoccerService soccerService;

    /**
     * Add player to the team.
     *
     * @param teamId   team_info id.
     * @param playerId team_info id.
     */
    @PutMapping("/soccer/{teamId}/{playerId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Add player to the team.")
    public void updateTeam(@PathVariable("teamId") UUID teamId, @PathVariable("playerId") UUID playerId) {
        soccerService.addPlayerToTeam(teamId, playerId);
    }


    /**
     * return team by player id
     *
     */
    @GetMapping("/soccer/team-by-player-id/{playerId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "return all players.")
    public ResponseEntity<ApiTeamResponse> getTeamByPlayerId(@PathVariable("playerId") UUID playerId) {
        return ResponseEntity.ok().body(soccerService.getTeamByPlayerId(playerId));
    }
}
