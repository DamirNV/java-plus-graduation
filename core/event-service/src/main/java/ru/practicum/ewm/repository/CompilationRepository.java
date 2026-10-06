package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Compilation;

import java.util.Collection;
import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    Page<Compilation> findAllByPinned(Boolean pinned, Pageable pageable);

    @Query("""
            select distinct compilation
            from Compilation compilation
            left join fetch compilation.events
            where compilation.id in :ids
            """)
    List<Compilation> findAllWithEventsByIds(
            @Param("ids") Collection<Long> ids
    );
}