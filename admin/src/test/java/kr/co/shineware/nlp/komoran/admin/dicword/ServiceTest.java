package kr.co.shineware.nlp.komoran.admin.dicword;

import kr.co.shineware.nlp.komoran.admin.KOMORANAdminApplication;
import kr.co.shineware.nlp.komoran.admin.domain.DicWord;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceDuplicatedException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceNotFoundException;
import kr.co.shineware.nlp.komoran.admin.repository.DicWordRepository;
import kr.co.shineware.nlp.komoran.admin.service.DicWordService;
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
    private DicWordService dicWordService;

    @Autowired
    private DicWordRepository dicWordRepository;

    @Before
    public void init() {
        dicWordRepository.deleteAll();
    }


    @Test
    public void AddItem_Test() {
        DicWord result = dicWordService.addItem("테스트", PosType.NNG, 10);

        assertThat(result.getToken(), is("테스트"));
        assertThat(result.getPos(), is(PosType.NNG));
        assertThat(result.getTf(), is(10));
    }


    @Test(expected = ResourceDuplicatedException.class)
    public void AddItem_Duplicated_Test() {
        DicWord result = dicWordService.addItem("테스트", PosType.NNG, 30);
        result = dicWordService.addItem("테스트", PosType.NNG, 50);
    }


    @Test(expected = ParameterInvalidException.class)
    public void AddItem_InvalidToken_Test() {
        DicWord result = dicWordService.addItem("", PosType.NNG, 30);
    }


    @Test(expected = ParameterInvalidException.class)
    public void AddItem_InvalidTf_Test() {
        DicWord result = dicWordService.addItem("테스트", PosType.NNG, -30);
    }


    @Test
    public void CheckGivenTokenAndPosTypeExist_Test() {
        dicWordService.addItem("테스트", PosType.EC, 10);
        DicWord result = dicWordService.checkGivenTokenAndPosTypeExist("테스트", PosType.EC);

        assertThat(result.getToken(), is("테스트"));
        assertThat(result.getPos(), is(PosType.EC));
        assertThat(result.getTf(), is(10));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void CheckGivenTokenAndPosTypeExist_NotExsited_Test() {
        dicWordService.checkGivenTokenAndPosTypeExist("존재하지 않는 이름", PosType.EC);
    }


    @Test
    public void PurgeAllData_Test() {
        dicWordService.addItem("테스트", PosType.NNG, 10);
        dicWordService.addItem("테스트", PosType.NP, 20);
        dicWordService.addItem("테스트", PosType.EC, 30);

        dicWordService.purgeAllData();

        assertThat(dicWordRepository.count(), is(0L));
    }


    @Test
    public void UpdateTokenById_Test() {
        DicWord result = dicWordService.addItem("테스트", PosType.NNG, 10);
        int idToTest = result.getId();

        result = dicWordService.updateTokenById(idToTest, "바뀐것");

        assertThat(result.getId(), is(idToTest));
        assertThat(result.getToken(), is("바뀐것"));
        assertThat(result.getPos(), is(PosType.NNG));
        assertThat(result.getTf(), is(10));

        assertThat(dicWordRepository.findById(idToTest).getToken(), is("바뀐것"));
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdateTokenById_InvalidId_Test() {
        DicWord result = dicWordService.updateTokenById(-1, "테스트");
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdateTokenById_InvalidToken_Test() {
        DicWord result = dicWordService.addItem("테스트", PosType.NNG, 10);
        int idToTest = result.getId();

        result = dicWordService.updateTokenById(idToTest, null);
    }


    @Test
    public void UpdateTfById_Test() {
        DicWord result = dicWordService.addItem("테스트", PosType.NNG, 20);
        int idToTest = result.getId();

        result = dicWordService.updateTfById(idToTest, 999);

        assertThat(result.getId(), is(idToTest));
        assertThat(result.getToken(), is("테스트"));
        assertThat(result.getPos(), is(PosType.NNG));
        assertThat(result.getTf(), is(999));

        assertThat(dicWordRepository.findById(idToTest).getTf(), is(999));
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdateTfById_InvalidId_Test() {
        DicWord result = dicWordService.updateTfById(-1, 999);
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdateTfById_InvalidTf_Test() {
        DicWord result = dicWordService.addItem("테스트", PosType.NNG, 20);
        int idToTest = result.getId();

        result = dicWordService.updateTfById(idToTest, -1);
    }


    @Test
    public void UpdatePosById_Test() {
        DicWord result = dicWordService.addItem("테스트", PosType.NNG, 20);
        int idToTest = result.getId();

        result = dicWordService.updatePosById(idToTest, PosType.NNP);

        assertThat(result.getId(), is(idToTest));
        assertThat(result.getToken(), is("테스트"));
        assertThat(result.getPos(), is(PosType.NNP));
        assertThat(result.getTf(), is(20));

        assertThat(dicWordRepository.findById(idToTest).getPos(), is(PosType.NNP));
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdatePosById_InvalidId_Test() {
        DicWord result = dicWordService.updatePosById(-1, PosType.NP);
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdatePosById_InvalidPos_Test() {
        DicWord result = dicWordService.addItem("테스트", PosType.NNG, 20);
        int idToTest = result.getId();

        result = dicWordService.updatePosById(idToTest, null);
    }


    @Test
    public void DeleteItemById_Test() {
        dicWordService.addItem("테스트", PosType.NNB, 10);
        DicWord result = dicWordService.addItem("테스트", PosType.NNG, 20);

        int idToTest = result.getId();

        result = dicWordService.deleteItemById(idToTest);

        assertThat(result.getId(), is(idToTest));
    }


    @Test(expected = ParameterInvalidException.class)
    public void DeleteItemById_InvalidId_Test() {
        dicWordService.addItem("테스트", PosType.NNB, 10);
        DicWord result = dicWordService.addItem("테스트", PosType.NNG, 20);

        int idToTest = result.getId();

        result = dicWordService.deleteItemById(-1);
    }

}