package kr.co.shineware.nlp.komoran.admin.repository;

import kr.co.shineware.nlp.komoran.admin.domain.DicWord;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

public interface DicWordRepository extends JpaRepository<DicWord, Long> {

    DicWord findById(int id);

    DicWord findByTokenAndPos(String token, PosType pos);

    // find without param
    Page<DicWord> findAll(Pageable pageable);

    // find with one param
    Page<DicWord> findByTokenContaining(String token, Pageable pageable);

    Page<DicWord> findByPos(PosType pos, Pageable pageable);

    Page<DicWord> findByTfGreaterThanEqual(int tf, Pageable pageable);

    // find with two param
    Page<DicWord> findByTokenContainingAndPos(String token, PosType pos, Pageable pageable);

    Page<DicWord> findByTokenContainingAndTfGreaterThanEqual(String token, int tf, Pageable pageable);

    Page<DicWord> findByPosAndTfGreaterThanEqual(PosType pos, int tf, Pageable pageable);

    // find with three param
    Page<DicWord> findByTokenContainingAndPosAndTfGreaterThanEqual(String token, PosType pos, int tf, Pageable pageable);


    // export all data with specified form
    @Query(value = "SELECT CONCAT(token, '\t', GROUP_CONCAT(pos || ':' || tf SEPARATOR '\t')) " +
            "FROM DICWORD " +
            "GROUP BY token " +
            "ORDER BY token",
            nativeQuery = true)
    Stream<String> getAllItemsWithExportForm();


    @Transactional
    @Modifying
    @Query(value = "TRUNCATE TABLE DICWORD", nativeQuery = true)
    void deleteAll();
}
