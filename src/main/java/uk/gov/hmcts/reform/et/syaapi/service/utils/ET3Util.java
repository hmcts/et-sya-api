package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.Et3Request;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants;
import uk.gov.hmcts.reform.et.syaapi.exception.ET3Exception;

public final class ET3Util {

    private ET3Util() {
        // restrict instantiation
    }

    public static void checkModifyEt3DataParameters(String authorisation, Et3Request et3Request) {
        if (StringUtils.isBlank(authorisation)) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_AUTHORISATION_TOKEN_BLANK));
        }
        checkEt3Request(et3Request);
        checkEt3Respondent(et3Request.getRespondent());
    }

    private static void checkEt3Request(Et3Request et3Request) {
        if (ObjectUtils.isEmpty(et3Request)) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_ET3_REQUEST_EMPTY));
        }
        if (StringUtils.isBlank(et3Request.getCaseSubmissionReference())) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_ET3_SUBMISSION_REFERENCE_BLANK));
        }
        if (StringUtils.isBlank(et3Request.getCaseTypeId())) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_ET3_CASE_TYPE_BLANK));
        }
        if (StringUtils.isBlank(et3Request.getRequestType())) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_ET3_REQUEST_TYPE_BLANK));
        }
    }

    private static void checkEt3Respondent(RespondentSumType et3Respondent) {
        if (ObjectUtils.isEmpty(et3Respondent)) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_ET3_RESPONDENT_EMPTY));
        }
        if (StringUtils.isBlank(et3Respondent.getIdamId())) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_ET3_RESPONDENT_IDAM_ID_IS_BLANK));
        }
    }
}
