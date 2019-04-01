package kr.co.shineware.nlp.komoran.admin.repository;

import kr.co.shineware.nlp.komoran.admin.domain.DicWord;
import kr.co.shineware.nlp.komoran.admin.domain.GrammarIn;
import kr.co.shineware.nlp.komoran.admin.domain.GrammarType;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

public interface GrammarInRepository extends JpaRepository<GrammarIn, Long> {

    GrammarIn findById(int id);

    GrammarIn findByStartAndNext(GrammarType start, GrammarType next);

    // find without param
    Page<GrammarIn> findAll(Pageable pageable);

    // find with one param
    Page<GrammarIn> findByStart(GrammarType start, Pageable pageable);
    Page<GrammarIn> findByNext(GrammarType next, Pageable pageable);
    Page<GrammarIn> findByTfGreaterThanEqual(int tf, Pageable pageable);

    // find with two param
    Page<GrammarIn> findByStartAndNext(GrammarType start, GrammarType next, Pageable pageable);
    Page<GrammarIn> findByStartAndTfGreaterThanEqual(GrammarType start, int tf, Pageable pageable);
    Page<GrammarIn> findByNextAndTfGreaterThanEqual(GrammarType next, int tf, Pageable pageable);

    // find with three param
    Page<GrammarIn> findByStartAndNextAndTfGreaterThanEqual(GrammarType start, GrammarType next, int tf, Pageable pageable);

    // export all data with specified form
    @Query(value = "SELECT CONCAT(start, '\t', GROUP_CONCAT(next || ':' || tf SEPARATOR ',')) " +
            "FROM GRAMMARIN " +
            "GROUP BY start " +
            "ORDER BY start",
            nativeQuery = true)
    Stream<String> getAllItemsWithExportForm();


    @Transactional
    @Modifying
    @Query(value = "TRUNCATE TABLE GRAMMARIN", nativeQuery = true)
    void deleteAll();
}
