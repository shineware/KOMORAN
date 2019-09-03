package kr.co.shineware.nlp.komoran.admin.fwduser;

import kr.co.shineware.nlp.komoran.admin.domain.FwdUser;
import kr.co.shineware.nlp.komoran.admin.repository.FwdUserRepository;
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
    FwdUserRepository fwdUserRepository;

    @Before
    public void init() {
        FwdUser testItem1 = new FwdUser("하늘을", "하늘/NNG 을/JKO");
        FwdUser testItem2 = new FwdUser("달리다", "달리/VV 다/EC");

        fwdUserRepository.save(testItem1);
        fwdUserRepository.save(testItem2);
    }

    @After
    public void down() {
        fwdUserRepository.deleteAll();
    }

    @Test
    public void FwdUser_Test() {
        FwdUser testItem = fwdUserRepository.findByFullAndAnalyzed("하늘을", "하늘/NNG 을/JKO");
        assertThat(testItem.getFull(), is("하늘을"));
        assertThat(testItem.getAnalyzed(), is("하늘/NNG 을/JKO"));

        testItem = fwdUserRepository.findByFullAndAnalyzed("달리다", "달리/VV 다/EC");
        assertThat(testItem.getFull(), is("달리다"));
        assertThat(testItem.getAnalyzed(), is("달리/VV 다/EC"));
    }
}
