package com.jptechgenius.payroll.repository;

import com.jptechgenius.payroll.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * [User Repository]
 * -----------------
 * Ei interface-tar kaj holo Database er 'users' table er sathe kotha bola.
 * JpaRepository extend korar karone save(), findById(), delete() egula automatic peye gechi.
 * Kintu amader project er requirement onujayi kichu extra query lagbe, ja ekhane lekha hoyeche.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Login er somoy username diye user khujar jonno ei method use hoy.
     * Optional return kore, mane user pawa jeteo pare, nao pawa jete pare.
     */
    Optional<User> findByUsername(String username);

    /**
     * Login process e jodi keu username er bodole email diye login korte chay,
     * tokhon ei method ta kaj korbe.
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    // ==========================================
    // CUSTOM DELETE QUERY
    // ==========================================
    /**
     * [Delete User by Employee ID]
     * Problem: User table e 'employee_id' ache, kintu sorasori 'deleteByEmployeeId' JPA te nai.
     * Solution: Tai amra @Query annotation diye nijera SQL query likhechi.
     * * @Modifying -> Database e change (delete) hobe tai eta dite hoy.
     * @Transactional -> (Service layer e deya ache) majhpothe fail korle rollback hobe.
     */
    @Modifying
    @Query("DELETE FROM User u WHERE u.employee.id = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Forgot password er somoy email diye user khujar jonno eta lagbe.
     * Jodi ei email e kono user na thake, tobe null return korbe.
     */
    Optional<User> findByEmail(String email);

    // [New Helper]
    // Employee delete korar age check korar jonno je tar kono User account ache kina.
    // Eta User object return korbe jodi pawa jay.
    @Query("SELECT u FROM User u WHERE u.employee.id = :employeeId")
    User findByEmployeeId(@Param("employeeId") Long employeeId);
}