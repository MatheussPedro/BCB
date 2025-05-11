package br.com.bigchatbrasil.repository;

import br.com.bigchatbrasil.model.FilaMensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilaMensagemRepository extends JpaRepository<FilaMensagem, Long> {
    @Query("SELECT f FROM FilaMensagem f ORDER BY " +
            "CASE WHEN f.priority = 'urgent' THEN 1 ELSE 2 END, f.timestamp ASC")
    List<FilaMensagem> findPrioritized();

    public long countByPriority(String priority);
}
