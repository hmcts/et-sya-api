package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.enums.RespondentSolicitorType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NoticeOfChangeUtil {

    private NoticeOfChangeUtil() {
        // restrict instantiation
    }


    /**
     * Finds the index of a {@link NoticeOfChangeAnswers} entry in the given {@link CaseData}
     * that matches the specified respondent name.
     *
     * <p>
     * The method iterates through the list of Notice of Change answers extracted from
     * the case data. It returns the index of the first answer whose {@code respondentName}
     * matches the provided {@code respondentName}, using a case-insensitive comparison.
     * </p>
     *
     * <p>
     * If the {@code caseData} is {@code null}, the {@code respondentName} is blank,
     * or no matching answer is found, the method returns {@code -1}.
     * </p>
     *
     * @param caseData        the case data containing potential Notice of Change answers;
     *                        may be {@code null}
     * @param respondentName  the respondent name to search for; must not be blank
     * @return the zero-based index of the matching {@link NoticeOfChangeAnswers},
     *         or {@code -1} if no match is found or input is invalid
     */
    public static int findNoticeOfChangeAnswerIndex(CaseData caseData, String respondentName) {
        if (ObjectUtils.isEmpty(caseData) || StringUtils.isBlank(respondentName)) {
            return -1;
        }
        List<NoticeOfChangeAnswers> answers = getNoticeOfChangeAnswers(caseData);
        if (CollectionUtils.isEmpty(answers)) {
            return -1;
        }
        for (int i = 0; i < answers.size(); i++) {
            NoticeOfChangeAnswers ans = answers.get(i);
            if (ObjectUtils.isEmpty(ans) || StringUtils.isBlank(ans.getRespondentName())) {
                continue;
            }
            if (respondentName.equalsIgnoreCase(ans.getRespondentName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Retrieves all non-empty {@link NoticeOfChangeAnswers} instances from the given {@link CaseData}.
     *
     * <p>
     * This method scans through the predefined set of Notice of Change answer fields
     * (from index {@code 0} to {@code 9}) in the {@link CaseData} object. For each index,
     * it extracts the corresponding {@link NoticeOfChangeAnswers} entry and collects
     * only those that are not empty.
     * </p>
     *
     * <p>
     * The result is a compact list containing only valid answers, in the order of their
     * indices (0 through 9). Any {@code null} or empty entries are skipped.
     * </p>
     *
     * @param caseData the case data containing Notice of Change answers; may be {@code null}
     * @return a list of non-empty {@link NoticeOfChangeAnswers}, never {@code null};
     *         the list may be empty if no valid answers exist
     *
     * @see #getNoticeOfChangeAnswer(CaseData, int)
     */
    public static List<NoticeOfChangeAnswers> getNoticeOfChangeAnswers(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData)) {
            return null;
        }
        return IntStream.rangeClosed(0, 9)
            .mapToObj(i -> getNoticeOfChangeAnswer(caseData, i))
            .filter(ObjectUtils::isNotEmpty)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific {@link NoticeOfChangeAnswers} entry from the given {@link CaseData}
     * by its index.
     *
     * <p>
     * The index corresponds to one of the predefined Notice of Change answer fields
     * in the {@link CaseData} object:
     * <ul>
     *   <li>0 → {@link CaseData#getNoticeOfChangeAnswers0()}</li>
     *   <li>1 → {@link CaseData#getNoticeOfChangeAnswers1()}</li>
     *   <li>2 → {@link CaseData#getNoticeOfChangeAnswers2()}</li>
     *   <li>3 → {@link CaseData#getNoticeOfChangeAnswers3()}</li>
     *   <li>4 → {@link CaseData#getNoticeOfChangeAnswers4()}</li>
     *   <li>5 → {@link CaseData#getNoticeOfChangeAnswers5()}</li>
     *   <li>6 → {@link CaseData#getNoticeOfChangeAnswers6()}</li>
     *   <li>7 → {@link CaseData#getNoticeOfChangeAnswers7()}</li>
     *   <li>8 → {@link CaseData#getNoticeOfChangeAnswers8()}</li>
     *   <li>9 → {@link CaseData#getNoticeOfChangeAnswers9()}</li>
     * </ul>
     * </p>
     *
     * <p>
     * If the {@code index} is outside the range {@code 0–9}, the method returns {@code null}.
     * </p>
     *
     * @param caseData the case data containing Notice of Change answers; must not be {@code null}
     * @param index    the index of the Notice of Change answer to retrieve (0–9)
     * @return the {@link NoticeOfChangeAnswers} at the given index, or {@code null} if the index is out of range
     */
    public static NoticeOfChangeAnswers getNoticeOfChangeAnswer(CaseData caseData, int index) {
        if (ObjectUtils.isEmpty(caseData) || index < 0 || index > 9) {
            return null;
        }
        return switch (index) {
            case 0 -> caseData.getNoticeOfChangeAnswers0();
            case 1 -> caseData.getNoticeOfChangeAnswers1();
            case 2 -> caseData.getNoticeOfChangeAnswers2();
            case 3 -> caseData.getNoticeOfChangeAnswers3();
            case 4 -> caseData.getNoticeOfChangeAnswers4();
            case 5 -> caseData.getNoticeOfChangeAnswers5();
            case 6 -> caseData.getNoticeOfChangeAnswers6();
            case 7 -> caseData.getNoticeOfChangeAnswers7();
            case 8 -> caseData.getNoticeOfChangeAnswers8();
            case 9 -> caseData.getNoticeOfChangeAnswers9();
            default -> null;
        };
    }

    /**
     * Finds a {@link RespondentSolicitorType} by its corresponding index.
     *
     * <p>
     * This method validates the provided index to ensure it falls within the
     * acceptable range (0–9). If the index is valid, the corresponding
     * {@link RespondentSolicitorType} is returned. If the index is out of range,
     * this method returns {@code null}.
     * </p>
     *
     * @param respondentSolicitorTypeIndex the index of the {@link RespondentSolicitorType}
     *                                     to look up (must be between 0 and 9 inclusive)
     * @return the {@link RespondentSolicitorType} associated with the given index,
     *         or {@code null} if the index is invalid
     */
    public static RespondentSolicitorType findRespondentSolicitorTypeByIndex(
        int respondentSolicitorTypeIndex) {

        if (respondentSolicitorTypeIndex < 0 || respondentSolicitorTypeIndex > 9) {
            return null;
        }
        return RespondentSolicitorType.getByIndex(respondentSolicitorTypeIndex);
    }
}
