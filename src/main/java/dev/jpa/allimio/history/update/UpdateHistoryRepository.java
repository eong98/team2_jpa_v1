package dev.jpa.allimio.history.update;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UpdateHistoryRepository extends JpaRepository<UpdateHistory, Long> {

  
}
