package dev.jpa.allimio.history.update;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;



public interface UpdateHistoryRepository extends JpaRepository<UpdateHistory, Long> {

  
}
