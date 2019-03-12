package kr.co.shineware.nlp.komoran.admin.dicword;

import kr.co.shineware.nlp.komoran.admin.KOMORANAdminApplication;
import kr.co.shineware.nlp.komoran.admin.domain.DicWord;
import kr.co.shineware.nlp.komoran.admin.domain.PosType;
import kr.co.shineware.nlp.komoran.admin.repository.DicWordRepository;
import kr.co.shineware.nlp.komoran.admin.service.DicWordService;
import org.hamcrest.MatcherAssert;
import org.json.simple.JSONObject;
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
        JSONObject result = dicWordService.addItem("테스트", PosType.NNG, 10);

        assertThat(result.get("success"), is(true));
        assertThat(result.get("error"), is(false));


        result = dicWordService.addItem("테스트", PosType.NNG, 15);

        assertThat(result.get("success"), is(false));
        assertThat(result.get("error"), is(true));
        assertThat(result.get("message"), is("해당 단어/품사는 이미 등록되어 있습니다."));
    }

    @Test
    public void CheckGivenTokenAndPosTypeExist_Test() {
        JSONObject result = dicWordService.checkGivenTokenAndPosTypeExist("테스트", PosType.EC);

        assertThat(result.get("success"), is(false));
        assertThat(result.get("error"), is(true));
        assertThat(result.get("message"), is("해당하는 단어/품사가 존재하지 않습니다."));


        result = dicWordService.addItem("테스트", PosType.EC, 10);

        assertThat(result.get("success"), is(true));
        assertThat(result.get("error"), is(false));
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
    public void UpdateTfById_Test() {
        JSONObject result = dicWordService.addItem("테스트", PosType.NNG, 20);
        int idToTest = ((DicWord) result.get("data")).getId();


        result = dicWordService.updateTfById(idToTest, 999);

        assertThat(result.get("success"), is(true));
        assertThat(result.get("error"), is(false));
        MatcherAssert.assertThat(dicWordRepository.findById(idToTest).getTf(), is(999));


        result = dicWordService.updateTfById(idToTest, -999);

        assertThat(result.get("success"), is(false));
        assertThat(result.get("error"), is(true));
        assertThat(result.get("message"), is("잘못된 빈도입니다."));
        MatcherAssert.assertThat(dicWordRepository.findById(idToTest).getTf(), is(999));


        result = dicWordService.updateTfById(-1, 999);

        assertThat(result.get("success"), is(false));
        assertThat(result.get("error"), is(true));
        assertThat(result.get("message"), is("ID가 존재하지 않습니다."));
    }

    @Test
    public void UpdatePosById_Test() {
        dicWordService.addItem("테스트", PosType.NNB, 10);
        JSONObject result = dicWordService.addItem("테스트", PosType.NNG, 20);

        int idToTest = ((DicWord) result.get("data")).getId();

        result = dicWordService.updatePosById(idToTest, PosType.NP);

        assertThat(result.get("success"), is(true));
        assertThat(result.get("error"), is(false));
        MatcherAssert.assertThat(dicWordRepository.findById(idToTest).getPos(), is(PosType.NP));


        result = dicWordService.updatePosById(idToTest, null);

        assertThat(result.get("success"), is(false));
        assertThat(result.get("error"), is(true));
        assertThat(result.get("message"), is("존재하지 않는 품사입니다."));


        result = dicWordService.updatePosById(-1, PosType.EC);

        assertThat(result.get("success"), is(false));
        assertThat(result.get("error"), is(true));
        assertThat(result.get("message"), is("ID가 존재하지 않습니다."));


        result = dicWordService.updatePosById(idToTest, PosType.NNB);

        assertThat(result.get("success"), is(false));
        assertThat(result.get("error"), is(true));
        assertThat(result.get("message"), is("해당 단어/품사는 이미 등록되어 있습니다."));
    }

    @Test
    public void DeleteItemById_Test() {
        dicWordService.addItem("테스트", PosType.NNB, 10);
        JSONObject result = dicWordService.addItem("테스트", PosType.NNG, 20);

        int idToTest = ((DicWord) result.get("data")).getId();


        result = dicWordService.deleteItemById(-1);

        assertThat(result.get("success"), is(false));
        assertThat(result.get("error"), is(true));
        assertThat(result.get("message"), is("ID가 존재하지 않습니다."));


        result = dicWordService.deleteItemById(idToTest);

        assertThat(result.get("success"), is(true));
        assertThat(result.get("error"), is(false));
    }
}