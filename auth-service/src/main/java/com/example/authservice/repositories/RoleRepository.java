package com.example.authservice.repositories;

import com.example.authservice.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
    @Query("""
        SELECT DISTINCT p.name
        FROM RoleEntity r
        JOIN r.permissions p
        WHERE r.name IN :roleNames
    """)
    Set<String> findPermissionsByRoleNames(@Param("roleNames") Collection<String> roleNames);
}
