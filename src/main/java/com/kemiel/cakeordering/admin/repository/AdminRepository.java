package com.kemiel.cakeordering.admin.repository;

import com.kemiel.cakeordering.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 管理員 Repository
 */
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);

    boolean existsByUsername(String username);
}