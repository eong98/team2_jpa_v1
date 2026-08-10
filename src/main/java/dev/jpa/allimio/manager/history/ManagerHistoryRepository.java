package dev.jpa.allimio.manager.history;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.allimio.member.Member;

public interface ManagerHistoryRepository extends JpaRepository<ManagerHistory, Long>{



}
