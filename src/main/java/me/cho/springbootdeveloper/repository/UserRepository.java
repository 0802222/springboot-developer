package me.cho.springbootdeveloper.repository;

import java.util.Optional;
import me.cho.springbootdeveloper.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email); // email 로 사용자 정보를 가져옴

}
