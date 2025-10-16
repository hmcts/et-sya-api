package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.enums.RespondentSolicitorType;

import static org.assertj.core.api.Assertions.assertThat;

public class NoticeOfChangeUtilTest {

    private static final String TEST_INVALID_RESPONDENT_NAME = "Test invalid respondent name";
    private static final String TEST_RESPONDENT_NAME_0 = "Test respondent name 0";
    private static final String TEST_RESPONDENT_NAME_1 = "Test respondent name 1";
    private static final String TEST_RESPONDENT_NAME_2 = "Test respondent name 2";
    private static final String TEST_RESPONDENT_NAME_3 = "Test respondent name 3";
    private static final String TEST_RESPONDENT_NAME_4 = "Test respondent name 4";
    private static final String TEST_RESPONDENT_NAME_5 = "Test respondent name 5";
    private static final String TEST_RESPONDENT_NAME_6 = "Test respondent name 6";
    private static final String TEST_RESPONDENT_NAME_7 = "Test respondent name 7";
    private static final String TEST_RESPONDENT_NAME_8 = "Test respondent name 8";
    private static final String TEST_RESPONDENT_NAME_9 = "Test respondent name 9";
    private static final String TEST_CLAIMANT_FIRST_NAME_0 = "Test claimant first name 0";
    private static final String TEST_CLAIMANT_FIRST_NAME_1 = "Test claimant first name 1";
    private static final String TEST_CLAIMANT_FIRST_NAME_2 = "Test claimant first name 2";
    private static final String TEST_CLAIMANT_FIRST_NAME_3 = "Test claimant first name 3";
    private static final String TEST_CLAIMANT_FIRST_NAME_4 = "Test claimant first name 4";
    private static final String TEST_CLAIMANT_FIRST_NAME_5 = "Test claimant first name 5";
    private static final String TEST_CLAIMANT_FIRST_NAME_6 = "Test claimant first name 6";
    private static final String TEST_CLAIMANT_FIRST_NAME_7 = "Test claimant first name 7";
    private static final String TEST_CLAIMANT_FIRST_NAME_8 = "Test claimant first name 8";
    private static final String TEST_CLAIMANT_FIRST_NAME_9 = "Test claimant first name 9";
    private static final String TEST_CLAIMANT_LAST_NAME_0 = "Test claimant last name 0";
    private static final String TEST_CLAIMANT_LAST_NAME_1 = "Test claimant last name 1";
    private static final String TEST_CLAIMANT_LAST_NAME_2 = "Test claimant last name 2";
    private static final String TEST_CLAIMANT_LAST_NAME_3 = "Test claimant last name 3";
    private static final String TEST_CLAIMANT_LAST_NAME_4 = "Test claimant last name 4";
    private static final String TEST_CLAIMANT_LAST_NAME_5 = "Test claimant last name 5";
    private static final String TEST_CLAIMANT_LAST_NAME_6 = "Test claimant last name 6";
    private static final String TEST_CLAIMANT_LAST_NAME_7 = "Test claimant last name 7";
    private static final String TEST_CLAIMANT_LAST_NAME_8 = "Test claimant last name 8";
    private static final String TEST_CLAIMANT_LAST_NAME_9 = "Test claimant last name 9";

    private static final int INTEGER_TEN = 10;

    private static final CaseData TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS = new CaseData();

    @BeforeAll
    static void beforeAll() {
        TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS.setNoticeOfChangeAnswers0(
            NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_0)
                .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_0).claimantLastName(TEST_CLAIMANT_LAST_NAME_0).build());
        TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS.setNoticeOfChangeAnswers1(
            NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_1)
                .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_1).claimantLastName(TEST_CLAIMANT_LAST_NAME_1).build());
        TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS.setNoticeOfChangeAnswers2(
            NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_2)
                .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_2).claimantLastName(TEST_CLAIMANT_LAST_NAME_2).build());
        TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS.setNoticeOfChangeAnswers3(
            NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_3)
                .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_3).claimantLastName(TEST_CLAIMANT_LAST_NAME_3).build());
        TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS.setNoticeOfChangeAnswers4(
            NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_4)
                .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_4).claimantLastName(TEST_CLAIMANT_LAST_NAME_4).build());
        TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS.setNoticeOfChangeAnswers5(
            NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_5)
                .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_5).claimantLastName(TEST_CLAIMANT_LAST_NAME_5).build());
        TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS.setNoticeOfChangeAnswers6(
            NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_6)
                .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_6).claimantLastName(TEST_CLAIMANT_LAST_NAME_6).build());
        TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS.setNoticeOfChangeAnswers7(
            NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_7)
                .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_7).claimantLastName(TEST_CLAIMANT_LAST_NAME_7).build());
        TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS.setNoticeOfChangeAnswers8(
            NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_8)
                .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_8).claimantLastName(TEST_CLAIMANT_LAST_NAME_8).build());
        TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS.setNoticeOfChangeAnswers9(
            NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_9)
                .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_9).claimantLastName(TEST_CLAIMANT_LAST_NAME_9).build());

    }

    @Test
    void theFindNoticeOfChangeAnswerIndex() {

        // Should return -1 if caseData is null
        assertThat(NoticeOfChangeUtil.findNoticeOfChangeAnswerIndex(null, TEST_RESPONDENT_NAME_0))
            .isEqualTo(NumberUtils.INTEGER_MINUS_ONE);

        // Should return -1 if respondentName is blank
        assertThat(NoticeOfChangeUtil.findNoticeOfChangeAnswerIndex(TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS,
                                                                    StringUtils.EMPTY))
            .isEqualTo(NumberUtils.INTEGER_MINUS_ONE);

        // Should return -1 if no answers are present in the case data
        assertThat(NoticeOfChangeUtil.findNoticeOfChangeAnswerIndex(new CaseData(),
                                                                    TEST_RESPONDENT_NAME_0))
            .isEqualTo(NumberUtils.INTEGER_MINUS_ONE);

        // Should return -1 if no matching answer is found
        assertThat(NoticeOfChangeUtil.findNoticeOfChangeAnswerIndex(TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS,
                                                                    TEST_INVALID_RESPONDENT_NAME))
            .isEqualTo(NumberUtils.INTEGER_MINUS_ONE);
        // Should return index of matching notice of change answer when respondent name found
        assertThat(NoticeOfChangeUtil.findNoticeOfChangeAnswerIndex(TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS,
                                                                    TEST_RESPONDENT_NAME_1))
            .isEqualTo(NumberUtils.INTEGER_ONE);
        // Should return index of matching notice of change answer when respondent name found
        assertThat(NoticeOfChangeUtil.findNoticeOfChangeAnswerIndex(TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS,
                                                                    TEST_RESPONDENT_NAME_0))
            .isEqualTo(NumberUtils.INTEGER_ZERO);
        // Should return index of matching notice of change answer when respondent name found
        assertThat(NoticeOfChangeUtil.findNoticeOfChangeAnswerIndex(TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS,
                                                                    TEST_RESPONDENT_NAME_2))
            .isEqualTo(NumberUtils.INTEGER_TWO);
    }

    @Test
    void theGetNoticeOfChangeAnswers() {
        // Should return null if caseData is null
        assertThat(NoticeOfChangeUtil.getNoticeOfChangeAnswers(null)).isNull();

        // Should return a size of 1 list if there is only 1 answer present
        CaseData caseDataWithOneAnswer = new CaseData();
        caseDataWithOneAnswer.setNoticeOfChangeAnswers0(
            NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_0)
                .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_0).claimantLastName(TEST_CLAIMANT_LAST_NAME_0).build());
        assertThat(NoticeOfChangeUtil.getNoticeOfChangeAnswers(caseDataWithOneAnswer)).hasSize(NumberUtils.INTEGER_ONE);

        // Should return a size of 10 list if all notice of change answers present
        assertThat(NoticeOfChangeUtil.getNoticeOfChangeAnswers(TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS))
            .hasSize(INTEGER_TEN);
    }

    @Test
    void theGetNoticeOfChangeAnswer() {
        // Should return null if caseData is null
        assertThat(NoticeOfChangeUtil.getNoticeOfChangeAnswer(null, NumberUtils.INTEGER_ZERO)).isNull();

        // Should return null if index is less than 0
        assertThat(NoticeOfChangeUtil.getNoticeOfChangeAnswer(TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS,
                                                              NumberUtils.INTEGER_MINUS_ONE)).isNull();

        // Should return null if index is greater than 9
        assertThat(NoticeOfChangeUtil.getNoticeOfChangeAnswer(TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS,
                                                              INTEGER_TEN
        )).isNull();

        // Should return the correct notice of change answer for the given index
        for (int i = 0; i < INTEGER_TEN; i++) {
            NoticeOfChangeAnswers answer = NoticeOfChangeUtil.getNoticeOfChangeAnswer(
                TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS, i);
            assertThat(answer).isNotNull();
            assertThat(answer.getRespondentName()).isEqualTo("Test respondent name " + i);
            assertThat(answer.getClaimantFirstName()).isEqualTo("Test claimant first name " + i);
            assertThat(answer.getClaimantLastName()).isEqualTo("Test claimant last name " + i);
        }
    }

    @Test
    void theFindRespondentSolicitorTypeByIndex() {
        // Should return null if respondent solicitor type index is less than 0
        assertThat(NoticeOfChangeUtil.findRespondentSolicitorTypeByIndex(NumberUtils.INTEGER_MINUS_ONE)).isNull();

        // Should return null if respondent solicitor type index is greater than 9
        assertThat(NoticeOfChangeUtil.findRespondentSolicitorTypeByIndex(INTEGER_TEN)).isNull();

        // Should return the correct respondent solicitor type for the given index
        for (int i = 0; i < INTEGER_TEN; i++) {
            RespondentSolicitorType respondentSolicitorType = NoticeOfChangeUtil.findRespondentSolicitorTypeByIndex(i);
            assertThat(respondentSolicitorType).isEqualTo(RespondentSolicitorType.values()[i]);
        }
    }

    @Test
    void theResetNoticeOfChangeAnswerByIndex() {
        // Should do nothing if caseData is null
        NoticeOfChangeUtil.resetNoticeOfChangeAnswerByIndex(null, NumberUtils.INTEGER_ZERO);

        // Should do nothing if index is less than 0
        NoticeOfChangeUtil.resetNoticeOfChangeAnswerByIndex(TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS,
                                                            NumberUtils.INTEGER_MINUS_ONE);

        // Should do nothing if index is greater than 9
        NoticeOfChangeUtil.resetNoticeOfChangeAnswerByIndex(TEST_CASE_DATA_WITH_NOTICE_OF_CHANGE_ANSWERS,
                                                            INTEGER_TEN);

        // Should reset the correct notice of change answer for the given index
        for (int i = 0; i < INTEGER_TEN; i++) {
            CaseData caseDataCopy = new CaseData();
            caseDataCopy.setNoticeOfChangeAnswers0(
                NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_0)
                    .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_0).claimantLastName(TEST_CLAIMANT_LAST_NAME_0).build());
            caseDataCopy.setNoticeOfChangeAnswers1(
                NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_1)
                    .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_1).claimantLastName(TEST_CLAIMANT_LAST_NAME_1).build());
            caseDataCopy.setNoticeOfChangeAnswers2(
                NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_2)
                    .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_2).claimantLastName(TEST_CLAIMANT_LAST_NAME_2).build());
            caseDataCopy.setNoticeOfChangeAnswers3(
                NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_3)
                    .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_3).claimantLastName(TEST_CLAIMANT_LAST_NAME_3).build());
            caseDataCopy.setNoticeOfChangeAnswers4(
                NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_4)
                    .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_4).claimantLastName(TEST_CLAIMANT_LAST_NAME_4).build());
            caseDataCopy.setNoticeOfChangeAnswers5(
                NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_5)
                    .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_5).claimantLastName(TEST_CLAIMANT_LAST_NAME_5).build());
            caseDataCopy.setNoticeOfChangeAnswers6(
                NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_6)
                    .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_6).claimantLastName(TEST_CLAIMANT_LAST_NAME_6).build());
            caseDataCopy.setNoticeOfChangeAnswers7(
                NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_7)
                    .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_7).claimantLastName(TEST_CLAIMANT_LAST_NAME_7).build());
            caseDataCopy.setNoticeOfChangeAnswers8(
                NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_8)
                    .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_8).claimantLastName(TEST_CLAIMANT_LAST_NAME_8).build());
            caseDataCopy.setNoticeOfChangeAnswers9(
                NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_9)
                    .claimantFirstName(TEST_CLAIMANT_FIRST_NAME_9).claimantLastName(TEST_CLAIMANT_LAST_NAME_9).build());
            NoticeOfChangeUtil.resetNoticeOfChangeAnswerByIndex(caseDataCopy, i);
            for (int j = 0; j < INTEGER_TEN; j++) {
                NoticeOfChangeAnswers answer = NoticeOfChangeUtil.getNoticeOfChangeAnswer(caseDataCopy, j);
                if (i == j) {
                    assertThat(answer).isNull();
                } else {
                    assertThat(answer).isNotNull();
                    assertThat(answer.getRespondentName()).isEqualTo("Test respondent name " + j);
                    assertThat(answer.getClaimantFirstName()).isEqualTo("Test claimant first name " + j);
                    assertThat(answer.getClaimantLastName()).isEqualTo("Test claimant last name " + j);
                }
            }
        }
    }
}
