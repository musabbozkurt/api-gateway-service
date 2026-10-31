package com.mb.modulith.playermarket.data.repository;

import com.mb.modulith.playermarket.data.model.TeamInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamInfoRepository extends JpaRepository<TeamInfo, UUID> {

    List<TeamInfo> findAllByDeletedIsFalse();

}
