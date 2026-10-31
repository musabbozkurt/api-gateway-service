package com.mb.modulith.playermarket.api.controller;

import com.mb.modulith.playermarket.service.PlayerService;
import com.mb.modulith.playermarket.service.dto.player.ApiPlayerRequest;
import com.mb.modulith.playermarket.service.dto.player.ApiPlayerResponse;
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
public class PlayerController {

    private final PlayerService playerService;

    /**
     * Create player.
     *
     * @param apiPlayerRequest to create player.
     */
    @PostMapping("/players")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create player.")
    public ResponseEntity<ApiPlayerResponse> createPlayer(@RequestBody ApiPlayerRequest apiPlayerRequest) {
        return ResponseEntity.ok().body(playerService.createPlayer(apiPlayerRequest));
    }

    /**
     * return all players
     *
     */
    @GetMapping("/players")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "return all players.")
    public ResponseEntity<List<ApiPlayerResponse>> getPlayers() {
        return ResponseEntity.ok().body(playerService.getPlayers());
    }

    /**
     * delete player_info record
     *
     * @param id player_info id.
     */
    @DeleteMapping("/players/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "delete player_info record.")
    public ResponseEntity<List<ApiPlayerResponse>> deletePlayer(@PathVariable UUID id) {
        playerService.deletePlayer(id);
        return ResponseEntity.ok().body(playerService.getPlayers());
    }


    /**
     * Update player.
     *
     * @param apiPlayerRequest to update player.
     * @param id               player_info id.
     */
    @PutMapping("/players/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Update player.")
    public ResponseEntity<ApiPlayerResponse> updatePlayer(@PathVariable("id") UUID id, @RequestBody ApiPlayerRequest apiPlayerRequest) {
        return ResponseEntity.ok().body(playerService.updatePlayer(id, apiPlayerRequest));
    }

}
