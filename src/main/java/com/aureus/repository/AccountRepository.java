package com.aureus.repository;

import com.aureus.model.Account;
import com.aureus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserOrderByEsPrincipalDescNameAsc(User user);

    List<Account> findByUser(User user);

    Optional<Account> findByIdAndUser(Long id, User user);

    boolean existsByNameAndUser(String name, User user);

    /** La cuenta principal del usuario — siempre existe exactamente una. */
    Optional<Account> findByUserAndEsPrincipalTrue(User user);

    long countByUser(User user);
}
