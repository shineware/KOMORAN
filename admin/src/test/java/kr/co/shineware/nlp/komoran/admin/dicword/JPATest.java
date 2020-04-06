package kr.co.shineware.nlp.komoran.admin.dicword;

import kr.co.shineware.nlp.komoran.admin.domain.DicWord;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.repository.DicWordRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@DataJpaTest
public class JPATest {
    @Autowired
    DicWordRepository dicWordRepository;

    @Before
    public void init() {
        // for DicWordTest
        DicWord testItem1 = new DicWord("테스트", PosType.NNG, 10);
        DicWord testItem2 = new DicWord("테스트", PosType.NP, 100);
        dicWordRepository.save(testItem1);
        dicWordRepository.save(testItem2);
    }

    @Test
    public void DicWordTest() {
        DicWord testItem = dicWordRepository.findByTokenAndPos("테스트", PosType.NNG);
        assertThat(testItem.getToken(), is("테스트"));
        assertThat(testItem.getPos(), is(PosType.NNG));
        assertThat(testItem.getTf(), is(10));

        testItem = dicWordRepository.findByTokenAndPos("테스트", PosType.NP);
        assertThat(testItem.getToken(), is("테스트"));
        assertThat(testItem.getPos(), is(PosType.NP));
        assertThat(testItem.getTf(), is(100));

        dicWordRepository.deleteAll();
    }

}
