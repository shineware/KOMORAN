package kr.co.shineware.nlp.komoran.admin.usermodel;

import kr.co.shineware.nlp.komoran.admin.KOMORANAdminApplication;
import kr.co.shineware.nlp.komoran.admin.config.SetupDefaultData;
import kr.co.shineware.nlp.komoran.admin.service.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = KOMORANAdminApplication.class)
public class ServiceTest {
    @Autowired
    UserModelService userModelService;

    @Autowired
    DicUserService dicUserService;

    @Autowired
    DicWordService dicWordService;

    @Autowired
    FwdUserService fwdUserService;

    @Autowired
    GrammarInService grammarInService;

    @Autowired
    SetupDefaultData setupDefaultData;

    @Before
    public void init() {
        dicUserService.purgeAllData();
        dicWordService.purgeAllData();
        fwdUserService.purgeAllData();
        grammarInService.purgeAllData();

        try {
            dicWordService.importFromFile(setupDefaultData.getDefaultFilePath("dicword"));
            grammarInService.importFromFile(setupDefaultData.getDefaultFilePath("grammarin"));
        } catch (IOException e) {
            // TODO:
            // TEST FAILED due to initialization failure
        }
    }


    @Test
    public void BuildUserModel_Test() {
        boolean builtResult = userModelService.buildNewModel();

        assertThat(builtResult, is(true));
    }


    @Test
    public void ListUserModels_Test() {
        List<String> modelList = userModelService.getModelList();

        assertThat(modelList.size() >= 1, is(true));
    }


    @Test
    public void ListUserModelsContainDefaultModel_Test() {
        List<String> modelList = userModelService.getModelList();

        assertThat("DEFAULT".equals(modelList.get(0)), is(true));
    }


    @Test
    public void ListUserModelsAfterNewModelBuilt_Test() {
        List<String> modelListBefore = userModelService.getModelList();
        userModelService.buildNewModel();
        List<String> modelListAfter = userModelService.getModelList();

        assert (modelListAfter.size() == (modelListBefore.size() + 1));
    }


    @Test
    public void ModelListShouldBeSorted_Test() {
        List<String> modelListBefore = userModelService.getModelList();
        userModelService.buildNewModel();
        List<String> modelListAfter = userModelService.getModelList();

        String newModel = modelListAfter.remove(1);

        assertThat(modelListBefore.equals(modelListAfter), is(true));
    }
}
