package kr.co.shineware.nlp.komoran.admin.repository;

import kr.co.shineware.nlp.komoran.admin.domain.DicUser;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

public interface DicUserRepository extends JpaRepository<DicUser, Long> {

    DicUser findById(int id);

    DicUser findByTokenAndPos(String token, PosType pos);

    // find without param
    Page<DicUser> findAll(Pageable pageable);

    // find with one param
    Page<DicUser> findByTokenContaining(String token, Pageable pageable);

    Page<DicUser> findByPos(PosType pos, Pageable pageable);

    // find with two param
    Page<DicUser> findByTokenContainingAndPos(String token, PosType pos, Pageable pageable);


    // export all data with specified form
    @Query(value = "SELECT CONCAT(token, '\t', pos) " +
            "FROM DICUSER " +
            "ORDER BY token",
            nativeQuery = true)
    Stream<String> getAllItemsWithExportForm();


    @Transactional
    @Modifying
    @Query(value = "TRUNCATE TABLE DICUSER", nativeQuery = true)
    void deleteAll();
}
