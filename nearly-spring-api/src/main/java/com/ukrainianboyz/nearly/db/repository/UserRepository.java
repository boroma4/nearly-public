package com.ukrainianboyz.nearly.db.repository;

import com.ukrainianboyz.nearly.db.entity.DatabaseUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<DatabaseUser, String> {

    DatabaseUser findByEmail(String email);

    @Query(value = "SELECT u.userName FROM DatabaseUser u WHERE u.userId = :id")
    String findNameByUserId(@Param("id") String userId);

    List<DatabaseUser> findByAppUserIdStartsWithOrUserNameStartsWith(String startsWithAppUserId, String startsWithUserName, Pageable pageable);
}
