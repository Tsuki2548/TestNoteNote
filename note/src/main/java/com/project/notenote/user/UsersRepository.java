package com.project.notenote.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
    // jpa repository จะมี method พื้นฐานเช่น save(), findAll() ซึ่ง UserRepository ก็จะได้รับจากการสืบทอด
                                                    //<entity class, primary key>
public interface UsersRepository extends JpaRepository<Users, Long> {
    // เป็นการ query จากโดยใช้ parameter = SELECT * FROM user WHERE username = ?
    // ? คือ ค่าที่ส่งผ่าน argument มาที่ parameter ที่ชื่อ username และจะเรียงตามลำดับ
    Optional<Users> findByUsername(String username);
    // ตัว jpa จะตีความเงื่อนไขตามชื่อของ method เช่น findByUsernameOrEmail = SELECT * FROM users WHERE username = ? OR email = ?
    Optional<Users> findByUsernameOrEmail(String username, String email);

    // findByUsernameAndEmail(String username, String email) = SELECT * FROM user WHERE username = ? AND email = ?
}
