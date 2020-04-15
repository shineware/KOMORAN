package kr.co.shineware.nlp.komoran.admin.fwduser;

import kr.co.shineware.nlp.komoran.admin.KOMORANAdminApplication;
import kr.co.shineware.nlp.komoran.admin.domain.FwdUser;
import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceDuplicatedException;
import kr.co.shineware.nlp.komoran.admin.repository.FwdUserRepository;
import kr.co.shineware.nlp.komoran.admin.service.FwdUserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = KOMORANAdminApplication.class)
public class ServiceTest {
    @Autowired
    private FwdUserService fwdUserService;

    @Autowired
    private FwdUserRepository fwdUserRepository;

    @Before
    public void init() {
        fwdUserRepository.deleteAll();
    }


    @Test
    public void AddItem_Test() {
        FwdUser result = fwdUserService.addItem("하늘을", "하늘/NNG 을/JKO");

        assertThat(result.getFull(), is("하늘을"));
        assertThat(result.getAnalyzed(), is("하늘/NNG 을/JKO"));
    }


    @Test(expected = ResourceDuplicatedException.class)
    public void AddItem_Duplicated_Test() {
        FwdUser result = fwdUserService.addItem("하늘을", "하늘/NNG 을/JKO");
        result = fwdUserService.addItem("하늘을", "하늘/NNG 을/JKO");
    }


    @Test(expected = ParameterInvalidException.class)
    public void AddItem_InvalidFull_Test() {
        FwdUser result = fwdUserService.addItem(null, "하늘/NNG 을/JKO");
    }


    @Test
    public void CheckGivenFullAndAnalyzedExist_Test() {
        fwdUserService.addItem("달리다", "달리/VV 다/EC");
        FwdUser result = fwdUserService.checkGivenFullAndAnalyzedExist("달리다", "달리/VV 다/EC");

        assertThat(result.getFull(), is("달리다"));
        assertThat(result.getAnalyzed(), is("달리/VV 다/EC"));
    }


    @Test(expected = ParameterInvalidException.class)
    public void CheckGivenStartAndNextExist_Invalid_Test() {
        fwdUserService.checkGivenFullAndAnalyzedExist(null, "달리/VV 다/EC");
    }


    @Test
    public void PurgeAllData_Test() {
        fwdUserService.addItem("하늘을", "하늘/NNG 을/JKO");
        fwdUserService.addItem("달리다", "달리/VV 다/EC");
        fwdUserService.addItem("이러쿵저러쿵", "이러쿵저러쿵/MAG");

        fwdUserService.purgeAllData();

        assertThat(fwdUserRepository.count(), is(0L));
    }


    @Test
    public void UpdateFullById_Test() {
        FwdUser result = fwdUserService.addItem("하늘을", "하늘/NNG 을/JKO");
        int idToTest = result.getId();

        result = fwdUserService.updateFullById(idToTest, "테스트");

        assertThat(result.getId(), is(idToTest));
        assertThat(result.getFull(), is("테스트"));
        assertThat(result.getAnalyzed(), is("하늘/NNG 을/JKO"));

        assertThat(fwdUserRepository.findById(idToTest).getFull(), is("테스트"));
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdateFullById_InvalidId_Test() {
        fwdUserService.updateFullById(-1, "테스트");
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdateFullById_InvalidFull_Test() {
        FwdUser result = fwdUserService.addItem("하늘을", "하늘/NNG 을/JKO");
        int idToTest = result.getId();

        fwdUserService.updateFullById(idToTest, "");
    }


    @Test
    public void UpdateAnalyzedById_Test() {
        FwdUser result = fwdUserService.addItem("달리다", "달리/VV 다/EC");
        int idToTest = result.getId();

        result = fwdUserService.updateAnalyzedById(idToTest, "테스트/NA");

        assertThat(result.getId(), is(idToTest));
        assertThat(result.getFull(), is("달리다"));
        assertThat(result.getAnalyzed(), is("테스트/NA"));

        assertThat(fwdUserRepository.findById(idToTest).getAnalyzed(), is("테스트/NA"));
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdateAnalyzedById_InvalidId_Test() {
        fwdUserService.updateAnalyzedById(-1, "테스트/NA");
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdateAnalyzedById_InvalidAnalyzed_Test() {
        FwdUser result = fwdUserService.addItem("달리다", "달리/VV 다/EC");
        int idToTest = result.getId();

        fwdUserService.updateAnalyzedById(idToTest, "");
    }


    @Test
    public void DeleteItemById_Test() {
        fwdUserService.addItem("하늘을", "하늘/NNG 을/JKO");
        FwdUser result = fwdUserService.addItem("달리다", "달리/VV 다/EC");

        int idToTest = result.getId();

        result = fwdUserService.deleteItemById(idToTest);

        assertThat(result.getId(), is(idToTest));
    }


    @Test(expected = ParameterInvalidException.class)
    public void DeleteItemById_InvalidId_Test() {
        fwdUserService.addItem("하늘을", "하늘/NNG 을/JKO");
        FwdUser result = fwdUserService.addItem("달리다", "달리/VV 다/EC");

        int idToTest = result.getId();

        result = fwdUserService.deleteItemById(-1);
    }

}