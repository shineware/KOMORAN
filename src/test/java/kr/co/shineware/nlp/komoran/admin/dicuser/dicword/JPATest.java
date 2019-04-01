package kr.co.shineware.nlp.komoran.admin.dicuser.dicword;

import kr.co.shineware.nlp.komoran.admin.domain.DicUser;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.repository.DicUserRepository;
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
    DicUserRepository dicUserRepository;

    @Before
    public void init() {
        // for DicUserTest
        DicUser testItem1 = new DicUser("테스트", PosType.NNG);
        DicUser testItem2 = new DicUser("테스트", PosType.NP);
        dicUserRepository.save(testItem1);
        dicUserRepository.save(testItem2);
    }


    @Test
    public void DicUserTest() {
        DicUser testItem = dicUserRepository.findByTokenAndPos("테스트", PosType.NNG);
        assertThat(testItem.getToken(), is("테스트"));
        assertThat(testItem.getPos(), is(PosType.NNG));

        testItem = dicUserRepository.findByTokenAndPos("테스트", PosType.NP);
        assertThat(testItem.getToken(), is("테스트"));
        assertThat(testItem.getPos(), is(PosType.NP));

        dicUserRepository.deleteAll();
    }

}
