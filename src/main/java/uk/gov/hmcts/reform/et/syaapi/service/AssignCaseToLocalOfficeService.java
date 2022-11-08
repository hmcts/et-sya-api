package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;

import java.util.List;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.CASE_FIELD_MANAGING_OFFICE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.UNASSIGNED_OFFICE;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignCaseToLocalOfficeService {
    private final PostcodeToOfficeService postcodeToOfficeService;

    /**
     * Assigns case to local office. Returns @{@link CaseData} object.
     *
     * @param caseRequest which would be in json format
     * @return @link CaseData
     */
    public CaseData convertCaseRequestToCaseDataWithTribunalOffice(CaseRequest caseRequest) {
        CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseRequest.getCaseData());
        List<RespondentSumTypeItem> respondentSumTypeList = caseData.getRespondentCollection();
        String managingOffice = UNASSIGNED_OFFICE;
        if (caseData.getClaimantWorkAddress() != null
            && caseData.getClaimantWorkAddress().getClaimantWorkAddress() != null
            && StringUtils.isNotBlank(caseData.getClaimantWorkAddress().getClaimantWorkAddress().getPostCode())) {
            managingOffice = getManagingOffice(
                caseData.getClaimantWorkAddress().getClaimantWorkAddress().getPostCode());
        } else if (!CollectionUtils.isEmpty(respondentSumTypeList)) {
            for (RespondentSumTypeItem respondentSumTypeItem : respondentSumTypeList) {
                if (respondentSumTypeItem.getValue() != null
                    && respondentSumTypeItem.getValue().getRespondentAddress() != null
                    && StringUtils.isNotBlank(respondentSumTypeItem.getValue().getRespondentAddress().getPostCode())) {
                    managingOffice = getManagingOffice(
                        respondentSumTypeItem.getValue().getRespondentAddress().getPostCode());
                    break;
                }
            }
        }
        caseData.setManagingOffice(managingOffice);
        caseRequest.getCaseData().put(CASE_FIELD_MANAGING_OFFICE, managingOffice);
        return caseData;
    }

    private String getManagingOffice(String postcode) {
        try {
            return postcodeToOfficeService.getTribunalOfficeFromPostcode(postcode)
                .map(TribunalOffice::getOfficeName)
                .orElse(UNASSIGNED_OFFICE);
        } catch (InvalidPostcodeException e) {
            log.info("Failed to find tribunal office : {} ", e.getMessage());
            return UNASSIGNED_OFFICE;
        }
    }
}
