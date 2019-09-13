package kr.co.shineware.nlp.komoran.admin.repository;

import kr.co.shineware.nlp.komoran.admin.domain.FwdUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

public interface FwdUserRepository extends JpaRepository<FwdUser, Long> {

    FwdUser findById(int id);

    FwdUser findByFull(String full);

    FwdUser findByFullAndAnalyzed(String full, String analyzed);

    // find without param
    Page<FwdUser> findAll(Pageable pageable);

    // find with one param
    Page<FwdUser> findByFullContaining(String full, Pageable pageable);

    Page<FwdUser> findByAnalyzedContaining(String analyzed, Pageable pageable);

    // find with two param
    Page<FwdUser> findByFullContainingAndAnalyzedContaining(String full, String analyzed, Pageable pageable);


    // export all data with specified form
    @Query(value = "SELECT CONCAT(fullwords, '\t', analyzed) " +
            "FROM FWDIC " +
            "ORDER BY fullwords",
            nativeQuery = true)
    Stream<String> getAllItemsWithExportForm();


    @Transactional
    @Modifying
    @Query(value = "TRUNCATE TABLE FWDIC", nativeQuery = true)
    void deleteAll();
}
