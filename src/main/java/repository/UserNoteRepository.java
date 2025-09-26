package repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import entity.UserNote;

import java.util.List;

@Repository
public interface UserNoteRepository extends JpaRepository<UserNote, Long> {
    
    @Query("SELECT un FROM UserNote un WHERE un.user.id = :userId ORDER BY un.createdDate DESC")
    Page<UserNote> findByUserIdOrderByCreatedDateDesc(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT un FROM UserNote un WHERE un.user.id = :userId ORDER BY un.createdDate DESC")
    List<UserNote> findByUserIdOrderByCreatedDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(un) FROM UserNote un WHERE un.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}
