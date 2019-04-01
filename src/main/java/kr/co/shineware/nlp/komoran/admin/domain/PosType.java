package kr.co.shineware.nlp.komoran.admin.domain;


public enum PosType {
    // 체언
    NNG("일반 명사"),
    NNP("고유 명사"),
    NNB("의존 명사"),
    NP("대명사"),
    NR("수사"),

    // 용언
    VV("동사"),
    VA("형용사"),
    VX("보조 용언"),
    VCP("긍정 지정사"),
    VCN("부정 지정사"),

    // 수식언
    MM("관형사"),
    MAG("일반 부사"),
    MAJ("접속 부사"),

    // 독립언
    IC("감탄사"),

    // 관계언
    JKS("주격 조사"),
    JKC("보격 조사"),
    JKG("관형격 조사"),
    JKO("목적격 조사"),
    JKB("부사격 조사"),
    JKV("호격 조사"),
    JKQ("인용격 조사"),
    JX("보조사"),
    JC("접속 조사"),

    // 의존형태
    EP("선어말 어미"),
    EF("종결 어미"),
    EC("연결 어미"),
    ETN("명사형 전성 어미"),
    ETM("관형형 전성 어미"),
    XPN("체언 접두사"),
    XSN("명사 파생 접미사"),
    XSV("동사 파생 접미사"),
    XSA("형용사 파생 접미사"),
    XR("어근"),

    // 기호
    SF("마침표,물음표,느낌표"),
    SP("쉼표,가운뎃점,콜론,빗금"),
    SS("따옴표,괄호표,줄표"),
    SE("줄임표"),
    SO("붙임표(물결,숨김,빠짐)"),
    SL("외국어"),
    SH("한자"),
    SW("기타기호(논리수학기호,화폐기호)"),
    NF("명사추정범주"),
    NV("용언추정범주"),
    SN("숫자"),
    NA("분석불능범주");

    private String posName;

    PosType(String posName) {
        this.posName = posName;
    }

    public String getPosName() {
        return this.posName;
    }

    public static boolean contains(String posNameToCheck) {
        for (PosType pos : PosType.values()) {
            if (pos.name().equals(posNameToCheck)) {
                return true;
            }
        }

        return false;
    }

}
