package kr.co.shineware.nlp.komoran.admin.grammarin;

import kr.co.shineware.nlp.komoran.admin.domain.GrammarIn;
import kr.co.shineware.nlp.komoran.admin.domain.GrammarType;
import kr.co.shineware.nlp.komoran.admin.repository.GrammarInRepository;
import org.junit.After;
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
    GrammarInRepository grammarInRepository;

    @Before
    public void init() {
        GrammarIn testItem1 = new GrammarIn(GrammarType.BOE, GrammarType.NNP, 999);
        GrammarIn testItem2 = new GrammarIn(GrammarType.EF, GrammarType.EOE, 999);

        grammarInRepository.save(testItem1);
        grammarInRepository.save(testItem2);
    }

    @After
    public void down() {
        grammarInRepository.deleteAll();
    }

    @Test
    public void GrammarIn_Test() {
        GrammarIn testItem = grammarInRepository.findByStartAndNext(GrammarType.BOE, GrammarType.NNP);
        assertThat(testItem.getStart(), is(GrammarType.BOE));
        assertThat(testItem.getNext(), is(GrammarType.NNP));
        assertThat(testItem.getTf(), is(999));

        testItem = grammarInRepository.findByStartAndNext(GrammarType.EF, GrammarType.EOE);
        assertThat(testItem.getStart(), is(GrammarType.EF));
        assertThat(testItem.getNext(), is(GrammarType.EOE));
        assertThat(testItem.getTf(), is(999));
    }
}
