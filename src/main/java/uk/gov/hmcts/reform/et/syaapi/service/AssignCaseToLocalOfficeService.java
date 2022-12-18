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
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import java.util.List;
import java.util.Optional;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.CASE_FIELD_MANAGING_OFFICE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SCOTLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.UNASSIGNED_OFFICE;
import static uk.gov.hmcts.ecm.common.model.helper.TribunalOffice.ENGLANDWALES_OFFICES;
import static uk.gov.hmcts.ecm.common.model.helper.TribunalOffice.SCOTLAND_OFFICES;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.LawOfDemeter"})
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
        if (claimantHasWorkingAddressPostCode(caseData)) {
            managingOffice = getManagingOffice(
                caseData.getClaimantWorkAddress().getClaimantWorkAddress().getPostCode(),
                caseRequest.getCaseTypeId());
        } else if (!CollectionUtils.isEmpty(respondentSumTypeList)) {
            ClaimantWorkAddressType claimantWorkAddressType = new ClaimantWorkAddressType();
            for (RespondentSumTypeItem respondentSumTypeItem : respondentSumTypeList) {
                if (respondentSumTypeItem.getValue() != null
                    && respondentSumTypeItem.getValue().getRespondentAddress() != null
                    && StringUtils.isNotBlank(respondentSumTypeItem.getValue().getRespondentAddress().getPostCode())) {
                    claimantWorkAddressType.setClaimantWorkAddress(
                        respondentSumTypeItem.getValue().getRespondentAddress());
                    caseData.setClaimantWorkAddress(claimantWorkAddressType);
                    managingOffice = getManagingOffice(
                        respondentSumTypeItem.getValue().getRespondentAddress().getPostCode(),
                        caseRequest.getCaseTypeId());
                    break;
                }
            }
        }
        caseData.setManagingOffice(managingOffice);
        caseRequest.getCaseData().put(CASE_FIELD_MANAGING_OFFICE, managingOffice);
        return caseData;
    }

    private String getManagingOffice(String postcode, String caseTypeId) {
        try {
            Optional<TribunalOffice> office = postcodeToOfficeService.getTribunalOfficeFromPostcode(postcode);
            return retrieveManagingOfficeAccordingToCaseTypeId(caseTypeId, office);
        } catch (InvalidPostcodeException e) {
            log.info("Failed to find tribunal office : {} ", e.getMessage());
            return UNASSIGNED_OFFICE;
        }
    }

    /**
     * Checks if caseTypeId matches the office postcode area(E.g. ET_EnglandWales -> Leeds), otherwise it will assign
     * to Unassigned office.
     * @param caseTypeId 'ET_EnglandWales' or 'ET_Scotland'
     * @param office retrieved from getTribunalOfficeFromPostcode method
     * @return Returns officeName
     */
    private String retrieveManagingOfficeAccordingToCaseTypeId(String caseTypeId, Optional<TribunalOffice> office) {
        if (office.isEmpty()) {
            return UNASSIGNED_OFFICE;
        }
        if (ENGLAND_CASE_TYPE.equals(caseTypeId) && SCOTLAND_OFFICES.contains(office.get())
            || SCOTLAND_CASE_TYPE.equals(caseTypeId) && ENGLANDWALES_OFFICES.contains(office.get())) {
            return UNASSIGNED_OFFICE;
        } else {
            return reassignAnyScottishOfficeToGlasgow(office);
        }
    }

    /**
     * All Scottish cases, that have provided a valid Scottish respondent/work postcode, should be assigned by default
     * to the Glasgow office.
     * @param office retrieved from getTribunalOfficeFromPostcode method
     * @return Returns officeName
     */
    private String reassignAnyScottishOfficeToGlasgow(Optional<TribunalOffice> office) {
        if (SCOTLAND_OFFICES.contains(office.get())) {
            return TribunalOffice.GLASGOW.getOfficeName();
        } else {
            return office.get().getOfficeName();
        }
    }

    private boolean claimantHasWorkingAddressPostCode(CaseData caseData) {
        return caseData.getClaimantWorkAddress() != null
            && caseData.getClaimantWorkAddress().getClaimantWorkAddress() != null
            && StringUtils.isNotBlank(caseData.getClaimantWorkAddress().getClaimantWorkAddress().getPostCode());
    }
}
