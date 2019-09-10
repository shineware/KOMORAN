package kr.co.shineware.nlp.komoran.admin.dicuser;

import kr.co.shineware.nlp.komoran.admin.KOMORANAdminApplication;
import kr.co.shineware.nlp.komoran.admin.domain.DicUser;
import kr.co.shineware.nlp.komoran.admin.domain.DicWord;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceDuplicatedException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceNotFoundException;
import kr.co.shineware.nlp.komoran.admin.repository.DicUserRepository;
import kr.co.shineware.nlp.komoran.admin.service.DicUserService;
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
    private DicUserService dicUserService;

    @Autowired
    private DicUserRepository dicUserRepository;

    @Before
    public void init() {
        dicUserRepository.deleteAll();
    }


    @Test
    public void AddItem_Test() {
        DicUser result = dicUserService.addItem("테스트", PosType.NNG);

        assertThat(result.getToken(), is("테스트"));
        assertThat(result.getPos(), is(PosType.NNG));
    }


    @Test(expected = ResourceDuplicatedException.class)
    public void AddItem_Duplicated_Test() {
        DicUser result = dicUserService.addItem("테스트", PosType.NNG);
        result = dicUserService.addItem("테스트", PosType.NNG);
    }


    @Test(expected = ParameterInvalidException.class)
    public void AddItem_InvalidToken_Test() {
        DicUser result = dicUserService.addItem("", PosType.NNG);
    }


    @Test
    public void CheckGivenTokenAndPosTypeExist_Test() {
        dicUserService.addItem("테스트", PosType.EC);
        DicUser result = dicUserService.checkGivenTokenAndPosTypeExist("테스트", PosType.EC);

        assertThat(result.getToken(), is("테스트"));
        assertThat(result.getPos(), is(PosType.EC));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void CheckGivenTokenAndPosTypeExist_NotExsited_Test() {
        dicUserService.checkGivenTokenAndPosTypeExist("존재하지 않는 이름", PosType.EC);
    }


    @Test
    public void PurgeAllData_Test() {
        dicUserService.addItem("테스트1", PosType.NNG);
        dicUserService.addItem("테스트2", PosType.NP);
        dicUserService.addItem("테스트3", PosType.EC);

        dicUserService.purgeAllData();

        assertThat(dicUserRepository.count(), is(0L));
    }


    @Test
    public void UpdateTokenById_Test() {
        DicUser result = dicUserService.addItem("테스트", PosType.NNG);
        int idToTest = result.getId();

        result = dicUserService.updateTokenById(idToTest, "바뀐것");

        assertThat(result.getId(), is(idToTest));
        assertThat(result.getToken(), is("바뀐것"));
        assertThat(result.getPos(), is(PosType.NNG));

        assertThat(dicUserRepository.findById(idToTest).getToken(), is("바뀐것"));
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdateTokenById_InvalidId_Test() {
        DicUser result = dicUserService.updateTokenById(-1, "테스트");
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdateTokenById_InvalidToken_Test() {
        DicUser result = dicUserService.addItem("테스트", PosType.NNG);
        int idToTest = result.getId();

        result = dicUserService.updateTokenById(idToTest, null);
    }


    @Test
    public void UpdatePosById_Test() {
        DicUser result = dicUserService.addItem("테스트", PosType.NNG);
        int idToTest = result.getId();

        result = dicUserService.updatePosById(idToTest, PosType.NNP);

        assertThat(result.getId(), is(idToTest));
        assertThat(result.getToken(), is("테스트"));
        assertThat(result.getPos(), is(PosType.NNP));

        assertThat(dicUserRepository.findById(idToTest).getPos(), is(PosType.NNP));
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdatePosById_InvalidId_Test() {
        DicUser result = dicUserService.updatePosById(-1, PosType.NP);
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdatePosById_InvalidPos_Test() {
        DicUser result = dicUserService.addItem("테스트", PosType.NNG);
        int idToTest = result.getId();

        result = dicUserService.updatePosById(idToTest, null);
    }


    @Test
    public void DeleteItemById_Test() {
        dicUserService.addItem("테스트", PosType.NNB);
        DicUser result = dicUserService.addItem("테스트", PosType.NNG);

        int idToTest = result.getId();

        result = dicUserService.deleteItemById(idToTest);

        assertThat(result.getId(), is(idToTest));
    }


    @Test(expected = ParameterInvalidException.class)
    public void DeleteItemById_InvalidId_Test() {
        dicUserService.addItem("테스트", PosType.NNB);
        DicUser result = dicUserService.addItem("테스트", PosType.NNG);

        int idToTest = result.getId();

        result = dicUserService.deleteItemById(-1);
    }

}