package com.murillo.finsecure_api.repository;

import com.murillo.finsecure_api.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByCpfCnpj(String cpfCnpj);

    boolean existsByEmail(String email);

    boolean existsByCpfCnpj(String cpfCnpj);

}
