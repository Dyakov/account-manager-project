package com.example.account.manager.project.repositories;

import com.example.account.manager.project.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
