package com.kongtoon.utils;

import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

public class TestConst {
    public static final String COMMON_EX_OBJ_SCHEMA = "공통 예외 객체";
    public static final String COMMON_EX_OBJ_DETAIL_SCHEMA = "공통 예외 객체 - 요청 에러 시";
    public static final String ERROR_MESSAGE_FIELD = "message";
    public static final String ERROR_CODE_FIELD = "code";
    public static final String INPUT_ERROR_INFOS_FIELD = "inputErrors";
    public static final String INPUT_ERROR_INFOS_MESSAGE_FIELD = "inputErrors[*].message";
    public static final String INPUT_ERROR_INFOS_FIELD_FIELD = "inputErrors[*].field";
    public static final String ERROR_MESSAGE_DESCRIPTION = "에러 메세지";
    public static final String ERROR_CODE_DESCRIPTION = "에러 코드";
    public static final String INPUT_ERROR_INFOS_DESCRIPTION = "요청 에러 정보";
    public static final String INPUT_ERROR_INFOS_MESSAGE_DESCRIPTION = "요청 에러 메세지";
    public static final String INPUT_ERROR_INFOS_FIELD_DESCRIPTION = "요청 에러 필드";

    public static ResponseFieldsSnippet createErrorResponseDocs() {
        return responseFields(
                fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
                fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
                fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.NULL).description(INPUT_ERROR_INFOS_DESCRIPTION)
        );
    }

    public static ResponseFieldsSnippet createInputErrorResponseDocs() {
        return responseFields(
                fieldWithPath(ERROR_MESSAGE_FIELD).type(JsonFieldType.STRING).description(ERROR_MESSAGE_DESCRIPTION),
                fieldWithPath(ERROR_CODE_FIELD).type(JsonFieldType.STRING).description(ERROR_CODE_DESCRIPTION),
                fieldWithPath(INPUT_ERROR_INFOS_FIELD).type(JsonFieldType.ARRAY).description(INPUT_ERROR_INFOS_DESCRIPTION),
                fieldWithPath(INPUT_ERROR_INFOS_MESSAGE_FIELD).type(JsonFieldType.STRING).description(INPUT_ERROR_INFOS_MESSAGE_DESCRIPTION),
                fieldWithPath(INPUT_ERROR_INFOS_FIELD_FIELD).type(JsonFieldType.STRING).description(INPUT_ERROR_INFOS_FIELD_DESCRIPTION)
        );
    }
}