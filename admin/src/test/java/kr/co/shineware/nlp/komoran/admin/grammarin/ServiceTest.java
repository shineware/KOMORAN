package kr.co.shineware.nlp.komoran.admin.grammarin;

import kr.co.shineware.nlp.komoran.admin.KOMORANAdminApplication;
import kr.co.shineware.nlp.komoran.admin.domain.GrammarIn;
import kr.co.shineware.nlp.komoran.admin.domain.GrammarType;
import kr.co.shineware.nlp.komoran.admin.exception.ParameterInvalidException;
import kr.co.shineware.nlp.komoran.admin.exception.ResourceDuplicatedException;
import kr.co.shineware.nlp.komoran.admin.repository.GrammarInRepository;
import kr.co.shineware.nlp.komoran.admin.service.GrammarInService;
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
    private GrammarInService grammarInService;

    @Autowired
    private GrammarInRepository grammarInRepository;

    @Before
    public void init() {
        grammarInRepository.deleteAll();
    }


    @Test
    public void AddItem_Test() {
        GrammarIn result = grammarInService.addItem(GrammarType.BOE, GrammarType.NNG, 999);

        assertThat(result.getStart(), is(GrammarType.BOE));
        assertThat(result.getNext(), is(GrammarType.NNG));
        assertThat(result.getTf(), is(999));
    }


    @Test(expected = ResourceDuplicatedException.class)
    public void AddItem_Duplicated_Test() {
        GrammarIn result = grammarInService.addItem(GrammarType.BOE, GrammarType.NNG, 999);
        result = grammarInService.addItem(GrammarType.BOE, GrammarType.NNG, 10);
    }


    @Test(expected = ParameterInvalidException.class)
    public void AddItem_InvalidStart_Test() {
        GrammarIn result = grammarInService.addItem(null, GrammarType.NNG, 999);
    }


    @Test
    public void CheckGivenStartAndNextExist_Test() {
        grammarInService.addItem(GrammarType.VV, GrammarType.EC, 10);
        GrammarIn result = grammarInService.checkGivenStartAndNextExist(GrammarType.VV, GrammarType.EC);

        assertThat(result.getStart(), is(GrammarType.VV));
        assertThat(result.getNext(), is(GrammarType.EC));
        assertThat(result.getTf(), is(10));
    }


    @Test(expected = ParameterInvalidException.class)
    public void CheckGivenStartAndNextExist_Invalid_Test() {
        grammarInService.checkGivenStartAndNextExist(null, GrammarType.BOE);
    }


    @Test
    public void PurgeAllData_Test() {
        grammarInService.addItem(GrammarType.BOE, GrammarType.NNG, 999);
        grammarInService.addItem(GrammarType.VV, GrammarType.EC, 888);
        grammarInService.addItem(GrammarType.EF, GrammarType.EOE, 777);

        grammarInService.purgeAllData();

        assertThat(grammarInRepository.count(), is(0L));
    }


    @Test
    public void UpdateNextById_Test() {
        GrammarIn result = grammarInService.addItem(GrammarType.BOE, GrammarType.NNG, 999);
        int idToTest = result.getId();

        result = grammarInService.updateNextById(idToTest, GrammarType.NNP);

        assertThat(result.getId(), is(idToTest));
        assertThat(result.getStart(), is(GrammarType.BOE));
        assertThat(result.getNext(), is(GrammarType.NNP));
        assertThat(result.getTf(), is(999));

        assertThat(grammarInRepository.findById(idToTest).getNext(), is(GrammarType.NNP));
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdateNextById_InvalidId_Test() {
        grammarInService.updateNextById(-1, GrammarType.NP);
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdateNextById_InvalidNext_Test() {
        GrammarIn result = grammarInService.addItem(GrammarType.BOE, GrammarType.NNG, 999);
        int idToTest = result.getId();

        grammarInService.updateNextById(idToTest, null);
    }


    @Test
    public void UpdateTfById_Test() {
        GrammarIn result = grammarInService.addItem(GrammarType.BOE, GrammarType.NNG, 999);
        int idToTest = result.getId();

        result = grammarInService.updateTfById(idToTest, 10);

        assertThat(result.getId(), is(idToTest));
        assertThat(result.getStart(), is(GrammarType.BOE));
        assertThat(result.getNext(), is(GrammarType.NNG));
        assertThat(result.getTf(), is(10));

        assertThat(grammarInRepository.findById(idToTest).getTf(), is(10));
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdateTfById_InvalidId_Test() {
        grammarInService.updateTfById(-1, 10);
    }


    @Test(expected = ParameterInvalidException.class)
    public void UpdateTfById_InvalidTf_Test() {
        GrammarIn result = grammarInService.addItem(GrammarType.BOE, GrammarType.NNG, 999);
        int idToTest = result.getId();

        grammarInService.updateTfById(idToTest, -10);
    }

    @Test
    public void DeleteItemById_Test() {
        grammarInService.addItem(GrammarType.BOE, GrammarType.NNG, 999);
        GrammarIn result = grammarInService.addItem(GrammarType.EF, GrammarType.EOE, 777);

        int idToTest = result.getId();

        result = grammarInService.deleteItemById(idToTest);

        assertThat(result.getId(), is(idToTest));
    }


    @Test(expected = ParameterInvalidException.class)
    public void DeleteItemById_InvalidId_Test() {
        grammarInService.addItem(GrammarType.BOE, GrammarType.NNG, 999);
        GrammarIn result = grammarInService.addItem(GrammarType.EF, GrammarType.EOE, 777);

        int idToTest = result.getId();

        result = grammarInService.deleteItemById(-1);
    }

}