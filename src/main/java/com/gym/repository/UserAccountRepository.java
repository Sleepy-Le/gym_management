package com.gym.repository;

import com.gym.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {

    UserAccount findByUsernameAndPasswordAndStatus(String username, String password, String status);

    UserAccount findByUsername(String username);

    Page<UserAccount> findAllByOrderByUserIdAsc(Pageable pageable);
}
