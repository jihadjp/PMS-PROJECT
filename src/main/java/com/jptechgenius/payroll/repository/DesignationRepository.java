package com.jptechgenius.payroll.repository;

import com.jptechgenius.payroll.model.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * [Designation Repository]
 * ------------------------
 * Ei interface-ta 'designations' table-er sathe jogajog kore.
 * Employee add korar somoy je dropdown menu thake (jekhane Job Title select kora hoy),
 * sei list-ta database theke anar jonno ei repository use kora hoy.
 */
@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {

    // Ekhane kono manual code likhar dorkar nai!
    // Karon 'JpaRepository' extend korle Spring Boot automatic onek gula method banaye dey.
    // Jemon:
    // - findAll() : Sob gula job title/salary list anar jonno.
    // - save()    : Notun designation add korar jonno (jodi future e feature add kori).
    // - findById(): Specific ekta designation khuje ber korar jonno.
}